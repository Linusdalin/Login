package system;

import dataRepresentation.DBTimeStamp;
import log.PukkaLogger;
import pukkaBO.condition.*;
import pukkaBO.exceptions.BackOfficeException;

import java.util.HashMap;
import java.util.Map;

/**
 *
 *          Session manager
 *
 *
 *      //TODO: findExisting and create new uses similar validation of user/pwd. Refactor this into one
 *
 */

public class SessionManagement {

    private static final int SESSION_TIME = 180;
    private PortalUser sessionUser = null;
    private PortalUser system = null;

    private Map<String, String> orgAccess = new HashMap<String, String>();

    public String close(String sessionToken) throws BackOfficeException {

            // Lookup the session

            PortalSession session = new PortalSession(new LookupItem()
                    .addFilter(new ColumnFilter(PortalSessionTable.Columns.Token.name(), sessionToken)));

            String status = "closed";

            if(!session.exists()){

                status = "unknown session"; //TODO: Log this.

            }else if(session.getStatusId().equals(SessionStatus.gettimeout())){

                status = "implicit";
            }else{

                // Close session

                session.setStatus(SessionStatus.getclosed().getKey());
                session.update();

            }

            return status;
    }

    /************************************************************************************'''
     *
     *              Validate user credentials and create a new session
     *
     *
     * @param name      - the user name
     * @param password  - user password
     * @param ipAddress - the ip address for the session
     * @return - the session
     * @throws BackOfficeException
     */


    public PortalSession createSession(String name, String password, String ipAddress) throws BackOfficeException{

        PortalSession emptySession = new PortalSession();
        PasswordManager pwdManager = new PasswordManager();


        try {


            // Lookup the user

            PortalUser user = new PortalUser(new LookupItem()
                            .addFilter(new ColumnFilter(PortalUserTable.Columns.Name.name(), name)));

            if(!user.exists())
               return emptySession;

            // Validate the password

            if(!pwdManager.authenticate(password, user.getPassword().getBytes("ISO-8859-1"), user.getSalt().getBytes("ISO-8859-1")))
                return emptySession;

            // Create a new session

            return new PortalSessionTable().createNewSession(user, ipAddress);

        } catch (Exception e) {

            //TODO: Log this
            PukkaLogger.log(PukkaLogger.Level.INFO, "Error");
            return emptySession;

        }

    }


    /*******************************************************************
     *
     *      Validate the session token that is received in all service requests
     *      The token should uniquely identify the user
     *
     *
     *
     * @param sessionToken - token from the web service call
     * @param accessIP
     * @return - true if the session is active
     *
     *
     *      // TODO: Add more error codes
     */


    public boolean validate(String sessionToken, String accessIP) throws BackOfficeException {

        // Lookup the last session

       PortalSession session = new PortalSession(new LookupItem()
                    .addFilter(new ColumnFilter(PortalSessionTable.Columns.Token.name(), sessionToken))
                    .addSorting(new Sorting(PortalSessionTable.Columns.Start.name(), Ordering.LAST)));


       if(!session.exists())
            return false;

        // Check the IP address. It is not allowed to access a session from another IP address.
        // This will prevent malicious session hijacking

        if(!session.getIP().equals(accessIP)){

            PukkaLogger.log(PukkaLogger.Level.WARNING, "Access attempt on "+ session.getUser().getName()+" account from another IP address. (Login: " + session.getIP() + " access: " + accessIP + ")");
            return false;

        }


        // Check if the session is open and not expired

       boolean isActive =(session.getStatusId().equals(SessionStatus.getopen().getKey()) && !expired(session));

       if(isActive){

           sessionUser = getUserFromSession(sessionToken);
           session.setLatest(new DBTimeStamp());
           session.update();
       }

        // Store the system user TODO: This could be optimized, but there is no constants generated for PortalUser
        system = new PortalUser(new LookupItem().addFilter(new ColumnFilter(PortalUserTable.Columns.Name.name(), "System")));


        return isActive;


    }


    /*******************************************************************
     *
     *      An expired session is when latest + 60 min is before now.
     *
     * @param session - the session
     * @return - true if the session is expired
     *
     *      NOTE: If we fail to lookup a session, we close it.
     *      NOTE: The start time is used for long live tokens
     */

    private boolean expired(PortalSession session) {

        try {
            DBTimeStamp latestUpdate = session.getLatest();
            DBTimeStamp startTime = session.getStart();
            DBTimeStamp endTime = latestUpdate.addMinutes(SESSION_TIME);
            DBTimeStamp now = new DBTimeStamp();

            return endTime.isBefore(now) && startTime.isBefore(now);

        } catch (BackOfficeException e) {
            e.logError("Error looking for time for session. Fail = expire");
            return true;
        }
    }


    /***********************************************************
     *
     *          As the session token is used to identify the web service call, we use it to
     *          retrieve the correct user for the actions.
     *
     *          This will ensure security in all the actions
     *
     * @param sessionToken - token from the web service call.
     * @return - the user that the session was created for
     * @throws BackOfficeException
     */

    private PortalUser getUserFromSession(String sessionToken) throws BackOfficeException {

        PortalSession session = new PortalSession(new LookupItem()
                    .addFilter(new ColumnFilter(PortalSessionTable.Columns.Token.name(), sessionToken)));

        if(!session.exists())
            throw new BackOfficeException(BackOfficeException.Usage, "No session exists for token " + sessionToken);

        return session.getUser();

    }

    public PortalUser getUser() throws BackOfficeException{

        if(sessionUser == null)
            throw new BackOfficeException(BackOfficeException.AccessError, "Session not validated before accessed");

        PukkaLogger.log(PukkaLogger.Level.INFO, "Request user is " + sessionUser.getName() + "( "+ sessionUser.getKey()+" )");

        return sessionUser;
    }

    /********************************************************************************'
     *
     *          Find Existing Session retrieves a session if it exists
     *
     *          Exists means that it is open and still active.
     *
     *          If there is no session, it will return an empty session
     *
     * @param name - user
     * @param password - password
     *
     * @return - the session
     * @throws BackOfficeException
     */


    public PortalSession findExistingSession(String name, String password) throws BackOfficeException{

        PortalSession emptySession = new PortalSession();
        PasswordManager pwdManager = new PasswordManager();

        try {

            PukkaLogger.log(PukkaLogger.Level.DEBUG, "Looking for existing session");


            // Lookup the user

            PortalUser user = new PortalUser(new LookupItem()
                            .addFilter(new ColumnFilter(PortalUserTable.Columns.Name.name(), name)));

            if(!user.exists()){

                PukkaLogger.log(PukkaLogger.Level.DEBUG, "No such user " + name);
                return emptySession;

            }
            // Validate the password

            if(!pwdManager.authenticate(password, user.getPassword().getBytes("ISO-8859-1"), user.getSalt().getBytes("ISO-8859-1")))
                return emptySession;

            // Lookup a session for the user

            PortalSession session = new PortalSession(new LookupItem()
                        .addFilter(new ReferenceFilter(PortalSessionTable.Columns.User.name(), user.getKey())));


            if(!session.exists()){

                PukkaLogger.log(PukkaLogger.Level.DEBUG, "No session exists");
                return emptySession;
            }

            if(session.getStatusId().equals(SessionStatus.getopen().getKey()) && !expired(session)){

                PukkaLogger.log(PukkaLogger.Level.DEBUG, "Returning session");
                return session;
            }

            if(expired(session))
                PukkaLogger.log(PukkaLogger.Level.DEBUG, "Session already expired");

            if(session.getStatusId().equals(SessionStatus.getopen().getKey()))
                PukkaLogger.log(PukkaLogger.Level.DEBUG, "Session not open.. (" + session.getStatus().getName() + ")" );


            return emptySession;

        } catch (Exception e) {

            //TODO: Log this
            PukkaLogger.log(PukkaLogger.Level.INFO, "Error");
            return emptySession;

        }


    }


}
