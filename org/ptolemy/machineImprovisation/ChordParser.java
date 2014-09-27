
/* Parse incoming chord info into Chord objects in correct order

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

import org.ptolemy.machineImprovisation.Chord;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException; 

/**
<p> The Chord parser receives OSC information regarding the Chords and
builds a sequence of chord objects. It both (i) ensures all chords that
exist in the specification have been received and if so, (ii) outputs
a sequence of {@link Chord} objects, in sequence order.

 @author Ilge Akkaya
 @version  
 @Pt.ProposedRating Red (ilgea)
 @Pt.AcceptedRating 
 */
public class ChordParser extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
        *  @param container The container.
        *  @param name The name of this actor
        *  @param trainingSequence The input string that the oracle is built from
        *  @exception IllegalActionException If the actor cannot be contained
        *   by the proposed container.
        *  @exception NameDuplicationException If the container already has an
        *   actor with this name.
        */
    public ChordParser(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        chordName = new TypedIOPort(this, "chordName", true, false);
        chordName.setTypeEquals(BaseType.STRING);
        
        
        chordDuration = new TypedIOPort(this, "chordDuration", true, false);
        chordDuration.setTypeEquals(BaseType.DOUBLE);
        
        chordIndex = new TypedIOPort(this, "chordIndex", true, false);
        chordIndex.setTypeEquals(BaseType.INT);
        
        chords = new TypedIOPort(this, "chords", false, true);
        chords.setTypeEquals(BaseType.OBJECT);
   
        endChord = new TypedIOPort(this, "endChord", true, false);
        endChord.setTypeEquals(BaseType.INT);
        
        _durations = new LinkedList<Double>();

        _chordNames = new LinkedList<String>();
        _orderedChords = new HashMap<Integer, Chord>(); 
        _timestamps = new LinkedList<Integer>();
 
        _terminateChord = false;
        _chordLength = 0;

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////
  
    public TypedIOPort chordName; 
    
    public TypedIOPort chordDuration;
    
    public TypedIOPort chordIndex;
    
    public TypedIOPort endChord;
    
    public TypedIOPort chords; 
    
 

    public void fire() throws IllegalActionException {

        super.fire(); 
        
        if (chordName.hasToken(0)) { 
            String chord = ((StringToken) chordName.get(0)).stringValue();
            _chordNames.add(chord);
        }
        
        if (chordDuration.hasToken(0)) {
            double duration = ((DoubleToken) chordDuration.get(0)).doubleValue();
            _durations.add(duration);
        }
        
        if (chordIndex.hasToken(0)) {
            int ts = ((IntToken) chordIndex.get(0)).intValue();
            _timestamps.add(ts); 
            
        }  
        
        if (endChord.hasToken(0)) {
            _terminateChord = true;
            _chordLength = ((IntToken)endChord.get(0)).intValue();
            _orderedChords.put(_chordLength+1, new Chord(MusicSpecs.TERMINATION_CHORD,0.0));  
        } 
        
        
        if ( _chordNames.size() == _durations.size() 
                && _chordNames.size() == _timestamps.size() 
                && _chordNames.size() >= _chordLength &&
                        _chordLength > 0) {
            // sort
            for ( int i = _timestamps.size()-1; i >= 0; i--) {
                int index = _timestamps.get(i);
                if ( index < _durations.size() && index < _chordNames.size()) {
                    double duration = _durations.get(index);
                    _orderedChords.put(index,new Chord(_chordNames.get(index),duration)); 
                    _timestamps.remove(i); 
                } 
            }
        } 
        if (_terminateChord && _orderedChords.keySet().size() >= _chordLength) { 
            // sort hash map before sending out the tokens
            List keysSoFar = new LinkedList();
            keysSoFar.addAll(_orderedChords.keySet());
            Collections.sort(keysSoFar);
            Iterator iter = keysSoFar.iterator();
            while(iter.hasNext()){
                chords.send(0, new ObjectToken(_orderedChords.get(iter.next())));
            }
            _orderedChords.clear(); 
            _chordNames.clear();
            _durations.clear();
            _timestamps.clear();
            _terminateChord = false;
            _chordLength = 0; 
        }
    }  

    /** 
     * A list of chord duration tokens that are received so far.
     */
    private List<Double> _durations;
    /**
     * An map of indexed chords, labeled by their order in the sequence
     */
    private HashMap<Integer, Chord> _orderedChords;
    /**
     * A list of chord names that are received so far.
     */
    private List<String> _chordNames;
    /**
     * List of chord indices, representing the order of appearance of the chord in the music sequence.
     */
    private List<Integer> _timestamps;
    
    /**
     * Boolean indicating whether the termination symbol for the chord stream has been received.
     */
    private boolean _terminateChord; 
    
    /**
     * Total number of chords in the sequence
     */ 
    private int _chordLength;
}
