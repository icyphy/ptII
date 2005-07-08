/* A merge actor whose output type is the union of input types.

 Copyright (c) 1997-2005 The Regents of the University of California.
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
package ptolemy.domains.de.lib;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.UnionToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.data.type.UnionType;
import ptolemy.domains.de.kernel.DEActor;
import ptolemy.graph.Inequality;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// UnionMerge

/**
 A timed merge actor for the DE domain. Its operation is similar to
 the Merge actor but the output type is the union of the inputs.
 The labels for the output UnionToken are the names of the input
 ports. To use this actor, instantiate it, and then add input ports
 (instances of TypedIOPort).
 <p>
 There is a boolean parameter <i>discardEvents</i> associated
 with this actor, which decides how to handle simultaneously
 available inputs.  Each time this actor fires, it reads the first
 available tokens from an input port and sends them to the output
 port. If the <i>discardEvents</i> parameter is configured to true,
 then this actor discards all the remaining inputs in the rest of
 ports. Otherwise, this actor requests refirings at the current
 time until no more events are left in the ports. By default,
 the discardEvents parameter is false.

 @author Edward A. Lee, Haiyang Zheng, Yuhong Xiong
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Red (yuhongx)
 @Pt.AcceptedRating Red (yuhongx)
 */
public class UnionMerge extends DEActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name.
     *  @exception NameDuplicationException If an actor
     *   with an identical name already exists in the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     */
    public UnionMerge(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        output = new TypedIOPort(this, "output", false, true);

        discardEvents = new Parameter(this, "discardEvents");
        discardEvents.setExpression("false");
        discardEvents.setTypeEquals(BaseType.BOOLEAN);

        _attachText("_iconDescription", "<svg>\n"
                + "<polygon points=\"-10,20 10,10 10,-10, -10,-20\" "
                + "style=\"fill:green\"/>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The output port. The type of this port will be the union of the
     *  type of the input ports.
     */
    public TypedIOPort output;

    /** A flag to indicate whether the input events can be discarded.
     *  Its default value is false.
     */
    public Parameter discardEvents;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read the first available tokens from an input port and
     *  send them to the output port. If the discardEvents parameter
     *  is true, consume all the available tokens of the other ports
     *  and discard them. Otherwise, if the other ports have tokens,
     *  request a refiring at the current time to process them.
     *  @exception IllegalActionException If there is no director, or
     *  the input can not be read, or the output can not be sent.
     */
    public void fire() throws IllegalActionException {
        boolean discard = ((BooleanToken) discardEvents.getToken())
                .booleanValue();
        Token firstAvailableToken = null;

        Object[] portArray = inputPortList().toArray();
        int size = portArray.length;

        // If tokens can be discarded, this actor sends
        // out the first available tokens only. It discards all
        // remaining tokens from other input ports.
        // Otherwise, this actor handles one port at each firing
        // and requests refiring at the current time to handle the
        // the remaining ports that have tokens.
        for (int i = 0; i < size; i++) {
            IOPort port = (IOPort) portArray[i];
            if (port.hasToken(0)) {
                if (firstAvailableToken == null) {
                    // we see the first available tokens
                    String label = port.getName();
                    firstAvailableToken = port.get(0);
                    UnionToken outputToken = new UnionToken(label,
                            firstAvailableToken);
                    output.send(0, outputToken);

                    while (port.hasToken(0)) {
                        label = port.getName();
                        Token value = port.get(0);
                        outputToken = new UnionToken(label, value);
                        output.send(0, outputToken);
                    }
                } else {
                    if (discard) {
                        // this token is not the first available token
                        // in this firing, consume and discard all tokens
                        // from the input channel
                        while (port.hasToken(0)) {
                            // Token token = port.get(0);
                            port.get(0);
                        }
                    } else {
                        // Refiring the actor to handle the other tokens
                        // that are still in ports.
                        getDirector().fireAtCurrentTime(this);
                        break;
                    }
                }
            }
        }
    }

    /** Return the type constraints of this actor. The type constraint is
     *  that the output type is the union of the types of input ports.
     *  @return a list of Inequality.
     */
    public List typeConstraintList() {
        Object[] portArray = inputPortList().toArray();
        int size = portArray.length;
        String[] labels = new String[size];
        Type[] types = new Type[size];

        // form the declared type for the output port
        for (int i = 0; i < size; i++) {
            labels[i] = ((Port) portArray[i]).getName();
            types[i] = BaseType.UNKNOWN;
        }

        UnionType declaredType = new UnionType(labels, types);

        output.setTypeEquals(declaredType);

        // set the constraints between union fields and input ports
        List constraints = new LinkedList();

        // since the output port has a clone of the above UnionType, need to
        // get the type from the output port.
        UnionType outputType = (UnionType) output.getType();

        Iterator inputPorts = inputPortList().iterator();

        while (inputPorts.hasNext()) {
            TypedIOPort inputPort = (TypedIOPort) inputPorts.next();
            String label = inputPort.getName();
            Inequality inequality = new Inequality(inputPort.getTypeTerm(),
                    outputType.getTypeTerm(label));
            constraints.add(inequality);
        }

        return constraints;
    }
}
