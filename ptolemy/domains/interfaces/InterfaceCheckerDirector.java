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
package ptolemy.domains.interfaces;

import java.util.HashSet;
import java.util.Iterator;
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

/** This director checks the interfaces of its contained actors.
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
     *  @param container
     *  @param name
     *  @throws IllegalActionException
     *  @throws NameDuplicationException
     */
    public InterfaceCheckerDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }
    
    /** Check that the interfaces in the model are valid.
     * 
     *  @throws IllegalActionException
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        
        Nameable container = getContainer();
        
        if (container instanceof CompositeActor) {
            Iterator<Entity> actors = ((CompositeActor) container)
                    .entityList().iterator();
            
            while (actors.hasNext() && !_stopRequested) {
                Entity entity = actors.next();
                if (!(entity instanceof Actor)) continue;
                Actor actor = (Actor) entity;
                
                System.out.println("On actor " + actor.getFullName());
                String result = _checkInterface(actor);

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
                    assert (result.startsWith("sat"));
                }
            }
        }
        
    }

    /** Check that the interface of the given actor are valid.
     * 
     *  In this first implementation, this is only a check of
     *  satisfiability, although we could potentially also check
     *  for other properties.
     * 
     *  @param actor Actor whose interface is to be checked
     *  @return A string representing the result of the SMT check
     *  @throws IllegalActionException 
     */
    protected String _checkInterface(Actor actor)
            throws IllegalActionException {
        String yicesInput = _getInterface(actor).getYicesInput();
        System.out.println("Yices input is: " + yicesInput);
        SMTSolver sc = new SMTSolver();
        return sc.check(yicesInput);
    }

    /** Infer the interface of a composite actor from its contained actors.
     * 
     *  @param container The composite actor whose interface we are querying.
     *  @return The inferred interface.
     */
    private RelationalInterface _getCompositeInterface(
            CompositeActor container) {
        /*
        Iterator<Entity> actors = container.entityList().iterator();

        while (actors.hasNext() && !_stopRequested) {
            Entity entity = actors.next();
            if (!(entity instanceof Actor)) continue;
            Actor actor = (Actor) entity;

            System.out.println("On contained actor: " + actor.getFullName());
        }
         */
        Iterator<IOPort> inputPorts = container.inputPortList().iterator();
        Set<String> inputNames = new HashSet<String>();
        while (inputPorts.hasNext()) {
            IOPort inputPort = inputPorts.next();
            inputNames.add(inputPort.getName());            
            
        }
        
        return null;
    }

    /** Return the interface of a given actor.
     * 
     *  In the case that the given actor is a CompositeActor, we
     *  will try to infer the interface from the contained actors.
     *  Otherwise, we will simply look for annotations of the
     *  interface contract, and chose the inputs and outputs of
     *  the actor as inputs and outputs of the interface.
     * 
     *  @param actor The actor whose interface we are querying.
     *  @return The overall interface.
     *  @throws IllegalActionException
     */
    private RelationalInterface _getInterface(Actor actor)
            throws IllegalActionException {
        
        // We want to infer the interfaces of composite actors.
        if (actor instanceof CompositeActor) {
            return _getCompositeInterface((CompositeActor)actor);
        }
        
        String contract = _getSMTFormula(actor);

        return new RelationalInterface(actor.inputPortList(),
                actor.outputPortList(), contract);
    }

    /** Read the SMT formula for the contract of an actor from the
     *  appropriate parameter.
     * 
     *  This method first checks for a parameter named _interfaceExpr that
     *  is a Ptolemy expression.  If that doesn't exist, it looks for
     *  a parameter named _interfaceStr that is a String.
     * 
     *  @param actor The actor whose contract we are querying.
     *  @return The contract, as a string.
     *  @throws IllegalActionException
     */
    protected String _getSMTFormula(Actor actor)
            throws IllegalActionException {
        Parameter interfaceExpr = (Parameter)
                        ((Entity)actor).getAttribute("_interfaceExpr");
        Parameter interfaceStr = (Parameter)
                        ((Entity)actor).getAttribute("_interfaceStr");
        if (interfaceExpr != null) {
            // If there is a Ptolemy Expression, we will use that
            String expression = interfaceExpr.getExpression();

            PtParser parser = new PtParser();
            ASTPtRootNode parseTree;
            parseTree = parser.generateParseTree(expression);

            SMTFormulaBuilder formulaBuilder = new SMTFormulaBuilder();
            return formulaBuilder.parseTreeToSMTFormula(parseTree);

        } else if (interfaceStr != null) {
            // If there is no Ptolemy expression, we can use a string.
            // This must already be formatted in the Yices input language.
            return ((StringToken)interfaceStr.getToken()).stringValue();

        } else { //(interfaceExpr == null && interfaceStr == null)
            throw new IllegalActionException(actor, "No interface specified");
        }
    }

}
