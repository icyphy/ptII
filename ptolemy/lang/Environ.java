/*
An environment for declarations, which may be contained in another
environment. Code converted from Environ in Titanium.

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

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Iterator;


//////////////////////////////////////////////////////////////////////////
//// Environ
/** An environment for declarations, which may be contained in another
 *  environment. Code converted from Environ in Titanium.
 *  @author ctsay@eecs.berkeley.edu
 */
public class Environ {

  public Environ() {
    this(null, new LinkedList());
  }

  public Environ(Environ parent) {
    this(parent, new LinkedList());
  }

  public Environ(Environ parent, LinkedList declList) {
    _parent = parent;
    _declList = declList;
  }

  public Environ parent() {
    return _parent;
  }

  public void add(Decl decl) {
    _declList.addLast(decl);
  }

  public void copyDeclList(Environ env) {
    _declList.clear();
    _declList.addAll(env._declList);
  }

  public Decl lookup(String name) {
    MutableBoolean dummy = new MutableBoolean();
    return lookup(name, Decl.CG_ANY, dummy, false);
  }

  public Decl lookup(String name, int mask) {
    MutableBoolean dummy = new MutableBoolean();
    return lookup(name, mask, dummy, false);
  }

  public Decl lookup(String name, MutableBoolean more) {
    return lookup(name, Decl.CG_ANY, more, false);
  }

  public Decl lookup(String name, int mask, MutableBoolean more) {
    return lookup(name, mask, more, false);
  }

  public Decl lookupProper(String name) {
    return lookup(name, Decl.CG_ANY, new MutableBoolean(), true);
  }

  public Decl lookupProper(String name, int mask) {
    return lookup(name, mask, new MutableBoolean(), true);
  }

  public Decl lookupProper(String name, MutableBoolean more) {
    return lookup(name, Decl.CG_ANY, more, true);
  }

  public Decl lookupProper(String name, int mask, MutableBoolean more) {
    return lookup(name, mask, more, true);
  }

  public Decl lookup(String name, int mask, MutableBoolean more, boolean proper) {
    EnvironIter itr = lookupFirst(name, mask, proper);

    if (itr.hasNext()) {
       Decl retval = (Decl) itr.next();
       more.setValue(itr.hasNext());
       return retval;
    }
    more.setValue(false);
    return null;
  }

  public EnvironIter lookupFirst(String name) {
    return lookupFirst(name, Decl.CG_ANY, false);
  }

  public EnvironIter lookupFirst(String name, int mask) {
    return lookupFirst(name, mask, false);
  }

  public EnvironIter lookupFirstProper(String name) {
    return lookupFirst(name, Decl.CG_ANY, true);
  }

  public EnvironIter lookupFirstProper(String name, int mask) {
    return lookupFirst(name, mask, true);
  }

  public EnvironIter lookupFirst(String name, int mask, boolean proper) {
    Environ parent = proper ? null : _parent;

    return new EnvironIter(parent, _declList.listIterator(), name, mask);
  }

  public EnvironIter allDecls() {
    return lookupFirst("*", Decl.CG_ANY, false);
  }

  public EnvironIter allDecls(int mask) {
    return lookupFirst(Decl.ANY_NAME, mask, false);
  }

  public EnvironIter allDecls(String name) {
    return lookupFirst(name, Decl.CG_ANY, false);
  }

  public ListIterator allProperDecls() {
    return _declList.listIterator();
  }

  public EnvironIter allProperDecls(int mask) {
    return lookupFirst(Decl.ANY_NAME, mask, true);
  }

  public EnvironIter allProperDecls(String name) {
    return lookupFirst(name, Decl.CG_ANY, true);
  }

  // See if there's more than one Decl only in this Environ
  public boolean moreThanOne(String name, int mask) {
    return moreThanOne(name, mask, false);
  }

  // See if there's more than one Decl only in this Environ
  public boolean moreThanOne(String name, int mask, boolean proper) {
    MutableBoolean more = new MutableBoolean();

    lookup(name, mask, more, proper);

    return more.getValue();
  }

  public String toString() {
    return toString(true);
  }

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

  protected Environ _parent;
  protected LinkedList _declList;
}
