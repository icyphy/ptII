/* An interface used by the expression parser for identifier lookup.

 Copyright (c) 2001 The Regents of the University of California.
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

import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.NamedList;

//////////////////////////////////////////////////////////////////////////
//// ParserScope
/**
An interface used by the expression parser for identifier lookup.
<p>
An object implementing this interface represents the set of identifiers that
can be used in an expression being parsed by PtParser.

@author Xiaojun Liu
@version $Id$
@see ptolemy.data.expr.PtParser
*/

public interface ParserScope {

    /** Look up and return the attribute with the specified name in the
     *  scope. Return null if such an attribute does not exist.
     *  @return The attribute with the specified name in the scope.
     */
    public Attribute get(String name);

    /** Return the list of attributes within the scope.
     *  @return The list of attributes within the scope.
     */
    public NamedList attributeList();

}

