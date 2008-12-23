package us.antera.t5restfulws.services.impl;

import org.apache.tapestry5.services.*;

import java.util.*;
import java.io.IOException;

import us.antera.t5restfulws.services.RestfulWS;


/**
 * Created by IntelliJ IDEA.
 * User: holloway
 * Date: Dec 21, 2008
 * Time: 12:39:53 AM
 * To change this template use File | Settings | File Templates.
 */
public class RestfulWSDispatcher implements RequestFilter
{
    private final Map<String, RestfulWS> _services;

    public RestfulWSDispatcher(Map<String, RestfulWS> services)
    {
        _services = new TreeMap<String, RestfulWS>(services);
    }

    public boolean service (Request request, Response response, RequestHandler handler) throws IOException
    {
        String path = request.getPath();
        String[] sp = path.split("/");
        if (sp.length > 0 && _services.containsKey(sp[1]))
        {
            List<String> spArrList = Arrays.asList(sp);
            RestfulWS service = _services.get(sp[1]);
            service.serve(spArrList.subList(2, spArrList.size()), request, response);
            return true;
        }

        return handler.service(request, response);
    }
}
