package external;

import log.PukkaLogger;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import pukkaBO.condition.ColumnFilter;
import pukkaBO.condition.LookupItem;
import pukkaBO.exceptions.BackOfficeException;
import system.Organization;
import system.PortalSession;
import system.PortalUser;
import system.PortalUserTable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class ActivationServlet extends GenericServlet {

    public static final String DataServletName = "Activation";


    /****************************************************************************'
     *
     *          Post to login session will create a new session given a username and password
     *
     *
     * @param req
     * @param resp
     * @throws java.io.IOException
     *
     *
     */


    public void doPost(HttpServletRequest req, HttpServletResponse resp)throws IOException {

       String activationCode;

        logRequest(req);

        setLoggerByParameters(req);

        Formatter formatter = getFormatFromParameters(req);

        try{

            activationCode           = getMandatoryString    ("activation", req);

            PortalUser user = new PortalUser(new LookupItem().addFilter(new ColumnFilter(PortalUserTable.Columns.ActivationCode.name(), activationCode)));

            if(!user.exists()){

                returnError("Fail to activate user. No matching activation code", ErrorType.SESSION, HttpServletResponse.SC_FORBIDDEN, resp);
                resp.flushBuffer();
                return;
            }


            PukkaLogger.log(PukkaLogger.Level.ACTION, "Activating user " + user.getName());

            user.setActive(true);
            user.update();

            JSONObject response = new JSONObject().put(DataServletName, "OK");

            sendJSONResponse(response, formatter, resp);



        } catch (BackOfficeException e) {

            returnError(e.narration, HttpServletResponse.SC_BAD_REQUEST, resp);
            e.printStackTrace();

        } catch (JSONException e) {

            returnError(e.getMessage(), HttpServletResponse.SC_BAD_REQUEST, resp);
            e.printStackTrace();

        }

     }


    public void doGet(HttpServletRequest req, HttpServletResponse resp)throws IOException {

        doPost(req, resp);

    }

    public void doDelete(HttpServletRequest req, HttpServletResponse resp)throws IOException {

        returnError("Delete not supported in " + DataServletName, HttpServletResponse.SC_METHOD_NOT_ALLOWED, resp);
        resp.flushBuffer();

    }


}
