package system;
import java.util.UUID;

/**
 *          Session Token is holding the token string
 *
 */

public class SessionToken {

    private final UUID token;

    public SessionToken(){

        //this.token = "newDummySessionToken";
        this.token = UUID.randomUUID();
    }


    /**************************************************************
     *
     *      Equals is used to compare.
     *
     * @param aThat
     * @return
     */

    @Override
    public boolean equals(Object aThat) {
        //check for self-comparison
        if ( this == aThat ) return true;

        if ( !(aThat instanceof SessionToken) ) return false;

        SessionToken that = (SessionToken)aThat;

        return
          token.toString().equals(that.toString());
      }


    /***********************************************************'
     *
     *          Exporting to string
     *
     * @return
     */


    public String toString(){

        return token.toString();
    }
}



