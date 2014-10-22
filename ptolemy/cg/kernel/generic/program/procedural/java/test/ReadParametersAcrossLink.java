/*  Read parameters in the containers of a remote port.

 Copyright (c) 2011-2014 The Regents of the University of California.
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

import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// ReadParameterAcrossLink

/**
 Read parameters in the containers of a remote port.

 <p>If we have a model CompositeA -> CompositeB and CompositeA
has a parameter Foo and CompositeB has this actor in it, then
this actor reads the value of the Foo parameter in CompositeA.

 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 10.0
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
        input = new TypedIOPort(this, "my input", true, false);
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
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        double sum = 0.0;
        //int i = 0;
        //System.out.println(getFullName() + ".fire() start");
        for (PopulationGroup group : _groups) {
            sum += group.performCalculation();
        }
        for (int x = 0; x < input.getWidth(); ++x) {
            /*Discard the input */
            //System.out.println(getFullName() + ".fire() reading input " + x);
            // Note that we don't check for input, which is bad, but common.
            sum += ((ScalarToken) input.get(x)).doubleValue();
        }
        //System.out.println(getFullName() + ".fire() sending " + sum);
        output.send(0, new DoubleToken(sum));
    }

    /** Get all parameters named "remoteParameter"
     *  from the container of the port on the other side of
     *  the input port.
     *  @exception IllegalActionException If the parameter named
     *  "remoteParameter" cannot be read from the container of the
     *  remote port.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        List<Relation> linkedRelationList = input.linkedRelationList();

        // Traverse all the relations connected to the input and instantiate
        // a PopulationGroup object with the value of the parameter
        // named "remoteParameter".

        // The list of PopulationGroups is used in fire().
        _groups = new LinkedList<PopulationGroup>();
        for (Relation relation : linkedRelationList) {
            NamedObj container = ((TypedIOPort) relation.linkedPortList(input)
                    .get(0)).getContainer();
            // The index of the relation.
            int relationIndex = IOPort.getRelationIndex(input, relation, true);

            // Add the current group to the list of groups.
            PopulationGroup group = new PopulationGroup();
            //System.out.println(getFullName() + ".preinitialize(): adding " + relationIndex);
            _groups.add(relationIndex, group);

            //System.out.println(getFullName() + ".preinitialize(): container: " + container);
            //System.out.println(getFullName() + ".preinitialize(): container.getAttribute(\"remoteParameter\"): " + container.getAttribute("remoteParameter"));
            if (container.getAttribute("remoteParameter") != null) {
                double remoteParameter = ((DoubleToken) ((Variable) container
                        .getAttribute("remoteParameter")).getToken())
                        .doubleValue();
                group.setRemoteParameter(remoteParameter);
            } else {
                throw new IllegalActionException(this,
                        "Could not find a parameter named \"remoteParameter\".");
            }
        }
    }

    /** A list of objects where each element contains the value of a
     *  remote parameter contained in a container connected to the
     *  input.
     */
    private List<PopulationGroup> _groups;

    /** An object that contains a double that could have a complex
     *  operation performed on it.  In real user code, the PopulationGroup
     *  class would have many fields and several methods that performed
     *  complex operations on those fields.  For simplicity, we have
     *  one field and one method that multiplies the value of the field
     *  by 2.0.
     */
    private static class PopulationGroup {
        // FindBugs indicates that this should be a static class.

        public double performCalculation() {
            // This method could be arbitrarily complex and involve multiple
            // parameters.  For the sake of simplicity, we just return
            // the value * 2.0
            return _remoteParameter * 2.0;
        }

        public void setRemoteParameter(double remoteParameter) {
            _remoteParameter = remoteParameter;
        }

        private double _remoteParameter;
    }
}
