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

import java.util.Random;
import java.util.ResourceBundle;

import org.junit.Test;

import ptolemy.actor.CompositeActor;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.domains.pn.kernel.PNDirector;
import ptserver.PtolemyServer;
import ptserver.communication.RemoteModel;
import ptserver.communication.RemoteModel.RemoteModelType;
import ptserver.communication.RemoteModelResponse;
import ptserver.control.IServerManager;
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

    public static final ResourceBundle CONFIG = PtolemyServer.CONFIG;
    private volatile int counter = 0;
    private volatile boolean isWaiting = true;

    /** Start the server and client
     *  @exception Exception If the the setup or shutdown of the simulation fails.
     */
    @Test(timeout = 2000)
    public void initialize() throws Exception {
        String servletUrl = String.format("http://%s:%s%s", "localhost",
                CONFIG.getString("SERVLET_PORT"),
                CONFIG.getString("SERVLET_PATH"));
        String brokerUrl = String.format("tcp://%s@%s", "localhost",
                CONFIG.getString("BROKER_PORT"));

        // Create the server and servlet proxy.
        PtolemyServer server = PtolemyServer.getInstance();
        IServerManager serverManager = (IServerManager) new HessianProxyFactory()
                .create(IServerManager.class, servletUrl);

        // Open the model on the server.
        RemoteModelResponse response = serverManager.open(IServerManager.class
                .getResource("/ptserver/test/junit/addermodel.xml").toString());

        // Open the model on the client.
        Ticket ticket = response.getTicket();
        RemoteModel model = new RemoteModel(ticket.getTicketID() + "_SERVER",
                ticket.getTicketID() + "_CLIENT", RemoteModelType.CLIENT);

        // Initialize the client.
        IMqttClient mqttClient = MqttClient.createMqttClient(brokerUrl, null);
        mqttClient.connect("Android" + new Random().nextInt(1000), true,
                (short) 10);

        model.setMqttClient(mqttClient);
        model.initModel(response.getModelXML(), response.getModelTypes());
        model.setUpInfrastructure();

        CompositeActor topLevelActor = model.getTopLevelActor();
        topLevelActor.setDirector(new PNDirector(topLevelActor, "PNDirector"));

        // Set the delegate for a returned token.
        SysOutActor actor = (SysOutActor) topLevelActor.getEntity("Display");
        actor.setDelegator(new TokenDelegator() {

            public void getToken(Token token) {
                if (counter < 10) {
                    assertEquals(((IntToken) token).intValue() / 2, counter);
                    counter++;
                } else {
                    synchronized (RemoteModelTest.this) {
                        isWaiting = false;
                        RemoteModelTest.this.notifyAll();
                    }
                }
            }
        });

        // Wait for a roundtrip response from the server.
        serverManager.start(ticket);
        model.getManager().startRun();

        synchronized (RemoteModelTest.this) {
            while (isWaiting) {
                wait();
            }
        }

        // Cleanup running processes. 
        serverManager.stop(ticket);
        model.getManager().stop();
        server.shutdown();
        server = null;
    }
}
