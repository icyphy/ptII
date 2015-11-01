/* Test cases for the RemoteModel implementation.

 Copyright (c) 2011-2014 The Regents of the University of California.
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

import java.util.ArrayList;
import java.util.ResourceBundle;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.injection.ActorModuleInitializer;
import ptolemy.actor.injection.PtolemyInjector;
import ptolemy.actor.injection.PtolemyModule;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.domains.pn.kernel.PNDirector;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Settable;
import ptserver.communication.ProxyModelAdapter;
import ptserver.communication.ProxyModelInfrastructure;
import ptserver.communication.ProxyModelResponse;
import ptserver.control.IServerManager;
import ptserver.control.PtolemyServer;
import ptserver.control.SimulationTask;
import ptserver.control.Ticket;
import ptserver.test.SysOutActor;
import ptserver.test.SysOutActor.TokenDelegator;
import ptserver.util.ProxyModelBuilder.ProxyModelType;
import ptserver.util.ServerUtility;

import com.caucho.hessian.client.HessianProxyFactory;

///////////////////////////////////////////////////////////////////
//// RemoteModelTest

/** Test the ability to ability of starting the server and receiving
 *  a token in return.
 *
 *  <p>To run:
 *  <pre>
 *  /usr/local/sbin/mosquitto &
 *  (cd $PTII/ptserver/test/junit/; java -classpath ${PTII}:${PTII}/lib/ptjacl.jar:${PTII}/lib/junit-4.8.2.jar:${PTII}/ptserver/lib/hessian-4.0.7.jar:${PTII}/lib/jetty-all-8.1.5-v20120716.jar:${PTII}/lib/javax.servlet-api-3.0.1.jar:${PTII}/ptserver/lib/wmqtt.jar org.junit.runner.JUnitCore ptserver.test.junit.RemoteModelTest)
 *  </pre>
 *
 *  @author Justin Killian
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (jkillian)
 *  @Pt.AcceptedRating Red (jkillian)
 */
public class RemoteModelTest {
    static {
        // FIXME remove PTServerModule after SysOutActor is deleted
        // or create a proper initializer for it
        ArrayList<PtolemyModule> modules = new ArrayList<PtolemyModule>();
        modules.addAll(ActorModuleInitializer.getModules());
        modules.add(new PtolemyModule(ResourceBundle
                .getBundle("ptserver.util.PTServerModule")));
        PtolemyInjector.createInjector(modules);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Access the ResourceBundle containing configuration parameters.
     */
    public static final ResourceBundle CONFIG = PtolemyServer.CONFIG;

    /** Used to ensure that a minimum number of tokens are received
     *  during the test.
     */
    private volatile int counter = 0;

    /** Control the loop that synchronizes the response from the server.
     */
    private volatile boolean isWaiting = true;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Set up the initial singleton reference and Hessian proxy factory
     *  that will be used within the JUnit test cases.
     *  @exception Exception If there is an error creating the Hessian proxy.
     */
    @Before
    public void setup() throws Exception {
        _server = PtolemyServer.getInstance();

        HessianProxyFactory factory = new HessianProxyFactory();
        factory.setUser("guest");
        factory.setPassword("guest");

        _proxy = (IServerManager) factory.create(
                IServerManager.class,
                String.format("http://%s:%s%s", "localhost",
                        CONFIG.getString("SERVLET_PORT"), "/"
                                + PtolemyServer.SERVLET_NAME));
        counter = 0;
        isWaiting = true;
    }

    /** Find the model file on the server and execute the simulation.
     *  @exception Exception If the the setup or shutdown of the simulation fails.
     */
    @Test(timeout = 5000)
    public void runSimulation() throws Exception {
        // Open the model on the server.
        ProxyModelResponse response = _openRemoteModel();
        ProxyModelInfrastructure model = _setUpClientModel(response);
        // Set the delegate for a returned token.
        SysOutActor actor = (SysOutActor) model.getTopLevelActor().getEntity(
                "Display");
        assertNotNull(actor);
        actor.setDelegator(new TokenDelegator() {

            @Override
            public void getToken(Token token) {
                if (counter < 10) {
                    if (token instanceof IntToken) {
                        assertEquals(counter, ((IntToken) token).intValue() / 2);
                        counter++;
                    }
                } else {
                    synchronized (RemoteModelTest.this) {
                        isWaiting = false;
                        RemoteModelTest.this.notifyAll();
                    }
                }
            }
        });
        // Wait for a roundtrip response from the server.
        _proxy.start(response.getTicket());
        model.getManager().startRun();

        synchronized (RemoteModelTest.this) {
            while (isWaiting) {
                wait();
            }
        }

        // stop running processes.
        _proxy.stop(response.getTicket());
        model.getManager().stop();
        // close the simulation
        _proxy.close(response.getTicket());
        model.close();
        assertEquals(10, counter);
    }

    private ProxyModelResponse _openRemoteModel() throws IllegalActionException {
        String[] modelUrls = _proxy.getModelListing();
        assertNotNull(modelUrls);
        assertTrue(modelUrls.length > 0);
        String adderModel = getJUnitModel(modelUrls);
        assertNotNull(adderModel);

        String[] layoutUrls = _proxy.getLayoutListing(adderModel);
        assertNotNull(layoutUrls);
        assertTrue(layoutUrls.length > 0);
        String adderModelLayout = getJUnitModelLayout(layoutUrls);
        assertNotNull(adderModelLayout);

        ProxyModelResponse response = _proxy.open(adderModel, adderModelLayout);
        assertNotNull(response);

        // Open the model on the client.
        Ticket ticket = response.getTicket();
        assertNotNull(ticket);
        // Just to keep the test output clean.
        SimulationTask task = PtolemyServer.getInstance().getSimulationTask(
                response.getTicket());
        SysOutActor actor2 = (SysOutActor) task.getProxyModelInfrastructure()
                .getTopLevelActor().getEntity("Display2");
        actor2.setDelegator(new TokenDelegator() {

            @Override
            public void getToken(Token token) {
            }
        });

        return response;
    }

    private ProxyModelInfrastructure _setUpClientModel(
            ProxyModelResponse response) throws Exception {
        Ticket ticket = response.getTicket();
        ProxyModelInfrastructure model = new ProxyModelInfrastructure(
                ProxyModelType.CLIENT, (CompositeActor) ServerUtility
                        .createMoMLParser().parse(response.getModelXML()),
                response.getModelTypes());
        model.setUpInfrastructure(ticket, _server.getBrokerUrl());

        CompositeActor topLevelActor = model.getTopLevelActor();
        assertNotNull(topLevelActor);

        topLevelActor.setDirector(new PNDirector(topLevelActor, "PNDirector"));
        return model;
    }

    @Test(timeout = 5000)
    public void testRemoteAttribute() throws Exception {
        ProxyModelResponse response = _openRemoteModel();
        ProxyModelInfrastructure clientModel = _setUpClientModel(response);
        clientModel.setTimeoutPeriod(0);
        ProxyModelInfrastructure serverModel = PtolemyServer.getInstance()
                .getSimulationTask(response.getTicket())
                .getProxyModelInfrastructure();
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

    @Test(timeout = 5000)
    public void testRemoteAttributeSimulation() throws Exception {
        ProxyModelResponse response = _openRemoteModel();
        ProxyModelInfrastructure clientModel = _setUpClientModel(response);
        clientModel.setTimeoutPeriod(0);
        Settable clientSettable = (Settable) clientModel.getTopLevelActor()
                .getAttribute("Ramp2.init");
        assertNotNull(clientSettable);
        clientSettable.setExpression("1");
        SysOutActor actor = (SysOutActor) clientModel.getTopLevelActor()
                .getEntity("Display");
        assertNotNull(actor);
        actor.setDelegator(new TokenDelegator() {

            @Override
            public void getToken(Token token) {
                if (counter < 10) {
                    if (token instanceof IntToken) {
                        assertEquals(counter, ((IntToken) token).intValue() / 2);
                        assertEquals(1, ((IntToken) token).intValue() % 2);
                        counter++;
                    }
                } else {
                    synchronized (RemoteModelTest.this) {
                        isWaiting = false;
                        RemoteModelTest.this.notifyAll();
                    }
                }
            }
        });
        // Just to keep the test output clean.
        SimulationTask task = PtolemyServer.getInstance().getSimulationTask(
                response.getTicket());
        SysOutActor actor2 = (SysOutActor) task.getProxyModelInfrastructure()
                .getTopLevelActor().getEntity("Display2");
        actor2.setDelegator(new TokenDelegator() {

            @Override
            public void getToken(Token token) {
            }
        });
        assertNotNull(actor);
        actor.setDelegator(new TokenDelegator() {

            @Override
            public void getToken(Token token) {
                if (counter < 10) {
                    if (token instanceof IntToken) {
                        assertEquals(counter, ((IntToken) token).intValue() / 2);
                        assertEquals(1, ((IntToken) token).intValue() % 2);
                        counter++;
                    }
                } else {
                    synchronized (RemoteModelTest.this) {
                        isWaiting = false;
                        RemoteModelTest.this.notifyAll();
                    }
                }
            }
        });

        // Wait for a roundtrip response from the server.
        _proxy.start(response.getTicket());
        clientModel.getManager().startRun();

        synchronized (RemoteModelTest.this) {
            while (isWaiting) {
                wait();
            }
        }

        // stop running processes.
        _proxy.stop(response.getTicket());
        clientModel.getManager().stop();
        // close the simulation
        _proxy.close(response.getTicket());
        clientModel.close();
        assertEquals(10, counter);
    }

    @Test(timeout = 10000)
    public void testModelTimeout() throws Exception {
        ProxyModelResponse response = _openRemoteModel();
        // Wait for a roundtrip response from the server.
        SimulationTask task = PtolemyServer.getInstance().getSimulationTask(
                response.getTicket());
        final int timeoutPeriod = 1000;
        task.getProxyModelInfrastructure().setTimeoutPeriod(timeoutPeriod);
        final long time = System.currentTimeMillis();
        task.getProxyModelInfrastructure().addProxyModelListener(
                new ProxyModelAdapter() {

                    @Override
                    public void modelConnectionExpired(
                            ProxyModelInfrastructure remoteModel) {
                        synchronized (RemoteModelTest.this) {
                            long diff = System.currentTimeMillis() - time;
                            assertTrue("Timeout period " + timeoutPeriod
                                    + " diff " + diff,
                                    diff < 2 * timeoutPeriod * 1.05);
                            isWaiting = false;
                            RemoteModelTest.this.notifyAll();
                        }
                    }
                });
        _proxy.start(response.getTicket());
        synchronized (this) {
            while (isWaiting) {
                this.wait();
            }
        }
        assertTrue(!isWaiting);
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

    private String getJUnitModel(String[] modelUrls) {
        for (String model : modelUrls) {
            if (model.endsWith("junitmodel.xml")) {
                return model;
            }
        }
        return null;
    }

    private String getJUnitModelLayout(String[] layoutUrls) {
        for (String model : layoutUrls) {
            if (model.contains("junitmodel") && model.endsWith(".layout.xml")) {
                return model;
            }
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Handle to the Ptolemy server singleton.
     */
    private PtolemyServer _server;

    /** Handle to the Hessian servlet proxy.
     */
    private IServerManager _proxy;
}
