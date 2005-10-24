/* Assignment transformation rule.

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

import org.eclipse.jdt.core.dom.CompilationUnit;

import ptolemy.backtrack.ast.TypeAnalyzer;

import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// AssignmentRule

/**
 Assignment transformation rule. This rule specifies the actions to be
 executed before and after {@link TypeAnalyzer} traverses an AST. Those
 actions refactor the AST to add in support for backtracking.

 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class AssignmentRule extends TransformRule {
    ///////////////////////////////////////////////////////////////////
    ////                       public methods                      ////

    /** Execute actions after the AST is traversed. (Not necessary for
     *  this rule.)
     *
     *  @param root The root of the AST.
     */
    public void afterTraverse(TypeAnalyzer analyzer, CompilationUnit root) {
    }

    /** Add a handler (@link AssignmentTransformer) to the type analyzer.
     *  The handler refactors the AST while the type analyzer traverses it.
     *
     *  @param analyzer The type analyzer.
     *  @param root The root of the AST.
     */
    public void beforeTraverse(TypeAnalyzer analyzer, CompilationUnit root) {
        AssignmentTransformer assignmentTransformer = new AssignmentTransformer();
        ConstructorTransformer constructorTransformer = new ConstructorTransformer();

        _handlers.add(assignmentTransformer);
        _handlers.add(constructorTransformer);

        analyzer.getHandlers().addAliasHandler(assignmentTransformer);
        analyzer.getHandlers().addAssignmentHandler(assignmentTransformer);
        analyzer.getHandlers().addClassHandler(assignmentTransformer);
        analyzer.getHandlers().addCrossAnalysisHandler(assignmentTransformer);
        analyzer.getHandlers()
                .addFieldDeclarationHandler(assignmentTransformer);
        analyzer.getHandlers().addMethodDeclarationHandler(
                assignmentTransformer);

        analyzer.getHandlers().addClassHandler(constructorTransformer);
        analyzer.getHandlers().addConstructorHandler(constructorTransformer);
        analyzer.getHandlers().addCrossAnalysisHandler(constructorTransformer);
        analyzer.getHandlers().addMethodDeclarationHandler(
                constructorTransformer);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private fields                      ////

    /** The list of handlers used.
     */
    private List _handlers = new LinkedList();
}
