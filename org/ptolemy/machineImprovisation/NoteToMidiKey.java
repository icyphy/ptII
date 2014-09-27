/* Type conversion from note name to MIDI key 

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

import org.ptolemy.machineImprovisation.MusicSpecs;

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
 Simple conversion actor that converts a letter note as "C4" into
 a MIDI key integer</p> 

 @author Ilge Akkaya
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating 
 @Pt.AcceptedRating 
 */
public class NoteToMidiKey extends TypedAtomicActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public NoteToMidiKey(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name); 
        
        letterNote = new TypedIOPort(this, "letterNote", true, false);
        letterNote.setTypeEquals(BaseType.STRING);
        
        midiKey = new TypedIOPort(this, "midiKey", false, true);
        midiKey.setTypeEquals(BaseType.INT);
        
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    
    /**
     * A note, represented by a string, consisting of the pitch letter A-G, the ocrave, and (possibly) a trailing symbol
     */
    public TypedIOPort letterNote;

    /**
     * The midi key equivalent of the input letter note
     */
    public TypedIOPort midiKey; 
  
    public void fire() throws IllegalActionException {
        super.fire();
        if (letterNote.hasToken(0)) {
            String letter = ((StringToken)letterNote.get(0)).stringValue();
            midiKey.send(0, new IntToken(MusicSpecs.translateNoteToKey(letter)));
        } 
    }  
}
