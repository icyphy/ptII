/* Interface of the assignment handlers called by TypeAnalyzer.

 Copyright (c) 2005 The Regents of the University of California.
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
package ptolemy.backtrack.ast.transform;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;

import ptolemy.backtrack.ast.TypeAnalyzerState;

//////////////////////////////////////////////////////////////////////////
//// AssignmentHandler

/**
 Interface of the assignment handlers called by {@link TypeAnalyzer}.
 Users may register assignment handlers (and other kinds of supported
 handlers) to the {@link TypeAnalyzer} used to analyze Java source code.
 When the analyzer detects an assignment, it calls back those assignment
 handlers after proper types are assigned to both the left-hand side and
 the right-hand side of the assignment.
 <p>
 Assignment handlers are allowed to modify the assignment, either by
 modifying its children in the AST, or by replacing the whole assignment
 with another expression. This is because the handler is called after the
 subtree rooted at the assignment is completely visited by the analyzer.
 However, modifying any node out of this subtree (e.g., changing the parent
 of this assignment to another one) may cause unexpected effect.

 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public interface AssignmentHandler {
    /** Handle an assignment. The assignment can be an assignment acting as a
     *  statement, or an assignment as a sub-expression in a larger expression.
     *  Assignments in field declarations or local variable declarations are
     *  not handled by this function, because they are initializers, where the
     *  old values of the declared fields or variables are meaningless.
     *
     *  @param node The assignment to be handled.
     *  @param state The current state of the analyzer.
     */
    public void handle(Assignment node, TypeAnalyzerState state);

    /** Handle a postfix expression with a "++" operator or a "--" operator.
     *  The assignment can be an assignment acting as a statement, or an
     *  assignment as a sub-expression in a larger expression.
     *
     *  @param node The assignment to be handled.
     *  @param state The current state of the analyzer.
     */
    public void handle(PostfixExpression node, TypeAnalyzerState state);

    /** Handle a prefix expression with a "++" operator or a "--" operator. The
     *  assignment can be an assignment acting as a statement, or an assignment
     *  as a sub-expression in a larger expression.
     *
     *  @param node The assignment to be handled.
     *  @param state The current state of the analyzer.
     */
    public void handle(PrefixExpression node, TypeAnalyzerState state);
}
