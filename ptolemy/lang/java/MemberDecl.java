/* Copyright (c) 1998-2000 The Regents of the University of California.
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
import ptolemy.lang.java.nodetypes.*;

public abstract class MemberDecl extends TypedDecl {
    public MemberDecl(String name, int category0, TypeNode type, int modifiers, TreeNode source, JavaDecl container) {
        super(name, category0, type, modifiers);
        _source = source;
        _container = container;
    }

    public final boolean hasSource() {
        return true;
    }

    public final TreeNode getSource() {
        return _source;
    }

    public final void setSource(TreeNode source) {
        _source = source;
    }

    public final boolean hasContainer() {
        return true;
    }

    public final JavaDecl getContainer() {
        return _container;
    }

    public final void setContainer(JavaDecl container) {
        _container = container;
    }

    protected TreeNode  _source;
    protected JavaDecl  _container;
}
