/* A helper class for ptolemy.data.expr.ASTPtLeafNode.

 Copyright (c) 2006 The Regents of the University of California.
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

package ptolemy.data.properties.lattice.staticDynamic.data.expr;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.Token;
import ptolemy.data.expr.Constants;
import ptolemy.data.expr.ModelScope;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.Variable;
import ptolemy.data.properties.PropertyConstraintASTNodeHelper;
import ptolemy.data.properties.PropertyConstraintSolver;
import ptolemy.data.properties.lattice.staticDynamic.Lattice;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// ASTPtLeafNode

/**
 A helper class for ptolemy.data.expr.ASTPtLeafNode.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 6.2
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class ASTPtLeafNode extends PropertyConstraintASTNodeHelper {

    /**
     * Construct an property constraint helper for the given ASTPtLeafNode.
     * @param solver The given solver to get the lattice from.
     * @param node The given ASTPtLeafNode.
     * @throws IllegalActionException Thrown if the parent construct
     *  throws it.
     */
    public ASTPtLeafNode(PropertyConstraintSolver solver, 
        ptolemy.data.expr.ASTPtLeafNode node) 
            throws IllegalActionException {
        super(solver, node, false);

        //ParseTreeEvaluator evaluator = new ParseTreeEvaluator();
        //evaluator.evaluateParseTree(node, new AttributeScope());        

        Lattice lattice = (Lattice) solver.getLattice();

        String name = node.getName();
        
        if (name != null) {
            if (_variables.contains(name)) {
                setEquals(node, lattice.DYNAMIC);
                
            } else if (Constants.get(name) != null) {
                setEquals(node, lattice.STATIC);
            }

        } else if (node.isConstant()) {
            setEquals(node, lattice.STATIC);
        }
    }
    
    
    /** The of names of the expression variables that are non-static.
     */
    private static List _variables = Arrays.asList( 
            new String[]{ "time", "iteration"
    });        
    
    
    private class AttributeScope extends ModelScope {
        /** Look up and return the attribute with the specified name in the
         *  scope. Return null if such an attribute does not exist.
         *  @return The attribute with the specified name in the scope.
         */
        public Token get(String name) throws IllegalActionException {

            Attribute attribute = 
                getAttribute((ptolemy.data.expr.ASTPtRootNode) _component);
            
            Variable result = getScopedVariable(null, 
                    attribute,
                    name);


            if (result != null) {
                return result.getToken();
            }

            return null;
        }

        /** Look up and return the type of the attribute with the
         *  specified name in the scope. Return null if such an
         *  attribute does not exist.
         *  @return The attribute with the specified name in the scope.
         */
        public Type getType(String name) throws IllegalActionException {
            Variable result = getScopedVariable(null, 
                    getAttribute((ptolemy.data.expr.ASTPtRootNode) _component),
                    name);

            if (result != null) {
                return (Type) result.getTypeTerm().getValue();
            }

            return null;
        }

        /** Look up and return the type term for the specified name
         *  in the scope. Return null if the name is not defined in this
         *  scope, or is a constant type.
         *  @return The InequalityTerm associated with the given name in
         *  the scope.
         *  @exception IllegalActionException If a value in the scope
         *  exists with the given name, but cannot be evaluated.
         */
        public ptolemy.graph.InequalityTerm getTypeTerm(String name)
                throws IllegalActionException {
            Variable result = getScopedVariable(null, 
                    getAttribute((ptolemy.data.expr.ASTPtRootNode) _component),
                    name);

            if (result != null) {
                return result.getTypeTerm();
            }

            return null;
        }

        /** Return the list of identifiers within the scope.
         *  @return The list of identifiers within the scope.
         */
        public Set identifierSet() {
            return getAllScopedVariableNames(null, 
                    getAttribute((ptolemy.data.expr.ASTPtRootNode) _component));
        }
    }

    
}
