/* An interface used by the expression parser for identifier lookup.

 Copyright (c) 2001-2003 The Regents of the University of California.
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

@ProposedRating Red (liuxj@eecs.berkeley.edu)
@AcceptedRating Red (liuxj@eecs.berkeley.edu)

*/

package ptolemy.data.expr;

import ptolemy.kernel.util.IllegalActionException;

import java.util.Set;

//////////////////////////////////////////////////////////////////////////
//// ParserScope
/**
An interface used by the expression parser for identifier lookup.
<p>
An object implementing this interface represents the set of identifiers that
can be used in an expression being evaluated.

@author Xiaojun Liu, Steve Neuendorffer
@version $Id$
@see ptolemy.data.expr.PtParser
*/

public interface ParserScope {

    /** Look up and return the value with the specified name in the
     *  scope. Return null if the name is not defined in this scope.
     *  @return The token associated with the given name in the scope.
     *  @exception IllegalActionException If a value in the scope
     *  exists with the given name, but cannot be evaluated.
     */
    public ptolemy.data.Token get(String name)
            throws IllegalActionException;

    /** Look up and return the type of the value with the specified
     *  name in the scope. Return null if the name is not defined in
     *  this scope.
     *  @return The token associated with the given name in the scope.
     *  @exception IllegalActionException If a value in the scope
     *  exists with the given name, but cannot be evaluated.
     */
    public ptolemy.data.type.Type getType(String name)
            throws IllegalActionException;

    /** Look up and return the type term for the specified name
     *  in the scope. Return null if the name is not defined in this
     *  scope, or is a constant type.
     *  @return The InequalityTerm associated with the given name in
     *  the scope.
     *  @exception IllegalActionException If a value in the scope
     *  exists with the given name, but cannot be evaluated.
     */
    public ptolemy.graph.InequalityTerm getTypeTerm(String name)
            throws IllegalActionException;

    /** Return a list of names corresponding to the identifiers
     *  defined by this scope.  If an identifier is returned in this
     *  list, then get() and getType() will return a value for the
     *  identifier.  Note that generally speaking, this list is
     *  extremely expensive to compute, and users should avoid calling
     *  it.  It is primarily used for debugging purposes.
     *  @exception IllegalActionException If constructing the list causes
     *  it.
     */
    public Set identifierSet()
            throws IllegalActionException;
}

