package system;

import system.*;
import dataRepresentation.*;
import databaseLayer.DBKeyInterface;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import log.PukkaLogger;
import pukkaBO.exceptions.BackOfficeException;
import pukkaBO.condition.*;
import pukkaBO.database.*;

import pukkaBO.acs.*;

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

    public enum Columns {Name, UserId, Password, Salt, Registration, Organization, Active, ActivationCode, }

    private static final ColumnStructureInterface[] DATA = new ColumnStructureInterface[] {

            new StringColumn("Name", DataColumn.noFormatting),
            new IntColumn("UserId", DataColumn.noFormatting),
            new StringColumn("Password", DataColumn.noFormatting),
            new StringColumn("Salt", DataColumn.noFormatting),
            new DateColumn("Registration", DataColumn.noFormatting),
            new ReferenceColumn("Organization", DataColumn.noFormatting, new TableReference("Organization", "Name")),
            new BoolColumn("Active", DataColumn.noFormatting),
            new StringColumn("ActivationCode", DataColumn.noFormatting),
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

          {"ItClarifiesSystem", "0", "not used", "salt", "2014-01-01", "itClarifies", "true", "code", "system"},
          {"Super", "1", "abc123", "salt", "2014-01-01", "itClarifies", "true", "code", "system"},



    };
    private static final String[][] TestValues = {

          {"DemoSystem", "0", "not used", "salt", "2014-01-01", "demo.org", "true", "code", "system"},
          {"demo", "2", "demodemo", "salt", "2014-01-01", "demo.org", "true", "code", "system"},
          {"admin", "3", "adminadmin", "salt", "2014-01-01", "demo.org", "true", "code", "system"},
          {"linus", "4", "linus", "salt", "2014-01-01", "demo.org", "true", "code", "system"},
          {"EvilSystem", "0", "not used", "salt", "2014-01-01", "evil.org", "true", "code", "system"},



    };

    @Override
    public void clearConstantCache(){

        PortalUser.clearConstantCache();
    }

    /* Code below this point will not be replaced when regenerating the file*/

    /*__endAutoGenerated*/


}
