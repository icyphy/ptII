/* A token that contains a figure.

Copyright (c) 1997-2005 The Regents of the University of California.
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

@ProposedRating Red (neuendor)
@AcceptedRating Red (neuendor)

*/
package ptolemy.apps.vergil.graph.util;

import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

import diva.canvas.Figure;

import java.io.Serializable;


//////////////////////////////////////////////////////////////////////////
//// FigureToken

/**


@author Steve Neuendorffer
@version $Id$

*/
public class FigureToken extends Token {
    public FigureToken(Figure figure) {
        super();
        _figure = figure;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Convert the specified token to an instance of this class, if it
     *  is not already such an instance.
     *  @param token A Token to be converted.
     *  @return The argument, if it is a FigureToken.
     *  @exception IllegalActionException If the token is not a FigureToken.
     */
    public static Token convert(Token token) throws IllegalActionException {
        if (token instanceof FigureToken) {
            return token;
        } else {
            throw new IllegalActionException("Attempt to convert token "
                + token + " into a figure token, which is not possible.");
        }
    }

    /** Return the figure contained by this token.
     */
    public Figure getFigure() {
        return _figure;
    }

    /** Return the type of this token.
     *  @return
     */
    public Type getType() {
        return TYPE;
    }

    /** Test for equality of the values of this Token and the argument Token.
     *  It should be overridden in derived classes to provide type specific
     *  actions for equality testing.
     *  @param token The token with which to test equality.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A BooleanToken which contains the result of the test.
     */
    public BooleanToken isEqualTo(Token token) throws IllegalActionException {
        if (token instanceof FigureToken) {
            return new BooleanToken(this == token);
        } else {
            throw new IllegalActionException(
                "Equality test not supported between "
                + this.getClass().getName() + " and "
                + token.getClass().getName() + ".");
        }
    }

    /** Return the value of this token as a string that can be parsed
     *  by the expression language to recover a token with the same value.
     *  This method should be overriden by derived classes.
     *  In this base class, return the String "present" to indicate
     *  that an event is present.
     *  @return The String "present".
     */
    public String toString() {
        return "FigureToken(" + _figure + ")";
    }

    public static class FigureType implements Type, Serializable {
        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** Return this, that is, return the reference to this object.
         *  @return A BaseType.
         */
        public Object clone() {
            return this;
        }

        /** Convert the specified token to a token having the type
         *  represented by this object.
         *  @param t A token.
         *  @return A token.
         *  @exception IllegalActionException If lossless conversion cannot
         *   be done.
         */
        public Token convert(Token t) throws IllegalActionException {
            return FigureToken.convert(t);
        }

        /** Test if the argument token is compatible with this type. The method
         *  returns true if this type is UNKNOWN, since any type is a substitution
         *  instance of it. If this type is not UNKNOWN, this method returns true
         *  if the argument type is less than or equal to this type in the type
         *  lattice, and false otherwise.
         *  @param t A Token.
         *  @return True if the argument token is compatible with this type.
         */
        public boolean isCompatible(Token t) {
            return t.getType() == this;
        }

        /** Test if this Type is UNKNOWN.
         *  @return True if this Type is not UNKNOWN; false otherwise.
         */
        public boolean isConstant() {
            return true;
        }

        /** Determine if the argument represents the same BaseType as this
         *  object.
         *  @param t A Type.
         *  @return True if the argument represents the same BaseType as
         *   this object; false otherwise.
         */
        public boolean isEqualTo(Type t) {
            return this == t;
        }

        /** Determine if this type corresponds to an instantiable token
         *  classes. A BaseType is instantiable if it does not correspond
         *  to an abstract token class, or an interface, or UNKNOWN.
         *  @return True if this type is instantiable.
         */
        public boolean isInstantiable() {
            return true;
        }

        /** Return true if the argument is a
         *  substitution instance of this type.
         *  @param type A Type.
         *  @return True if this type is UNKNOWN; false otherwise.
         */
        public boolean isSubstitutionInstance(Type type) {
            return this == type;
        }

        /** Return the string representation of this type.
         *  @return A String.
         */
        public String toString() {
            return "figure";
        }
    }

    public static final Type TYPE = new FigureType();

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private Figure _figure;
}
