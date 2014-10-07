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

            String username         = getMandatoryString("username", req);
            String password         = getMandatoryString("password", req);

            logRequest(req);

            if(!validateSession(req, resp))
                return;


            PortalUser parent = sessionManagement.getUser();

            PortalUser user = new PortalUser(new LookupItem().addFilter(new ColumnFilter(PortalUserTable.Columns.Name.name(), username)));

            if(user.exists()){

                returnError("User Already Exists", HttpServletResponse.SC_OK, resp);
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

            long existingUsersForOrganization = org.getUsers();

            PortalUser newUser = new PortalUser(username, (existingUsersForOrganization + 1), "", encodedPwd, encodedSalt, registrationDate.getISODate(), org);
            newUser.store();

            org.setUsers(existingUsersForOrganization + 1);
            org.update();


            PukkaLogger.log(PukkaLogger.Level.MAJOR_EVENT, "Created a new user " + newUser.getName() + " with id " + newUser.getKey());

            Formatter formatter = getFormatFromParameters(req);

            JSONObject response = new JSONObject()
                    .put("user", newUser.getUserId());

            sendJSONResponse(response, formatter, resp);


        }catch(BackOfficeException e){

            returnError("Error creating user: " + e.narration, ErrorType.GENERAL, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, PukkaLogger.Level.FATAL, resp);
            e.logError( "Error in Post in Portal User");

        } catch ( Exception e) {

            returnError("Error creating user: " + e.getMessage(), ErrorType.GENERAL, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, PukkaLogger.Level.FATAL, resp);
            resp.flushBuffer();

            PukkaLogger.log( e );

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

            Formatter formatter = getFormatFromParameters(req);

            PortalUser user = sessionManagement.getUser();        // TODO: Add test for the case that the user already is deleted here

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
