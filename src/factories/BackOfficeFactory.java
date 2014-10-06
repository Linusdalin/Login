package factories;


import backend.LoginSystem;

/**
 * Created with IntelliJ IDEA.
 * User: Linus
 * Date: 2014-09-05
 * Time: 22:15
 * To change this template use File | Settings | File Templates.
 *
 *
 */

public class BackOfficeFactory {

    public static Object getBackOffice() {

        return new LoginSystem();
    }


}
