package de.metacoder.blog.services;

import java.io.IOException;

import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.ioc.Configuration;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.Local;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.RequestFilter;
import org.apache.tapestry5.services.RequestHandler;
import org.apache.tapestry5.services.Response;
import org.slf4j.Logger;

/**
 * This module is automatically included as part of the Tapestry IoC Registry if
 * <em>tapestry.execution-mode</em> includes <code>development</code>.
 */
public class DevelopmentModule {

	@Contribute(WebSecurityManager.class)
	public static void resetAdminPassword(final Configuration<Object> configuration, final UserService userService) {
		userService.createUser("dev-admin", "admin");
	}

	public static void contributeApplicationDefaults(
			final MappedConfiguration<String, Object> configuration) {
		// The factory default is true but during the early stages of an
		// application overriding to false is a good idea. In addition, this is
		// often overridden on the command line as
		// -Dtapestry.production-mode=false
		configuration.add(SymbolConstants.PRODUCTION_MODE, false);

		// The application version number is incorprated into URLs for some
		// assets. Web browsers will cache assets because of the far future
		// expires header. If existing assets are changed, the version number
		// should also change, to force the browser to download new versions.
		configuration.add(SymbolConstants.APPLICATION_VERSION,
				"0.0.1-SNAPSHOT-DEV");
	}

	/**
	 * This is a service definition, the service will be named "TimingFilter".
	 * The interface, RequestFilter, is used within the RequestHandler service
	 * pipeline, which is built from the RequestHandler service configuration.
	 * Tapestry IoC is responsible for passing in an appropriate Logger
	 * instance. Requests for static resources are handled at a higher level, so
	 * this filter will only be invoked for Tapestry related requests.
	 * <p/>
	 * <p/>
	 * Service builder methods are useful when the implementation is inline as
	 * an inner class (as here) or require some other kind of special
	 * initialization. In most cases, use the static bind() method instead.
	 * <p/>
	 * <p/>
	 * If this method was named "build", then the service id would be taken from
	 * the service interface and would be "RequestFilter". Since Tapestry
	 * already defines a service named "RequestFilter" we use an explicit
	 * service id that we can reference inside the contribution method.
	 */
	public RequestFilter buildTimingFilter(final Logger log) {
		return new RequestFilter() {
			@Override
			public boolean service(final Request request,
					final Response response, final RequestHandler handler)
					throws IOException {
				final long startTime = System.currentTimeMillis();

				try {
					// The responsibility of a filter is to invoke the
					// corresponding method
					// in the handler. When you chain multiple filters together,
					// each filter
					// received a handler that is a bridge to the next filter.

					return handler.service(request, response);
				} finally {
					final long elapsed = System.currentTimeMillis() - startTime;

					log.info(String.format("Request time: %d ms", elapsed));
				}
			}
		};
	}

	/**
	 * This is a contribution to the RequestHandler service configuration. This
	 * is how we extend Tapestry using the timing filter. A common use for this
	 * kind of filter is transaction management or security. The @Local
	 * annotation selects the desired service by type, but only from the same
	 * module. Without @Local, there would be an error due to the other
	 * service(s) that implement RequestFilter (defined in other modules).
	 */
	public void contributeRequestHandler(
			final OrderedConfiguration<RequestFilter> configuration,
			@Local final RequestFilter filter) {
		// Each contribution to an ordered configuration has a name, When
		// necessary, you may set constraints to precisely control the
		// invocation order of the contributed filter within the pipeline.
		configuration.add("Timing", filter);
	}
}
