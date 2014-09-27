/* Class that defines the musical Chord object

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

import java.util.HashSet; 
import java.util.List;
import java.util.Set;

import ptolemy.kernel.util.IllegalActionException;
/**
 * A class that defines a Chord object. A chord is either given by
 * a set of notes that it contains, or a name. In the latter case, 
 * the notes contained by the chord can be retrieved by a local library,
 * currently defined in {@link MusicSpecs}.
 @author Ilge Akkaya
 @version  $Id$
 @since Ptolemy II 10.1
 @Pt.ProposedRating Yellow (ilgea)
 @Pt.AcceptedRating 
 */
public class Chord {
    /**
     * Construct a chord with a name and a duration.
     * @param name Chord name
     * @param duration Chord duration 
     */
    public Chord(String name, double duration) {
        _name = name;
        _duration = duration;
        _chordTones = new HashSet<Note>();
        this.setChordTones(name);
    }
    /**
     * Construct a chord by a set of note objects
     * @param chordTones a Set of notes that are a part of the specified Chord.
     */
    public Chord(Set<Note> chordTones) { 
        _chordTones = new HashSet<Note>();
        for (Note n : chordTones) {
            _chordTones.add(n);
        }
    }
    /**
     * Construct an empty Chord with no chord tones.
     */
    public Chord(){
        _chordTones = new HashSet<Note>();
    }
    /**
     * Add a note to this chord.
     * @param n Note to be added 
     */
    public void addNote(Note n) {
        _chordTones.add(n);
    }
    /**
     * Get notes contained by this Chord.
     * @return the Set of chord tones contained by this chord
     */
    public Set<Note> getNotes() {
        return this._chordTones;
    }
    /**
     * Get the duration of this Chord.
     * @return duration of chord
     */
    public double getDuration() {
        return this._duration;
    }
    /** 
     * Get name of this Chord.
     * @return Chord name
     */
    public String getName() {
        return this._name;
    }
    /** 
     * Set the notes contained by this chord by a dictionary lookup.
     * @param chord The chord name
     * @throws IllegalActionException 
     */
    public void setChordTones() throws IllegalActionException{
        
        if (this._name == null) {
            throw new IllegalActionException("Chord name is undefined.");
        } else {
            List<String> chordNotes = MusicSpecs.getChordPitches(this._name, true);
            if(chordNotes != null){
                for( int i = 0; i< chordNotes.size(); i++){
                    _chordTones.add( new Note((String)chordNotes.get(i)));
                }
            } 
        }
    }
    
    /** 
     * Set the notes contained by this chord, by name reference.
     * @param chord The chord name
     */
    public void setChordTones(String chord){
        
        List<String> chordNotes = MusicSpecs.getChordPitches(chord, true);
        if(chordNotes != null){
            for( int i = 0; i< chordNotes.size(); i++){
                _chordTones.add( new Note((String)chordNotes.get(i)));
            }
        } 
    }
    /** The set of notes that define this chord object. */
    private final Set<Note> _chordTones;
    /** Duration of this chord in beats. 
     */
    private double _duration = 0.0;
    
    /** Name of chord. */
    private String _name;
}
