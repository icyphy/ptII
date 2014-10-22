/* DummyReferenceToken is a token encapsulating a reference to a shared
   data object. It is used for testing the OptimizingSDFDirector.

 Copyright (c) 1997-2014 The Regents of the University of California.
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

/**
 *
 */
package ptolemy.domains.sdf.optimize.lib;

import ptolemy.data.Token;

/**
<h1>Class comments</h1>
DummyReferenceToken is a token encapsulating a reference to a shared
data object. It is used for testing the OptimizingSDFDirector.
<p>
See {@link ptolemy.domains.sdf.optimize.OptimizingSDFDirector} and
{@link ptolemy.domains.sdf.optimize.lib.DummyFrame} for more information.
</p>
@see ptolemy.domains.sdf.optimize.OptimizingSDFDirector
@see ptolemy.domains.sdf.optimize.lib.DummyFrame

@author Marc Geilen
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (mgeilen)
@Pt.AcceptedRating Red ()
 */
public class DummyReferenceToken extends Token {

    /**
     * Create an instance of a reference token.
     * @param r the object the token shall refer to
     */
    public DummyReferenceToken(Object r) {
        _ref = r;
    }

    /**
     * Gets the reference.
     * @return referenced object
     */
    public Object getReference() {
        return _ref;
    }

    /**
     * Provide a string representation of the token.
     * @return string representation of the token
     */
    @Override
    public String toString() {
        return _ref.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////           private fields               ////

    /**
     * Holds the reference to some object.
     */
    private Object _ref;
}
