/* Transformation rule.

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

//////////////////////////////////////////////////////////////////////////
//// TransformRule
/**
   Transformation rule to be executed by {@link TypeAnalyzer} while it
   traverses the program AST.
   <p>
   This type of source ransformation is specified with rules, each of
   which defines the actions to be executed <em>before</em> the traversal
   and <em>after</em> the traversal. Implementation of a rule may add
   special handlers ({@link AssignmentHandler}, {@link ClassHandler}, etc.)
   to the analyzer before the traversal, and finalize its transformation
   after the traversal.

   @author Thomas Feng
   @version $Id$
   @since Ptolemy II 5.1
   @Pt.ProposedRating Red (tfeng)
   @Pt.AcceptedRating Red (tfeng)
*/
public abstract class TransformRule {

    /** Execute actions after the AST is traversed by {@link TypeAnalyzer}.
     *
     *  @param analyzer The type analyzer.
     *  @param root The root of the AST.
     *  @see #beforeTraverse(TypeAnalyzer, CompilationUnit)
     */
    public abstract void afterTraverse(TypeAnalyzer analyzer,
            CompilationUnit root);

    /** Execute actions before the AST is traversed by {@link TypeAnalyzer}.
     *
     *  @param analyzer The type analyzer.
     *  @param root The root of the AST.
     *  @see #afterTraverse(CompilationUnit)
     */
    public abstract void beforeTraverse(TypeAnalyzer analyzer,
            CompilationUnit root);

}
