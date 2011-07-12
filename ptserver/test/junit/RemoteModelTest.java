/* Test cases for the RemoteModel implementation.

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
import static org.junit.Assert.assertTrue;

import java.util.ResourceBundle;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ptolemy.actor.CompositeActor;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.domains.pn.kernel.PNDirector;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Settable;
import ptserver.communication.RemoteModel;
import ptserver.communication.RemoteModel.RemoteModelListener;
import ptserver.communication.RemoteModel.RemoteModelType;
import ptserver.communication.RemoteModelResponse;
import ptserver.control.IServerManager;
import ptserver.control.PtolemyServer;
import ptserver.control.SimulationTask;
import ptserver.control.Ticket;
import ptserver.data.ServerEventToken.EventType;
import ptserver.test.SysOutActor;
import ptserver.test.SysOutActor.TokenDelegator;
import ptserver.util.PtolemyModuleJavaSEInitializer;

import com.caucho.hessian.client.HessianProxyFactory;

///////////////////////////////////////////////////////////////////
//// RemoteModelTest

/** Test the ability to ability of starting the server and receiving
 *  a token in return.
 *
 *  @author Justin Killian
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (jkillian)
 *  @Pt.AcceptedRating Red (jkillian)
 */
public class RemoteModelTest {

    /** Load the injector based on the platform.
     */
    static {
        PtolemyModuleJavaSEInitializer.initializeInjector();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Access the ResourceBundle containing configuration parameters.
     */
    public static final ResourceBundle CONFIG = PtolemyServer.CONFIG;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Find the model file on the server and execute the simulation.
     *  @exception Exception If the the setup or shutdown of the simulation fails.
     */
    @Test(timeout = 10000)
    public void runSimulation() throws Exception {
        // Open the model on the server.
        RemoteModelResponse response = _openRemoteModel();
        RemoteModel model = _setUpClientModel(response);

        // Set the delegate for a returned token.
        SysOutActor actor = (SysOutActor) model.getTopLevelActor().getEntity(
                "Display");
        assertNotNull(actor);
        actor.setDelegator(new TokenDelegator() {

            public void getToken(Token token) {
                if (_counter < 10) {
                    if (token instanceof IntToken) {
                        assertEquals(_counter,
                                ((IntToken) token).intValue() / 2);
                        _counter++;
                    }
                } else {
                    synchronized (RemoteModelTest.this) {
                        _isWaiting = false;
                        RemoteModelTest.this.notifyAll();
                    }
                }
            }
        });

        // Wait for a roundtrip response from the server.
        _proxy.start(response.getTicket());
        model.getManager().startRun();

        synchronized (RemoteModelTest.this) {
            while (_isWaiting) {
                wait();
            }
        }

        // Stop running processes.
        _proxy.stop(response.getTicket());
        model.getManager().stop();

        // Close the simulation.
        _proxy.close(response.getTicket());
        model.close();
        assertEquals(10, _counter);
    }

    /** Test getting the remote attribute of a ramp actor.
     *  @exception Exception If there was an error setting the attribute.
     */
    @Test
    public void testRemoteAttribute() throws Exception {
        RemoteModelResponse response = _openRemoteModel();
        RemoteModel clientModel = _setUpClientModel(response);
        clientModel.setTimeoutPeriod(0);
        RemoteModel serverModel = PtolemyServer.getInstance()
                .getSimulationTask(response.getTicket()).getRemoteModel();
        serverModel.setTimeoutPeriod(0);
        Settable clientSettable = (Settable) clientModel.getTopLevelActor()
                .getAttribute("Ramp2.init");
        assertNotNull(clientSettable);

        clientSettable.setExpression("1");
        Settable serverSettable = (Settable) serverModel.getTopLevelActor()
                .getAttribute("Ramp2.init");
        assertNotNull(serverSettable);

        Thread.sleep(1000);
        assertEquals("1", serverSettable.getExpression());

        _proxy.close(response.getTicket());
        clientModel.close();
    }

    /** Test setting an attribute on an actor.
     *  @exception Exception If there was an error running the simulation.
     */
    @Test(timeout = 3000)
    public void testRemoteAttributeSimulation() throws Exception {
        RemoteModelResponse response = _openRemoteModel();
        RemoteModel clientModel = _setUpClientModel(response);
        clientModel.setTimeoutPeriod(0);
        Settable clientSettable = (Settable) clientModel.getTopLevelActor()
                .getAttribute("Ramp2.init");
        assertNotNull(clientSettable);

        clientSettable.setExpression("1");
        SysOutActor actor = (SysOutActor) clientModel.getTopLevelActor()
                .getEntity("Display");
        assertNotNull(actor);

        actor.setDelegator(new TokenDelegator() {
            public void getToken(Token token) {
                if (_counter < 10) {
                    if (token instanceof IntToken) {
                        assertEquals(_counter,
                                ((IntToken) token).intValue() / 2);
                        assertEquals(1, ((IntToken) token).intValue() % 2);
                        _counter++;
                    }
                } else {
                    synchronized (RemoteModelTest.this) {
                        _isWaiting = false;
                        RemoteModelTest.this.notifyAll();
                    }
                }
            }
        });

        // Wait for a roundtrip response from the server.
        _proxy.start(response.getTicket());
        clientModel.getManager().startRun();

        synchronized (RemoteModelTest.this) {
            while (_isWaiting) {
                wait();
            }
        }

        // Stop running processes.
        _proxy.stop(response.getTicket());
        clientModel.getManager().stop();

        // Close the simulation
        _proxy.close(response.getTicket());
        clientModel.close();
        assertEquals(10, _counter);
    }

    /** Test the timeout functionality of a running model.
     *  @exception Exception If there was an error running the simulation.
     */
    @Test(timeout = 10000)
    public void testModelTimeout() throws Exception {
        RemoteModelResponse response = _openRemoteModel();

        // Wait for a roundtrip response from the server.
        SimulationTask task = PtolemyServer.getInstance().getSimulationTask(
                response.getTicket());
        final int timeoutPeriod = 1000;
        task.getRemoteModel().setTimeoutPeriod(timeoutPeriod);
        final long time = System.currentTimeMillis();

        task.getRemoteModel().addRemoteModelListener(new RemoteModelListener() {

            public void modelConnectionExpired(RemoteModel remoteModel) {
                synchronized (RemoteModelTest.this) {
                    long diff = System.currentTimeMillis() - time;
                    assertTrue("Difference was " + diff,
                            timeoutPeriod - 500 < diff
                                    && diff < timeoutPeriod + 2000);
                    _isWaiting = false;
                    RemoteModelTest.this.notifyAll();
                }
            }

            public void modelException(RemoteModel remoteModel, String message,
                    Throwable exception) {
                exception.printStackTrace();
                _isWaiting = false;
                RemoteModelTest.this.notifyAll();
            }

            public void modelEvent(RemoteModel remoteModel, String message,
                    EventType type) {
                System.out.println(type);
                _isWaiting = false;
                RemoteModelTest.this.notifyAll();
            }
        });

        _proxy.start(response.getTicket());
        synchronized (this) {
            while (_isWaiting) {
                this.wait();
            }
        }

        assertTrue(!_isWaiting);
    }

    /** Set up the initial singleton reference and Hessian proxy factory
     *  that will be used within the JUnit test cases.
     *  @exception Exception If there is an error creating the Hessian proxy.
     */
    @Before
    public void setup() throws Exception {
        HessianProxyFactory factory = new HessianProxyFactory();
        factory.setUser("guest");
        factory.setPassword("guest");

        _server = PtolemyServer.getInstance();
        _servletUrl = _server.getServletUrl();
        _brokerUrl = _server.getBrokerUrl();
        _proxy = (IServerManager) factory.create(IServerManager.class,
                _servletUrl);
        _counter = 0;
        _isWaiting = true;
    }

    /** Call the shutdown() method on the singleton and destroy all
     *  references to it.
     *  @exception Exception If there was an error shutting down the broker or
     *  servlet.
     */
    @After
    public void shutdown() throws Exception {
        _server.shutdown();
        _server = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Get the addermodel.xml file URL.
     *  @param modelUrls The URL collection to look through.
     *  @return The URL of the file, null if not found.
     */
    private String _getAdderModel(String[] modelUrls) {
        for (String model : modelUrls) {
            if (model.endsWith("addermodel.xml")) {
                return model;
            }
        }

        return null;
    }

    /** Get the addermodel.xml layout file URL.
     * @param layoutUrls The URL collection to look through.
     * @return The URL of the file, null if not found.
     */
    private String _getAdderModelLayout(String[] layoutUrls) {
        for (String model : layoutUrls) {
            if (model.contains("addermodel") && model.endsWith(".layout.xml")) {
                return model;
            }
        }

        return null;
    }

    /** Open the model file.
     *  @return The response from the server opening the file.
     *  @exception IllegalActionException
     */
    private RemoteModelResponse _openRemoteModel()
            throws IllegalActionException {
        String[] modelUrls = _proxy.getModelListing();
        assertNotNull(modelUrls);
        assertTrue(modelUrls.length > 0);

        String adderModel = _getAdderModel(modelUrls);
        assertNotNull(adderModel);

        String[] layoutUrls = _proxy.getLayoutListing(adderModel);
        assertNotNull(layoutUrls);
        assertTrue(layoutUrls.length > 0);

        String adderModelLayout = _getAdderModelLayout(layoutUrls);
        assertNotNull(adderModelLayout);

        RemoteModelResponse response = _proxy
                .open(adderModel, adderModelLayout);
        assertNotNull(response);

        // Open the model on the client.
        Ticket ticket = response.getTicket();
        assertNotNull(ticket);

        return response;
    }

    /** Initialize the client model.
     *  @param response The response received from opening the model on the server.
     *  @return The remote model to be run on the client.
     *  @exception Exception If there was an error setting up the client model.
     */
    private RemoteModel _setUpClientModel(RemoteModelResponse response)
            throws Exception {
        Ticket ticket = response.getTicket();
        RemoteModel model = new RemoteModel(RemoteModelType.CLIENT);
        model.initModel(response.getModelXML(), response.getModelTypes());
        model.setUpInfrastructure(ticket, _brokerUrl);

        CompositeActor topLevelActor = model.getTopLevelActor();
        assertNotNull(topLevelActor);

        topLevelActor.setDirector(new PNDirector(topLevelActor, "PNDirector"));
        return model;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Store the address to the MQTT broker.
     */
    private String _brokerUrl;

    /** Used to ensure that a minimum number of tokens are received during the test.
     */
    private volatile int _counter = 0;

    /** Control the loop that synchronizes the response from the server.
     */
    private volatile boolean _isWaiting = true;

    /** Handle to the Hessian servlet proxy.
     */
    private IServerManager _proxy;

    /** Handle to the Ptolemy server singleton.
     */
    private PtolemyServer _server;

    /** Store the address to the Hessian servlet.
     */
    private String _servletUrl;
}
