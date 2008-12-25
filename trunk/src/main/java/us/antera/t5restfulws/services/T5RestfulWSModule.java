package us.antera.t5restfulws.services;

import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.annotations.Local;
import org.apache.tapestry5.services.RequestFilter;
import us.antera.t5restfulws.services.impl.RestfulWSDispatcher;

/**
 * Created by IntelliJ IDEA.
 * User: holloway
 * Date: Dec 21, 2008
 * Time: 5:34:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class T5RestfulWSModule
{
    public static void bind (ServiceBinder binder)
    {
        binder.bind (RestfulWSDispatcher.class);
    }

    public void contributeRequestHandler (OrderedConfiguration<RequestFilter> configuration,
            @Local RequestFilter dispatcher)
    {
        configuration.add ("RestfulWebServices", dispatcher);
    }
}
