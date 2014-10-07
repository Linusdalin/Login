package test;

import external.GenericServlet;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.hamcrest.CoreMatchers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * Created with IntelliJ IDEA.
 * User: Linus
 * Date: 2014-07-03
 * Time: 15:42
 * To change this template use File | Settings | File Templates.
 */
public class ServletTests {

    protected void isKey(String key){

        assertNotNull( key );
        assertTrue(key.length() > 10);    // This is just a small test. Google App Engine keys are longer

        assertFalse(key.contains("{"));
        assertFalse(key.contains("}"));
        assertFalse(key.contains("["));
        assertFalse(key.contains("}"));
    }


    protected void assertCorrectlyDeleted(String service, JSONObject json) {

        assertThat(json.getString(service), is("DELETED"));
    }


    protected void assertError(JSONObject json, GenericServlet.ErrorType session) {

        JSONArray errorData = json.getJSONArray("error");
        JSONObject first = (JSONObject)errorData.get(0);
        assertThat(first.getString("type"), is(session.name()));

    }






}
