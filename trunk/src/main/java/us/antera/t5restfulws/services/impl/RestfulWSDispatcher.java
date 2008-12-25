package us.antera.t5restfulws.services.impl;

import org.apache.tapestry5.services.*;

import java.util.*;
import java.io.IOException;
import java.lang.reflect.Method;

import us.antera.t5restfulws.services.RestfulWebMethod;


/**
 * Created by IntelliJ IDEA.
 * User: holloway
 * Date: Dec 21, 2008
 * Time: 12:39:53 AM
 * To change this template use File | Settings | File Templates.
 */
public class RestfulWSDispatcher <T> implements RequestFilter
{
    private final Map<String, Map<String, Method>> _methodMap;

    private final Map<String, Object> _services;

    public RestfulWSDispatcher(Map<String, Object> services)
    {
        _services = services;
        _methodMap = new TreeMap<String, Map<String, Method>>();
        buildMethodMap (services);
    }

    private void buildMethodMap(Map<String, Object> services) {
        for (Map.Entry<String, Object> entry : services.entrySet())
        {
            Map<String, Method> methodMap = new TreeMap<String, Method>();
            Method[] methods = entry.getValue().getClass().getDeclaredMethods();
            for (Method m : methods)
            {
                if(m.isAnnotationPresent(RestfulWebMethod.class))
                {
                    methodMap.put(m.getName().toLowerCase(), m);
                }
            }
            _methodMap.put(entry.getKey(), methodMap);
        }
    }

    public boolean service (Request request, Response response, RequestHandler handler) throws IOException
    {
        String path = request.getPath();
        String[] queryParams = path.split("/");
        if (queryParams.length > 1 && _services.containsKey(queryParams[1]))
        {
            String[] args = setupArgs(queryParams);

            return invokeMethod(queryParams[1], queryParams[2], request, response, args);
        }

        return handler.service(request, response);
    }

    private String[] setupArgs(String[] queryParams)
    {
        String[] args = new String[0];
        if (queryParams.length > 3)
        {
            args = new String[queryParams.length];
            for (int k = 3; k < queryParams.length; k++)
            {
                args[k - 3] = queryParams[k];
            }
        }
        return args;
    }

    private boolean invokeMethod (String serviceId, String methodName, Request request, Response response,
                                  String... args)
    {
        Object service = _services.get(serviceId);
        Map<String, Method> methodMap = _methodMap.get(serviceId);
        if (methodMap == null)
            throw new RuntimeException("There are no methods on service " + serviceId);
        Method m = methodMap.get(methodName);
        if (m == null)
            throw new RuntimeException("The service identified by " + serviceId +
                " has no method with lower-case name " + methodName);

        Class[] mparams = m.getParameterTypes();

        if (mparams.length < 2)
            throw new RuntimeException("The method " + methodName + " on the service " + serviceId +
                    "must take at least" +
                    " two arguments: the request and response objects");

        if (!mparams[0].equals(Request.class) || !mparams[1].equals(Response.class))
            throw new RuntimeException("The first argument to method " + methodName + " in service" +
                    " " + serviceId + " must be a Request object, and the second must be" +
                    " a Reponse object.");

        if (!m.getReturnType().equals(Void.TYPE))
            throw new RuntimeException("Web method " + methodName + " must return void type.");

        try
        {
            m.invoke(service, args);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        return true;
    }
}
