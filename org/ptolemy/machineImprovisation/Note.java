/* A Note object

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

import java.util.Set; 

/** Note values: 1/64,1/32,1/16,1/8,1/2,1,2,4,8,16,32**/
public class Note{
    
    public Note(String name){
        this._name = name;
    }
    
    public Note(String name, double duration){
        this._name = name;
        this._noteValue = duration;
    }
    
    public Note(String name, double duration, int octave, Chord c){ 
        this._noteValue = duration;
        this._octave = octave;
        this._name = name;
        this._numericNoteIndex = _getNumericIndexForName(name);
        this._chord = c;
    }
    public Note(int index, double duration, int octave, Chord c){
        this._numericNoteIndex = index;
        this._noteValue = duration;
        this._name = _getNameFromIndex( index);
        this._octave = octave;
        this._chord = c;
    }
    public double getDuration(){
        return _noteValue;
    }
    public String getName(){
        return _name;
    }
    public int getCompleteIndex(){
        return _getCompleteKeyIndex(_numericNoteIndex, _octave);
    }
    public int getKeyIndex(){
        return _numericNoteIndex;
    }
    
    public Chord getChord(){
        return _chord;
    }
    
    public Set<Note> getChordTones(){
        return _chord.getNotes();
    }
    
    private int _getNumericIndexForName( String noteName){
        
        int noteIndex = -2;
        char noteRoot = noteName.charAt(0);
        char noteKeyValue = ' ';
        if(noteName.length() == 2){
            noteKeyValue = noteName.charAt(1);        
        }
    
        switch( noteRoot){
                case 'A' : noteIndex = 9;  break;
                case 'B' : noteIndex = 11; break;
                case 'C' : noteIndex = 0;  break;
                case 'D' : noteIndex = 2;  break;
                case 'E' : noteIndex = 4;  break;
                case 'F' : noteIndex = 5;  break;
                case 'G' : noteIndex = 7;  break;
                case 'R' : noteIndex = -1; break; //rest
                default: noteIndex = -2; break;
            }
        
        // add accidentals
        if(noteKeyValue == '#'){
            noteIndex += 1;
        }
        return noteIndex;
    }
    private String _getNameFromIndex( int noteIndex){
        
        int note = -1; // default is rest
        if ( noteIndex > 0){
            note = noteIndex % 12;
        }
        
        String noteName = "";
        switch(note){
            case -1: noteName = "R"; break; //rest
            case 0 : noteName = "C";  break;
            case 1 : noteName = "C#"; break;
            case 2 : noteName = "D";  break;
            case 3 : noteName = "D#"; break;
            case 4 : noteName = "E";  break;
            case 5 : noteName = "F";  break;
            case 6 : noteName = "F#"; break;
            case 7 : noteName = "G";  break;
            case 8 : noteName = "G#"; break;
            case 9 : noteName = "A";  break;
            case 10: noteName = "A#"; break;
            case 11: noteName = "B";  break;
            default: break;
       }
        return noteName;
    
    }
    private int _getCompleteKeyIndex(int noteIndex, int octave){
        return noteIndex + 12*(octave+1);
    }
    
    public boolean isRest(){
        return _name.startsWith("R");
    }

    private int _numericNoteIndex;
    private int _octave;
    private String _name;
    private double _noteValue;
    private Chord _chord; // the chord to which the note belongs to.
}
