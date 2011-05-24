/*
 
 Copyright (c) 2002-2010 The Regents of the University of California.
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
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.HashSet;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.pn.kernel.PNDirector;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptserver.actor.RemoteSink;
import ptserver.actor.RemoteSource;

import com.ibm.mqtt.MqttException;

public class PtolemyClient {

    public static void main(String[] args) {
        MoMLParser parser = new MoMLParser();
        MoMLParser.setMoMLFilters(BackwardCompatibility.allFilters());
        HashMap<String, RemoteSourceData> remoteSourceMap = new HashMap<String, RemoteSourceData>();
        HashMap<String, RemoteSink> remoteSinkMap = new HashMap<String, RemoteSink>();
        HashSet<ComponentEntity> serverActors = new HashSet<ComponentEntity>();
        TokenPublisher tokenPublisher = null;
        try {
            tokenPublisher = new TokenPublisher(100, new MQTTPublisher(
                    "localhost", "PtolemyClient"), "PtolemyTopic", 100);
        } catch (MqttException e1) {
            e1.printStackTrace();
        }
        try {
            CompositeActor topLevelActor = (CompositeActor) parser.parse(null,
                    new File(args[0]).toURI().toURL());
            for (Object actorObject : topLevelActor.entityList()) {
                ComponentEntity actor = (ComponentEntity) actorObject;
                Attribute attribute = actor.getAttribute("_remote");
                if (attribute != null) {
                    Parameter param = (Parameter) attribute;
                    if ("source".equals(param.getExpression())) {
                        RemoteSink remoteSink = new RemoteSink(topLevelActor,
                                actor, false);
                        remoteSink.setTokenPublisher(tokenPublisher);
                        remoteSinkMap.put(remoteSink.getTargetEntityName(),
                                remoteSink);
                    } else if ("sink".equals(param.getExpression())) {
                        RemoteSource remoteSource = new RemoteSource(
                                topLevelActor, actor, false);
                        RemoteSourceData data = new RemoteSourceData(
                                remoteSource);
                        remoteSourceMap.put(remoteSource.getName(), data);
                    }
                } else {
                    serverActors.add(actor);
                }
            }

            for (ComponentEntity componentEntity : serverActors) {
                try {
                    componentEntity.setContainer(null);
                } catch (IllegalActionException e) {
                    e.printStackTrace();
                } catch (NameDuplicationException e) {
                    e.printStackTrace();
                }
            }
            serverActors.clear();
            serverActors = null;
            Manager manager = new Manager(topLevelActor.workspace(),
                    "PtolemyClient");
            topLevelActor.setManager(manager);
            topLevelActor
                    .setDirector(new PNDirector(topLevelActor.workspace()));
            manager.execute();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
