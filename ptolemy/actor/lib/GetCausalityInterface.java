/* Actor that reads the causality interface of its container and produces a
 * string describing it.

 Copyright (c) 2003-2014 The Regents of the University of California.
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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.CausalityInterface;
import ptolemy.data.StringToken;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// GetCausalityInterface

/**
 Actor that reads the causality interface of its container or an
 actor inside the container and produces a
 string describing it. This actor is meant mainly for testing.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class GetCausalityInterface extends Source {

    /** Construct an actor with the given container and name.
     *  The output and trigger ports are also constructed.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public GetCausalityInterface(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        output.setTypeEquals(BaseType.STRING);

        dependents = new TypedIOPort(this, "dependents", false, true);
        dependents.setTypeEquals(BaseType.STRING);

        equivalences = new TypedIOPort(this, "equivalences", false, true);
        equivalences.setTypeEquals(BaseType.STRING);

        actorName = new StringParameter(this, "actorName");
        actorName.setExpression("");
    }

    ///////////////////////////////////////////////////////////////////
    ////                  ports and parameters                     ////

    /** Name of the actor to get the causality interface of. If this
     *  is the empty string (the default), then the container is used.
     */
    public StringParameter actorName;

    /** Output port on which to put the description of the dependent ports. */
    public TypedIOPort dependents;

    /** Output port on which to put the description of the equivalence classes. */
    public TypedIOPort equivalences;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read the causality interface from the container and produce a
     *  description of it as an output.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        Actor container = (Actor) getContainer();
        Actor target = container;
        String targetName = actorName.stringValue();
        if (targetName != null && !targetName.trim().equals("")) {
            target = (Actor) ((CompositeEntity) container)
                    .getEntity(targetName);
            if (target == null) {
                throw new IllegalActionException(this, "No actor named "
                        + targetName);
            }
        }
        CausalityInterface causalityInterface = target.getCausalityInterface();
        output.send(0, new StringToken(causalityInterface.toString()));

        StringBuffer dependentsResult = new StringBuffer();
        List<IOPort> inputs = target.inputPortList();
        for (IOPort input : inputs) {
            Collection<IOPort> outputs = causalityInterface
                    .dependentPorts(input);
            if (outputs.size() > 0) {
                dependentsResult.append(input.getName());
                dependentsResult.append(" has output port dependencies:\n");
                List<String> outputList = new LinkedList<String>();
                for (IOPort output : outputs) {
                    outputList.add(output.getName());
                }
                // To get deterministic results for testing, sort alphabetically.
                Collections.sort(outputList);
                for (String name : outputList) {
                    dependentsResult.append("  ");
                    dependentsResult.append(name);
                    dependentsResult.append("\n");
                }
            }
        }
        List<IOPort> outputs = target.outputPortList();
        for (IOPort output : outputs) {
            Collection<IOPort> ports = causalityInterface
                    .dependentPorts(output);
            if (ports.size() > 0) {
                dependentsResult.append(output.getName());
                dependentsResult.append(" has input port dependencies:\n");
                List<String> inputList = new LinkedList<String>();
                for (IOPort input : ports) {
                    inputList.add(input.getName());
                }
                // To get deterministic results for testing, sort alphabetically.
                Collections.sort(inputList);
                for (String name : inputList) {
                    dependentsResult.append("  ");
                    dependentsResult.append(name);
                    dependentsResult.append("\n");
                }
            }
        }
        dependents.send(0, new StringToken(dependentsResult.toString()));

        StringBuffer equivalencesResult = new StringBuffer();
        for (IOPort input : inputs) {
            Collection<IOPort> equivalents = causalityInterface
                    .equivalentPorts(input);
            if (equivalents.size() > 0) {
                equivalencesResult.append(input.getName());
                equivalencesResult.append(" has equivalent input ports:\n");
                List<String> equivalentsList = new LinkedList<String>();
                for (IOPort port : equivalents) {
                    equivalentsList.add(port.getName());
                }
                // To get deterministic results for testing, sort alphabetically.
                Collections.sort(equivalentsList);
                for (String name : equivalentsList) {
                    equivalencesResult.append("  ");
                    equivalencesResult.append(name);
                    equivalencesResult.append("\n");
                }
            }
        }
        equivalences.send(0, new StringToken(equivalencesResult.toString()));
    }
}
