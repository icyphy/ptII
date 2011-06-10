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

import java.util.Random;
import java.util.ResourceBundle;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ptolemy.actor.CompositeActor;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.domains.pn.kernel.PNDirector;
import ptserver.communication.RemoteModel;
import ptserver.communication.RemoteModel.RemoteModelType;
import ptserver.communication.RemoteModelResponse;
import ptserver.control.IServerManager;
import ptserver.control.PtolemyServer;
import ptserver.control.Ticket;
import ptserver.test.SysOutActor;
import ptserver.test.SysOutActor.TokenDelegator;

import com.caucho.hessian.client.HessianProxyFactory;
import com.ibm.mqtt.IMqttClient;
import com.ibm.mqtt.MqttClient;

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
        _servletUrl = String.format("http://%s:%s%s", "localhost",
                CONFIG.getString("SERVLET_PORT"),
                CONFIG.getString("SERVLET_PATH"));
        _brokerUrl = String.format("tcp://%s@%s", "localhost",
                CONFIG.getString("BROKER_PORT"));
        _proxy = (IServerManager) new HessianProxyFactory().create(
                IServerManager.class, _servletUrl);
    }

    /** Find the model file on the server and execute the simulation.
     *  @exception Exception If the the setup or shutdown of the simulation fails.
     */
    @Test(timeout = 3000)
    public void runSimulation() throws Exception {
        // Open the model on the server.
        String[] modelUrls = _proxy.getModelListing();
        assertNotNull(modelUrls);
        assertTrue(modelUrls.length > 0);

        RemoteModelResponse response = _proxy.open(modelUrls[0]);
        assertNotNull(response);

        // Open the model on the client.
        Ticket ticket = response.getTicket();
        assertNotNull(ticket);

        RemoteModel model = new RemoteModel(ticket.getTicketID() + "_SERVER",
                ticket.getTicketID() + "_CLIENT", RemoteModelType.CLIENT);

        // Initialize the client.
        IMqttClient mqttClient = MqttClient.createMqttClient(_brokerUrl, null);
        mqttClient.connect("Android" + new Random().nextInt(1000), true,
                (short) 10);

        model.setMqttClient(mqttClient);
        model.initModel(response.getModelXML(), response.getModelTypes());
        model.setUpInfrastructure();

        CompositeActor topLevelActor = model.getTopLevelActor();
        assertNotNull(topLevelActor);

        topLevelActor.setDirector(new PNDirector(topLevelActor, "PNDirector"));

        // Set the delegate for a returned token.
        SysOutActor actor = (SysOutActor) topLevelActor.getEntity("Display");
        assertNotNull(actor);

        actor.setDelegator(new TokenDelegator() {

            public void getToken(Token token) {
                if (counter < 10) {
                    if ((token != null) && (token instanceof IntToken)) {
                        assertEquals(((IntToken) token).intValue() / 2, counter);
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
        _proxy.start(ticket);
        model.getManager().startRun();

        synchronized (RemoteModelTest.this) {
            while (isWaiting) {
                wait();
            }
        }

        // Cleanup running processes. 
        _proxy.stop(ticket);
        model.getManager().stop();
    }

    @After
    /** Call the shutdown() method on the singleton and destroy all
     *  references to it.
     *  @exception Exception If there was an error shutting down the broker or
     *  servlet.
     */
    public void shutdown() throws Exception {
        _server.shutdown();
        _server = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Handle to the Ptolemy server singleton.
     */
    private PtolemyServer _server;

    /** Handle to the Hessian servlet proxy.
     */
    private IServerManager _proxy;

    /** Store the address to the Hessian servlet.
     */
    private String _servletUrl;

    /** Store the address to the MQTT broker.
     */
    private String _brokerUrl;
}
