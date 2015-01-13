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
 *    PortalSession - Data Object
 *    Automatically generated file by GenerateTable.java
 *
 *
 */

public class PortalSession extends DataObject implements DataObjectInterface{

    private static PortalSession longLifeSystem = null;  


    private static final DataTableInterface TABLE = (DataTableInterface) new PortalSessionTable();

    public PortalSession(){

        super();

        if(table == null)
            table = TABLE;
    }

    public PortalSession(DataObjectInterface user, String token, String ip, String start, String latest, DataObjectInterface status) throws BackOfficeException{

        this(user.getKey(), token, ip, start, latest, status);
    }


    public PortalSession(DBKeyInterface user, String token, String ip, String start, String latest, DataObjectInterface status){

        this();
        try{
           ColumnStructureInterface[] columns = getColumnFromTable();


           data = new ColumnDataInterface[columns.length];

           data[0] = new ReferenceData(user, columns[0].getTableReference());
           data[1] = new StringData(token);
           data[2] = new StringData(ip);
           data[3] = new TimeStampData(start);
           data[4] = new TimeStampData(latest);
           data[5] = new ConstantData(status.get__Id(), columns[5].getTableReference());

           exists = true;
        }catch(BackOfficeException e){
            PukkaLogger.log(PukkaLogger.Level.FATAL, "Could not create object.");
            exists = false;
        }


    }
    /*********************************************************************''
     *
     *          Load from database
     *
     * @param condition - the SQL condition for selecting ONE UNIQUE object
     */

    public PortalSession(ConditionInterface condition){

        this();

        try{
            exists = load(condition);

        }catch(BackOfficeException e){

            System.out.println("Error loading object from database" + e.narration);
            e.printStackTrace();
        }

    }

    public DataObjectInterface createNew(ColumnDataInterface[] data ) throws BackOfficeException {

        PortalSession o = new PortalSession();
        o.data = data;
        o.exists = true;
        return o;
    }

    public DBKeyInterface getUserId(){

        ReferenceData data = (ReferenceData)this.data[0];
        return data.value;
    }

    public PortalUser getUser(){

        ReferenceData data = (ReferenceData)this.data[0];
        return new PortalUser(new LookupByKey(data.value));
    }

    public void setUser(DBKeyInterface user){

        ReferenceData data = (ReferenceData)this.data[0];
        data.value = user;
    }



    public String getToken(){

        StringData data = (StringData) this.data[1];
        return data.getStringValue();
    }

    public void setToken(String token){

        StringData data = (StringData) this.data[1];
        data.setStringValue(token);
    }



    public String getIP(){

        StringData data = (StringData) this.data[2];
        return data.getStringValue();
    }

    public void setIP(String ip){

        StringData data = (StringData) this.data[2];
        data.setStringValue(ip);
    }



    public DBTimeStamp getStart()throws BackOfficeException{

        TimeStampData data = (TimeStampData) this.data[3];
        return new DBTimeStamp(DBTimeStamp.SQL_TIMESTAMP, data.value);
    }

    public void setStart(DBTimeStamp start){

        TimeStampData data = (TimeStampData) this.data[3];
        data.value = start.getSQLTime().toString();
    }



    public DBTimeStamp getLatest()throws BackOfficeException{

        TimeStampData data = (TimeStampData) this.data[4];
        return new DBTimeStamp(DBTimeStamp.SQL_TIMESTAMP, data.value);
    }

    public void setLatest(DBTimeStamp latest){

        TimeStampData data = (TimeStampData) this.data[4];
        data.value = latest.getSQLTime().toString();
    }



    public SessionStatus getStatus(){

        ConstantData data = (ConstantData)this.data[5];
        return (SessionStatus)(new SessionStatusTable().getConstantValue(data.value));

    }

    public void setStatus(DataObjectInterface status){

        ConstantData data = (ConstantData)this.data[5];
        data.value = status.get__Id();
    }



    public static PortalSession getlongLifeSystem( ) throws BackOfficeException{

       if(PortalSession.longLifeSystem == null)
          PortalSession.longLifeSystem = new PortalSession(new LookupItem().addFilter(new ColumnFilter("User", "ItClarifiesSystem")));
       if(!PortalSession.longLifeSystem.exists())
          throw new BackOfficeException(BackOfficeException.TableError, "Constant longLifeSystem is missing (db update required?)");

       return PortalSession.longLifeSystem;
    }


    public static void clearConstantCache(){

        //  Clear all cache when the application is uploaded.

        PortalSession.longLifeSystem = null;
    }

    /* Code below this point will not be replaced when regenerating the file*/

    /*__endAutoGenerated*/



}
