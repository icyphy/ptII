/* Type conversion from MIDI key index to note name

Copyright (c) 2013 The Regents of the University of California.
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
package org.ptolemy.machineImprovisation;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
////

/**
 <p>
 Simple conversion actor that converts a midi key integer into a letter
 note</p>

 @author Ilge Akkaya
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating
 @Pt.AcceptedRating
 */
public class MidiKeyToNote extends TypedAtomicActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public MidiKeyToNote(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        letterNote = new TypedIOPort(this, "letterNote", false, true);
        letterNote.setTypeEquals(BaseType.STRING);

        midiKey = new TypedIOPort(this, "midiKey", true, false);
        midiKey.setTypeEquals(BaseType.INT);

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * A note equivalent of the key, represented by a string, consisting of the pitch letter A-G, the ocrave, and (possibly) a trailing symbol
     */
    public TypedIOPort letterNote;

    /**
     * The midi key index
     */
    public TypedIOPort midiKey;

    public void fire() throws IllegalActionException {
        super.fire();
        if (midiKey.hasToken(0)) {
            int key = ((IntToken)midiKey.get(0)).intValue();
            letterNote.send(0, new StringToken(MusicSpecs.translateKeyToLetterNote(key, true)));
        }
    }
}
