package external;

import log.PukkaLogger;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import pukkaBO.exceptions.BackOfficeException;
import system.Organization;
import system.PortalSession;
import system.PortalUser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class PingServlet extends GenericServlet {

    public static final String DataServletName = "Ping";


    /****************************************************************************'
     *
     *          Post to åing service will fire up server or scale up during load
     *
     *
     * @param req
     * @param resp
     * @throws java.io.IOException
     *
     *
     */


    public void doPost(HttpServletRequest req, HttpServletResponse resp)throws IOException {

        try{
        logRequest(req);
        Formatter formatter = getFormatFromParameters(req);

            JSONObject response = new JSONObject()
                    .put(DataServletName, "Pong");

            sendJSONResponse(response, formatter, resp);


        } catch (JSONException e) {

            returnError(e.getMessage(), HttpServletResponse.SC_BAD_REQUEST, resp);
            e.printStackTrace();

        } catch (Exception e) {

            returnError("Internal Error", HttpServletResponse.SC_BAD_REQUEST, resp);
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
