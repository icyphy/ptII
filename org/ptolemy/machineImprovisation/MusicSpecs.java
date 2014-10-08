/* Music Specifications

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

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
/** A static class that contains musical specifications

 @author Ilge Akkaya
 @version  $Id$
 @since Ptolemy II 10.1
 @Pt.ProposedRating Red (ilgea)
 @Pt.AcceptedRating 
 */
public class MusicSpecs {

    /**
     * Get the chord tones contained by the given chords.
     * @param chords The list of chords
     * @return a map of pitches contained by the specified list of chords
     */
    public static HashMap getChordTones(List<String> chords) {
        HashMap chordMap = new HashMap(); 
        for (int i = 0; i < chords.size(); i++) {
            String chordKey = chords.get(i);
            chordMap.put(chordKey, getChordPitches(chords.get(i), true));
        } 
        return chordMap;
    }
    
    /**
     * Get a list of notes contained by the chord
     * @param chord The chord name
     * @param useScale A boolean specifying whether scale notes should be included
     * @return a List of notes
     */
    public static List getChordPitches(String chord, boolean useScale) {
        
        
        List notesInChord = new LinkedList<String>();

        
        if (chord.length() <=1) {
            // invalid chord name
            return null;
        }

        String rootNote = chord.charAt(0)+"";

        String spec = chord.substring(1);
        // parsing sharp/flat notes
        char trailingSymbol = spec.charAt(0);
        if (trailingSymbol == '#' || trailingSymbol == 'b') {
            rootNote+=trailingSymbol;
            spec = spec.substring(1); 
        }      
        
        int baseInteger = translateLetterToKeyIndex(rootNote);
        if (baseInteger >= 0) {
            List<Integer> noteIndices = new LinkedList<Integer>(); 

            if ( !useScale) {
                noteIndices.add(0); // the root note will always be in the chord 
                 
                if (spec.equals("maj") || spec.equals("M") || spec.equals("")) {
                    noteIndices.addAll(Arrays.asList(4,7));
                } else if (spec.equals("min") || spec.equals("m")) {
                    noteIndices.addAll(Arrays.asList(3,7));           
                } else if (spec.equals("aug") || spec.equals("+")) {
                    noteIndices.addAll(Arrays.asList(4,8));    
                } else if (spec.equals("dim") || spec.equals("o")) {
                    noteIndices.addAll(Arrays.asList(3,6));      
                } else if (spec.equals("7#5#9" )) {
                    noteIndices.addAll(Arrays.asList(4,8,11));
                } else if (spec.equals("6") || spec.equals("6/B")) {
                    noteIndices.addAll(Arrays.asList(0,4,7,9)); 
                } else if (spec.equals("m6") || spec.equals("min6")) {
                    noteIndices.addAll(Arrays.asList(3,5,7,9));
                } else if (spec.equals("7") || spec.equals("dom7")) {
                    noteIndices.addAll(Arrays.asList(4,7,10));
                } else if (spec.equals("M7") || spec.equals("maj7")) {
                    noteIndices.addAll(Arrays.asList(4,7,11));
                } else if (spec.equals("mM7") || spec.equals("minmaj7")) {
                    noteIndices.addAll(Arrays.asList(3,7,10)); 
                } else if (spec.equals("m7") || spec.equals("min7")) {
                    noteIndices.addAll(Arrays.asList(3,7,10));
                } else if (spec.equals("+M7") || spec.equals("augmaj7")) {
                    noteIndices.addAll(Arrays.asList(4,8,11));
                } else if (spec.equals("+7") || spec.equals("aug7") || spec.equals("7+")) {
                    noteIndices.addAll(Arrays.asList(4,8,10));
                } else if (spec.equals("o7") ||spec.equals("dim7")) {
                    noteIndices.addAll(Arrays.asList(3,6,9));   
                } else if (spec.equals("7b5") ||spec.equals("7dim5")) {
                    noteIndices.addAll(Arrays.asList(4,6,10));   
                } else if (spec.equals("7b9") ||spec.equals("7dim9")) {
                    noteIndices.addAll(Arrays.asList(1,4,7));      
                } else if (spec.equals("M9") || spec.equals("maj9")) {
                    noteIndices.addAll(Arrays.asList(4,7,11,2)); 
                } else if (spec.equals("9") || spec.equals("dom9")) {
                    noteIndices.addAll(Arrays.asList(4,7,10,2)); 
                } else if (spec.equals("mM9") || spec.equals("minmaj9")) {
                    noteIndices.addAll(Arrays.asList(3,7,11,2)); 
                } else if (spec.equals("m9") || spec.equals("min9")) {
                    noteIndices.addAll(Arrays.asList(3,7,10,2)); 
                } else if (spec.equals("+M9") || spec.equals("augmaj9")) {
                    noteIndices.addAll(Arrays.asList(4,8,11,2));
                } else if (spec.equals("+9") || spec.equals("aug9") || spec.equals("9+")) {
                    noteIndices.addAll(Arrays.asList(4,8,10,2));
                } else if (spec.equals("h9")) {
                    noteIndices.addAll(Arrays.asList(3,6,10,2)); 
                } else if (spec.equals("hmin9")) {
                    noteIndices.addAll(Arrays.asList(3,6,8,2)); 
                } else if (spec.equals("o9") ||spec.equals("dim9")) {
                    noteIndices.addAll(Arrays.asList(3,6,9,2));     
                } else if (spec.equals("ob9") ||spec.equals("dimb9")) {
                    noteIndices.addAll(Arrays.asList(3,6,9,1));    
                } else if (spec.equals("M11") || spec.equals("maj11")) {
                    noteIndices.addAll(Arrays.asList(0,4,7,11,2,5));
                } else if (spec.equals("11") || spec.equals("dom11")) {
                    noteIndices.addAll(Arrays.asList(0,4,7,10,2,5));
                } else if (spec.equals("mM11") || spec.equals("minmaj11")) {
                    noteIndices.addAll(Arrays.asList(0,3,7,11,2,5));
                } else if (spec.equals("m11") || spec.equals("min11")) {
                    noteIndices.addAll(Arrays.asList(0,3,7,10,2,5));
                } else if (spec.equals("+M11") || spec.equals("augmaj11")) {
                    noteIndices.addAll(Arrays.asList(0,4,8,11,2,5));
                } else if (spec.equals("+11") || spec.equals("aug11") || spec.equals("11+")) {
                    noteIndices.addAll(Arrays.asList(0,4,8,10,2,5));
                } else if (spec.equals("h11")) {
                    noteIndices.addAll(Arrays.asList(0,3,6,10,2,5));
                } else if (spec.equals("o11") ||spec.equals("dim11")) {
                    noteIndices.addAll(Arrays.asList(3,6,9,2,4)); 
                } else if (spec.equals("M13") || spec.equals("maj13")) {
                    noteIndices.addAll(Arrays.asList(0,4,7,11,2,5,9));
                } else if (spec.equals("13") || spec.equals("dom13")) {
                    noteIndices.addAll(Arrays.asList(4,7,10,2,5,9));
                } else if (spec.equals("13b9") || spec.equals("dom13b9")) {
                    noteIndices.addAll(Arrays.asList(4,7,10,1,9));  
                } else if (spec.equals("mM13") || spec.equals("minmaj13")) {
                    noteIndices.addAll(Arrays.asList(3,7,11,2,5,9));
                } else if (spec.equals("m13") || spec.equals("min13")) {
                    noteIndices.addAll(Arrays.asList(0,3,7,10,2,5,9));
                } else if (spec.equals("+M13") || spec.equals("augmaj13")) {
                    noteIndices.addAll(Arrays.asList(0,4,8,11,2,5,9));
                } else if (spec.equals("+13") || spec.equals("aug13") || spec.equals("13+")) {
                    noteIndices.addAll(Arrays.asList(0,4,8,10,2,5,9));
                } else if (spec.equals("h13")) {
                    noteIndices.addAll(Arrays.asList(0,3,6,10,2,5,9));
                } else if (spec.equalsIgnoreCase("pentatonicBlues")) {
                    noteIndices.addAll(Arrays.asList(0,3,6,8,10,1));
                } 
            } else { //also add scale notes
                List<Integer> mixolydianDom = Arrays.asList(0,2,4,7,9,10);
                List<Integer> major = Arrays.asList(0,2,4,7,9,11);
                List<Integer> majorAvoidRoot = Arrays.asList(2,4,7,9,11);
                List<Integer> melodicMinor = Arrays.asList(0,2,3,5,7,9,11);
                List<Integer> mmUpm3 = Arrays.asList(0,3,6,10,5,8,0,2); //Melodic minor, up minor third
                List<Integer> mmUpHs = Arrays.asList(1,3,4,6,8,10,0); // Melodic minor, up half step
                List<Integer> dorian = Arrays.asList(0,2,3,5,7,9,10);
                List<Integer> diminished = Arrays.asList(0,2,3,5,6,8,9,11,0);
                List<Integer> lydian = Arrays.asList(0,2,4,6,7,9,11);
                List<Integer> lydianDom = Arrays.asList(0,2,4,6,7,9,10);
                List<Integer> mixolydian = Arrays.asList(0,2,4,5,7,9,10);
                List<Integer> diminishedUpHs = Arrays.asList(0,1,3,4,6,7,9,10);

                if (spec.equals("maj") || spec.equals("M") || spec.equals("")) {
                    noteIndices.addAll(major);
                } else if (spec.equals("min") || spec.equals("m")) {
                    noteIndices.addAll(melodicMinor);               
                } else if (spec.equals("aug") || spec.equals("+")) {
                    noteIndices.addAll(Arrays.asList(0,4,8));     
                } else if (spec.equals("dim") || spec.equals("o")) {
                    noteIndices.addAll(Arrays.asList(0,3,6));      
                } else if (spec.equals("7#5#9" )) {
                    noteIndices.addAll(mmUpHs); 
                } else if (spec.equals("6") || spec.equals("6/B")) {
                    noteIndices.addAll(Arrays.asList(0,4,7,9)); 
                } else if (spec.equals("m6") || spec.equals("min6")) {
                    noteIndices.addAll(Arrays.asList(0,3,5,7,9));
                } else if (spec.equals("7") || spec.equals("dom7")) {
                    noteIndices.addAll(mixolydianDom); 
                } else if (spec.equals("M7") || spec.equals("maj7")) {
                    noteIndices.addAll(majorAvoidRoot); 
                } else if (spec.equals("mM7") || spec.equals("minmaj7")) {
                    noteIndices.addAll(melodicMinor); 
                } else if (spec.equals("m7") || spec.equals("min7")) {
                    noteIndices.addAll(dorian);
                } else if (spec.equals("+M7") || spec.equals("augmaj7")) {
                    noteIndices.addAll(Arrays.asList(0,4,8,11));
                } else if (spec.equals("+7") || spec.equals("aug7") || spec.equals("7+")) {
                    noteIndices.addAll(Arrays.asList(0,4,8,10));
                } else if (spec.equals("m7b5") ||spec.equals("h7")) {
                    noteIndices.addAll(mmUpm3);
                } else if (spec.equals("o7") ||spec.equals("dim7")) {
                    noteIndices.addAll(diminished);    
                } else if (spec.equals("7b5") ||spec.equals("7dim5")) {
                    noteIndices.addAll(Arrays.asList(0,4,6,10));   
                } else if (spec.equals("7b9") ||spec.equals("7dim9")) {
                    noteIndices.addAll(diminishedUpHs);   
                } else if (spec.equals("M9") || spec.equals("maj9")) {
                    noteIndices.addAll(Arrays.asList(0,4,7,11,2)); 
                } else if (spec.equals("9") || spec.equals("dom9")) {
                    noteIndices.addAll(mixolydianDom); 
                } else if (spec.equals("mM9") || spec.equals("minmaj9")) {
                    noteIndices.addAll(Arrays.asList(0,3,7,11,2)); 
                } else if (spec.equals("m9") || spec.equals("min9")) {
                    noteIndices.addAll(melodicMinor);
                } else if (spec.equals("+M9") || spec.equals("augmaj9")) {
                    noteIndices.addAll(Arrays.asList(0,4,8,11,2));
                } else if (spec.equals("+9") || spec.equals("aug9") || spec.equals("9+")) {
                    noteIndices.addAll(Arrays.asList(0,4,8,10,2));
                } else if (spec.equals("h9")) {
                    noteIndices.addAll(Arrays.asList(0,3,6,10,2)); 
                } else if (spec.equals("hmin9")) {
                    noteIndices.addAll(Arrays.asList(0,3,6,8,2)); 
                } else if (spec.equals("o9") ||spec.equals("dim9")) {
                    noteIndices.addAll(diminished);    
                } else if (spec.equals("ob9") ||spec.equals("dimb9")) {
                    noteIndices.addAll(Arrays.asList(0,3,6,9,1));    
                } else if (spec.equals("M11") || spec.equals("maj11")) {
                    noteIndices.addAll(Arrays.asList(0,4,7,11,2,5));
                } else if (spec.equals("11") || spec.equals("dom11")) {
                    noteIndices.addAll(Arrays.asList(0,4,7,10,2,5));
                } else if (spec.equals("mM11") || spec.equals("minmaj11")) {
                    noteIndices.addAll(Arrays.asList(0,3,7,11,2,5));
                } else if (spec.equals("m11") || spec.equals("min11")) {
                    noteIndices.addAll(Arrays.asList(0,3,7,10,2,5));
                } else if (spec.equals("+M11") || spec.equals("augmaj11")) {
                    noteIndices.addAll(Arrays.asList(0,4,8,11,2,5));
                } else if (spec.equals("+11") || spec.equals("aug11") || spec.equals("11+")) {
                    noteIndices.addAll(Arrays.asList(0,4,8,10,2,5));
                } else if (spec.equals("h11")) {
                    noteIndices.addAll(Arrays.asList(0,3,6,10,2,5));
                } else if (spec.equals("o11") ||spec.equals("dim11")) {
                    noteIndices.addAll(diminished); noteIndices.add(4);   
                } else if (spec.equals("M13") || spec.equals("maj13")) {
                    noteIndices.addAll(Arrays.asList(0,4,7,11,2,5,9));
                } else if (spec.equals("13") || spec.equals("dom13")) {
                    noteIndices.addAll(mixolydianDom);
                } else if (spec.equals("13b9") || spec.equals("dom13b9")) {
                    noteIndices.addAll(Arrays.asList(0,4,7,10,1,9));  
                } else if (spec.equals("mM13") || spec.equals("minmaj13")) {
                    noteIndices.addAll(Arrays.asList(0,3,7,11,2,5,9));
                } else if (spec.equals("m13") || spec.equals("min13")) {
                    noteIndices.addAll(Arrays.asList(0,3,7,10,2,5,9));
                } else if (spec.equals("+M13") || spec.equals("augmaj13")) {
                    noteIndices.addAll(Arrays.asList(0,4,8,11,2,5,9));
                } else if (spec.equals("+13") || spec.equals("aug13") || spec.equals("13+")) {
                    noteIndices.addAll(Arrays.asList(0,4,8,10,2,5,9));
                } else if (spec.equals("h13")) {
                    noteIndices.addAll(Arrays.asList(0,3,6,10,2,5,9));
                } else if (spec.equalsIgnoreCase("pentatonicBlues")) {
                    noteIndices.addAll(Arrays.asList(0,3,6,8,10,1));
                }  
            }

            for (int i = 0; i< noteIndices.size(); i++) {
               
                notesInChord.add(translateKeyToLetterNote(baseInteger + (int)noteIndices.get(i),
                        false));
            }  
        }
        return notesInChord; 
    }

    /**
     * Translate a MIDI key to its equivalent letter note representation
     * @param key MIDI key index
     * @param addOctave boolean -- if true indicates that octave information should be output
     * @return letter note representation of note
     */
    public static String translateKeyToLetterNote(double key, boolean addOctave) { 

        if ( key == TERMINATION_NOTE_KEY) {
            return TERMINATION_NOTE_SYMBOL; //termination
        } else if (key < 0) {
            return REST_SYMBOL;
        }
        int keyIndex = (int)key;
        int note = keyIndex % 12;
        String noteName = "";
        switch (note) {
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
        }
        if (addOctave) {
            int octave = (keyIndex - (keyIndex % 12))/12 - 1;
            noteName = noteName.concat(octave+"");
        }
        return noteName;

    }

    /**
     * Translate a letter note to MIDI key
     * @param note a letter note
     * @return the MIDI key index
     */
    public static int translateNoteToKey(String note) {
        int octave =  -1;
        String letterPart = note;
        if (!note.startsWith(REST_SYMBOL)) {
            octave = Integer.parseInt(note.charAt(note.length()-1)+"");
            letterPart = note.substring(0,note.length());
        } 
        int noteIndex = 0;
        int noteKeyValue = 0;
 
        switch (letterPart.charAt(0)) {
        case 'A' : noteIndex = 9;  break;
        case 'B' : noteIndex = 11; break;
        case 'C' : noteIndex = 0;  break;
        case 'D' : noteIndex = 2;  break;
        case 'E' : noteIndex = 4;  break;
        case 'F' : noteIndex = 5;  break;
        case 'G' : noteIndex = 7;  break; 
        }
        // FIXME: TEST!
        if (letterPart.length() >1) {
            if (letterPart.charAt(1) == '#') {
                noteIndex += 1;
            } else if (letterPart.charAt(1) == 'b') {
                noteIndex -= 1;
            }
        }

        if (noteIndex >= 0) {      
            noteKeyValue = (octave+1)*12 + noteIndex;
        }
        else {
            noteKeyValue = -1;
        } 
        return noteKeyValue; 
    }

    /**
     * Translate a letter note to MIDI key
     * @param note a letter note
     * @return the MIDI key index in base 12
     */
    public static int translateLetterToKeyIndex(String keyLetter) {  
        String letterPart = keyLetter.substring(0,keyLetter.length()); 
        int noteIndex = 0;    
        char note = letterPart.charAt(0);
                 switch (note) {
                 case 'A' : noteIndex = 9;  break;
                 case 'B' : noteIndex = 11; break;
                 case 'C' : noteIndex = 0;  break;
                 case 'D' : noteIndex = 2;  break;
                 case 'E' : noteIndex = 4;  break;
                 case 'F' : noteIndex = 5;  break;
                 case 'G' : noteIndex = 7;  break;
                 default:   noteIndex = -1; break;
                 } 
        if (letterPart.length() >1) {
            if (letterPart.charAt(1) == '#') {
                noteIndex += 1;
            } else if (letterPart.charAt(1) == 'b') {
                noteIndex -= 1;
            }
        }  
        return noteIndex; 
    } 
    /**
     * Symbol for the termination chord
     */
    public static final String TERMINATION_CHORD = "T";
    /**
     * Symbol for the termination note
     */
    public static final String TERMINATION_NOTE_SYMBOL = "T";
    /**
     * Rest symbol
     */
    public static final String REST_SYMBOL = "R";
    /**
     * MIDI key for the termination note
     */
    public static final int TERMINATION_NOTE_KEY = -100; 
}
