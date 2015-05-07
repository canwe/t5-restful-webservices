#How to get up and running with t5-restful-webservices

# Introduction #

**Version 0.2.0 -- major re-write**

This Tapestry 5 contributed module allows you to expose methods on a class as RESTful web service methods.

This is done by annotating your methods with the `@RestfulWebMethod` annotation and using Tapestry's `Response` to render an appropriate response to the browser.  You contribute your class to the `RestfulWSDispatcher` service.

`RestfulWSDispatcher` adds a `RequestFilter` to Tapestry 5's pipline to trap HTTP requests intended to be serviced by the RESTful web methods you have written.


# Details #

## Install ##

Just drop the source jar (t5-restful-webservices-0.2.0.jar) into the classpath for your Tapestry 5 application.  No other configuration is necessary.  Yes, Tapestry 5 makes it that simple!

## Usage ##

Let's imagine we want to author a method named `foo`, accessible as a RESTful web method, that will take an integer argument (42) and a boolean argument (true) and return the following XML to the browser:

```
<foo>
   <intArg>42</intArg>
   <booleanArg>true</booleanArg>
</foo>
```

We need write a class having `foo` as an instance method.  `foo` must

  * Be void return type
  * Be annotated with the `@RestfulWebMethod` annotation (part of this module)
  * Take the Tapestry `Request` and `Response` objects as its first two arguments, in that order

```
public class AClassOfMine
{
    @RestfulWebMethod
    public void foo (Request request, Response response, int n, boolean b) throws IOException
    {
        PrintWriter writer = response.getPrintWriter("text/xml");
        writer.append("<foo>");
        writer.append("<intArg>" + n + "</intArg>");
        writer.append("<booleanArg>" + b + "</booleanArg>");
        writer.append("</foo>");
        writer.flush();
        writer.close();
    }

    // Other methods, restful or not!...
```

The `Request` object is provided as a convenience should we need it.  The `Response` object is necessary to send a response to the browser.

To expose our method to the web, we now contribute this class to the `RestfulWSDispatcher` service, using a `MappedConfiguration`, in our Tapestry 5 application's IoC module.  The key for the class' map entry must be a, _URL-friendly_ string (unique among other web service class map keys of course :)

```
public void contributeRestfulWSDispatcher (MappedConfiguration<String, Object> config)
{
    config.add("my-web-service", new AClassOfMine());        
}
```

When we start our application, the web service is ready.  If the base URL for our Tapestry 5 web application is `http://myapp.example.org/` (i.e., the root context path is "/") then the URL for our web service method is

`http://myapp.example.org/<class-map-key>/<lowercase-method-name>/args...`

In the case of our example the URL is

`http://myapp.example.org/my-web-service/foo/42/true`

The web service method's arguments (42 and true) come into the `RestfulWSDispatcher` initially as pure strings.  The `RestfulWSDispatcher` uses the application's `ValueEncoder`s to translate them to the Java values we specified in our method signature -- an (`int` and `boolean` in our case).

Since Tapestry's boolean `ValueEncoder` translates any non-empty string as a boolean true on the server side, try replacing "true" in the URL with "baz" or something.