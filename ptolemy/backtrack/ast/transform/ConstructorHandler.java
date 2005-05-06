/* Interface of the constructor handlers called by TypeAnalyzer.

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

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;

import ptolemy.backtrack.ast.TypeAnalyzerState;


//////////////////////////////////////////////////////////////////////////
//// ConstructorHandler

/**
   Interface of the constructor handlers called by {@link TypeAnalyzer}.
   Users may register constructor handlers (and other kinds of supported
   handlers) to the {@link TypeAnalyzer} used to analyze Java source code.
   When the analyzer detects a constructor declaration or a call to a
   constructor, it calls back those constructor handlers after proper types are
   assigned to all the arguments.
   <p>
   Constructor handlers are allowed to modify the constructor, either by
   modifying its children in the AST, or by replacing the whole constructor
   with another valid AST node. This is because the handler is called after the
   subtree rooted at the constructor is completely visited by the analyzer.
   However, modifying any node out of this subtree (e.g., changing the parent
   of this constructor to another one) may cause unexpected effect.

   @author Thomas Feng
   @version $Id$
   @since Ptolemy II 5.1
   @Pt.ProposedRating Red (tfeng)
   @Pt.AcceptedRating Red (tfeng)
*/
public interface ConstructorHandler {
    
    /** Enter a field declaration.
     * 
     *  @param node The field declaration node.
     *  @param state The current state of the type analyzer.
     */
    public void enter(FieldDeclaration node, TypeAnalyzerState state);

    /** Exit a field declaration.
     * 
     *  @param node The field declaration node.
     *  @param state The current state of the type analyzer.
     */
    public void exit(FieldDeclaration node, TypeAnalyzerState state);

    /** Handle a class instance creation (with the <tt>new</tt> operator).
     * 
     *  @param node The class instance creation node.
     *  @param state The current state of the type analyzer.
     */
    public void handle(ClassInstanceCreation node, TypeAnalyzerState state);

    /** Handle a method declaration node that corresponds to a constructor. The
     *  type analyzer calls back this method when it reaches a constructor. In
     *  an Eclipse AST, a constructor is defined with a {@link
     *  MethodDeclaration} as other methods, but its <tt>isConstructor</tt>
     *  method returns <tt>true</tt>.
     * 
     *  @param node The constructor node.
     *  @param state The current state of the type analyzer.
     */
    public void handle(MethodDeclaration node, TypeAnalyzerState state);

    /** Handle a super constructor invocation (with the <tt>super</tt>
     *  keyword).
     * 
     *  @param node The super constructor invocation node.
     *  @param state The current state of the type analyzer.
     */
    public void handle(SuperConstructorInvocation node,
            TypeAnalyzerState state);
}
