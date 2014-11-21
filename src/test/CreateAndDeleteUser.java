package test;

import backend.LoginSystem;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import external.*;
import log.PukkaLogger;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import pukkaBO.backOffice.BackOfficeInterface;
import pukkaBO.condition.ColumnFilter;
import pukkaBO.condition.LookupItem;
import pukkaBO.condition.ReferenceFilter;
import system.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/*******************************************************************
 *
 *          Testing the service with mocked request and response messages
 *
 */


public class CreateAndDeleteUser extends ServletTests {


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
     *           - Create a new User
     *           - Verify that the user exists
     *           - Verify that the user can log in
     *           - Delete the user
     *           - Verify
     *
     *
     * @throws Exception
     */


    @Test
    public void testNewUser() throws Exception {

        try{
            MockWriter mockWriter;
            String output;

            Organization demoOrg = new Organization(new LookupItem().addFilter(new ColumnFilter(OrganizationTable.Columns.Name.name(), "demo.org")));
            assertThat("Pre-requisite demo.org exists ", demoOrg.exists(), is(true) );

            int existingUsers = new PortalUserTable().getCount();


            // Create a new user

            mockWriter = new MockWriter();

            when(request.getParameter("user")).thenReturn("Kalle");
            when(request.getParameter("password")).thenReturn("secretpassword");
            when(request.getParameter("session")).thenReturn("DummyAdminToken");
            when(request.getRemoteAddr()).thenReturn("127.0.0.1");
            when(response.getWriter()).thenReturn(mockWriter.getWriter());

            new UserServlet().doPost(request, response);

            output = mockWriter.getOutput();
            PukkaLogger.log(PukkaLogger.Level.INFO, "JSON: " + output);

            int userRecount = new PortalUserTable().getCount();

            assertThat("There is a new user created.", userRecount, is(existingUsers + 1));


            // Now try to login

            mockWriter = new MockWriter();

            when(request.getParameter("user")).thenReturn("Kalle");
            when(request.getParameter("password")).thenReturn("secretpassword");
            when(response.getWriter()).thenReturn(mockWriter.getWriter());

            new LoginServlet().doPost(request, response);

            output = mockWriter.getOutput();
            PukkaLogger.log(PukkaLogger.Level.INFO, "JSON: " + output);

            JSONObject json = new JSONObject(output);
            String token = json.getString("Login");
            int idForCreatedUser = json.getInt("User");


            // Now we delete the user again

            mockWriter = new MockWriter();

            when(request.getParameter("session")).thenReturn("DummyAdminToken");
            when(request.getParameter("user")).thenReturn("" + idForCreatedUser);
            when(response.getWriter()).thenReturn(mockWriter.getWriter());

            new UserServlet().doDelete(request, response);

            output = mockWriter.getOutput();
            PukkaLogger.log(PukkaLogger.Level.INFO, "JSON: " + output);

            json = new JSONObject(output);
            assertThat(json.getString("User"), is("DELETED"));

            userRecount = new PortalUserTable().getCount();

            assertThat("There is now as many users as when we begun.", userRecount, is(existingUsers));



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

