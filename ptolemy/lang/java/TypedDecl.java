/*
A declaration that is typed.

Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (ctsay@eecs.berkeley.edu)
@AcceptedRating Red (ctsay@eecs.berkeley.edu)

*/

package ptolemy.lang.java;

import ptolemy.lang.IVisitor;
import java.util.LinkedList;

import ptolemy.lang.TreeNode;

//////////////////////////////////////////////////////////////////////////
//// TypedDecl
/** Decls of entities that "have a type"---such as local variables,
 *  parameters, fields, and methods---inherit from this class.  Types are
 *  represented as pointers to AST nodes of type TypeNode, just as they
 *  are during parsing.  Packages, statement labels, classes, and
 *  interfaces don't have types (although the latter two ARE types).
 *  (comment from Titanium project)
 *
 *  @author ctsay
 */
public abstract class TypedDecl extends JavaDecl {
    public TypedDecl(String name, int category0, TypeNode type, int modifiers) {
        super(name, category0);
        _type = type;
        _modifiers = modifiers;
    }

    public final boolean hasType() {
        return true;
    }

    public final TypeNode getType() {
        return _type;
    }

    public final void setType(TypeNode type) {
        _type = type;
    }

    public final boolean hasModifiers() {
        return true;
    }

    public final int getModifiers() {
        return _modifiers;
    }

    public final void setModifiers(int modifiers) {
        _modifiers = modifiers;
    }

    protected TypeNode  _type;
    protected int  _modifiers;
}
