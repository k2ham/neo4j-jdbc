package org.neo4j.jdbc;

import sun.misc.BASE64Decoder;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author mh
 * @since 03.12.12
 */
public class TestAuthenticationFilter implements Filter {
    static String USER = "foo";
    static String PASSWORD = "bar";

    @Override public void init(final FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain)
            throws ServletException, IOException {
        if (!(req instanceof HttpServletRequest) || !(res instanceof HttpServletResponse)) {
            throw new ServletException("request not allowed");
        }

        final HttpServletRequest request = (HttpServletRequest) req;
        final HttpServletResponse response = (HttpServletResponse) res;

        final String header = request.getHeader("Authorization");

        if (checkAuth(((HttpServletRequest) req).getMethod(), header)) {
            chain.doFilter(request, response);
        } else {
            sendAuthHeader(response);
        }
    }

    public void destroy() {
    }

    private boolean checkAuth(String method, String header) throws IOException {
        if (header == null) {
            return false;
        }

        final String encoded = header.substring(header.indexOf(" ") + 1);
        byte[] credentials = new BASE64Decoder().decodeBuffer(encoded);
        final String credentialString = new String(credentials);
        return credentialString.equals(String.format("%s:%s", USER, PASSWORD));
    }

    private void sendAuthHeader(HttpServletResponse response) throws IOException {
        String realmName = "test.neo4j.org";
        response.setHeader("WWW-Authenticate", "Basic realm=\"" + realmName + "\"");
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }
}