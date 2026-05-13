/* Thin wrapper around com.sun.net.httpserver.HttpServer with
 path-pattern routing.

 Copyright (c) 2024-2026 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY
 */
package org.ptolemy.agent.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import org.ptolemy.agent.util.JsonResponse;

///////////////////////////////////////////////////////////////////
//// SimpleHttpServer

/**
 A small wrapper around {@link com.sun.net.httpserver.HttpServer} that
 supports two things the JDK server does not natively give us:

 <ol>
 <li>Path-parameter routing, e.g. {@code GET /api/v1/sessions/{id}/moml}
     where the matched id is exposed via
     {@link RouteHandler#handle(HttpExchange, Map)}.</li>
 <li>Automatic 204 reply for CORS preflight {@code OPTIONS} requests.</li>
 </ol>

 <p>Each route is registered against a regex; the server iterates
 routes in registration order and dispatches to the first match. A
 fixed thread pool serves requests so that simulation runs and HTTP
 I/O do not starve each other.

 @author Ptolemy II Agent contributors
 @version $Id$
 @since Ptolemy II 11.1
 */
public class SimpleHttpServer {

    /** Functional interface implemented by route handlers. */
    @FunctionalInterface
    public interface RouteHandler {
        /** Handle an HTTP exchange.
         *  @param exchange The incoming exchange.
         *  @param pathParams Path parameters extracted from the URL.
         *  @throws IOException If writing the response fails.
         */
        void handle(HttpExchange exchange, Map<String, String> pathParams)
                throws IOException;
    }

    private static final class Route {
        final String method;
        final Pattern pattern;
        final List<String> paramNames;
        final RouteHandler handler;

        Route(String method, Pattern pattern, List<String> paramNames,
                RouteHandler handler) {
            this.method = method;
            this.pattern = pattern;
            this.paramNames = paramNames;
            this.handler = handler;
        }
    }

    private static final Pattern PARAM_PATTERN = Pattern
            .compile("\\{([a-zA-Z_][a-zA-Z0-9_]*)\\}");

    private final int _port;
    private final List<Route> _routes = new ArrayList<>();
    private HttpServer _server;

    /** @param port TCP port to listen on. */
    public SimpleHttpServer(int port) {
        _port = port;
    }

    /** Register a route.
     *  @param method HTTP method, upper-case.
     *  @param pathPattern Path pattern, e.g. {@code /api/v1/sessions/{id}}.
     *  @param handler Handler invoked for matching requests.
     */
    public void route(String method, String pathPattern,
            RouteHandler handler) {
        List<String> paramNames = new ArrayList<>();
        Matcher m = PARAM_PATTERN.matcher(pathPattern);
        StringBuffer regex = new StringBuffer("^");
        int last = 0;
        while (m.find()) {
            regex.append(Pattern.quote(pathPattern.substring(last,
                    m.start())));
            regex.append("([^/]+)");
            paramNames.add(m.group(1));
            last = m.end();
        }
        regex.append(Pattern.quote(pathPattern.substring(last)));
        regex.append("$");
        _routes.add(new Route(method.toUpperCase(),
                Pattern.compile(regex.toString()),
                Collections.unmodifiableList(paramNames), handler));
    }

    /** Start the server. Blocks only briefly. */
    public void start() throws IOException {
        _server = HttpServer.create(new InetSocketAddress(_port), 0);
        _server.createContext("/", new DispatchHandler());
        Executor executor = Executors.newFixedThreadPool(8);
        _server.setExecutor(executor);
        _server.start();
    }

    /** Stop the server, allowing in-flight requests to finish. */
    public void stop() {
        if (_server != null) {
            _server.stop(1);
            _server = null;
        }
    }

    /** Read the request body as a UTF-8 string. */
    public static String readBody(HttpExchange exchange) throws IOException {
        try (InputStream in = exchange.getRequestBody();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int n;
            while ((n = in.read(buffer)) > 0) {
                out.write(buffer, 0, n);
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    private final class DispatchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod().toUpperCase();
            String path = exchange.getRequestURI().getPath();
            if ("OPTIONS".equals(method)) {
                JsonResponse.noContent(exchange);
                return;
            }
            for (Route route : _routes) {
                if (!route.method.equals(method)) {
                    continue;
                }
                Matcher m = route.pattern.matcher(path);
                if (!m.matches()) {
                    continue;
                }
                Map<String, String> params = new HashMap<>();
                for (int i = 0; i < route.paramNames.size(); i++) {
                    params.put(route.paramNames.get(i), m.group(i + 1));
                }
                try {
                    route.handler.handle(exchange, params);
                } catch (Throwable t) {
                    JsonResponse.error(exchange, 500,
                            "handler crashed: " + t.getMessage());
                }
                return;
            }
            JsonResponse.error(exchange, 404,
                    "no route for " + method + " " + path);
        }
    }
}
