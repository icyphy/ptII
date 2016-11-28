/* A director that checks the interfaces of its contained actors.

 Copyright (c) 1998-2009 The Regents of the University of California.
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
package ptolemy.apps.interfaces;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.data.StringToken;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.PtParser;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;

/** A director that checks the interfaces of its contained actors.
 *
 *  For each actor it checks first for a parameter named _interfaceExpr,
 *  which is interpreted as a boolean-valued Ptolemy expression
 *  representing the interface of the actor.
 *
 *  If that is not present, it checks for a parameter named _interfaceStr,
 *  which is interpreted as a string in the Yices expression language
 *  representing the interface.
 *
 *  If neither of these are present, it labels the given actor defective
 *  and raises an exception.
 *
 *  @author Ben Lickly
 *  @version $Id$
 *  @since Ptolemy II 8.1
 *  @Pt.ProposedRating Red (blickly)
 *  @Pt.AcceptedRating Red (blickly)
 */
public class InterfaceCheckerDirector extends Director {

    /** Construct a new InterfaceCheckerDirector, with the given container
     *  and name.
     *
     *  @param container The container.
     *  @param name The name of this director.
     *  @throws IllegalActionException If the superclass throws it.
     *  @throws NameDuplicationException If the superclass throws it.
     */
    public InterfaceCheckerDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       public methods                      ////

    /** Check that the interfaces in the model are valid.
     *
     *  @throws IllegalActionException If the interfaces of any actors in
     *    the model cannot be determined.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        final Nameable container = getContainer();

        if (container instanceof CompositeActor) {
            final Iterator<Entity> actors = ((CompositeActor) container)
                    .entityList().iterator();

            while (actors.hasNext() && !_stopRequested) {
                final Entity entity = actors.next();
                if (!(entity instanceof Actor)) {
                    continue;
                }
                final Actor actor = (Actor) entity;

                System.out.println("On actor " + actor.getFullName());
                final String result = _checkInterface(actor);

                if (result.equals("")) {
                    // could not get proof
                    throw new IllegalActionException(actor,
                            "Could not determine satisfiability of interface"
                                    + "of " + actor.getFullName());
                } else if (result.startsWith("unsat")) {
                    // unsat
                    throw new IllegalActionException(actor, actor.getFullName()
                            + "'s contract is unsatisfiable.");
                } else {
                    // sat
                    assert result.startsWith("sat");
                }
            }
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                       private methods                     ////

    /** Check that the interface of the given actor are valid.
     *
     *  In this first implementation, this is only a check of
     *  satisfiability, although we could potentially also check
     *  for other properties.
     *
     *  @param actor Actor whose interface is to be checked
     *  @return A string representing the result of the SMT check
     *  @throws IllegalActionException If the interface of the actor
     *    cannot be determined.
     */
    private String _checkInterface(Actor actor) throws IllegalActionException {
        final String yicesInput = _getInterface(actor).getYicesInput();
        System.out.println("Yices input is: " + yicesInput);
        final SMTSolver sc = new SMTSolver();
        return sc.check(yicesInput);
    }

    /** Infer the interface of a composite actor from its contained actors.
     *
     *  @param container The composite actor whose interface we are querying.
     *  @return The inferred interface.
     *  @throws IllegalActionException If no interface can be inferred.
     */
    private RelationalInterface _getCompositeInterface(CompositeActor container)
            throws IllegalActionException {
        final Set<String> newConstraints = new HashSet<String>();
        // Common input ports
        final Set<String> inputNames = new HashSet<String>();
        final List<IOPort> inputPorts = container.inputPortList();
        for (final IOPort compositeIn : inputPorts) {
            inputNames.add(compositeIn.getName());
            final List<IOPort> linkedPorts = compositeIn.insidePortList();
            for (final IOPort insideIn : linkedPorts) {
                newConstraints.add("(= " + insideIn.getName() + " "
                        + compositeIn.getName() + ")");
            }
        }
        // Common output ports
        final Set<String> outputNames = new HashSet<String>();
        final List<IOPort> outputPorts = container.outputPortList();
        for (final IOPort compositeOut : outputPorts) {
            outputNames.add(compositeOut.getName());
            final List<IOPort> linkedPorts = compositeOut.insidePortList();
            for (final IOPort insideOut : linkedPorts) {
                newConstraints.add("(= " + insideOut.getName() + " "
                        + compositeOut.getName() + ")");
            }
        }
        // Deal with contained actors
        List<Entity> actors = container.entityList();
        if (actors.size() == 1) {
            // Feedback Composition
            final Actor actor = (Actor) actors.get(0);
            Set<Connection> connections = _getConnectionsBetween(actor, actor);
            RelationalInterface actorInterface = _getInterface(actor);

            if (!connections.isEmpty()) { // FIXME: Move this check into interface.
                actorInterface.addFeedback(connections);
            }
            newConstraints.add(actorInterface.getContract());
            outputNames.addAll(actorInterface.getVariables());

        } else if (actors.size() == 2) {
            // Cascade/Parallel Composition
            Set<Connection> selfLoop1 = _getConnectionsBetween((Actor) actors.get(0), (Actor) actors.get(0));
            Set<Connection> selfLoop2 = _getConnectionsBetween((Actor) actors.get(1), (Actor) actors.get(1));
            Set<Connection> connection1 = _getConnectionsBetween((Actor) actors.get(0), (Actor) actors.get(1));
            Set<Connection> connection2 = _getConnectionsBetween((Actor) actors.get(1), (Actor) actors.get(0));

            if (!selfLoop1.isEmpty() || !selfLoop2.isEmpty() ||
                    (!connection1.isEmpty() && !connection2.isEmpty())) {
                throw new IllegalActionException(container,
                        "Cannot handle cascade and feedback in the same actor"
                      + "\nPlease separate hierarchy.");
            }

            final Actor actor0;
            final Actor actor1;
            Set<Connection> connections;
            if (connection2.isEmpty()) {
                actor0 = (Actor) actors.get(0); actor1 = (Actor) actors.get(1);
                connections = connection1;
            } else {
                assert (connection1.isEmpty());
                actor0 = (Actor) actors.get(1); actor1 = (Actor) actors.get(0);
                connections = connection2;
            }

            RelationalInterface compositeInterface;
            if (connections.isEmpty()) {
                compositeInterface = _getInterface(actor0).
                    parallelComposeWith(_getInterface(actor1));
            } else {
                compositeInterface = _getInterface(actor0).
                    cascadeComposeWith(_getInterface(actor1), connections);
            }
            newConstraints.add(compositeInterface.getContract());
            outputNames.addAll(compositeInterface.getVariables());

        } else if (actors.size() > 2) { // Not handled
            throw new IllegalActionException(container,
                    "Composition of more than two actors not yet supported");
        }
        final String contract = LispExpression.conjunction(newConstraints);
        return new RelationalInterface(inputNames, outputNames, contract);
    }

    /** Return a set of the connections between two actors.
     *  @param actor1 The first actor.
     *  @param actor2 The second actor.
     *  @return The set of connections.
     */
    private Set<Connection> _getConnectionsBetween(final Actor actor1,
            final Actor actor2) {
        Set<Connection> connections = new HashSet<Connection>();
        final List<IOPort> outputPorts = actor1.outputPortList();
        for (final IOPort outputPort : outputPorts) {
            final List<IOPort> inputPorts =
                (List<IOPort>) outputPort.connectedPortList();
            for (final IOPort inputPort : inputPorts) {
                if (inputPort.getContainer() == actor2) {
                    connections.add(new Connection(outputPort.getName(),
                            inputPort.getName()));
                }
            }
        }
        return connections;
    }

    /** Return the interface of a given actor.
     *
     *  To find the contract, this method first checks for a parameter
     *  named _interfaceExpr that is a Ptolemy expression.
     *  If that doesn't exist, it looks for a parameter named
     *  _interfaceStr that is a string representation.
     *  In the case that neither of those two options work, and
     *  the given actor is a CompositeActor, we can try to infer
     *  the interface from those of the contained actors.
     *
     *  @param actor The actor whose interface we are querying.
     *  @return The overall interface.
     *  @throws IllegalActionException If an interface doesn't exist and
     *    cannot be inferred.
     */
    private RelationalInterface _getInterface(Actor actor)
            throws IllegalActionException {

        final Parameter interfaceExpr = (Parameter) ((Entity) actor)
                .getAttribute("_interfaceExpr");
        final Parameter interfaceStr = (Parameter) ((Entity) actor)
                .getAttribute("_interfaceStr");

        String contract;
        if (interfaceExpr != null) {
            // If there is a Ptolemy Expression, we will use that
            final String expression = interfaceExpr.getExpression();

            final PtParser parser = new PtParser();
            ASTPtRootNode parseTree;
            parseTree = parser.generateParseTree(expression);

            final SMTFormulaBuilder formulaBuilder = new SMTFormulaBuilder();
            contract = formulaBuilder.parseTreeToSMTFormula(parseTree);

        } else if (interfaceStr != null) {
            // If there is no Ptolemy expression, we can use a string.
            // This must already be formatted in the Yices input language.
            contract = ((StringToken) interfaceStr.getToken()).stringValue();

        } else if (actor instanceof CompositeActor) {
            RelationalInterface compositeInterface =
                _getCompositeInterface((CompositeActor) actor);
            System.out.println("Inferred composite contract: "
                    + compositeInterface.getContract());
            return compositeInterface;
        } else { //(interfaceExpr == null && interfaceStr == null)
            throw new IllegalActionException(actor,
                    "No interface specified for" + actor.toString());
        }
        // FIXME: Ports may not be complete for composite actors that don't
        // infer interface (since internal ports should be outputs).
        return new RelationalInterface(actor.inputPortList(), actor
                .outputPortList(), contract);
    }

}
