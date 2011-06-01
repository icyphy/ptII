/* Test cases for the Hessian servlets

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

import org.junit.Before;
import org.junit.Test;

import ptserver.PtolemyServer;
import ptserver.control.IServerManager;
import ptserver.control.Ticket;

import com.caucho.hessian.client.HessianProxyFactory;

///////////////////////////////////////////////////////////////////
//// ServletTest

/** This class is responsible testing the Hessian servlet
 * 
 * @author pdf
 * @version $Id$
 * @Pt.ProposedRating Red (pdf)
 * @Pt.AcceptedRating Red (pdf)
 */
public class ServletTest {

    public static final ResourceBundle CONFIG = PtolemyServer.CONFIG;

    @Before
    public void setUp() throws Exception {
        this._ptolemyServer = PtolemyServer.getInstance();

        HessianProxyFactory proxyFactory = new HessianProxyFactory();
        String servletUrl = String.format("http://%s:%s%s", "localhost",
                CONFIG.getString("SERVLET_PORT"),
                CONFIG.getString("SERVLET_PATH"));

        this._servletProxy = (IServerManager) proxyFactory.create(
                IServerManager.class, servletUrl);
    }

    /**
     * Test that tries to create a new thread and makes sure the following:
     * 1. A not null ticket is returned
     * 2. The returned ticket has an id
     * 3. The number of simulations stored increased by one
     * 
     * @throws Exception
     */
    @Test
    public void openThread() throws Exception {
        int simulations = this._ptolemyServer.getNumberOfSimulationsRunning();
        Ticket ticket = openTicket();

        assertNotNull(ticket);
        assertNotNull(ticket.getTicketID());
        assertEquals(simulations + 1,
                this._ptolemyServer.getNumberOfSimulationsRunning());
    }

    /**
     * Test that tries to open, start, stop, and close a thread
     * 
     * @throws Exception
     */
    @Test
    public void manipulateThread() throws Exception {
        Ticket ticket = openTicket();
        assertNotNull(ticket);

        int simulations = this._ptolemyServer.getNumberOfSimulationsRunning();

        try {
            this._ptolemyServer.start(ticket);
        } catch (Exception e) {
            e.printStackTrace();
        }
        /** Starting the thread should not change the number of threads registered. **/
        assertEquals(simulations,
                this._ptolemyServer.getNumberOfSimulationsRunning());

        try {
            this._ptolemyServer.stop(ticket);
        } catch (Exception e) {
            e.printStackTrace();
        }
        /** Stopping the thread should not change the number of threads registered. **/
        assertEquals(simulations,
                this._ptolemyServer.getNumberOfSimulationsRunning());

        try {
            this._ptolemyServer.close(ticket);
        } catch (Exception e) {
            e.printStackTrace();
        }
        /** Closing the thread should decrease the number of threads registered. **/
        assertEquals(simulations - 1,
                this._ptolemyServer.getNumberOfSimulationsRunning());

        try {
            this._ptolemyServer.stop(ticket);
        } catch (Exception e) {
            e.printStackTrace();
        }
        /** Stopping an invalid thread should throw an exception. **/
        assertEquals(simulations,
                this._ptolemyServer.getNumberOfSimulationsRunning());

    }

    //////////////////////////////////////////////////////////////////////
    ////                private methods
    private Ticket openTicket() throws Exception {
        URL url = ServletTest.class.getResource("HelloWorld.xml");
        return this._servletProxy.open(url);
    }

    //////////////////////////////////////////////////////////////////////
    ////                private variables
    private PtolemyServer _ptolemyServer;
    private IServerManager _servletProxy;
}
