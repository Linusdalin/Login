package test;


import backend.LoginSystem;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import log.PukkaLogger;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import pukkaBO.backOffice.BackOfficeInterface;
import pukkaBO.condition.ColumnFilter;
import pukkaBO.condition.LookupByKey;
import pukkaBO.condition.LookupItem;
import pukkaBO.exceptions.BackOfficeException;
import pukkaBO.password.PasswordManager;
import system.*;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

/************************************************************************
 *
 *          Simple tests for the Session management
 */


public class SessionManagerTest {


    private static BackOfficeInterface bo;
    private static LocalServiceTestHelper helper;


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


    }




    /******************************************************************
     *
     *
     *          The session token shall be different. Perform a test where
     *          we generate n tokens and check to se there are no duplicates
     *
     *
     */



    @Test
    public void randomnessTest(){


        List<SessionToken> listOfTokens = new ArrayList<SessionToken>();

        for(int i = 0; i < 1000; i++){

            SessionToken t = new SessionToken();
            assertFalse(listOfTokens.contains(t));   // Not seen before
            listOfTokens.add(t);
        }



    }

    /************************************************************************************'
     *
     *      Create and close a session in the database is done through the
     *      two static methods in the SessionManagementClass.
     *
     *
     */

    @Test
    public void unValidatedUser(){

        try {

            String userName = "demo";
            String pwd = "demo";
            String ipAddress = "127.0.0.1";

            SessionManagement sessionManagement = new SessionManagement();
            sessionManagement.createSession(userName, pwd, ipAddress);
            PortalUser user = sessionManagement.getUser();

            Assert.assertTrue(false);

        } catch (BackOfficeException e) {

            e.swallow("Expected");

        }catch(Exception e){

            // No exceptions expected
            e.printStackTrace();
            assertTrue(false);

        }

    }

    @Test
    public void createSessionTest(){


        try {

            String userName = "demo";
            String pwd = "demodemo";
            String ipAddress = "127.0.0.1";

            SessionManagement sessionManagement = new SessionManagement();

            // Create a session and get a token
            PortalSession session = sessionManagement.createSession(userName, pwd, ipAddress);

            assertThat("Session exists", session.exists(), (is(true)));
            String token = session.getToken();

            // Check that it now exists in the database

            PortalSession sessionInDB = new PortalSession(new LookupItem().addFilter(new ColumnFilter(
                    PortalSessionTable.Columns.Token.name(), token)));

            assertThat(sessionInDB.exists(), is(true));
            assertThat(sessionInDB.getStatus().getName(), is("open"));
            assertThat(sessionInDB.getIP(), is(ipAddress));

            // It should be possible to lookup the user from the session

            sessionManagement.validate(token, ipAddress);
            PortalUser user = sessionManagement.getUser();

            assertThat(user.getName(), is(userName));

            // As this session is open it should be validated

            assertThat(sessionManagement.validate(token, ipAddress), is(true));

            // Now close hte session.

            sessionManagement.close(session.getToken());

            sessionInDB = new PortalSession(new LookupItem().addFilter(new ColumnFilter(
                    PortalSessionTable.Columns.Token.name(), session.getToken())));

            // The closed session still exists in the database, but it is closed

            assertThat(sessionInDB.exists(), is(true));
            assertThat(sessionInDB.getStatus().getName(), is("closed"));

            // And with a closed session it should not validate

            assertThat(sessionManagement.validate(token, ipAddress), is(false));


        } catch (BackOfficeException e) {

            e.logError("Error in create Session test");
            assertTrue(false);
        }catch(Exception e){

            // No exceptions expected
            e.printStackTrace();
            assertTrue(false);

        }

    }


    @Test
    public void passwordTest(){

        try {
        PasswordManager pwdManager = new PasswordManager();
        String password = "very hemligt password";

            // Create a password

            byte[] salt = pwdManager.generateSalt();
            byte[] encryptedPassword = pwdManager.getEncryptedPassword(password, salt);

            PukkaLogger.log(PukkaLogger.Level.INFO, "key:" + new String(encryptedPassword));
            PukkaLogger.log(PukkaLogger.Level.INFO, "Length:" + encryptedPassword.length);

            assertThat(encryptedPassword.length, is(20));

            // Create it again and expect to get the same

            byte[] encryptedAgain = pwdManager.getEncryptedPassword(password, salt);

            assertThat(encryptedAgain, is(encryptedPassword));


        } catch (Exception e) {

            e.printStackTrace();
            assertTrue(false);
        }

    }

    /****************************************************************************
     *
     *
     *      Salt is used to iterate the key and to ensure that two generated passwords are
     *      not the same even if they accidently were the same in clear text
     *
     */

    @Test
    public void saltTest(){

        try {
        PasswordManager pwdManager = new PasswordManager();
        String password = "very hemligt password";

            // Create a password

            byte[] salt1 = pwdManager.generateSalt();
            byte[] salt2 = pwdManager.generateSalt();
            byte[] salt3 = pwdManager.generateSalt();

            byte[] encryptedPassword1 = pwdManager.getEncryptedPassword(password, salt1);
            byte[] encryptedPassword2 = pwdManager.getEncryptedPassword(password, salt2);
            byte[] encryptedPassword3 = pwdManager.getEncryptedPassword(password, salt3);


            assertThat(encryptedPassword1, not(is(encryptedPassword2)));
            assertThat(encryptedPassword1, not(is(encryptedPassword3)));
            assertThat(encryptedPassword2, not(is(encryptedPassword3)));


        } catch (Exception e) {

            e.printStackTrace();
            assertTrue(false);
        }

    }

    /*******************************************************************'
     *
     *

     byte[] b = string.getBytes();
     byte[] b = string.getBytes(Charset.forName("UTF-8"));


     *
     */

    @Test
    public void storeAndRetrievePasswordTest(){

        try {
            PasswordManager pwdManager = new PasswordManager();
            String password = "very hemligt password";

            Organization org = new Organization(new LookupItem().addFilter(new ColumnFilter(OrganizationTable.Columns.Name.name(), "demo.org")));

            // Create a password

            byte[] salt = pwdManager.generateSalt();
            byte[] encryptedPassword = pwdManager.getEncryptedPassword(password, salt);

            String encryptedString = new String(encryptedPassword, "ISO-8859-1");
            byte[] convertBack = encryptedString.getBytes("ISO-8859-1");

            // Test the conversion itself

            assertThat(convertBack, is(encryptedPassword));

            // Store the password in a new user in the database

            PortalUser user = new PortalUser("Linus2", 4711,  encryptedString, new String(salt, "ISO-8859-1"), "2014-06-01", org.getKey(), true, "");
            user.store();

            // Read back the user

            PortalUser readBackUser = new PortalUser(new LookupByKey(user.getKey()));
            String passwordString = readBackUser.getPassword();

            PukkaLogger.log(PukkaLogger.Level.INFO, "Got user " + user.getName());

            byte[] readBackPassword = passwordString.getBytes("ISO-8859-1");

            assertThat(readBackPassword, is(encryptedPassword));


            assertTrue(pwdManager.authenticate(password, readBackPassword, salt));



        } catch (Exception e) {

            e.printStackTrace();
            assertTrue(false);
        }

    }


}

