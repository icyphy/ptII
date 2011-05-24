/*
 CommunicationToken encapsulates tokens from all ports and their channels
 that were received within one iteration.
 
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
package ptserver.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

import ptolemy.data.Token;

//////////////////////////////////////////////////////////////////////////
////CommunicationToken
/**
* CommunicationToken encapsulates tokens from all ports and their channels
* that were received within one iteration.
* @author ahuseyno
* @version $Id$ 
* @since Ptolemy II 8.0
* @Pt.ProposedRating Red (ahuseyno)
* @Pt.AcceptedRating Red (ahuseyno)
*/
public class CommunicationToken extends Token {

    /**
     * Create a new instance with targetActor set to null.
     */
    public CommunicationToken() {
    }

    /**
     * Create a new instance and set the name of the targetActor.
     * @param targetActor
     */
    public CommunicationToken(String targetActor) {
        this.setTargetActorName(targetActor);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Add the port with the specified number of channels.
     * @param port
     * @param width
     */
    public void addPort(String port, int width) {
        ArrayList<Token[]> list = new ArrayList<Token[]>(width);
        portChannelTokenMap.put(port, list);

    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        // This test rules out subclasses.
        if (object.getClass() != getClass()) {
            return false;
        }

        if (isNil() || ((CommunicationToken) object).isNil()) {
            return false;
        }

        CommunicationToken other = (CommunicationToken) object;
        if (targetActorName == null) {
            if (other.targetActorName != null)
                return false;
        } else if (!targetActorName.equals(other.targetActorName))
            return false;

        //portChannelTokenMap.equals(other.portChannelTokenMap) would not work because
        //the maps contain array objects and equals the array objects checks equality of a reference
        if (portChannelTokenMap == null) {
            if (other.portChannelTokenMap != null)
                return false;
        } else {
            if (other.portChannelTokenMap == null) {
                return false;
            }
            if (!portChannelTokenMap.keySet().equals(
                    other.portChannelTokenMap.keySet())) {
                return false;
            }
            for (Entry<String, ArrayList<Token[]>> entry : portChannelTokenMap
                    .entrySet()) {
                ArrayList<Token[]> otherChannelTokens = other.portChannelTokenMap
                        .get(entry.getKey());
                ArrayList<Token[]> channelTokens = entry.getValue();
                if (otherChannelTokens.size() != channelTokens.size()) {
                    return false;
                }
                for (int i = 0; i < channelTokens.size(); i++) {
                    if (!Arrays.equals(channelTokens.get(i),
                            otherChannelTokens.get(i))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Return mapping from ports to their channels with tokens received within one iteration.
     * @return
     */
    public HashMap<String, ArrayList<Token[]>> getPortChannelTokenMap() {
        return portChannelTokenMap;
    }

    /**
     * Return number of original tokens contained within the Communication Token.
     * @return number of original tokens
     */
    public int getSize() {
        return size;
    }

    /**
     * Return name of the target actor that received the tokens encapsulated by the
     * CommunicationToken.
     * @return
     * @see #setTargetActorName(String)
     */
    public String getTargetActorName() {
        return targetActorName;
    }

    /**
     * Return tokens of a specific port and channel.
     * @param port Port of the targetActor
     * @param channel Port's channel of the targetActor
     * @return tokens of a specific port and channel
     */
    public Token[] getTokens(String port, int channel) {
        return portChannelTokenMap.get(port).get(channel);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        //TODO: check if hashCode works correctly since equals is not using equals method of portChannelTokenMap
        result = prime
                * result
                + ((portChannelTokenMap == null) ? 0 : portChannelTokenMap
                        .hashCode());
        result = prime * result
                + ((targetActorName == null) ? 0 : targetActorName.hashCode());
        return result;
    }

    /**
     * Put tokens received from the port and channel into the CommunicationToken.
     * @param port port of the targetActor
     * @param channel channel of the port
     * @param tokens Array of tokens received from the channel within one iteration
     */
    public void putTokens(String port, int channel, Token[] tokens) {
        ArrayList<Token[]> channelTokenList = portChannelTokenMap.get(port);
        channelTokenList.add(channel, tokens);
        size += tokens.length;
    }

    /**
     * Set the name of targetActor that received the tokens that the
     * CommunicationToken encapsulates.
     * @param targetActorName the name of the target actor
     * @see #getTargetActorName()
     */
    public void setTargetActorName(String targetActorName) {
        this.targetActorName = targetActorName;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * Name of the target actor
     */
    private String targetActorName;

    /**
     * Mapping from port to its channels with tokens
     */
    private final HashMap<String, ArrayList<Token[]>> portChannelTokenMap = new HashMap<String, ArrayList<Token[]>>();

    /**
     * Number of tokens encapsulated by the communication token
     * transient to indicate that the size field won't be serialized but is here just for managing size of batching
     */
    private transient int size = 0;
}
