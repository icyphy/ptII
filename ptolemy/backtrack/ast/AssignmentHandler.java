/* Interface of assignment handler in AST traversal.

Copyright (c) 1997-2004 The Regents of the University of California.
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

package ptolemy.backtrack.ast;

import net.sourceforge.jrefactory.ast.ASTAssignmentOperator;
import net.sourceforge.jrefactory.ast.ASTConditionalExpression;
import net.sourceforge.jrefactory.ast.ASTExpression;
import net.sourceforge.jrefactory.ast.ASTPrimaryExpression;
import net.sourceforge.jrefactory.ast.ASTVariableDeclaratorId;
import net.sourceforge.jrefactory.ast.ASTVariableInitializer;
import net.sourceforge.jrefactory.ast.Node;

//////////////////////////////////////////////////////////////////////////
//// AssignmentHandler
/**
 *  During an AST traversal, assignments may be specially handled with
 *  an assignment handler. The type analyzer is such an AST visitor that
 *  it may take an assignment handler and call back its functions when
 *  an assignment is seen in the AST.
 *  <p>
 *  There are three kinds of assignment occurances: assignment as a
 *  statement, assignment as a sub-expression, and assignment in a field
 *  or local variable declaration.
 * 
 *  @author Thomas Feng
 *  @version $Id$
 *  @since Ptolemy II 4.1
 *  @Pt.ProposedRating Red (tfeng)
 *  @see {@link TypeAnalyzer}
 */
public interface AssignmentHandler {

    /** Handle an assignment as a statement, and replace it
     *  with a single node if necessary.
     *  <p>
     *  The grammar for such a statement expression is:
     *  <tt>StatementExpression ::- PrimaryExpression AssignmentOperator Expression</tt>
     *  
     *  @param leftHandSide The left-hand side of the assignment.
     *  @param assignmentOperator The assignment operator.
     *  @param rightHandSide The right-hand side of the assignment.
     *  @return If not <tt>null</tt>, it must the a new AST node,
     *   which will replace the three nodes in the parameters and
     *   become the single child of the parent <tt>Expression</tt>.
     *  @throws ASTException Thrown when error occurs.
     */
    public Node handleAssignment(ASTPrimaryExpression leftHandSide,
            ASTAssignmentOperator assignmentOperator,
            ASTExpression rightHandSide) throws ASTException;
    
    /** Handle an assignment as a sub-expression, and replace it
     *  with a single node if necessary.
     *  <p>
     *  The grammar for such a sub-expression is:
     *  <tt>Expression ::- ConditionalExpression AssignmentOperator Expression</tt>
     *  
     *  @param leftHandSide The left-hand side of the assignment.
     *  @param assignmentOperator The assignment operator.
     *  @param rightHandSide The right-hand side of the assignment.
     *  @return If not <tt>null</tt>, it must the a new AST node,
     *   which will replace the three nodes in the parameters and
     *   become the single child of the parent <tt>Expression</tt>.
     *  @throws ASTException Thrown when error occurs.
     */
    public Node handleAssignment(
            ASTConditionalExpression leftHandSide,
            ASTAssignmentOperator assignmentOperator,
            ASTExpression rightHandSide) throws ASTException;
    
    /** Handle an assignment in a field or variable declaration,
     *  and replace it with a single node if necessary.
     *  <p>
     *  The grammar for such a variable declarator is:
     *  <tt>VariableDeclarator ::- VariableDeclaratorId VariableInitializer</tt>
     *  
     *  @param leftHandSide The left-hand side of the assignment.
     *  @param rightHandSide The right-hand side of the assignment.
     *  @return If not <tt>null</tt>, it must the a new AST node,
     *   which will replace the two nodes in the parameters and
     *   become the single child of the parent <tt>Expression</tt>.
     *  @throws ASTException Thrown when error occurs.
     */
    public Node handleAssignment(ASTVariableDeclaratorId leftHandSide,
            ASTVariableInitializer rightHandSide) throws ASTException;
    
}
