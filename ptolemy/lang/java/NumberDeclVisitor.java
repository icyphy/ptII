/*
Number the declarations uniquely. Declarations are numbered from those contained
in the root of the tree downwards. Declarations on sibling nodes are numbered
from left to right.

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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import ptolemy.lang.*;

/** A visitor that numbers declarations uniquely. Declarations are numbered from those contained
 *  in the root of the tree downwards. Declarations on sibling nodes are numbered
 *  from left to right.
 *
 *  @author ctsay@eecs.berkeley.edu
 */
public class NumberDeclVisitor extends JavaVisitor {
    public NumberDeclVisitor() {
        this(new HashSet());
    }

    public NumberDeclVisitor(HashSet declSet) {
        super(TM_CHILDREN_FIRST);

        _declSet = declSet;
    }

    /** The default visit method. */
    protected Object _defaultVisit(TreeNode node, LinkedList args) {

        Iterator itr = node.values().iterator(); // iterate over all properties

        while (itr.hasNext()) {
            Object obj = itr.next();

            if (obj instanceof JavaDecl) {
               JavaDecl decl = (JavaDecl) obj;
               if (!decl.hasProperty(PropertyMap.NUMBER_KEY)) {
                  decl.setProperty(PropertyMap.NUMBER_KEY, new Integer(_declSet.size()));
                  _declSet.add(decl);
               }
            }
        }

        return _declSet;
    }

    /** Given a list of TreeNode's, number all declarations in the list uniquely,
     *  including descendents of individual TreeNode's.
     *  Return the set of all declarations.
     */
    public static Set numberDecls(Collection decls) {
        HashSet set = new HashSet();
        NumberDeclVisitor v = new NumberDeclVisitor(set);

        Iterator itr = decls.iterator();

        while (itr.hasNext()) {
            TreeNode node = (TreeNode) itr.next();
            node.accept(v, null); // modifies set
        }

        return set;
    }

    protected HashSet _declSet;
}
