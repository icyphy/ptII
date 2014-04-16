/* A token that contains an actor.

 Copyright (c) 2003-2013 The Regents of the University of California.
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
package ptolemy.data;

import java.io.IOException;
import java.io.Writer;

import ptolemy.data.type.ActorType;
import ptolemy.data.type.Type;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// ActorToken

/**
 A token that contains an actor.  This token allows components to be
 moved around in a model.  One subtlety is that actors are not,
 generally immutable objects.  In order to prevent the actor
 transmitted from appearing in multiple places in a model, and the
 semantic fuzziness that would result, the actor is always cloned when
 being retrieved from this token.

 @author Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class ActorToken extends Token implements Cloneable {
    /** Construct an ActorToken.
     *  @param entity The entity that this Token contains.
     *  @exception IllegalActionException If cloning the entity fails.
     */
    public ActorToken(Entity entity) throws IllegalActionException {
        super();

        try {
            _entity = (Entity) entity.clone();
        } catch (CloneNotSupportedException ex) {
            throw new IllegalActionException(null, ex,
                    "Failed to create actor token");
        }
    }
    
    /** Construct an empty ActorToken to be used as a reference for the Actor type designator. */
    private ActorToken() {
    	super();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a clone of the entity contained by this token.
     *  @return The clone of the entity.
     */
    public Entity getEntity() {
        try {
            return (Entity) _entity.clone();
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException(
                    "Failed to clone actor, but I already cloned it once!!!");
        }
    }

    /** Return a clone of the entity contained by this token.
     *
     *  @param workspace The workspace that the returned entity is in.
     *  @return The clone of the entity.
     */
    public Entity getEntity(Workspace workspace) {
        try {
            return (Entity) _entity.clone(workspace);
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException(
                    "Failed to clone actor, but I already cloned it once!!!");
        }
    }

    /** Output the MoML of the enclosed entity to the given writer.
     *
     *  @param output The writer to which the MoML is output.
     *  @exception IOException If an I/O error occurs.
     */
    public void getMoML(Writer output) throws IOException {
        _entity.exportMoML(output);
    }

    /** Return the type of this token.
     *  @return the type of this token.
     */
    public Type getType() {
        return TYPE;
    }

    /** Test for closeness of the values of this Token and the argument
     *  Token.  For actor tokens, checking for closeness is the same
     *  as checking for equality.
     *  @param rightArgument The token to compare to this token.
     *  @param epsilon This argument is ignored in this method.
     *  @return A true-valued token if the first argument is equal to
     *  this token.
     *  @exception IllegalActionException If thrown by
     *  {@link #isEqualTo(Token)}.
     */
    public BooleanToken isCloseTo(Token rightArgument, double epsilon)
            throws IllegalActionException {
        return isEqualTo(rightArgument);
    }

    /** Model for equality of the values of this Token and the argument Token.
     *  It should be overridden in derived classes to provide type specific
     *  actions for equality testing.
     *  @param token The token with which to test equality.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A BooleanToken which contains the result of the test.
     */
    public BooleanToken isEqualTo(Token token) throws IllegalActionException {
        if (token instanceof ActorToken) {
            return new BooleanToken(toString().equals(token.toString()));
        } else {
            throw new IllegalActionException(
                    "Equality test not supported between "
                            + this.getClass().getName() + " and "
                            + token.getClass().getName() + ".");
        }
    }

    /** Return the value of this token as a string that can be parsed
     *  by the expression language to recover a token with the same value.
     *  This method returns a string of the form `parseMoML("<i>text</i>")',
     *  where <i>text</i> is a MoML description of this actor with quotation
     *  marks and backslashes escaped.
     *  @return A MoML description of this actor.
     */
    public String toString() {
        return "parseMoML(\""
                + StringUtilities.escapeString(_entity.exportMoMLPlain())
                + "\")";
    }
    
    /** Empty Entity instance of this token. */
    public static final ActorToken EMPTY = new ActorToken();

    /** Singleton reference to this type. */
    public static final Type TYPE = new ActorType();

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private Entity _entity;
}
