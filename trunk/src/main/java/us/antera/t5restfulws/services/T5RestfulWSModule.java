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
 *
 * Application module for the t5-restful-webservices module.
 */
public class T5RestfulWSModule
{
    public static void bind (ServiceBinder binder)
    {
        // Bind in the dispatcher.
        binder.bind (RestfulWSDispatcher.class);
    }

    /**
     * Contribute the restful web service dispatcher to Tapestry's {@link org.apache.tapestry5.services.RequestHandler}
     * pipeline as a {@link org.apache.tapestry5.services.RequestFilter}
     *
     * @param configuration Incoming configuration of web service classes from the user.
     * @param dispatcher The {us.antera.t5restfulws.services.impl.RestfulWSDispatcher}
     * instance.
     */
    public void contributeRequestHandler (OrderedConfiguration<RequestFilter> configuration,
            @Local RequestFilter dispatcher)
    {
        configuration.add ("RestfulWebServices", dispatcher);
    }
}
