package ptserver.data;

import java.util.ArrayList;
import java.util.HashMap;

import ptolemy.data.Token;

public class CommunicationToken extends Token {

    private String targetActor;
    private HashMap<String, ArrayList<Token[]>> portChannelTokenMap = new HashMap<String, ArrayList<Token[]>>();
    //transient just to indicate that the size field won't be serialized but is here just for managing size of batching
    private transient int size = 0;

    public CommunicationToken(String targetActor) {
        this.targetActor = targetActor;
    }

    public void addPort(String port, int width) {
        ArrayList<Token[]> list = new ArrayList<Token[]>(width);
        portChannelTokenMap.put(port, list);

    }

    public void putTokens(String port, int channel, Token[] tokens) {
        ArrayList<Token[]> channelTokenList = portChannelTokenMap.get(port);
        channelTokenList.add(channel, tokens);
        size += tokens.length;
    }

    public HashMap<String, ArrayList<Token[]>> getPortChannelTokenMap() {
        return portChannelTokenMap;
    }

    public String getTargetActor() {
        return targetActor;
    }

    public int getSize() {
        return size;
    }
    
    public Token[] getTokens(String port, int channel) {
        return portChannelTokenMap.get(port).get(channel);
    }

}
