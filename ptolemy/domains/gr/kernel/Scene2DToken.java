/* A Token that contains a scene graph.

 Copyright (c) 2003 The Regents of the University of California.
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

@ProposedRating Red (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (neuendor@eecs.berkeley.edu)

*/
package ptolemy.domains.gr.kernel;

import diva.canvas.Figure;

import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

import java.io.Serializable;

//////////////////////////////////////////////////////////////////////////
//// Scene2DToken
/**
A token that contains a two-dimension GR scene.  Two dimensional GR scenes are represented by Diva canvases.  This is used by the GR domain to get
proper type checking across 2D GR actors.

@author Steve Neuendorffer
@version $Id$
@since Ptolemy II 3.1

*/
public class Scene2DToken extends Token {

    public Scene2DToken(Figure figure) {
        super();
        _figure = figure;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the object contained by this token.
     */
    public Figure getFigure() {
        return _figure;
    }

    /** Return the type of this token.
     *  @return the type of this token.
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
        if (token instanceof Scene2DToken) {
            return new BooleanToken(this == token);
        } else
            throw new IllegalActionException(
                    "Equality test not supported between "
                    + this.getClass().getName() + " and "
                    + token.getClass().getName() + ".");
    }

    /** Return the value of this token as a string that can be parsed
     *  by the expression language to recover a token with the same value.
     *  This method should be overriden by derived classes.
     *  In this base class, return the String "present" to indicate
     *  that an event is present.
     *  @return The String "present".
     */
    public String toString() {
        return "Scene2DToken(" + _figure + ")";
    }

    public static class Scene2DType implements Type, Serializable {

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
         *  @param token A token.
         *  @return A token.
         *  @exception IllegalActionException If lossless conversion cannot
         *   be done.
         */
        public Token convert(Token token)
                throws IllegalActionException {
            if (token instanceof Scene2DToken) {
                return token;
            } else {
                throw new IllegalActionException("Attempt to convert token "
                        + token +
                        " into a scene graph token, which is not possible.");
            }
        }

        /** Return the class for tokens that this basetype represents.
         */
        public Class getTokenClass() {
            return Scene2DToken.class;
        }

        /** Test if the argument type is compatible with this type.
         *  The method returns true if this type is UNKNOWN, since any type
         *  is a substitution instance of it. If this type is not UNKNOWN,
         *  this method returns true if the argument type is less than or
         *  equal to this type in the type lattice, and false otherwise.
         *  @param type An instance of Type.
         *  @return True if the argument type is compatible with this type.
         */
        public boolean isCompatible(Type type) {
            return type == this;
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
        public boolean equals(Type t) {
            return this == t;
        }

        /** Return this type's figure index in the (constant) type lattice.
         * @return this type's figure index in the (constant) type lattice.
         */
        public int getTypeHash() {
            return Type.HASH_INVALID;
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
            return "scene2D";
        }
    }

    public static final Type TYPE = new Scene2DType();

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private Figure _figure;
}
