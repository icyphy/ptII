/* An scope for declarations, which may be contained in another
scope.

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

package ptolemy.lang;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;


//////////////////////////////////////////////////////////////////////////
//// Scope
/** An scope for declarations, which may be contained in another
scope.  Scopes are used to implement scoping for declarations.
<p>
Portions of this code were derived from sources developed under the
auspices of the Titanium project, under funding from the DARPA, DoE,
and Army Research Office.

@author Jeff Tsay
@version $Id$
 */
public class Scope {

    /** Construct an empty scope. */
    public Scope() {
        this(null, new LinkedList());
    }

    /** Construct an scope nested inside the parent argument,
     *  without its own proper Decl's.
     */
    public Scope(Scope parent) {
        this(parent, new LinkedList());
    }

    /** Construct an scope nested inside the parent argument,
     *  with the given List of Decl's in this scope itself.
     */
    public Scope(Scope parent, List declList) {
        _parent = parent;
        _declList = declList;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Adds a mapping to the argument decl in this scope proper.
     *  This does not affect any Scopes in which this is nested.
     */
    public void add(Decl decl) {
        _declList.add(decl);
    }

    /** Return an ScopeIterator that will iterate over all the Decls
     *  in this Scope.
     */
    public ScopeIterator allDecls() {
        return lookupFirst(Decl.ANY_NAME, Decl.CG_ANY, false);
    }

    /** Return an ScopeIterator that will iterate over all the Decls
     *  that have any of the categories bits set in mask.
     */
    public ScopeIterator allDecls(int mask) {
        return lookupFirst(Decl.ANY_NAME, mask, false);
    }

    /** Return an ScopeIterator that will iterate over all the Decls
     *  that have the same name.
     */
    public ScopeIterator allDecls(String name) {
        return lookupFirst(name, Decl.CG_ANY, false);
    }

    /** Return an ListIterator that will iterate over all the proper Decls
     *  in this Scope and in any parent Scopes.
     */
    public ListIterator allProperDecls() {
        return _declList.listIterator();
    }

    /** Return an ListIterator that will iterate over all the proper Decls
     *  in this Scope and in any parent Scopes that have a matching mask.
     */
    public ScopeIterator allProperDecls(int mask) {
        return lookupFirst(Decl.ANY_NAME, mask, true);
    }

    /** Return an ScopeIterator that will iterate over all the proper Decls
     *  in this Scope and in any parent Scopes that have the same name.
     */
    public ScopeIterator allProperDecls(String name) {
        return lookupFirst(name, Decl.CG_ANY, true);
    }

    /** Copy the declList from env. */
    public void copyDeclList(Scope env) {
	// FIXME: This is a little strange, but if two envs share a declList
	// then copyDeclList will effectively set them to null
        _declList.clear();
        _declList.addAll(env._declList);
    }

    /** Lookup a decl by name in the current scope, do not look
     *  in the parent scope, if any.
     */
    public Decl lookup(String name) {
        return lookup(name, Decl.CG_ANY, new boolean[1], false);
    }

    /** Lookup a decl by name and mask in the current scope, do not look
     *  in the parent scope, if any.
     */
    public Decl lookup(String name, int mask) {
        return lookup(name, mask, new boolean[1], false);
    }

    /** Lookup a decl by name in the current scope, do not look
     *  in the parent scope. Set more[0] to true if there are
     *  other decls with the same name in the scope.
     */
    public Decl lookup(String name, boolean[] more) {
        return lookup(name, Decl.CG_ANY, more, false);
    }

    /** Lookup a decl by name and mask in the current scope, do not look
     *  in the parent scope, if any. Set more[0] to true if there
     *  is one or more decls with the same name in the scope.
     *  Note that more[0] will be true if the other decls have the
     *  same name but different masks.
     */
    public Decl lookup(String name, int mask, boolean[] more) {
        return lookup(name, mask, more, false);
    }

    /** Lookup a decl by name in the current scope and
     *  in the parent scope, if any.
     */
    public Decl lookupProper(String name) {
        return lookup(name, Decl.CG_ANY, new boolean[1], true);
    }

    /** Lookup a decl by name and mask in the current scope and
     *  in the parent scope, if any.
     */
    public Decl lookupProper(String name, int mask) {
        return lookup(name, mask, new boolean[1], true);
    }

    /** Lookup a decl by name in the current scope and
     *  in the parent scope, if any. Set more[0] to true if there
     *  is one or more decls with the same name in the scope.
     *  Note that more[0] will be true if the other decls have the
     *  same name but different masks.
     */
    public Decl lookupProper(String name, boolean[] more) {
        return lookup(name, Decl.CG_ANY, more, true);
    }

    /** Lookup a decl by name and mask in the current scope and
     *  in the parent scope, if any. Set more[0] to true if there
     *  is one or more decls with the same name in the scope.
     *  Note that more[0] will be true if the other decls have the
     *  same name but different masks.
     */
    public Decl lookupProper(String name, int mask, boolean[] more) {
        return lookup(name, mask, more, true);
    }

    /** Lookup a decl by name and mask in the current scope or
     *  in the parent scope, if any. Set more[0] to true if there
     *  is one or more decls with the same name in the scope.
     *  Note that more[0] will be true if the other decls have the
     *  same name but different masks.  If the proper argument is
     *  true, then look in the parent scope, if any.  If it
     *  is false, then do not look in the parent scope.
     */
    public Decl lookup(String name, int mask, boolean[] more, boolean proper) {
        ScopeIterator itr = lookupFirst(name, mask, proper);

        if (itr.hasNext()) {
            Decl retval = (Decl) itr.next();
            more[0] = itr.hasNext();
            return retval;
        }
        more[0] = false;
        return null;
    }

    /** Lookup a decl by name in the current Scope. */
    public ScopeIterator lookupFirst(String name) {
        return lookupFirst(name, Decl.CG_ANY, false);
    }

    /** Lookup a decl by name and mask in the current Scope. */
    public ScopeIterator lookupFirst(String name, int mask) {
        return lookupFirst(name, mask, false);
    }

    /** Lookup a decl by name in the current Scope or the
     *  parent Scopes
     */
    public ScopeIterator lookupFirstProper(String name) {
        return lookupFirst(name, Decl.CG_ANY, true);
    }

    /** Lookup a decl by name and mask in the current Scope or the
     *  parent Scopes
     */
    public ScopeIterator lookupFirstProper(String name, int mask) {
        return lookupFirst(name, mask, true);
    }

    /** Lookup a decl by name and mask in the current Scope or the
     *  parent Scopes.  If the proper argument is true, the look
     *  in the parent Scopes, if the proper argument is false, the
     *  do not look in the parent Scopes.
     */
    public ScopeIterator lookupFirst(String name, int mask, boolean proper) {
        Scope parent = proper ? null : _parent;

        return new ScopeIterator(parent, _declList.listIterator(), name, mask);
    }

    /** Return true if there is more than one matching Decl only
     *  in this Scope.
     */
    public boolean moreThanOne(String name, int mask) {
        return moreThanOne(name, mask, false);
    }

    /** Return true if there is more than one matching Decl in this Scope,
     *  and if proper is true, in the enclosing Scopes.
     */
    public boolean moreThanOne(String name, int mask, boolean proper) {
        boolean[] more = new boolean[1];

        lookup(name, mask, more, proper);

        return more[0];
    }

    /** Return the parent Scope of this Scope. */
    public Scope parent() {
        return _parent;
    }

    /** Remove the first Decl that matches the decl arg */
    public void remove(Decl decl) {
        _declList.remove(decl);
    }

    /** Return a recursive String representation of this Scope. */
    public String toString() {
        return toString(true);
    }

    /** Return a String representation of this Scope.  If the
     *  recursive argument is true, then append string representations
     *  of parent Scopes as well.
     *  @param recursive True if parent Scopes are also traversed.
     *  @return a possibly recursive String representation of this Scope.
     */
    public String toString(boolean recursive) {
        ListIterator declItr = _declList.listIterator();

        StringBuffer retval = new StringBuffer("[");

        while (declItr.hasNext()) {
            Decl d = (Decl) declItr.next();
            retval.append(d.toString());
            if (declItr.hasNext()) {
                retval.append(", ");
            }
        }

        retval.append("] ");

        if (_parent != null) {
            retval.append("has parent\n");

            if (recursive) {
                retval.append(_parent.toString(true));
            }
        } else {
            retval.append("no parent\n");
        }
        return retval.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The parent of this Scope. */
    protected Scope _parent;

    /** The list of Decls in this Scope. */
    protected List _declList;
}
