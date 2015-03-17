/* An actor that assembles multiple inputs to an [Ordered]RecordToken.

 Copyright (c) 2009-2014 The Regents of the University of California.
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
package ptolemy.actor.lib;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.OrderedRecordToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// OrderedRecordAssembler

/**
 On each firing, read one token from each input port and assemble them
 into a RecordToken. The labels for the RecordToken are the names of the
 input ports.  To use this class, instantiate it, and then add input ports
 (instances of TypedIOPort).  This actor is polymorphic. The type constraint
 is that the type of each record field is no less than the type of the
 corresponding input port.
 <p>
 This actor differs from the RecordAssembler in that it ensures that the
 order of the fields of the record is preserved, matching the order
 of the input ports. This is probably not relevant unless you are writing
 Java code that iterates over the fields of the record.</p>

 <p>Note that if the display name of a port is set, display name is used in
 the type constraints instead of name. This is useful in case fields to
 add to the record contain a period, because periods are not allowed in
 port names.</p>

 @author Ben Leinfelder, Christopher Brooks
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 @see ptolemy.actor.lib.RecordAssembler
 @see ptolemy.actor.lib.RecordDisassembler
 */
public class OrderedRecordAssembler extends RecordAssembler {
    /** Construct an OrderedRecordAssembler with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If this actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public OrderedRecordAssembler(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read one token from each input port, assemble them into a RecordToken,
     *  and send the RecordToken to the output.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        int i = 0;
        Set<Entry<String, TypedIOPort>> entries = _portMap.entrySet();
        String[] labels = new String[entries.size()];
        Token[] values = new Token[entries.size()];

        for (Entry<String, TypedIOPort> entry : entries) {
            labels[i] = entry.getKey();
            values[i] = entry.getValue().get(0);
            i++;
        }

        RecordToken result = new OrderedRecordToken(labels, values);

        output.send(0, result);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a new _portMap, which is a map between
     *  port names and strings.  Derived classes
     *  like OrderedRecordAssembler return
     *  a map with a different ordering.
     *  @return a Map from port names to TypedIOPorts.
     */
    protected Map<String, TypedIOPort> _newPortMap() {
        // RecordToken._initializeStorage() should probably
        // use a similar Collection class.
        return new LinkedHashMap<String, TypedIOPort>();
    }
}
