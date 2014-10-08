
/* Parses incoming token streams to output a sequence of Note objects
 * 
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

import org.ptolemy.machineImprovisation.MusicSpecs;
import org.ptolemy.machineImprovisation.Note; 

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
 

/**
<p> The NoteParser receives OSC information regarding the Notes and
builds a sequence of chord objects. Outputs
a sequence of {@link Note} objects, in logical timestamp order.

 @author Ilge Akkaya
 @version  
 @Pt.ProposedRating Red (ilgea)
 @Pt.AcceptedRating 
 */
public class NoteParser extends TypedAtomicActor {

    /**
     * Construct an actor with the given container and name.
     * @param container The container.
     * @param name The name of this actor   
     * @throws NameDuplicationException
     * @throws IllegalActionException
     */
    public NoteParser(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        freq = new TypedIOPort(this, "freq", true, false);
        freq.setTypeEquals(BaseType.DOUBLE);
        
        
        dur = new TypedIOPort(this, "dur", true, false);
        dur.setTypeEquals(BaseType.DOUBLE);
        
        timestamp = new TypedIOPort(this, "timestamp", true, false);
        timestamp.setTypeEquals(BaseType.INT);
        

        resetFO = new TypedIOPort(this, "resetFO", true, false);
        resetFO.setTypeEquals(BaseType.DOUBLE);
        
        notes = new TypedIOPort(this, "notes", false, true);
        notes.setTypeEquals(BaseType.OBJECT);
 
        replicationProbability = new TypedIOPort(this, "replicationProbability", false, true);
        replicationProbability.setTypeEquals(BaseType.DOUBLE);
 
        bars = new TypedIOPort(this, "bars", false, true);
        bars.setTypeEquals(BaseType.DOUBLE);
        StringAttribute cardinality = new StringAttribute( bars, "_cardinal");
        cardinality.setExpression("SOUTH");
        
        endTraining = new TypedIOPort(this, "endTraining", true, false);
        endTraining.setTypeEquals(BaseType.INT);
        
        _durations = new LinkedList<Double>();
        _orderedNotes = new HashMap<Integer,Note>(); 
        _notes = new LinkedList<Double>();
        _timestamps = new LinkedList<Integer>();
 
        _terminate = false;
        _terminateLength = 0;

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////
  
    /**
     * Sequence of note tokens
     */
    public TypedIOPort notes; 
    
    /**
     * The time stamp tokens
     */
    public TypedIOPort timestamp;
    
    /**
     * Indicator that the note sequence has terminated
     */
    public TypedIOPort endTraining;
    
    /**
     * Indicator that a new sequence has started
     */
    public TypedIOPort resetFO;
    
    /**
     * The replication probability of the factor oracle
     */
    public TypedIOPort replicationProbability;
    
    /**
     * Number of bars in training melody
     */
    public TypedIOPort bars; 
    
    /**
     * Note frequency
     */
    public TypedIOPort freq;
    
    /**
     * Note durations
     */
    public TypedIOPort dur; 
    
 

    public void fire() throws IllegalActionException {

        super.fire(); 
        
        if (resetFO.hasToken(0)) {
            _replicationProbability = ((DoubleToken)resetFO.get(0)).doubleValue();
        }

        if (freq.hasToken(0)) { 
            double pitch = ((DoubleToken) freq.get(0)).doubleValue();
            _notes.add(pitch);
        }
        
        if (dur.hasToken(0)) {
            double duration = ((DoubleToken) dur.get(0)).doubleValue();
            _durations.add(duration);
        }
        
        if (timestamp.hasToken(0)) {
            int ts = ((IntToken) timestamp.get(0)).intValue();
            _timestamps.add(ts); 
            
        } 
        
        if (endTraining.hasToken(0)) {
            _terminate = true;
            _terminateLength = ((IntToken) endTraining.get(0)).intValue();
            // insert a termination note at the end of the list
            _orderedNotes.put(_terminateLength+1, 
                    new Note(MusicSpecs.TERMINATION_NOTE_SYMBOL,-100)); 
        } 
        
        if ( _notes.size() == _durations.size() 
                && _notes.size() == _timestamps.size() 
                && _notes.size() >= _terminateLength &&
                _terminateLength > 0) {
            // sort
            for ( int i = _timestamps.size()-1 ; i >=0; i--) {
                int index = _timestamps.get(i);
                if ( index < _durations.size() && index < _notes.size()) {
                    double duration = _durations.get(index);
                    _orderedNotes.put(index, 
                            new Note(MusicSpecs.translateKeyToLetterNote(_notes.get(index), true),
                                    duration));
                    _nBeats += duration; 
                    _timestamps.remove(i); 
                } 
            }
        } 
        
         
        if (_terminate && _orderedNotes.keySet().size() >= _terminateLength) {
             
            List keysSoFar = new LinkedList();
            keysSoFar.addAll(_orderedNotes.keySet());
            Collections.sort(keysSoFar);
            Iterator iter = keysSoFar.iterator();
            while(iter.hasNext()){
                notes.send(0, new ObjectToken((Note)_orderedNotes.get(iter.next())));
            }
            _orderedNotes.clear();  
            _notes.clear();
            _durations.clear();
            _timestamps.clear();
            _terminate = false;
            _terminateLength = 0;
            
            bars.send(0, new DoubleToken(_nBeats));
            _nBeats = 0;
            if(_replicationProbability <= 0){
                _replicationProbability= 0.1;
            }
            replicationProbability.send(0, new DoubleToken(_replicationProbability)); 
        }  
    }  
     
    private List<Double> _durations;
    private HashMap<Integer, Note> _orderedNotes; 
    private List<Double> _notes;
    private List<Integer> _timestamps;
    private boolean _terminate; 
    private int _terminateLength; 
    private double _replicationProbability = 0.8;
    private double _nBeats = 0.0;
}
