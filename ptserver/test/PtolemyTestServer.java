/*
 Entry Point of the Ptolemy Server

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
package ptserver.test;

import java.net.URL;

import ptolemy.actor.Manager;
import ptserver.communication.RemoteModel;
import ptserver.communication.RemoteModel.RemoteModelType;
import ptserver.control.Ticket;

///////////////////////////////////////////////////////////////////
//// PtolemyTestServer
/**
* Entry Point of the Ptolemy MQTT Server which is used for testing purposes.
* This class sets up the server to accept requests from Android client.
* @author Anar Huseynov
* @version $Id$
* @since Ptolemy II 8.0
* @Pt.ProposedRating Red (ahuseyno)
* @Pt.AcceptedRating Red (ahuseyno)
*/
public class PtolemyTestServer {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /**
     * Start the server.
     * It replaces marked sinks and sources with RemoteSink and RemoteSource instances and sets up communication infrastructure.
     * @param args currently the first argument indicates the model file that the server load
     */
    public static void main(String[] args) {
        try {
            RemoteModel model = new RemoteModel(RemoteModelType.SERVER);
            URL resource = PtolemyTestClient.class
                    .getResource("/ptserver/test/junit/addermodel.xml");
            model.loadModel(resource);
            Manager manager = model.setUpInfrastructure(
                    Ticket.generateTicket(null, null), "tcp://localhost@1883");
            //CompositeActor topLevelActor = model.getTopLevelActor();
            //            topLevelActor.getDirector().addDebugListener(new DebugListener() {
            //
            //                public void message(String message) {
            //                    System.out.println(message);
            //                }
            //
            //                public void event(DebugEvent event) {
            //                    System.out.println(event);
            //                }
            //            });
            manager.execute();

        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
