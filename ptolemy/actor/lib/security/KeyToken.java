/* Tokens that contain java.security.Keys

 Copyright (c) 2002-2003 The Regents of the University of California.
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
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.security;

import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import java.security.Key;

//////////////////////////////////////////////////////////////////////////
//// KeyToken
/**
Tokens that contain java.security.Keys

@author Christopher Hylands Brooks
@version $Id$
@since Ptolemy II 3.1
*/
public abstract class KeyToken extends Token {

    /** Construct an empty token.
     */
    public KeyToken() {
        super();
    }

    /** Construct a token with a specified java.security.Key
     */
    public KeyToken(Key value) {
        _value = value;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public Type getType() {
        return BaseType.OBJECT;
    }

    /** Return the java.security.Key
     */
    public java.security.Key getValue() {
        return _value;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The java.security.Key */
    private java.security.Key _value;
}

