/*
A declaration of a method or constructor.

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
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;

import ptolemy.lang.TreeNode;
import ptolemy.lang.java.nodetypes.TypeNode;

//////////////////////////////////////////////////////////////////////////
//// MethodDecl
/** A declaration of a method or constructor.
 *
 *  @author Jeff Tsay
 */
public class MethodDecl extends MemberDecl {
    public MethodDecl(String name, int category0, TypeNode type, int modifiers,
            TreeNode source, JavaDecl container, List paramList,
            Collection throwsCollection) {
        super(name, category0, type, modifiers, source, container);
        _paramList = paramList;
        _throwsSet = new HashSet(throwsCollection);
    }

    /** Return the MethodDecl that this MethodDecl overrides/hides.
     *  Return null if this is the initial definition of the method.
     */
    public MethodDecl getOverrides() {
        return _overrides;
    }

    /** Set the MethodDecl that this MethodDecl overrides/hides. */
    public void setOverrides(MethodDecl overrides) {
        _overrides = overrides;
    }

    public Set getOverriders() {
        return _overridersSet;
    }

    public boolean addOverrider(MethodDecl m) {
        return _overridersSet.add(m);
    }

    /** Return the set of interface methods this MethodDecl implements.
     *  Return an empty set if this MethodDecl implements no interface methods.
     */
    public Set getImplements() {
        return _implementsSet;
    }

    /** Add a MethodDecl to the set of interface methods this MethodDecl
     *  implements.
     */
    public boolean addImplement(MethodDecl m) {
        return _implementsSet.add(m);
    }

    /** Return list of TypeNodes representing the types of the arguments of
     *  this method.
     */
    public List getParams() {
        return _paramList;
    }

    /** Return the set of TypeNameNodes representing exceptions that can be
     *  thrown by this method.
     */
    public Set getThrows() {
        return _throwsSet;
    }

    /** The MethodDecl that this MethodDecl overrides/hides.
     *  null if this is the initial definition of the method.
     */
    protected MethodDecl _overrides = null;

    /** The list of TypeNodes representing the types of the arguments of
     *  this method. This list should not be modified after construction.
     */
    protected final List _paramList;

    /** The set of TypeNameNodes representing exceptions that can be thrown
     *  by this method.
     *  This set may change after construction, if overridden methods throw
     *  exceptions not declared explicitly.
     */
    protected final Set _throwsSet;

    protected final Set _overridersSet = new HashSet();

    /** The set of interface methods this MethodDecl implements.  Similar
     *  to overrides, but refers to a set of interface methods rather than
     *  a single superclass method.
     */
    protected final Set _implementsSet = new HashSet();
}
