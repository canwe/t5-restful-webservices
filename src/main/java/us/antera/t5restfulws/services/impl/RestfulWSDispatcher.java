/*
 THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL ANTERA
 CONSULTING, OR ANY CONTRIBUTORS TO THIS SOFTWARE, BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 OF SUCH DAMAGE.
 */

package us.antera.t5restfulws.services.impl;

import org.apache.tapestry5.services.*;
import org.apache.tapestry5.ValueEncoder;

import java.util.*;
import java.io.IOException;
import java.lang.reflect.Method;

import us.antera.t5restfulws.RestfulWebMethod;


/**
 * Created by IntelliJ IDEA.
 * User: holloway
 * Date: Dec 21, 2008
 * Time: 12:39:53 AM
 */
public class RestfulWSDispatcher <T> implements RequestFilter
{
    private final Map<String, Map<String, Method>> _methodMap;

    private final Map<String, Object> _services;

    private final ValueEncoderSource _valEncSource;

    public RestfulWSDispatcher(Map<String, Object> services, ValueEncoderSource ves)
    {
        _valEncSource = ves;
        _services = services;
        _methodMap = new TreeMap<String, Map<String, Method>>();
        buildMethodMap (services);
    }

    private void buildMethodMap (Map<String, Object> services)
    {
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
            LinkedList<Object> args = new LinkedList<Object>();
            args.add(request);
            args.add(response);
            appendFormalArgs(queryParams, args);

            return invokeMethod (queryParams[1], queryParams[2], args);
        }

        return handler.service(request, response);
    }

    /**
     * Appends request path arguments to the list that is
     * eventually passed to the method invocation.
     *
     * @param queryParams The full list of request parameters.
     * @param args the list of arguments customized by this method.
     */
    private void appendFormalArgs (String[] queryParams, List<Object> args)
    {
        if (queryParams.length > 3)
        {
            for (int k = 3; k < queryParams.length; k++)
            {
                args.add(queryParams[k]);
            }
        }
    }

    /**
     * Looks up and invokes the web method in the provided service (looked up by
     * its id).  Softens exceptions to runtimes.  This method runs the request-
     * path-supplied arguments through value encoders.
     *
     * @param serviceId The unique id of the service (provided by the user) upon
     * which to invoke the method.
     * @param methodName the lower-case name of the method to invoke
     * @param args The full list of arguments to pass to the web method, including
     * the request and response arguments.
     * @return true on success.
     */
    private boolean invokeMethod (String serviceId, String methodName, LinkedList<Object> args)
    {
        Object service = _services.get(serviceId);
        Map<String, Method> methodMap = _methodMap.get(serviceId);

        if (methodMap == null)
            throw new RuntimeException("There are no RESTful web service methods on the " +
                    "class with web service id " + serviceId);

        Method webMethod = methodMap.get(methodName);

        if (webMethod == null)
            throw new RuntimeException("There is no RESTful web service method \"" +
                    methodName + "\" on the class with web service id " + serviceId);

        Class[] mparams = webMethod.getParameterTypes();
        validateParams (serviceId, methodName, webMethod);
        encodeArgs (mparams, args);

        try
        {
            webMethod.invoke (service, args.toArray());
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        return true;
    }

    /**
     * Uses the {@link org.apache.tapestry5.services.ValueEncoderSource} service to
     * translate request-path string values to server-side objects.  The list of
     * arguments is altered <em>in place</em>.  The request and response objects
     * are in the provided list of arguments.  Those are not affected of course.
     *
     * @param paramClasses The parameter classes as specified by the user in the
     * web method.
     * @param args The string arguments from the request path.  Elements of
     * this list are replaced by their translate object values.
     */
    private void encodeArgs (Class<?>[] paramClasses, LinkedList<Object> args)
    {
        if (args.size() < 3) return;

        // The first two args are the Request and Response objects from
        // Tapestry.  We're skipping those for encoding purposes, so we start
        // the list iteration at the third item.
        for (int j = 2; j < args.size(); j++)
        {
            Class<?> paramClass = paramClasses[j];
            ValueEncoder ve = _valEncSource.getValueEncoder(paramClass);
            Object arg = args.get(j);
            Object paramObj = ve.toValue((String)arg);
            args.set(j, paramObj);
        }
    }

    /**
     * Does some validation on the provided fixture for the web method call:
     * <ul>
     * <li>Ensures that the method name corresponds to an existing method in the web service class</li>
     * <li>Ensures that the web method accepts (minimally) the {@link org.apache.tapestry5.services.Request}
     * and {@link org.apache.tapestry5.services.Response} objects as its first and second arguments,
     * respectively.</li>
     * <li>Ensures that the web service method is void return type.</li>
     * </ul>
     * @param serviceId The unique user-provided id of the web service class to call.
     * @param methodName The lower-case name of the method to call on the service class --
     * must be annotated with {@link us.antera.t5restfulws.RestfulWebMethod}.  Used for
     * messaging. (TODO refactor).
     * @param method Pointer to the web method being called.
     */
    private void validateParams(String serviceId, String methodName, Method method)
    {
        Class[] mparams = method.getParameterTypes();

        if (method == null)
            throw new RuntimeException("The service identified by " + serviceId +
                " has no method with lower-case name " + methodName);

        if (mparams.length < 2)
            throw new RuntimeException("The method " + methodName + " on the service " + serviceId +
                    "must take at least" +
                    " two arguments: the request and response objects");

        if (!mparams[0].equals(Request.class) || !mparams[1].equals(Response.class))
            throw new RuntimeException("The first argument to method " + methodName + " in service" +
                    " " + serviceId + " must be a Request object, and the second must be" +
                    " a Reponse object.");

        if (!method.getReturnType().equals(Void.TYPE))
            throw new RuntimeException("Web method " + methodName + " must return void type.");
    }
}
