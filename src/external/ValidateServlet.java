package external;

import log.PukkaLogger;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import pukkaBO.exceptions.BackOfficeException;
import system.PortalSession;
import system.PortalUser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class ValidateServlet extends GenericServlet {

    public static final String DataServletName = "Token";


    /****************************************************************************'
     *
     *          Validate a session token
     *
     *
     * @param req
     * @param resp
     * @throws java.io.IOException
     *
     *
     */


    public void doGet(HttpServletRequest req, HttpServletResponse resp)throws IOException {

       String name;
       String token;

        logRequest(req);

        setLoggerByParameters(req);

        Formatter formatter = getFormatFromParameters(req);

        try{

            token          = getMandatoryString    ("token", req);

            if(!sessionManagement.validate(token)){

                returnError("No session", ErrorType.SESSION, HttpServletResponse.SC_FORBIDDEN, resp);
                return;
            }

            PortalUser user = sessionManagement.getUser();

            if(!user.exists())
                returnError("No user found for session", ErrorType.GENERAL, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, resp);

            JSONObject response = new JSONObject()
                    .put("user", user.getKey().toString())
                    .put(DataServletName, "OK");

            sendJSONResponse(response, formatter, resp);



        } catch (BackOfficeException e) {

            returnError(e.narration, HttpServletResponse.SC_BAD_REQUEST, resp);
            e.printStackTrace();

        } catch (JSONException e) {

            returnError(e.getMessage(), HttpServletResponse.SC_BAD_REQUEST, resp);
            e.printStackTrace();

        }

     }



    public void doPost(HttpServletRequest req, HttpServletResponse resp)throws IOException {

        doGet(req, resp);

    }

    public void doDelete(HttpServletRequest req, HttpServletResponse resp)throws IOException {

        returnError("Delete not supported in " + DataServletName, HttpServletResponse.SC_METHOD_NOT_ALLOWED, resp);
        resp.flushBuffer();

    }


}