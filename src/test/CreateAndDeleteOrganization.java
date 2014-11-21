package test;

import backend.LoginSystem;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import external.GenericServlet;
import external.LoginServlet;
import external.OrganizationServlet;
import external.UserServlet;
import log.PukkaLogger;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import pukkaBO.backOffice.BackOfficeInterface;
import pukkaBO.condition.ColumnFilter;
import pukkaBO.condition.LookupItem;
import system.Organization;
import system.OrganizationTable;
import system.PortalUserTable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/*******************************************************************
 *
 *          Testing the service with mocked request and response messages
 *
 */


public class CreateAndDeleteOrganization extends ServletTests {


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
     *           - Create a new Organization
     *           - Verify that the organization exists
     *           - Delete the organization
     *           - Verify
     *
     *
     * @throws Exception
     */


    @Test
    public void testNewOrganization() throws Exception {

        try{
            MockWriter mockWriter;
            String output;

            int existingOrgs = new OrganizationTable().getCount();


            // Create a new organization

            mockWriter = new MockWriter();

            when(request.getParameter("name")).thenReturn("Kalle.org");
            when(request.getParameter("description")).thenReturn("test org");
            when(request.getParameter("link")).thenReturn("http://server.com");

            when(response.getWriter()).thenReturn(mockWriter.getWriter());

            new OrganizationServlet().doPost(request, response);

            output = mockWriter.getOutput();
            PukkaLogger.log(PukkaLogger.Level.INFO, "JSON: " + output);

            JSONObject json = new JSONObject(output);

            String organizationKey = json.getString("Organization");

            int orgRecount = new OrganizationTable().getCount();

            assertThat("There is a new user created.", orgRecount, is(existingOrgs + 1));



            // Now we delete the organization again

            mockWriter = new MockWriter();

            when(request.getParameter("key")).thenReturn(organizationKey);
            when(response.getWriter()).thenReturn(mockWriter.getWriter());

            new OrganizationServlet().doDelete(request, response);

            output = mockWriter.getOutput();
            PukkaLogger.log(PukkaLogger.Level.INFO, "JSON: " + output);

            json = new JSONObject(output);
            assertThat(json.getString("Organization"), is("DELETED"));

            orgRecount = new OrganizationTable().getCount();

            assertThat("There is now as many users as when we begun.", orgRecount, is(existingOrgs));



        }catch(NullPointerException e){

            e.printStackTrace();
            assertTrue(false);
        }
    }


    @Test
    public void testAlreadyExists() throws Exception {


        MockWriter mockWriter;
        String output;

        try{
            int existingOrgs = new OrganizationTable().getCount();


            // Create a new organization

            mockWriter = new MockWriter();

            when(request.getParameter("name")).thenReturn("demo.org");
            when(request.getParameter("description")).thenReturn("test org");
            when(response.getWriter()).thenReturn(mockWriter.getWriter());

            new OrganizationServlet().doPost(request, response);

            output = mockWriter.getOutput();
            PukkaLogger.log(PukkaLogger.Level.INFO, "JSON: " + output);

            JSONObject json = new JSONObject(output);

            assertError(json, GenericServlet.ErrorType.NAMING);

            int orgRecount = new OrganizationTable().getCount();

            assertThat("There is NO new user created.", orgRecount, is(existingOrgs));


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

