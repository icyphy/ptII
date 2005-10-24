/* Interface of the method handlers called by TypeAnalyzer.

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

import org.eclipse.jdt.core.dom.MethodDeclaration;

import ptolemy.backtrack.ast.TypeAnalyzerState;

//////////////////////////////////////////////////////////////////////////
//// MethodDeclarationHandler

/**
 Interface of the method handlers called by {@link TypeAnalyzer}.
 Users may register method handlers (and other kinds of supported
 handlers) to the {@link TypeAnalyzer} used to analyze Java source code.
 When the analyzer detects a method declaration, it calls back those
 method handlers before and after proper types are assigned to all the
 arguments.
 <p>
 {@link #exit(MethodDeclaration, TypeAnalyzerState)} of method handlers are
 allowed to modify the method declaration, either by modifying its children
 in the AST, or by replacing the whole method declaration with another valid
 AST node. This is because the handler is called after the subtree rooted at
 the method declaration is completely visited by the analyzer. However,
 modifying any node out of this subtree (e.g., changing the parent of this
 method declaration to another one) may cause unexpected effect.
 <p>
 {@link #enter(MethodDeclaration, TypeAnalyzerState)} should not modify the
 method declaration.


 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public interface MethodDeclarationHandler {
    /** Enter a method declaration. When this method is called back by the type
     *  analyzer, types are not yet assigned to the arguments of the method.
     *
     *  @param node The method declaration node.
     *  @param state The current state of the type analyzer.
     */
    public void enter(MethodDeclaration node, TypeAnalyzerState state);

    /** Exit a method declaration. When this method is called back by the type
     *  analyzer, types are assigned to the arguments of the method.
     *  Implementing classes may modify the method declaration in this method.
     *
     *  @param node The method declaration node.
     *  @param state The current state of the type analyzer.
     */
    public void exit(MethodDeclaration node, TypeAnalyzerState state);
}
