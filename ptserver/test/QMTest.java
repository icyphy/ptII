/*
 
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

import java.io.File;
import java.net.MalformedURLException;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.IntermediateReceiver;
import ptolemy.actor.Manager;
import ptolemy.actor.QuantityManager;
import ptolemy.actor.Receiver;
import ptolemy.data.ObjectToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;

public class QMTest { 

    public static void main(String[] args) throws MalformedURLException,
            Exception {
        MoMLParser parser = new MoMLParser();
        MoMLParser.setMoMLFilters(BackwardCompatibility.allFilters());
        CompositeActor topLevelActor = (CompositeActor) parser.parse(null,
                new File(args[0]).toURI().toURL());
        for (Object obj : topLevelActor.entityList()) {
            ComponentEntity actor = (ComponentEntity) obj;
            for (Object portObject : actor.portList()) {
                if (portObject instanceof IOPort) {
                    IOPort port = (IOPort) portObject;
                    QuantityManager quantityManager = new QuantityManager() {

                        public void sendToken(Receiver source,
                                Receiver receiver, Token token)
                                throws IllegalActionException {
                            System.out.println(token);
                        }

                        public void reset() {

                        }

                        public Receiver getReceiver(Receiver receiver)
                                throws IllegalActionException {
                            return new IntermediateReceiver(this, receiver);
                        }
                    };
                    Parameter parameter = new Parameter(port, "qm",
                            new ObjectToken(quantityManager));
                    port.attributeChanged(parameter);
                }
            }
        }
        Manager manager = new Manager(topLevelActor.workspace(), null);
        topLevelActor.setManager(manager);
        manager.execute();

    }
}
