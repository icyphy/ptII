/* Test cases for the Hessian servlets.

 Copyright (c) 2011 The Regents of the University of California.
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

package ptserver.test.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URL;
import java.util.ResourceBundle;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ptserver.communication.RemoteModelResponse;
import ptserver.control.IServerManager;
import ptserver.control.PtolemyServer;
import ptserver.control.Ticket;

import com.caucho.hessian.client.HessianProxyFactory;

///////////////////////////////////////////////////////////////////
//// ServletTest

/** Test the ability to open, start, stop, and close a simulation request
 *  on the Ptolemy server using the Hessian protocol.
 *
 *  @author Peter Foldes
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (pdf)
 *  @Pt.AcceptedRating Red (pdf)
 */
public class ServletTest {

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Access the ResourceBundle containing configuration parameters.
     */
    public static final ResourceBundle CONFIG = PtolemyServer.CONFIG;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Set up the initial singleton reference and Hessian proxy factory
     *  that will be used within the JUnit test cases.
     *  @exception Exception If there is an error creating the Hessian proxy.
     */
    @Before
    public void setUp() throws Exception {
        _ptolemyServer = PtolemyServer.getInstance();

        HessianProxyFactory proxyFactory = new HessianProxyFactory();
        String servletUrl = String.format("http://%s:%s%s", "localhost",
                CONFIG.getString("SERVLET_PORT"),
                CONFIG.getString("SERVLET_PATH"));

        _servletProxy = (IServerManager) proxyFactory.create(
                IServerManager.class, servletUrl);
    }

    /** Test the ability to create a new simulation request ensure that
     *  a non-null ticket is returned, the returned ticket has an id, and the
     *  number of simulations stored has increased by one.
     *  @exception Exception If there is an problem opening the model URL or
     *  communicating with the command servlet.
     */
    @Test
    public void openThread() throws Exception {
        int simulations = _ptolemyServer.numberOfSimulations();
        RemoteModelResponse response = _openRemoteModel();

        assertNotNull(response);
        assertNotNull(response.getTicket().getTicketID());
        assertEquals(simulations + 1, _ptolemyServer.numberOfSimulations());
    }

    /** Open, start, stop, and close the simulation request.
     *  @exception Exception If there is an problem opening the model URL or
     *  communicating with the command servlet.
     */
    @Test
    public void manipulateThread() throws Exception {
        RemoteModelResponse response = _openRemoteModel();
        assertNotNull(response);
        Ticket ticket = response.getTicket();
        int simulations = _ptolemyServer.numberOfSimulations();
        try {
            _servletProxy.start(ticket);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Start the thread and verify that doing so has not altered the thread count.
        assertEquals(simulations, _ptolemyServer.numberOfSimulations());

        try {
            _servletProxy.stop(ticket);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Stop the thread and verify that doing so has not altered the thread count.
        assertEquals(simulations, _ptolemyServer.numberOfSimulations());

        try {
            _servletProxy.close(ticket);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Close the thread and verify that the number of threads decreased.
        assertEquals(simulations - 1, _ptolemyServer.numberOfSimulations());
    }

    @After
    /** Call the shutdown() method on the singleton and destroy all
     *  references to it.
     *  @exception Exception If there was an error shutting down the broker or
     *  servlet.
     */
    public void shutdown() throws Exception {
        _ptolemyServer.shutdown();
        _ptolemyServer = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Submit a request to the servlet to open a simulation and acquire
     *  the ticket reference.
     *  @exception Exception If there is an problem opening the model URL or
     *  communicating with the command servlet.
     *  @return Ticket The ticket reference to the simulation request.
     */
    private RemoteModelResponse _openRemoteModel() throws Exception {
        URL url = ServletTest.class
                .getResource("/ptserver/test/junit/HelloWorld.xml");
        RemoteModelResponse response = _servletProxy.open(url.toExternalForm());
        return response;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Handle to the Ptolemy server singleton.
     */
    private PtolemyServer _ptolemyServer;

    /** Handle to the Hessian proxy.
     */
    private IServerManager _servletProxy;
}
