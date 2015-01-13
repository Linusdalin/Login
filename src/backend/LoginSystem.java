package backend;

import dataRepresentation.DataObjectInterface;
import dataRepresentation.DataTableInterface;
import log.PukkaLogger;
import pukkaAnalysis.DashboardPage;
import pukkaBO.Charts.ChartInterface;
import pukkaBO.GenericPage.PageInterface;
import pukkaBO.acs.ACS_User;
import pukkaBO.acs.ACS_UserTable;
import pukkaBO.acs.SSOLoginInterface;
import pukkaBO.backOffice.*;
import pukkaBO.condition.LookupList;
import pukkaBO.exceptions.BackOfficeException;
import pukkaBO.form.FormInterface;
import pukkaBO.formsPredefined.LoginForm;
import pukkaBO.links.LinkElement;
import pukkaBO.list.ListInterface;
import pukkaBO.pivot.PivotView;
import pukkaBO.renderer.TableRendererStarlightStatic;
import system.*;

import java.io.Serializable;
import java.util.Arrays;


/***************************************************************************'
 *
 *          It clarifies backoffice
 *
 *
 *
 */


public class LoginSystem extends AppBackOffice implements BackOfficeInterface, Serializable {

    public LoginSystem(){

        this("backoffice.config");  // Default Configuration if no file is given

    }

    /*********************************************************
     *
     *          This is an example of how to set up a back office structure.
     *          The main data is passed to the menu.
     *
     */

    public LoginSystem(String configFile){

        // 1. Call super to load properties

        super(configFile);


        // 2. Set the style config with settings for the back-office

        styleConfig = new StyleConfiguration(
                "bootstrap",
                //properties.style,                                 // Style
                "test/pukkaLogo.png",                               // TODO: Change logo to itClarifies logo
                "ItClarifies Backoffice",                           // Caption
                "Welcome to itClarifies backoffice ",               // Welcome text
                new TableRendererStarlightStatic(this)              // Table render logic to render all the tables
        );

                // Access rights

        whiteList.allow("217.13.245.*");        // Office IP
        whiteList.allow("127.0.0.1");         // Local host for testing
        whiteList.allow("213.89.59.*");       // Linus Home
        whiteList.allow("213.185.250.*");       // Ulf Home



        // Set the menu structure

        //PukkaLogger.log(PukkaLogger.Level.INFO, "!!!!!Configuring!!!!");

        menu = new Menu(

                // The menu is an array of sections.

                new Section[] {

                        // Home section
                        // A section is built by a section group list, link elements, lists and pivot tables

                    new Section("Home", "login.jsp", "table.jsp", Icon.Home,

                            new SectionGroups(

                                new SectionGroup[]{

                                   new SectionGroup("Contracts",

                                        new DataTableInterface[] {
                                        }
                                   )
                                }
                            ),

                            new LinkElement[] {

                            },
                            new ListInterface[] {

                            },
                            new PivotView[] {

                            }

                    ),



                            new Section("Users", "login.jsp", "table.jsp", Icon.Users,

                                    new SectionGroups(

                                        new SectionGroup[]{


                                                new SectionGroup("Users",

                                                     new DataTableInterface[] {

                                                             new OrganizationTable( ),
                                                            new PortalUserTable( ),
                                                     }
                                                 ),
                                                new SectionGroup("Session",

                                                     new DataTableInterface[] {

                                                            new PortalSessionTable( ),
                                                            new SessionStatusTable( ),
                                                     }
                                                 ),
                                        }
                                    ),

                                    new LinkElement[] {

                                    },
                                    new ListInterface[] {

                                            //new OrganizationList(this),
                                            //new UserList(this),   // LIst of users
                                    },
                                    new PivotView[] {

                                    }

                            ),



                    new Section("Invoicing", "login.jsp", "table.jsp", Icon.Dollartag,

                            new SectionGroups(

                                new SectionGroup[]{

                                   new SectionGroup("Contracts",

                                        new DataTableInterface[] {
                                        }
                                   )
                                }
                            ),

                            new LinkElement[] {

                            },
                            new ListInterface[] {

                            },
                            new PivotView[] {

                            }

                    ),




            },

                properties);


        // Add forms to the backoffice. These will be possible to lookup the same way as lists and tables.

        forms = new FormInterface[] {

                new LoginForm(),
                //new contractManagement.FilterForm( this, "", null, null),

          //new ExampleForm(this),
          //new ExampleForm2(this),
          //new NewAccessObjectForm(this),

        };


        charts = new ChartInterface[] {

                 //       new TestChart(),        // Test bar chart
                 //       new TestChart2(),       // Test pie chart

                };

        pages = new PageInterface[] {

                //new ReviewPage(),


                new DashboardPage(),
                //new TestLightbox(),
                //new TestPage(),        // Test bar chart
                //new SchedulerPage(),
                //new ExamplePivotPage(),
                //new UploadPage(),

                //new ACS_AdminPage(),  //TODO: Make this automatically added when using acs
        };


        //startScheduler();  //TODO: (Future) Implement closing this before turning back on

    }

    /*******************************************************************'
     *
     *      Values that will be set upon starting the system.
     *
     *
     *
     */


    public void populateSpecificValues() {

        PukkaLogger.log(PukkaLogger.Level.INFO, "Adding special values");

        // Generate encrypted passwords for the test users

        PasswordManager pwdManager = new PasswordManager();
        PortalUserTable allTestUsers = new PortalUserTable(new LookupList());

        try {

            for(DataObjectInterface object : allTestUsers.getValues()){

                PortalUser user = (PortalUser)object;

                byte[] salt = pwdManager.generateSalt();
                byte[] encodedPassword = pwdManager.getEncryptedPassword(user.getPassword(), salt);

                PukkaLogger.log(PukkaLogger.Level.INFO, "Encoding password " + user.getPassword() + " for user " + user.getName() + " to " + Arrays.toString(encodedPassword));


                user.setPassword(new String(encodedPassword, "ISO-8859-1"));
                user.setSalt(new String(salt, "ISO-8859-1"));
                user.update();

            }

                                // Do the same thing for the ACS_Admin

            ACS_UserTable allAdminUsers = new ACS_UserTable(new LookupList());

            for(DataObjectInterface object : allAdminUsers.getValues()){

                ACS_User user = (ACS_User)object;

                byte[] salt = pwdManager.generateSalt();
                byte[] encodedPassword = pwdManager.getEncryptedPassword(user.getPassword(), salt);

                PukkaLogger.log(PukkaLogger.Level.INFO, "Encoding password " + user.getPassword() + " for user " + user.getName() + " to " + Arrays.toString(encodedPassword));


                user.setPassword(new String(encodedPassword, "ISO-8859-1"));
                user.setSalt(new String(salt, "ISO-8859-1"));
                user.update();

            }


        } catch (Exception e) {

            PukkaLogger.log(PukkaLogger.Level.INFO, "Failed to generate passwords for test users");
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }


    /****************************************************************************************
     *
     *              Create the database and populate the default values
     *
     * @param args
     */


    public static void main(String[] args){

        BackOfficeInterface bo = new LoginSystem();
        bo.init();


    }

    public void init(){

        createDb();
        populateValues(false);
        populateSpecificValues();

    }


    public EventHandlerInterface getEventHandler() throws BackOfficeException{

        return null;
    }

    public SSOLoginInterface getSSO(){

        // Google SSO

        //return new GoogleSSO();
        return new EmptySSOService();
    }


    public String getCopyright() {
        return "2011 - 2014 ItClarifies";
    }

}




