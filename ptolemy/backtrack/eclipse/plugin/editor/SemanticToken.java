/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/* Semantic token in the Java source.

 Copyright (c) 2005-2013 The Regents of the University of California.
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
package ptolemy.backtrack.eclipse.plugin.editor;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.SimpleName;

///////////////////////////////////////////////////////////////////
//// SemanticToken
/**
 Semantic token in the Java source.

 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public final class SemanticToken {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the binding, or null if undefined.
     *
     *  @return The binding.
     */
    public IBinding getBinding() {
        if (!_isBindingResolved && (_node != null)) {
            _binding = _node.resolveBinding();
            _isBindingResolved = true;
        }

        return _binding;
    }

    /** Return the AST node of this token.
     *
     *  @return The AST node.
     */
    public SimpleName getNode() {
        return _node;
    }

    /** Return the AST root.
     *
     *  @return The AST root.
     */
    public CompilationUnit getRoot() {
        if (_root == null) {
            _root = (CompilationUnit) _node.getRoot();
        }

        return _root;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Clear this token. This method should only be called by {@link
     *  SemanticHighlightingReconciler}.
     */
    protected void _clear() {
        _node = null;
        _binding = null;
        _isBindingResolved = false;
        _root = null;
    }

    /** Update this token with the given AST node. This method should only be
     *  called by {@link SemanticHighlightingReconciler}.
     *
     *  @param node The AST node.
     */
    protected void _update(SimpleName node) {
        _node = node;
        _binding = null;
        _isBindingResolved = false;
        _root = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private fields                      ////

    /** The binding.
     */
    private IBinding _binding;

    /** Whether the binding resolved.
     */
    private boolean _isBindingResolved;

    /** The AST node of this token.
     */
    private SimpleName _node;

    /** The AST root.
     */
    private CompilationUnit _root;
}
