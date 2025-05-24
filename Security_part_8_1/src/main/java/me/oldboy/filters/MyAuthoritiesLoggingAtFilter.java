package me.oldboy.filters;

import jakarta.servlet.*;

import java.io.IOException;
import java.util.logging.Logger;

public class MyAuthoritiesLoggingAtFilter implements Filter {

	private final Logger LOG =
			Logger.getLogger(MyAuthoritiesLoggingAtFilter.class.getName());

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		LOG.info(" \n *** 3 - Log MyAuthoritiesLoggingAtFilter method *** \n" +
				      " *** Method is in progress *** ");
		chain.doFilter(request, response);
	}

}
