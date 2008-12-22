package us.antera.t5restfulws.services;

import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: holloway
 * Date: Dec 21, 2008
 * Time: 4:19:37 PM
 * To change this template use File | Settings | File Templates.
 */
public interface RestfulWS
{
    public void serve (List<String> args, Request request, Response response);
}
