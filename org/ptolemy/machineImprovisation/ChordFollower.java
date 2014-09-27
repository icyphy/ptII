/* Music sequencing and pitch-duration coupling

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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

///////////////////////////////////////////////////////////////////
//// A special actor that acts as a "metronome" in the improvisation process

/**
 <p>
 An actor that follows where the melody is within the measure.</p>

 @author Ilge Akkaya
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating  
 @Pt.AcceptedRating  
 */
public class ChordFollower extends TypedAtomicActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public ChordFollower(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        incomingDuration = new TypedIOPort(this, "incomingDuration", true, false);
        incomingDuration.setTypeEquals(BaseType.DOUBLE);

        chordSequence = new TypedIOPort(this, "chordSequence", true, false);
        chordSequence.setTypeEquals(BaseType.OBJECT);

        acceptedTuples = new TypedIOPort(this, "acceptedTuples", false, true); 
        Type[] tupleType = {BaseType.STRING,BaseType.DOUBLE}; 
        acceptedTuples.setTypeEquals(new RecordType(labels, tupleType));

        trigger = new TypedIOPort(this, "trigger", true, false);
        _triggersSinceLastOutput = 0;

        nextChord = new TypedIOPort(this, "nextChord", false, true);
        nextChord.setTypeEquals(BaseType.STRING);  

        reset = new TypedIOPort(this, "reset",true, false);
        StringAttribute cardinality = new StringAttribute( reset, "_cardinal");
        cardinality.setExpression("SOUTH");

        startLick = new TypedIOPort(this, "startLick", false, true);
        cardinality = new StringAttribute( startLick, "_cardinal");
        cardinality.setExpression("SOUTH");
        startLick.setTypeEquals(BaseType.BOOLEAN);

        currentBeat = new TypedIOPort(this, "currentBeat", false, true);
        currentBeat.setTypeEquals(BaseType.DOUBLE);

        incomingNote = new TypedIOPort(this, "incomingNote", true, false);
        incomingNote.setTypeEquals(BaseType.STRING);

        resetBeat = new TypedIOPort(this, "resetBeat", true, false);

        _allChords = new HashMap<Double,Chord>();
        _currentBeatCursor = 0.0; 
        _durations = new LinkedList<Double>(); 

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * Duration of the previous note in runtime.
     */
    public TypedIOPort incomingDuration;

    /**
     * The accepted duration value to be applied at the next tick.
     */
    public TypedIOPort acceptedTuples;

    /**
     * Accepted note in previous tick.
     */
    public TypedIOPort incomingNote;

    /**
     * The chord sequence to which the melody should adhere to.
     */
    public TypedIOPort chordSequence;

    /**
     * Next chord information provided to the Pitch oracle.
     */
    public TypedIOPort nextChord; 

    /**
     * Reset all inputs and begin new melody.
     */
    public TypedIOPort reset;

    /**
     * Reset beat only, keep chord information.
     */
    public TypedIOPort resetBeat;

    /*
     * Current beat count of the improvised melody.
     */
    public TypedIOPort currentBeat;

    /*
     * Trigger to begin counting.
     */
    public TypedIOPort trigger;

    /**
     * Boolean output to decide whether Pitch oracle should switch to a new lick.
     */
    public TypedIOPort startLick; 

    public void fire() throws IllegalActionException {
        super.fire();
        // when trigger is received, output the next chord up next
        // if the next note to be produced is a rest output a rest instead

        if (resetBeat.isOutsideConnected() && resetBeat.hasToken(0)) {
            if (((BooleanToken)resetBeat.get(0)).booleanValue()) { 
                _currentBeatCursor = 0.0; 
            }
        }

        if (reset.isOutsideConnected() &&reset.hasToken(0)) {
            if (((BooleanToken)reset.get(0)).booleanValue()) {
                _allChords.clear();
                _currentBeatCursor = 0.0;
                _barProgress = 0.0; 
                _durations.clear();
            }
        }

        // FIXME: In the future, the incoming chords could be provided through a 
        // port parameter as an array. This implementation supports individual Chord
        // objects to be received and stored, so that we do not need to wait until 
        // all chords to be received before starting the improvisation but can simultaneously
        // receive chords to follow, and improvise based on the chords that have 
        // already been received.
        if ( chordSequence.hasToken(0)) {
            Chord c = (Chord)((ObjectToken)chordSequence.get(0)).getValue(); 
            Double chordDuration = c.getDuration();
            String chordName = c.getName();

            if (chordName.equals(MusicSpecs.TERMINATION_CHORD)) {
                // do nothing,
                // termination chord has been received 
            } else { 
                Chord newChord = new Chord(chordName, chordDuration);
                _allChords.put(_barProgress, newChord);
                _barProgress += chordDuration; 
            }
        }

        if (trigger.isOutsideConnected() && trigger.hasToken(0)) {
            trigger.get(0); 
            _currentChord = _getChordForBeat(_currentBeatCursor);
            if (_currentChord!=null && !_currentChord.equals("NIL")) {
                nextChord.send(0, new StringToken(_currentChord));
            }
            _triggersSinceLastOutput ++;
        }

        if (_triggersSinceLastOutput >= 10) {
            // then start lick, because we're probably facing a deadlock
            startLick.send(0, new BooleanToken(true));
        }

        if (incomingNote.hasToken(0)) { 
            StringToken note = ((StringToken)incomingNote.get(0));
            if (_durations!= null 
                    && _durations.size() >0) { 
                double duration = _durations.get(0);    
                Token[] nextTokens = new Token[2];
                nextTokens[1] = new DoubleToken(duration);
                if ( duration < 0) {
                    // discard incoming note 
                    nextTokens[0] = new StringToken(MusicSpecs.REST_SYMBOL); 
                } else {
                    // if not a rest, attach with a note.
                    nextTokens[0] = note;
                }
                RecordToken next = new RecordToken(labels, nextTokens); 
                acceptedTuples.send(0,next);
                _currentBeatCursor += Math.abs(duration);
                currentBeat.send(0, new DoubleToken(_currentBeatCursor));
                _durations.remove(0); 

            }
        }  
        if (incomingDuration.hasToken(0)) { 
            // TODO: also, if the note is too short, repeat it until
            // bar progress reaches a multiple of 0.5 
            double nextDurationValue = ((DoubleToken)incomingDuration.get(0)).doubleValue();
            if (nextDurationValue != 0) {
                _durations.add(nextDurationValue);
            }
            // handling triplets
            if (Math.abs(nextDurationValue - 0.33) < 0.1){
                _durations.add(nextDurationValue);
                _durations.add(nextDurationValue);
            } 
        } 
    }
    public void wrapup(){ 
        _allChords.clear();
        _currentBeatCursor = 0.0;
        _barProgress = 0.0; 
        _durations.clear();
        _triggersSinceLastOutput = 0;
    }
    private String _getChordForBeat( double beat) {
        Iterator a = _allChords.keySet().iterator();
        String chordName = null;
        boolean found = false;
        while (a.hasNext()) { 
            double start = ((Double)a.next()).doubleValue();
            Chord c = ((Chord)_allChords.get(start));
            double dur = c.getDuration();
            if (start <= beat && start+dur > beat) {
                // found the bin where the current note resides.
                chordName = c.getName();
                found = true;
                break;
            }
        }
        if (!found) {
            return null;
        }
        return chordName;  
    }

    /** The beat count of the improvisation progress.*/
    private double _currentBeatCursor = 0.0;
    /** the keys are the starting points of chords in the bar, and the values are chord objects. */
    private HashMap _allChords;
    /** the cursor progress in the bar so far. incremented when new chords arrive. */
    private double _barProgress = 0.0;  
    /**
     * List of received durations, in time stamp order.
     */
    private List<Double> _durations; 
    /** 
     * Current chord according to the beat cursor.
     */
    private String _currentChord;
    
    /** 
     * Number of triggers sent to the pitch oracle, since 
     * a note-duration pair was successfully output.
     */
    private int _triggersSinceLastOutput; 

    /** 
     * Labels of output record tokens.
     */
    private String[] labels = {"frequency","duration"};
}
