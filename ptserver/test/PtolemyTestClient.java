/*
 Entry Point to the Ptolemy MQTT Client

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

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.domains.pn.kernel.PNDirector;
import ptserver.communication.RemoteModel;
import ptserver.communication.RemoteModel.RemoteModelType;
import ptserver.control.Ticket;
import ptserver.util.PtolemyModuleJavaSEInitializer;

//////////////////////////////////////////////////////////////////////////
////PtolemyTestClient
/**
 * Ptolemy MQTT Client which is used for testing purposes.
 *
 * The client would only run sinks and sources that have respective attribute and
 * would rely on the server to run the rest.  The communication is performed over MQTT
 * protocol and the MQTT broker must be running in order to execute this program.
 * @author Anar Huseynov
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public class PtolemyTestClient {
    static {
        PtolemyModuleJavaSEInitializer.initializeInjector();
    }

    /**
     * Entry point to the Ptolemy MQTT Client which is used for testing purposes.
     *
     * @param args The first argument is file path to the model
     */
    public static void main(String[] args) {
        try {
            RemoteModel model = new RemoteModel(RemoteModelType.CLIENT);

            URL resource = PtolemyTestClient.class
                    .getResource("/ptserver/test/junit/sequence.xml");
            model.loadModel(resource);
            CompositeActor topLevelActor = model.getTopLevelActor();

//            topLevelActor.getDirector().setContainer(null);
//            topLevelActor.setDirector(new PNDirector(topLevelActor,
//                    "PNDirector"));
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
            System.out.println(topLevelActor.exportMoML());
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

}
