/* A class that abstracts the notion of a channel that connects two actors.

@Copyright (c) 2013 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

package ptolemy.apps.hardrealtime;

import ptolemy.actor.IORelation;
import ptolemy.kernel.Port;

/**
 A class that abstracts the notion of a channel that connects two actors.
 It encapsulates the port of the downstream actor and the relation that connects
 the two actors.
 It is used instead of just the input port of the downstream actor in order to
 distinguish input channels that connect to the same multiport.
 It is used instead of the relation that connects the two actors in order
 to distinguish outputs that split to many downstream actors.
 The pair of the upstream output port and the downstream input port could
 be used.
 */
public class Channel {
    /** Construct a new Channel from the relation and the input port of an actor.
     *  @param port The input port of the downstream actor.
     *  @param relation The relation that connects the output port of the upstream
     *    actor and the input port of the downstream actor.
     */
    public Channel(Port port, IORelation relation) {
        this._port = port;
        this._relation = relation;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if the argument is an instance of Channel
     *  and wraps the same port and relation.
     *  @param obj The other channel.
     *  @return true if the port and relation contained by the other channel as the ones
     *    contained by this one.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Channel other = (Channel) obj;
        if (!_port.equals(other._port))
            return false;
        if (!_relation.equals(other._relation))
            return false;
        return true;
    }

    /** Return a hash code value for the channel. If two channels
     *  wrap the same port and channel they should have the same hash code.
     *  @return a hash code value for the channel.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + _port.hashCode();
        result = prime * result + _relation.hashCode();
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The downstream actor port. */
    protected Port _port;

    /** The relation that connects the two actors. */
    protected IORelation _relation;
}
