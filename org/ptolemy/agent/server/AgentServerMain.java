/* Main entry point for the Ptolemy II auto-modeling agent backend.

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

///////////////////////////////////////////////////////////////////
//// AgentServerMain

/**
 Standalone entry point that boots the Ptolemy II auto-modeling agent
 backend on a TCP port and registers all REST routes from
 {@link RestRoutes}.

 <p>Run from the command line:
 <pre>
 java -classpath "$PTII;$PTII/org/json/json.jar" \
      org.ptolemy.agent.server.AgentServerMain [port]
 </pre>

 <p>Defaults to port 7777 if no argument is given. The server runs
 forever; press Ctrl-C to terminate.

 @author Ptolemy II Agent contributors
 @version $Id$
 @since Ptolemy II 11.1
 */
public final class AgentServerMain {

    /** Default TCP port. */
    public static final int DEFAULT_PORT = 7777;

    private AgentServerMain() {
    }

    /** Parse the (optional) port argument and start the server.
     *  @param args Either an empty array or a single port number.
     */
    public static void main(String[] args) throws Exception {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println(
                        "Bad port argument; using default " + DEFAULT_PORT);
            }
        }

        SimpleHttpServer server = new SimpleHttpServer(port);
        RestRoutes.register(server);
        server.start();

        Runtime.getRuntime()
                .addShutdownHook(new Thread(server::stop, "agent-shutdown"));

        System.out.println(
                "Ptolemy II Auto-Modeling Agent backend started on port "
                        + port);
        System.out.println("Try:");
        System.out.println(
                "  curl http://localhost:" + port + "/api/v1/health");
        System.out.println(
                "  curl -X POST http://localhost:" + port + "/api/v1/sessions");
        System.out.println("Press Ctrl-C to stop.");

        // Block forever; the HttpServer daemon threads keep the JVM
        // alive only if explicit non-daemon threads exist.
        Thread block = new Thread(() -> {
            try {
                Thread.currentThread().join();
            } catch (InterruptedException ignored) {
                // graceful shutdown
            }
        }, "agent-keepalive");
        block.setDaemon(false);
        block.start();
    }
}
