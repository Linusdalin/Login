package system;

import system.*;
import dataRepresentation.*;
import databaseLayer.DBKeyInterface;
import java.util.List;
import java.util.Map;
import log.PukkaLogger;
import pukkaBO.exceptions.BackOfficeException;
import pukkaBO.condition.*;
import pukkaBO.database.*;

import pukkaBO.acs.*;

/********************************************************
 *
 *    PortalUser - Data Object
 *    Automatically generated file by GenerateTable.java
 *
 *
 */

public class PortalUser extends DataObject implements DataObjectInterface{

    private static PortalUser SystemUser = null;  
    private static PortalUser ExternalUser = null;  
    private static PortalUser Super = null;  


    private static final DataTableInterface TABLE = (DataTableInterface) new PortalUserTable();

    public PortalUser(){

        super();         if(table == null)
            table = TABLE;
    }

    public PortalUser(String name, long userid, String password, String salt, String registration, DataObjectInterface organization, boolean active) throws BackOfficeException{

        this(name, userid, password, salt, registration, organization.getKey(), active);
    }


    public PortalUser(String name, long userid, String password, String salt, String registration, DBKeyInterface organization, boolean active) throws BackOfficeException{

        this();
        ColumnStructureInterface[] columns = getColumnFromTable();


        data = new ColumnDataInterface[columns.length];

        data[0] = new StringData(name);
        data[1] = new IntData(userid);
        data[2] = new StringData(password);
        data[3] = new StringData(salt);
        data[4] = new DateData(registration);
        data[5] = new ReferenceData(organization, columns[5].getTableReference());
        data[6] = new BoolData(active);

        exists = true;


    }
    /*********************************************************************''
     *
     *          Load from database
     *
     * @param condition - the SQL condition for selecting ONE UNIQUE object
     */

    public PortalUser(ConditionInterface condition){

        this();

        try{
            exists = load(condition);

        }catch(BackOfficeException e){

            System.out.println("Error loading object from database" + e.narration);
            e.printStackTrace();
        }

    }

    public DataObjectInterface createNew(ColumnDataInterface[] data ) throws BackOfficeException {

        PortalUser o = new PortalUser();
        o.data = data;
        o.exists = true;
        return o;
    }

    public String getName(){

        StringData data = (StringData) this.data[0];
        return data.getStringValue();
    }

    public void setName(String name){

        StringData data = (StringData) this.data[0];
        data.setStringValue(name);
    }



    public long getUserId(){

        IntData data = (IntData) this.data[1];
        return data.value;
    }

    public void setUserId(long userid){

        IntData data = (IntData) this.data[1];
        data.value = userid;
    }



    public String getPassword(){

        StringData data = (StringData) this.data[2];
        return data.getStringValue();
    }

    public void setPassword(String password){

        StringData data = (StringData) this.data[2];
        data.setStringValue(password);
    }



    public String getSalt(){

        StringData data = (StringData) this.data[3];
        return data.getStringValue();
    }

    public void setSalt(String salt){

        StringData data = (StringData) this.data[3];
        data.setStringValue(salt);
    }



    public DBTimeStamp getRegistration()throws BackOfficeException{

        DateData data = (DateData) this.data[4];
        return new DBTimeStamp(DBTimeStamp.ISO_DATE, data.value);
    }

    public void setRegistration(DBTimeStamp registration){

        DateData data = (DateData) this.data[4];
        data.value = registration.getISODate().toString();
    }



    public DBKeyInterface getOrganizationId(){

        ReferenceData data = (ReferenceData)this.data[5];
        return data.value;
    }

    public Organization getOrganization(){

        ReferenceData data = (ReferenceData)this.data[5];
        return new Organization(new LookupByKey(data.value));
    }

    public void setOrganization(DBKeyInterface organization){

        ReferenceData data = (ReferenceData)this.data[5];
        data.value = organization;
    }



    public boolean getActive(){

        BoolData data = (BoolData) this.data[6];
        return data.value;
    }

    public void setActive(boolean active){

        BoolData data = (BoolData) this.data[6];
        data.value = active;
    }



    public static PortalUser getSystemUser( ) throws BackOfficeException{

       if(PortalUser.SystemUser == null)
          PortalUser.SystemUser = new PortalUser(new LookupItem().addFilter(new ColumnFilter("Name", "System")));
       if(!PortalUser.SystemUser.exists())
          throw new BackOfficeException(BackOfficeException.TableError, "Constant SystemUser is missing (db update required?)");

       return PortalUser.SystemUser;
     }

    public static PortalUser getExternalUser( ) throws BackOfficeException{

       if(PortalUser.ExternalUser == null)
          PortalUser.ExternalUser = new PortalUser(new LookupItem().addFilter(new ColumnFilter("Name", "External")));
       if(!PortalUser.ExternalUser.exists())
          throw new BackOfficeException(BackOfficeException.TableError, "Constant ExternalUser is missing (db update required?)");

       return PortalUser.ExternalUser;
     }

    public static PortalUser getSuper( ) throws BackOfficeException{

       if(PortalUser.Super == null)
          PortalUser.Super = new PortalUser(new LookupItem().addFilter(new ColumnFilter("Name", "Super")));
       if(!PortalUser.Super.exists())
          throw new BackOfficeException(BackOfficeException.TableError, "Constant Super is missing (db update required?)");

       return PortalUser.Super;
     }


    public static void clearConstantCache(){

        //  Clear all cache when the application is uploaded.

        PortalUser.SystemUser = null;
        PortalUser.ExternalUser = null;
        PortalUser.Super = null;
    }

    /* Code below this point will not be replaced when regenerating the file*/

    /*__endAutoGenerated*/



}
