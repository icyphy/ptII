package ptserver.communication;

import java.io.File;
import java.net.MalformedURLException;
import java.util.HashMap;

import ptolemy.actor.CompositeActor;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptserver.actor.RemoteSink;
import ptserver.actor.RemoteSource;

import com.ibm.mqtt.MqttException;

public class PtolemyServer {
    public static void main(String[] args) {
        MoMLParser parser = new MoMLParser();
        MoMLParser.setMoMLFilters(BackwardCompatibility.allFilters());
        HashMap<String, RemoteSourceData> remoteSourceMap = new HashMap<String, RemoteSourceData>();
        HashMap<String, RemoteSink> remoteSinkMap = new HashMap<String, RemoteSink>();
        TokenPublisher tokenPublisher = null;
        try {
            tokenPublisher = new TokenPublisher(100, new MQTTPublisher(
                    "localhost", "PtolemyServer"), "PtolemyTopic", 100);
        } catch (MqttException e1) {
            e1.printStackTrace();
        }
        try {
            CompositeActor topLevelActor = (CompositeActor) parser.parse(null,
                    new File(args[0]).toURI().toURL());
            for (Object obj : topLevelActor.entityList()) {
                ComponentEntity actor = (ComponentEntity) obj;
                Attribute attribute = actor.getAttribute("_remote");
                if (attribute != null) {
                    Parameter param = (Parameter) attribute;
                    System.out.println(actor);
                    System.out.println(param.getExpression());
                    if ("source".equals(param.getExpression())) {
                        RemoteSource remoteSource = new RemoteSource(
                                topLevelActor, actor);
                        RemoteSourceData data = new RemoteSourceData(
                                remoteSource);
                        remoteSourceMap.put(remoteSource.getName(), data);
                    } else if ("sink".equals(param.getExpression())) {
                        RemoteSink remoteSink = new RemoteSink(topLevelActor,
                                actor);
                        remoteSink.setPublisher(tokenPublisher);
                        remoteSinkMap.put(remoteSink.getOriginalActorName(), remoteSink);
                    }
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
