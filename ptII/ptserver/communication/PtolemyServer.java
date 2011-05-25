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
package ptserver.communication;

import java.io.File;
import java.util.HashMap;

import ptolemy.actor.CompositeActor;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptserver.actor.RemoteSink;
import ptserver.actor.RemoteSource;

//////////////////////////////////////////////////////////////////////////
//// PtolemyServer
/**
* Entry Point of the Ptolemy Server.
* This class sets up the server to accept requests from Android client
* @author ahuseyno
* @version $Id$ 
* @since Ptolemy II 8.0
* @Pt.ProposedRating Red (ahuseyno)
* @Pt.AcceptedRating Red (ahuseyno)
*/
public class PtolemyServer {
    /**
     * Start the server.
     * It replaces marked sinks and sources with RemoteSink and RemoteSource instances and sets up communication infrastructure
     * @param args currently the first argument indicates the model file that the server load
     */
    public static void main(String[] args) {
        MoMLParser parser = new MoMLParser();
        MoMLParser.setMoMLFilters(BackwardCompatibility.allFilters());
        final HashMap<String, RemoteSourceData> remoteSourceMap = new HashMap<String, RemoteSourceData>();
        final HashMap<String, RemoteSink> remoteSinkMap = new HashMap<String, RemoteSink>();

        final TokenPublisher tokenPublisher;
        try {
            tokenPublisher = new TokenPublisher(100, new MQTTPublisher(
                    "localhost", "PtolemyServer"), "PtolemyTopic", 100);

            //        MoMLParser.addMoMLFilter(new MoMLFilter() {
            //
            //            @Override
            //            public void filterEndElement(NamedObj container,
            //                    String elementName, StringBuffer currentCharData,
            //                    String xmlFile) throws Exception {
            //                // TODO Auto-generated method stub
            //                System.out.println(container);
            //                if (container.getName().equals("_remote")
            //                        && container instanceof Parameter
            //                        && container.getContainer() instanceof ComponentEntity) {
            //                    Parameter param = (Parameter) container;
            //                    ComponentEntity actor = (ComponentEntity) container
            //                            .getContainer();
            //
            //                    if ("source".equals(param.getExpression())) {
            //                        RemoteSource remoteSource = new RemoteSource(
            //                                (CompositeActor) actor.getContainer(), actor,
            //                                true);
            //                        RemoteSourceData data = new RemoteSourceData(
            //                                remoteSource);
            //                        remoteSourceMap.put(remoteSource.getName(), data);
            //                    } else if ("sink".equals(param.getExpression())) {
            //                        RemoteSink remoteSink = new RemoteSink(
            //                                (CompositeActor) actor.getContainer(), actor,
            //                                true);
            //                        remoteSink.setTokenPublisher(tokenPublisher);
            //                        remoteSinkMap.put(remoteSink.getTargetEntityName(),
            //                                remoteSink);
            //                    }
            //                }
            //            }
            //
            //            @Override
            //            public String filterAttributeValue(NamedObj container,
            //                    String element, String attributeName,
            //                    String attributeValue, String xmlFile) {
            //                System.out.println(container);
            //                System.out.println(element);
            //                System.out.println(attributeName);
            //                System.out.println(attributeValue);
            //
            //                return attributeValue;
            //            }
            //        });

            CompositeActor topLevelActor = (CompositeActor) parser.parse(null,
                    new File(args[0]).toURI().toURL());
            for (Object obj : topLevelActor.entityList()) {
                ComponentEntity actor = (ComponentEntity) obj;
                Attribute attribute = actor.getAttribute("_remote");
                if (attribute != null) {
                    Parameter parameter = (Parameter) attribute;
                    if (parameter.getExpression().equals("source")) {
                        RemoteSource remoteSource = new RemoteSource(
                                topLevelActor, actor, true);
                        RemoteSourceData data = new RemoteSourceData(
                                remoteSource);
                        remoteSourceMap.put(remoteSource.getName(), data);
                    } else if (parameter.getExpression().equals("sink")) {
                        RemoteSink remoteSink = new RemoteSink(topLevelActor,
                                actor, true);
                        remoteSink.setTokenPublisher(tokenPublisher);
                        remoteSinkMap.put(remoteSink.getTargetEntityName(),
                                remoteSink);
                    }
                }
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
