package external;

import dataRepresentation.DBTimeStamp;
import databaseLayer.DBKeyInterface;
import log.PukkaLogger;
import net.sf.json.JSONObject;
import pukkaBO.condition.ColumnFilter;
import pukkaBO.condition.LookupByKey;
import pukkaBO.condition.LookupItem;
import pukkaBO.exceptions.BackOfficeException;
import system.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.LookupOp;
import java.io.IOException;


/***************************************************
 *
 *         Portal User Servlet is used to crete/delete new users
 *         and get user details.
 *
 */

public class OrganizationServlet extends GenericServlet {

    public static final String DataServletName = "Organization";


    /**********************************************************************'
     *
     *              Create or update an organization
     *
     *              This should be resrticted from the application servers only
     *
     * @param req
     * @param resp
     * @throws java.io.IOException
     */



    public void doPost(HttpServletRequest req, HttpServletResponse resp)throws IOException {

        try{
            logRequest(req);

            String organizationName         = getMandatoryString("name", req);
            String organizationDescription  = getMandatoryString("description", req);
            String server                   = getMandatoryString("link", req);

            Organization organization = new Organization(new LookupItem().addFilter(new ColumnFilter(OrganizationTable.Columns.Name.name(), organizationName)));


            if(organization.exists()){

                returnError("Organization Already Exists", ErrorType.NAMING, HttpServletResponse.SC_OK, resp);
                resp.flushBuffer();
                return;

            }

            DBTimeStamp registrationDate = new DBTimeStamp();   // Set now as a registration date

            // Create the organization

            organization = new Organization(organizationName, 0, registrationDate.getISODate(), server, organizationDescription);

            organization.store();


            PukkaLogger.log(PukkaLogger.Level.MAJOR_EVENT, "Created a new organization " + organization.getName() + " on server " + server + " with id " + organization.getKey());

            Formatter formatter = getFormatFromParameters(req);

            JSONObject response = new JSONObject()
                    .put(DataServletName, organization.getKey().toString());

            sendJSONResponse(response, formatter, resp);


        }catch(BackOfficeException e){

            e.printStackTrace(System.out);
            returnError("Error creating organization: " + e.narration, ErrorType.GENERAL, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, PukkaLogger.Level.FATAL, resp);

        } catch ( Exception e) {

            e.printStackTrace(System.out);
            returnError("Error creating organization: " + e.getMessage(), ErrorType.GENERAL, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, PukkaLogger.Level.FATAL, resp);
            resp.flushBuffer();


        }
     }


    //TODO: Not implemented Access restriction to this

    public void doGet(HttpServletRequest req, HttpServletResponse resp)throws IOException {

        returnError("Get not supported in " + DataServletName, HttpServletResponse.SC_METHOD_NOT_ALLOWED, resp);
        resp.flushBuffer();



    }

    /****************************************************************************
     *
     *              Delete an organization
     *
     *              //TODO: Recursively delete all users belonging to the organization?
     *
     *
     * @param req
     * @param resp
     * @throws IOException
     */


    public void doDelete(HttpServletRequest req, HttpServletResponse resp)throws IOException {

        try{
            logRequest(req);

            DBKeyInterface _organization         = getMandatoryKey("key", req);

            Formatter formatter = getFormatFromParameters(req);

            Organization organization = new Organization(new LookupByKey(_organization));

            organization.delete();

            JSONObject json = createDeletedResponse(DataServletName, organization);

            sendJSONResponse(json, formatter, resp);


        }catch(BackOfficeException e){

            e.printStackTrace(System.out);
            returnError("Error deleting Organization: " + e.narration, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, resp);

        }catch ( Exception e) {

            e.printStackTrace(System.out);
            returnError("Error deleting Organization: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, resp);
        }

    }


}
