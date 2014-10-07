package test;

import backend.LoginSystem;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import databaseLayer.DBKeyInterface;
import databaseLayer.DatabaseAbstractionFactory;
import external.*;
import log.PukkaLogger;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import pukkaBO.backOffice.BackOfficeInterface;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import javax.servlet.http.*;

import pukkaBO.condition.ColumnFilter;
import pukkaBO.condition.LookupByKey;
import pukkaBO.condition.LookupItem;
import pukkaBO.condition.ReferenceFilter;
import system.*;

/*******************************************************************
 *
 *          Testing the service with mocked request and response messages
 *
 */


public class LoginAndValidate extends ServletTests {


    private static BackOfficeInterface bo;
    private static LocalServiceTestHelper helper;
    private static HttpServletRequest request;
    private static HttpServletResponse response;


    @AfterClass
    public static void tearDown() {

        helper.tearDown();
    }


    @BeforeClass
    public static void preAmble(){

        helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
        helper.setUp();

        bo = new LoginSystem();
        bo.createDb();
        bo.populateValues(true);
        bo.populateSpecificValues();

        PukkaLogger.setLogLevel(PukkaLogger.Level.DEBUG);

        try {

            request = mock(HttpServletRequest.class);
            response = mock(HttpServletResponse.class);

            PukkaLogger.setLogLevel(PukkaLogger.Level.DEBUG);


        } catch (Exception e) {

            e.printStackTrace();
            assertTrue(false);
        }

    }

    /****************************************************************************************
     *
     *          Main test use case
     *
     *           - Login
     *           - Verify that the session validates
     *           - Verifu that the login response returned the correct user
     *           - Get user details
     *           - Logout
     *           - Verify that the session does not validate
     *
     *
     * @throws Exception
     */


    @Test
    public void testLogin() throws Exception {

        try{

            Organization demoOrg = new Organization(new LookupItem().addFilter(new ColumnFilter(OrganizationTable.Columns.Name.name(), "demo.org")));
            assertThat("Pre-requisite demo.org exists ", demoOrg.exists(), is(true) );


            MockWriter mockWriter = new MockWriter();

            when(request.getParameter("user")).thenReturn("demo");
            when(request.getParameter("password")).thenReturn("demodemo");
            when(response.getWriter()).thenReturn(mockWriter.getWriter());

            new LoginServlet().doPost(request, response);

            String output = mockWriter.getOutput();
            PukkaLogger.log(PukkaLogger.Level.INFO, "JSON: " + output);

            JSONObject json = new JSONObject(output);
            String token = json.getString("Login");
            int id = json.getInt("user");

            PortalUser portalUser = new PortalUser(new LookupItem()
                    .addFilter(new ColumnFilter(PortalUserTable.Columns.UserId.name(), id))
                    .addFilter(new ReferenceFilter(PortalUserTable.Columns.Organization.name(), demoOrg.getKey())));

            assertThat(token, is("DummySessionToken"));
            assertTrue(portalUser.exists());
            assertThat(portalUser.getName(), is("demo"));

            // Check the session

            SessionManagement sessionManagement = new SessionManagement();
            assertTrue(sessionManagement.validate(token));

            // Check Validating the session

            mockWriter = new MockWriter();

            when(request.getParameter("token")).thenReturn(token);
            when(response.getWriter()).thenReturn(mockWriter.getWriter());

            new ValidateServlet().doGet(request, response);

            output = mockWriter.getOutput();
            PukkaLogger.log(PukkaLogger.Level.INFO, "JSON: " + output);

            json = new JSONObject(output);

            String confirmation = json.getString("Token");
            int userId = json.getInt("User");
            String organization = json.getString("Organization");


            assertThat(confirmation,     is("OK"));
            assertThat(userId,           is(1));
            assertThat(organization,    is("demo.org"));


            // Now log out

            mockWriter = new MockWriter();

            when(request.getParameter("session")).thenReturn(token);
            when(response.getWriter()).thenReturn(mockWriter.getWriter());

            new LogoutServlet().doPost(request, response);

            output = mockWriter.getOutput();
            PukkaLogger.log(PukkaLogger.Level.INFO, "JSON: " + output);

            json = new JSONObject(output);
            assertThat(json.getString("status"), is("closed"));

            assertFalse(sessionManagement.validate(token));


        }catch(NullPointerException e){

            e.printStackTrace();
            assertTrue(false);
        }
    }


    @Test
    public void testDeleteLogin() throws Exception {

        try{
            MockWriter mockWriter = new MockWriter();

            when(request.getParameter("user")).thenReturn("demo");
            when(response.getWriter()).thenReturn(mockWriter.getWriter());

            new LoginServlet().doDelete(request, response);


            String output = mockWriter.getOutput();
            PukkaLogger.log(PukkaLogger.Level.INFO, "JSON: " + output);

            JSONObject json = new JSONObject(output);
            JSONObject error = (JSONObject)json.getJSONArray("error").get(0);
            String message = error.getString("message");

            assertThat(message, is("Delete not supported in Login"));

        }catch(NullPointerException e){

            e.printStackTrace();
            assertTrue(false);
        }
    }



    @Test
    public void testFailPassword() throws Exception {

        try{
            MockWriter mockWriter;

            mockWriter = new MockWriter();

            when(request.getParameter("user")).thenReturn("demo");
            when(request.getParameter("password")).thenReturn("wrong pwd");
            when(response.getWriter()).thenReturn(mockWriter.getWriter());

            new LoginServlet().doPost(request, response);

            String output = mockWriter.getOutput();

            PukkaLogger.log(PukkaLogger.Level.INFO, "JSON: " + output);

            JSONObject json = new JSONObject(output);

            assertError(json, GenericServlet.ErrorType.SESSION);


        }catch(Exception e){

            e.printStackTrace();
            assertTrue(false);

        }
    }


    @Test
    public void testFailUser() throws Exception {

        try{
            MockWriter mockWriter = new MockWriter();

            when(request.getParameter("user")).thenReturn("unknown user");
            when(request.getParameter("password")).thenReturn("demo");
            when(response.getWriter()).thenReturn(mockWriter.getWriter());

            new LoginServlet().doPost(request, response);


            String output = mockWriter.getOutput();

            PukkaLogger.log(PukkaLogger.Level.INFO, "JSON: " + output);

            JSONObject json = new JSONObject(output);

            assertError(json, GenericServlet.ErrorType.SESSION);


        }catch(NullPointerException e){

            e.printStackTrace();
            assertTrue(false);

        }catch(JSONException e){

            e.printStackTrace();
            assertTrue(false);
        }


    }


}

