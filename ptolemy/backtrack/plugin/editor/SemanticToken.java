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
package ptolemy.backtrack.plugin.editor;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.SimpleName;

/**
 * Semantic token
 */
public final class SemanticToken {
    /** AST node */
    private SimpleName fNode;

    /** Binding */
    private IBinding fBinding;

    /** Is the binding resolved? */
    private boolean fIsBindingResolved;

    /** AST root */
    private CompilationUnit fRoot;

    /**
     * @return Returns the binding, can be <code>null</code>.
     */
    public IBinding getBinding() {
        if (!fIsBindingResolved && (fNode != null)) {
            fBinding = fNode.resolveBinding();
            fIsBindingResolved = true;
        }

        return fBinding;
    }

    /**
     * @return the AST node
     */
    public SimpleName getNode() {
        return fNode;
    }

    /**
     * @return the AST root
     */
    public CompilationUnit getRoot() {
        if (fRoot == null) {
            fRoot = (CompilationUnit) fNode.getRoot();
        }

        return fRoot;
    }

    /**
     * Update this token with the given AST node.
     * <p>
     * NOTE: Allowed to be used by {@link SemanticHighlightingReconciler} only.
     * </p>
     *
     * @param node the AST node
     */
    protected void update(SimpleName node) {
        fNode = node;
        fBinding = null;
        fIsBindingResolved = false;
        fRoot = null;
    }

    /**
     * Clears this token.
     * <p>
     * NOTE: Allowed to be used by {@link SemanticHighlightingReconciler} only.
     * </p>
     */
    protected void clear() {
        fNode = null;
        fBinding = null;
        fIsBindingResolved = false;
        fRoot = null;
    }
}
