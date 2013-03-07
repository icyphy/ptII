/* CommunicationToken encapsulates tokens from all ports and their channels
   that were received within one iteration.

 Copyright (c) 2011-2013 The Regents of the University of California.
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

///////////////////////////////////////////////////////////////////
//// CommunicationToken

/** Encapsulate tokens that were received within one iteration.
 *  <p>Note: Kahn process networks are not being handled right now.</p>
 *
 *  @author Anar Huseynov
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (ahuseyno)
 *  @Pt.AcceptedRating Red (ahuseyno)
 */
public class CommunicationToken extends Token {

    /** Create a new instance with targetActor set to null.
     */
    public CommunicationToken() {
        super();
    }

    /** Create a new instance and set the name of the targetActor.
     *  @param targetActor the target actor whose tokens this instance encapsulates.
     */
    public CommunicationToken(String targetActor) {
        setTargetActorName(targetActor);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add the port with the specified number of channels.
     *  @param port The port name as it's returned by NamedObj.getName() method.
     *  @param width The number of channel that the port has.
     */
    public void addPort(String port, int width) {
        ArrayList<Token[]> list = new ArrayList<Token[]>(width);
        _portChannelTokenMap.put(port, list);
    }

    /** Return true if the object is equal to the instance, false otherwise.
     *  The method checks if the object has the same target name and
     *  the portChannelMap contains the same ports each having the same number of channels with the same tokens.
     *  @param object The reference object with which to compare.
     *  @return true if the objects are equal, false otherwise.
     *  @see java.lang.Object#equals(java.lang.Object)
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
        if (_targetActorName == null) {
            if (other._targetActorName != null) {
                return false;
            }
        } else if (!_targetActorName.equals(other._targetActorName)) {
            return false;
        }

        // portChannelTokenMap.equals(other.portChannelTokenMap) would not work
        // because the maps contain array objects, and equals method on the array
        // objects checks equality of a reference but in our case we need to check
        // equality of each token within the array objects.
        if (_portChannelTokenMap == null) {
            if (other._portChannelTokenMap != null) {
                return false;
            }
        } else {
            if (other._portChannelTokenMap == null) {
                return false;
            }
            if (!_portChannelTokenMap.keySet().equals(
                    other._portChannelTokenMap.keySet())) {
                return false;
            }
            for (Entry<String, ArrayList<Token[]>> entry : _portChannelTokenMap
                    .entrySet()) {
                ArrayList<Token[]> otherChannelTokens = other._portChannelTokenMap
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

    /** Return the mapping from ports to their channels with tokens have been received within one iteration.
     *  @return the mapping from ports to their channels with tokens have been received within one iteration.
     */
    public HashMap<String, ArrayList<Token[]>> getPortChannelTokenMap() {
        return _portChannelTokenMap;
    }

    /** Return number of original tokens contained within the Communication Token.
     *  @return number of original tokens
     */
    public int getSize() {
        return _size;
    }

    /** Return the full name of the target actor that received the tokens encapsulated by the
     *  CommunicationToken.
     *  @return the full name of the target actor that received the tokens encapsulated by the
     *  CommunicationToken.
     *  @see #setTargetActorName(String)
     */
    public String getTargetActorName() {
        return _targetActorName;
    }

    /** Return the tokens of a specific port and channel.
     *  @param port Port of the targetActor
     *  @param channel Port's channel of the targetActor
     *  @return tokens of a specific port and channel
     */
    public Token[] getTokens(String port, int channel) {
        return _portChannelTokenMap.get(port).get(channel);
    }

    /** Compute hash code of the instance based on the targetActorname and portChannelTokenMap.
     *  @return the hash code based on targetActorName and tokens in the portChannelTokanMap.
     *  @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        //FIXME: check if hashCode works correctly since equals is not using equals method of portChannelTokenMap
        result = prime
                * result
                + ((_portChannelTokenMap == null) ? 0 : _portChannelTokenMap
                        .hashCode());
        result = prime
                * result
                + ((_targetActorName == null) ? 0 : _targetActorName.hashCode());
        return result;
    }

    /** Put tokens received from the port and channel into the CommunicationToken.
     *  @param port port of the targetActor
     *  @param channel channel of the port
     *  @param tokens Array of tokens received from the channel within one iteration
     */
    public void putTokens(String port, int channel, Token[] tokens) {
        ArrayList<Token[]> channelTokenList = _portChannelTokenMap.get(port);
        channelTokenList.add(channel, tokens);
        _size += tokens.length;
    }

    /** Set the full name of the targetActor that received the tokens that the
     *  CommunicationToken encapsulates.
     *  @param targetActorName the name of the target actor
     *  @see #getTargetActorName()
     */
    public void setTargetActorName(String targetActorName) {
        _targetActorName = targetActorName;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Mapping from port to its channels with tokens.
     */
    private final HashMap<String, ArrayList<Token[]>> _portChannelTokenMap = new HashMap<String, ArrayList<Token[]>>(
            2);

    /** Number of tokens encapsulated by the communication token.
     *  Its transient to indicate that the size field won't be serialized but is here just for managing size of batching.
     */
    private transient int _size = 0;

    /** Name of the target actor.
     */
    private String _targetActorName;
}
