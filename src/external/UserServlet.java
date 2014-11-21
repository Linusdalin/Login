package external;

import dataRepresentation.DBTimeStamp;
import log.PukkaLogger;
import net.sf.json.JSONObject;
import pukkaBO.condition.*;
import pukkaBO.exceptions.BackOfficeException;
import system.Organization;
import system.PasswordManager;
import system.PortalUser;
import system.PortalUserTable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/***************************************************
 *
 *         Portal User Servlet is used to crete/delete new users
 *         and get user details.
 *
 */

public class UserServlet extends GenericServlet {

    public static final String DataServletName = "User";


    /**********************************************************************'
     *
     *              Create or update a new user
     *
     *              This should be resrticted from the application servers
     *
     * @param req
     * @param resp
     * @throws IOException
     */



    public void doPost(HttpServletRequest req, HttpServletResponse resp)throws IOException {

        try{
            logRequest(req);

            String username         = getMandatoryString("user", req);
            String password         = getMandatoryString("password", req);

            if(!validateSession(req, resp))
                return;


            PortalUser parent = sessionManagement.getUser();

            PortalUser user = new PortalUser(new LookupItem().addFilter(new ColumnFilter(PortalUserTable.Columns.Name.name(), username)));

            if(user.exists()){

                returnError("User Already Exists",  ErrorType.NAMING, HttpServletResponse.SC_OK, resp);
                resp.flushBuffer();
                return;

            }

            DBTimeStamp registrationDate = new DBTimeStamp();   // Set now as a registration date
            Organization org = parent.getOrganization();

            // Create the user

            PasswordManager pwdManager = new PasswordManager();
            byte[] salt = new byte[0];
            String encodedPwd;
            String encodedSalt;
            try {
                salt = pwdManager.generateSalt();
                encodedPwd = new String(pwdManager.getEncryptedPassword(password, salt), "ISO-8859-1");
                encodedSalt = new String( salt, "ISO-8859-1");

            } catch (Exception e) {

                PukkaLogger.log(PukkaLogger.Level.FATAL, "Error creating password " + e.getMessage());
                return;

            }

            long existingUsers = new PortalUserTable().getCount();

            //    public PortalUser(String name, long userid, String password, String salt, String registration, DataObjectInterface organization, boolean active) throws BackOfficeException{


            PortalUser newUser = new PortalUser(username, (existingUsers + 1), encodedPwd, encodedSalt, registrationDate.getISODate(), org, true);
            newUser.store();

            //org.setUsers(existingUsers + 1);
            //org.update();


            PukkaLogger.log(PukkaLogger.Level.MAJOR_EVENT, "Created a new user " + newUser.getName() + " with id " + newUser.getKey());

            Formatter formatter = getFormatFromParameters(req);

            JSONObject response = new JSONObject()
                    .put("user", newUser.getUserId());

            sendJSONResponse(response, formatter, resp);


        }catch(BackOfficeException e){

            e.printStackTrace(System.out);
            returnError("Error creating user: " + e.narration, ErrorType.GENERAL, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, PukkaLogger.Level.FATAL, resp);

        } catch ( Exception e) {

            e.printStackTrace(System.out);
            returnError("Error creating user: " + e.getMessage(), ErrorType.GENERAL, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, PukkaLogger.Level.FATAL, resp);
            resp.flushBuffer();

        }
     }


    //TODO: Not implemented Access restriction to this

    public void doGet(HttpServletRequest req, HttpServletResponse resp)throws IOException {

        returnError("Get not supported in " + DataServletName, HttpServletResponse.SC_METHOD_NOT_ALLOWED, resp);
        resp.flushBuffer();



    }



    public void doDelete(HttpServletRequest req, HttpServletResponse resp)throws IOException {

        try{
            logRequest(req);

            long userId         = getMandatorylong("user", req);

            if(!validateSession(req, resp))
                return;


            Formatter formatter = getFormatFromParameters(req);

            PortalUser parent = sessionManagement.getUser();        // TODO: Add test for the case that the user already is deleted here

            PortalUser user = new PortalUser(new LookupItem().addFilter(new ColumnFilter(PortalUserTable.Columns.UserId.name(), userId)));

            if(!user.exists()){

                returnError("Error deleting user: User id "+ userId + "does not exist", ErrorType.GENERAL, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, PukkaLogger.Level.FATAL, resp);
                return;
            }

            user.delete();                              // TODO: Close the ongoing session. (Store token in session?)

            JSONObject json = createDeletedResponse(DataServletName, user);

            sendJSONResponse(json, formatter, resp);


        }catch(BackOfficeException e){
        
            returnError("Error deleting user: " + e.narration, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, resp);
            resp.flushBuffer();
            e.printStackTrace();

        }catch ( Exception e) {

            returnError("Error deleting user: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, resp);
            resp.flushBuffer();
            e.printStackTrace();
        }

    }


}
