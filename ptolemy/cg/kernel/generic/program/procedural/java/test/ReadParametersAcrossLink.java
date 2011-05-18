/*  Read parameters in the containers of a remote port.

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
package ptolemy.cg.kernel.generic.program.procedural.java.test;

import java.util.List;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// ReadParameterAcrossLink

/**
 Read parameters in the containers of a remote port.

 <p>If we have a model CompositeA -> CompositeB and CompositeA
has a parameter Foo and CompositeB has this actor in it, then
this actor reads the value of the Foo parameter in CompositeA.

 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 8.1
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class ReadParametersAcrossLink extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ReadParametersAcrossLink(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.DOUBLE);
        input.setMultiport(true);

        output = new TypedIOPort(this, "output", false, true);

        output.setTypeAtLeast(input);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The input port.  This base class imposes no type constraints except
     *  that the type of the input cannot be greater than the type of the
     *  output.  
     */
    public TypedIOPort input;

    /** The output port. By default, the type of this output is constrained
     *  to be at least that of the input.
     */
    public TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Send the sum of the parameters named "remoteParameter"
     *  in the containers of composites connected to the input.   
     *  @exception IllegalActionException If it is thrown by the
     *   send() method sending out the token.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        Token sum = null;
        for (int i = 0; i < input.getWidth(); i++) {
            /*Discard the input */
            input.get(i);
        }

        output.send(0, new DoubleToken(_remoteParameterTotal));
    }

    /** Get all parameters named "remoteParameter"
     *  from the container of the port on the other side of
     *  the input port.
     *  @exception IllegalActionException If the parameter named
     *  "remoteParameter" cannot be read from the container of the
     *  remote port.
     */
    public void preinitialize() throws IllegalActionException {
        List<Relation> linkedRelationList = input.linkedRelationList();
        for (Relation relation : linkedRelationList) {
            System.out.println("ReadParametersAcrossLink: relation: " + relation);
            System.out.println("ReadParametersAcrossLink: relation.linedPortList(input): " + relation.linkedPortList(input));
            System.out.println("ReadParametersAcrossLink: relation.linedPortList(input).get(0): " + relation.linkedPortList(input).get(0));
            NamedObj container = ((TypedIOPort)relation.linkedPortList(input).get(0)).getContainer();
            System.out.println("ReadParametersAcrossLink: container: " + container + " " + container.getFullName());
            double remoteParameter = ((DoubleToken)((Variable)container.getAttribute("remoteParameter")).getToken()).doubleValue();
            _remoteParameterTotal += remoteParameter;
        }
    }

    private double _remoteParameterTotal;
}
