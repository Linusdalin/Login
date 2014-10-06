package system;

import dataRepresentation.*;
import pukkaBO.exceptions.BackOfficeException;
import pukkaBO.condition.*;

/********************************************************
 *
 *    PortalUser - Data Table
 *    Automatically generated file by GenerateTable.java
 *
 *
 */

public class PortalUserTable extends DataTable implements DataTableInterface{

    private static final String TITLE = "User";
    public static final String TABLE = "PortalUser";
    private static final String DESCRIPTION = "All registered users.";

    public enum Columns {Name, Email, Password, Salt, Registration, Organization, }

    private static final ColumnStructureInterface[] DATA = new ColumnStructureInterface[] {

            new StringColumn("Name", DataColumn.noFormatting),
            new StringColumn("Email", DataColumn.noFormatting),
            new StringColumn("Password", DataColumn.noFormatting),
            new StringColumn("Salt", DataColumn.noFormatting),
            new DateColumn("Registration", DataColumn.noFormatting),
            new ReferenceColumn("Organization", DataColumn.noFormatting, new TableReference("Organization", "Name")),
    };

    private static final PortalUser associatedObject = new PortalUser();
    public PortalUserTable(){

        init(DATA, associatedObject, TABLE, TITLE, DESCRIPTION, DefaultValues, TestValues);
         /* No name column set for table. Using default ( 1 ) */
         // Not set as external
        // Not a constant table
    }

    public PortalUserTable(ConditionInterface condition){

        this();
        try{

            values = load(condition);
        }
        catch(BackOfficeException e){

            System.out.println("Error loading table values " + e.narration);
        }

    }
    private static final String[][] DefaultValues = {

          {"System", "", "not used", "salt", "2014-01-01", "itClarifies", "itClarifies", "itClarifies", "system"},
          {"External", "", "not used", "salt", "2014-01-01", "itClarifies", "itClarifies", "itClarifies", "system"},
          {"Linus", "Linus@dev.null", "abc123", "salt", "2014-01-01", "itClarifies", "itClarifies", "itClarifies", "system"},
          {"New", "Linus@dev.null", "abc123", "salt", "2014-01-01", "itClarifies", "itClarifies", "itClarifies", "system"},



    };
    private static final String[][] TestValues = {

          {"demo", "demo@dev.null", "demodemo", "salt", "2014-01-01", "demo.org", "itClarifies", "itClarifies", "system"},
          {"admin", "admin@dev.null", "adminadmin", "salt", "2014-01-01", "demo.org", "itClarifies", "itClarifies", "system"},
          {"ulf", "ulf@itclarifies.com", "ulfulf", "salt", "2014-01-01", "demo.org", "itClarifies", "itClarifies", "system"},
          {"eve", "eve@dev.null", "eve", "salt", "2014-01-01", "evil.org", "itClarifies", "itClarifies", "system"},



    };

    @Override
    public void clearConstantCache(){

        PortalUser.clearConstantCache();
    }

    /* Code below this point will not be replaced when regenerating the file*/

    /*__endAutoGenerated*/


}