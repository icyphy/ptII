/* An actor that converts a string to an array of integers.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.actor.lib.conversions;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
/// StringToIntArray

/**
 Convert a string to an integer-array.  The output is an array of integers
 constructed by placing one byte (i.e. one character) of the string into
 the least significant byte of each integer.  Typically, this byte is the
 ASCII code of the character.  NOTE: For the time being, this actor assumes
 an 8-bit character has been set as the Java default on the platform in use.
 This actor is designed to facilitate use of the SerialComm serial
 communication actor which uses the same kind of integer array.  Datagram
 actors can take this format as well.
 <p>
 @author Winthrop Williams
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (winthrop)
 @Pt.AcceptedRating Red (winthrop)
 */
public class StringToIntArray extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public StringToIntArray(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.STRING);

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(new ArrayType(BaseType.INT));

        _attachText("_iconDescription", "<svg>\n"
                + "<polygon points=\"-15,-15 15,15 15,-15 -15,15\" "
                + "style=\"fill:white\"/>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The port for the input, which has type <i>string</i>. */
    public TypedIOPort input;

    /** The output port, which has type <i>{int}</i>. */
    public TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Consume one string token on the input port and output a new array
     *  token of integer tokens on the output port.  The low byte of each
     *  integer is the byte form of one of the characters.  The other
     *  three bytes of each integer may be 0x000000 or 0xFFFFFF.  The
     *  first character of the string is copied to the first element of
     *  the array, and so on.  NOTE: Assumes an 8-bit character set is
     *  the default setting for this implementation of Java.
     *
     *  @exception IllegalActionException If there is no director.
     *  FIXME: Does this method actually check if there is a director?
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        String inputValue = ((StringToken) input.get(0)).stringValue();

        // DO THE CONVERSION:
        byte[] dataBytes = inputValue.getBytes(); //(Creates a new copy)

        // FIXME The following line assumes one byte per char.
        // True for out platform, but not all, under Java.
        // Replace with length of the created byte array.
        int bytesAvailable = inputValue.length();
        Token[] dataIntTokens = new Token[bytesAvailable];

        for (int j = 0; j < bytesAvailable; j++) {
            dataIntTokens[j] = new IntToken(dataBytes[j]);
        }

        output.send(0, new ArrayToken(BaseType.INT, dataIntTokens));
    }

    /** Return false if the input port has no token, otherwise return
     *  what the superclass returns (presumably true).
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        if (!input.hasToken(0)) {
            return false;
        }

        return super.prefire();
    }
}
