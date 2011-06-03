/*
 Task that will be used to execute a Ptolemy simulation.
 
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

package ptserver;

import java.net.URL;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptserver.communication.RemoteModel;
import ptserver.communication.RemoteModel.RemoteModelType;
import ptserver.control.Ticket;

import com.ibm.mqtt.IMqttClient;
import com.ibm.mqtt.MqttClient;

///////////////////////////////////////////////////////////////////
//// SimulationTask

/** Launch the simulation on the current thread under the provided
 *  ticket reference and wait for the user to issue control commands.
 *  
 *  @author jkillian
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (jkillian)
 *  @Pt.AcceptedRating Red (jkillian)
 */
public class SimulationTask implements Runnable {

    /** Create an instance of the simulation task to be run by the Ptolemy
     *  server application.
     * 
     *  @param ticket Reference to the simulation request.
     *  @exception Exception If the simulation encounters a problem setting
     *  the director or getting workspace access.
     */
    public SimulationTask(Ticket ticket) throws Exception {
        _owner = PtolemyServer.getInstance();
        _ticket = ticket;
        _remoteModel = new RemoteModel(_ticket.getTicketID(),
                _ticket.getTicketID() + "_CLIENT", _ticket.getTicketID()
                        + "_SERVER", RemoteModelType.SERVER);

        // Set the MQTT client.
        IMqttClient mqttClient = MqttClient.createMqttClient("tcp://localhost@"
                + Integer.toString(_owner.getBrokerPort()), null);
        if (mqttClient != null) {
            _remoteModel.setMqttClient(mqttClient);
        }

        // Load the model specified within the ticket.
        _remoteModel.loadModel(new URL(_ticket.getUrl()));

        // Set the simulation manager and director.
        CompositeActor topLevelActor = _remoteModel.getTopLevelActor();
        if (topLevelActor != null) {
            topLevelActor.setManager(new Manager(topLevelActor.workspace(),
                    _ticket.getTicketID()));
        }
    }

    /** Start the execution of the simulation by kicking off the thread.
     */
    public void run() {
        try {
            _remoteModel.getTopLevelActor().getManager().execute();
        } catch (IllegalActionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (KernelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /** Get the manager responsible for coordinating the model of computation. 
     *  @return The Manager used to control the simulation
     */
    public Manager getManager() {
        CompositeActor topLevelActor = _remoteModel.getTopLevelActor();
        if (topLevelActor == null) {
            return null;
        }

        return topLevelActor.getManager();
    }

    //////////////////////////////////////////////////////////////////////
    ////                private variables

    /** The Ptolemy server singleton.
     */
    private final PtolemyServer _owner;

    /** The ticket reference to the simulation request.
     */
    private final Ticket _ticket;

    /** The remote model that is used to replaced model actors.
     */
    private final RemoteModel _remoteModel;
}
