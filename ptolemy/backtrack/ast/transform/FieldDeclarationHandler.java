/* Interface of the field handlers called by TypeAnalyzer.

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

import org.eclipse.jdt.core.dom.FieldDeclaration;

import ptolemy.backtrack.ast.TypeAnalyzerState;

//////////////////////////////////////////////////////////////////////////
//// FieldDeclarationHandler

/**
 Interface of the field handlers called by {@link TypeAnalyzer}.
 Users may register field handlers (and other kinds of supported
 handlers) to the {@link TypeAnalyzer} used to analyze Java source code.
 When the analyzer detects a field declaration, it calls back those
 field handlers before and after proper types are assigned to all the
 arguments.
 <p>
 {@link #exit(FieldDeclaration, TypeAnalyzerState)} of field handlers are
 allowed to modify the field declaration, either by modifying its children
 in the AST, or by replacing the whole field declaration with another valid
 AST node. This is because the handler is called after the subtree rooted at
 the field declaration is completely visited by the analyzer. However,
 modifying any node out of this subtree (e.g., changing the parent of this
 field declaration to another one) may cause unexpected effect.
 <p>
 {@link #enter(FieldDeclaration, TypeAnalyzerState)} should not modify the
 field declaration.


 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public interface FieldDeclarationHandler {
    /** Enter a field declaration. When this field is called back by the type
     *  analyzer, types are not yet assigned to the arguments of the field.
     *
     *  @param node The field declaration node.
     *  @param state The current state of the type analyzer.
     */
    public void enter(FieldDeclaration node, TypeAnalyzerState state);

    /** Exit a field declaration. When this field is called back by the type
     *  analyzer, types are assigned to the arguments of the field.
     *  Implementing classes may modify the field declaration in this field.
     *
     *  @param node The field declaration node.
     *  @param state The current state of the type analyzer.
     */
    public void exit(FieldDeclaration node, TypeAnalyzerState state);
}
