package external;

import log.PukkaLogger;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import pukkaBO.exceptions.BackOfficeException;
import system.PortalSession;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class LogoutServlet extends GenericServlet {

    public static final String DataServletName = "Logout";


    /****************************************************************************'
     *
     *          Post to logout session will close the session
     *
     *
     * @param req
     * @param resp
     * @throws java.io.IOException
     *
     *
     */


    public void doPost(HttpServletRequest req, HttpServletResponse resp)throws IOException {

        String token;

        logRequest(req);

        setLoggerByParameters(req);

        Formatter formatter = getFormatFromParameters(req);

        try{

            token          = getMandatoryString    ("token", req);

            // Check if there is an active session

            String status = sessionManagement.close(token);

            JSONObject json = new JSONObject().put("status", status);

            sendJSONResponse(json, formatter, resp);


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
