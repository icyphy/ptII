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

import java.util.Iterator;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.ParseTreeSMTChecker;
import ptolemy.data.expr.PtParser;
import ptolemy.data.smtsolver.SMTSolver;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;

/** This director checks the interfaces of its contained actors.
 * 
 * For each actor it checks first for a parameter named _interfaceExpr,
 * which is interpreted as a boolean-valued Ptolemy expression
 * representing the interface of the actor.
 * 
 * If that is not present, it checks for a parameter named _interfaceStr,
 * which is interpreted as a string in the Yices expression language
 * representing the interface.
 *
 * If neither of these are present, it labels the given actor defective
 * and raises an exception.
 * 
 * @author Ben Lickly
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (blickly)
 * @Pt.AcceptedRating Red (blickly)
 */
public class InterfaceCheckerDirector extends Director {

    /**
     * @param container
     * @param name
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    public InterfaceCheckerDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        // TODO Auto-generated constructor stub
    }
    
    /** Check that the interfaces in the model are valid.
     * 
     * @throws IllegalActionException
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        
        Nameable container = getContainer();
        
        if (container instanceof CompositeActor) {
            Iterator<?> actors = ((CompositeActor) container).deepEntityList()
                    .iterator();
            
            while (actors.hasNext() && !_stopRequested) {
                NamedObj actor = (NamedObj) actors.next();
                String result = _checkInterface(actor);

                if (result.equals("")) {
                    // could not get proof
                    throw new IllegalActionException(actor,
                            "Could not determine satisfiability of interface"
                          + "of " + actor.getFullName());
                } else if (result.charAt(0) == 'u') {
                    // unsat
                    throw new IllegalActionException(actor, actor.getFullName()
                            + "'s contract is unsatisfiable.");
                } else {
                    // sat
                    assert (result.charAt(0) == 's');
                }
            }
        }
        
    }

    /** Check that the interface of the given actor are valid.
     * 
     * In this first implementation, this is only a check of
     * satisfiability, although later we will want to also
     * include notions of composition, refinement, etc.
     * 
     * @param actor Actor whose interface is to be checked
     * @return A string representing the result of the SMT check
     * @throws IllegalActionException 
     */
    protected String _checkInterface(NamedObj actor) throws IllegalActionException {
        Parameter interfaceExpr = (Parameter) actor.getAttribute("_interfaceExpr");
        Parameter interfaceStr = (Parameter) actor.getAttribute("_interfaceStr");
        if (interfaceExpr != null) {
            // If there is a Ptolemy Expression, we will use that
            String expression = interfaceExpr.getExpression();
            System.out.println("Interface is: " + expression);

            PtParser parser = new PtParser();
            ASTPtRootNode parseTree;
            parseTree = parser.generateParseTree(expression);

            ParseTreeSMTChecker ptsc = new ParseTreeSMTChecker();
            return ptsc.checkParseTree(parseTree);

        } else if (interfaceStr != null) {
            // If there is no Ptolemy expression, we can use a string.
            // This must already be formatted in the Yices input language.
            SMTSolver sc = new SMTSolver();
            return sc.check(interfaceStr.getExpression());
            
        } else { //(interfaceExpr == null && interfaceStr == null)
            throw new IllegalActionException(actor, "No interface specified");
        }
    }

}
