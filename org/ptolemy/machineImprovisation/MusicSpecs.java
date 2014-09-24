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

public class MusicSpecs {

    public static HashMap getChordTones(List<String> chords) {
        HashMap chordMap = new HashMap(); 
        for (int i = 0; i < chords.size(); i++) {
            String chordKey = chords.get(i);
            chordMap.put(chordKey, getChordPitches(chords.get(i), true));
        } 
        return chordMap;
    }
    public static List getChordPitches(String chord, boolean useScale) {
        List notesInChord = new LinkedList<String>();

        if (chord.length() <=1) {
            return null;
        }

        String rootNote = chord.charAt(0)+"";

        String spec = chord.substring(1);
        // if we have a sharp notation for the note, take it out of the spec
        if (spec.charAt(0) == '#') {
            rootNote+='#';
            spec = spec.substring(1); 
        } else if (spec.charAt(0) == 'b') {
            rootNote+='b';
            spec = spec.substring(1);
        }       
        int baseInteger = translateLetterToKeyIndex(rootNote+"");
        if (baseInteger >= 0) {
            List noteIndices = new LinkedList(); 

            if ( !useScale) {
                noteIndices.add(0); // the root note will always be in the chord
                System.err.println("MusicSpecs: commented out non-1.6 code.");
//                 switch (spec) {
//                 case "maj" : case "M": case "": noteIndices.addAll(Arrays.asList(4,7)); break;
//                 case "min" : case "m"         : noteIndices.addAll(Arrays.asList(3,7)); break;
//                 case "aug" : case "+"         : noteIndices.addAll(Arrays.asList(4,8)); break;
//                 case "dim" : case "o"         : noteIndices.addAll(Arrays.asList(3,6)); break;
//                 case "7#5#9"                  : noteIndices.addAll(Arrays.asList(4,8,11)); break;
//                 case "6"   : case "6/B"       :                   noteIndices.addAll(Arrays.asList(0,4,7,9)); break;
//                 case "m6" : case "min6"         : noteIndices.addAll(Arrays.asList(3,5,7,9)); break;
//                 case "7"   : case "dom7"      : noteIndices.add(4); noteIndices.add(7); noteIndices.add(10); break;
//                 case "M7"  : case "maj7"      : noteIndices.add(4); noteIndices.add(7); noteIndices.add(11); break;
//                 case "mM7" : case "minmaj7"   : noteIndices.add(3); noteIndices.add(7); noteIndices.add(11); break;
//                 case "m7"  : case "min7"      : noteIndices.add(3); noteIndices.add(7); noteIndices.add(10); break;
//                 case "+M7" : case "augmaj7"   : noteIndices.add(4); noteIndices.add(8); noteIndices.add(11); break;
//                 case "+7"  : case "aug7": case "7+"      : noteIndices.add(4); noteIndices.add(8); noteIndices.add(10); break;
//                 case "m7b5": case "h7"        : noteIndices.add(3); noteIndices.add(6); noteIndices.add(10); break;
//                 case "o7"  : case "dim7"      : noteIndices.add(3); noteIndices.add(6); noteIndices.add(9);  break;
//                 case "7b5" : case "7dim5"     : noteIndices.add(4); noteIndices.add(6); noteIndices.add(10); break;
//                 case "7b9" : case "7dim9"     : noteIndices.add(1); noteIndices.add(4); noteIndices.add(7); noteIndices.add(10); break;
//                 case "M9"  : case "maj9"      : noteIndices.add(4); noteIndices.add(7); noteIndices.add(11); noteIndices.add(2); break;
//                 case "9"   : case "dom9"      : noteIndices.add(4); noteIndices.add(7); noteIndices.add(10); noteIndices.add(2); break;
//                 case "mM9" : case "minmaj9"   : noteIndices.add(3); noteIndices.add(7); noteIndices.add(11); noteIndices.add(2); break;
//                 case "m9"  : case "min9"      : noteIndices.add(3); noteIndices.add(7); noteIndices.add(10); noteIndices.add(2); break;
//                 case "+M9" : case "augmaj9"   : noteIndices.add(4); noteIndices.add(8); noteIndices.add(11); noteIndices.add(2); break;
//                 case "+9"  : case "aug9": case "9+"       : noteIndices.add(4); noteIndices.add(8); noteIndices.add(10); noteIndices.add(2); break;
//                 case "h9"                     : noteIndices.add(3); noteIndices.add(6); noteIndices.add(10); noteIndices.add(2); break;
//                 case "hmin9"                  : noteIndices.add(3); noteIndices.add(6); noteIndices.add(9);  noteIndices.add(2); break;   
//                 case "o9"  : case "dim9"      : noteIndices.add(3); noteIndices.add(6); noteIndices.add(9);  noteIndices.add(2); break;
//                 case "ob9" : case "dimb9"     : noteIndices.add(3); noteIndices.add(6); noteIndices.add(9);  noteIndices.add(1); break;
//                 case "M11" : case "maj11"     : noteIndices.add(4); noteIndices.add(7); noteIndices.add(11); noteIndices.add(2); noteIndices.add(5); break;
//                 case "11"  : case "dom11"     : noteIndices.add(4); noteIndices.add(7); noteIndices.add(10); noteIndices.add(2); noteIndices.add(5); break;
//                 case "mM11": case "minmaj11"  : noteIndices.add(3); noteIndices.add(7); noteIndices.add(11); noteIndices.add(2); noteIndices.add(5); break;
//                 case "m11" : case "min11"     : noteIndices.add(3); noteIndices.add(7); noteIndices.add(10); noteIndices.add(2); noteIndices.add(5); break;
//                 case "+M11": case "augmaj11"  : noteIndices.add(4); noteIndices.add(8); noteIndices.add(11); noteIndices.add(2); noteIndices.add(5); break;
//                 case "+11" : case "aug11"   :case "11+"  : noteIndices.add(4); noteIndices.add(8); noteIndices.add(10); noteIndices.add(2); noteIndices.add(5); break;
//                 case "h11"                    : noteIndices.add(3); noteIndices.add(6); noteIndices.add(10); noteIndices.add(2); noteIndices.add(5); break;
//                 case "o11" : case "dim11"     : noteIndices.add(3); noteIndices.add(6); noteIndices.add(9);  noteIndices.add(2); noteIndices.add(4); break;
//                 case "M13" : case "maj13"     : noteIndices.add(4); noteIndices.add(7); noteIndices.add(11); noteIndices.add(2); noteIndices.add(5); noteIndices.add(9); break;
//                 case "13"  : case "dom13"     : noteIndices.add(4); noteIndices.add(7); noteIndices.add(10); noteIndices.add(2); noteIndices.add(5); noteIndices.add(9); break;
//                 case "13b9": case "dom13b9"   : noteIndices.addAll(Arrays.asList(4,7,10,1,9)); break;
//                 case "mM13": case "minmaj13"  : noteIndices.addAll(Arrays.asList(3,7,11,2,5,9)); break;
//                 case "m13" : case "min13"     : noteIndices.addAll(Arrays.asList(3,7,10,2,5,9)); break;
//                 case "+M13": case "augmaj13"  : noteIndices.addAll(Arrays.asList(4,8,11,2,5,9)); break;
//                 case "+13" : case "aug13"  : case "13+"   : noteIndices.addAll(Arrays.asList(4,8,10,2,5,9)); break;
//                 case "h13"                    : noteIndices.addAll(Arrays.asList(3,6,10,2,5,9)); break;
//                 case "pentatonicBlues"        : noteIndices.addAll(Arrays.asList(3,6,8,10,1)); break; // Ebb Gb Ab Bb Db Eb
//                 default: noteIndices.add(0);
//                 }
//             } else { //also add scale notes
//                 List mixolydianDom = Arrays.asList(0,2,4,7,9,10);
//                 List major = Arrays.asList(0,2,4,7,9,11);
//                 List majorAvoidRoot = Arrays.asList(2,4,7,9,11);
//                 List melodicMinor = Arrays.asList(0,2,3,5,7,9,11);
//                 List mmUpm3 = Arrays.asList(0,3,6,10,5,8,0,2); //Melodic minor, up minor third
//                 List mmUpHs = Arrays.asList(1,3,4,6,8,10,0); // Melodic minor, up half step
//                 List dorian = Arrays.asList(0,2,3,5,7,9,10);
//                 List diminished = Arrays.asList(0,2,3,5,6,8,9,11,0);
//                 List lydian = Arrays.asList(0,2,4,6,7,9,11);
//                 List lydianDom = Arrays.asList(0,2,4,6,7,9,10);
//                 List mixolydian = Arrays.asList(0,2,4,5,7,9,10);
//                 List diminishedUpHs = Arrays.asList(0,1,3,4,6,7,9,10);
//                 switch (spec) {
//                 case "maj" : case "M": case "": noteIndices.addAll(major);                      break;  
//                 case "min" : case "m"         : noteIndices.addAll(melodicMinor);               break;  
//                 case "aug" : case "+"         : noteIndices.addAll(Arrays.asList(0,4,8));       break;
//                 case "dim" : case "o"         : noteIndices.addAll(Arrays.asList(0,3,6));       break;
//                 case "7#5#9"                  : noteIndices.addAll(mmUpHs);                     break;
//                 case "6"   : case "6/B"       :            noteIndices.addAll(Arrays.asList(0,4,7,9));     break;
//                 case "m6"  : case "min6"       : noteIndices.addAll(Arrays.asList(0,3,5,7,9)); break;
//                 case "7"   : case "dom7"      : noteIndices.addAll(mixolydianDom);              break;  
//                 case "M7"  : case "maj7"      : noteIndices.addAll(majorAvoidRoot);             break; //Major without root
//                 case "mM7" : case "minmaj7"   : noteIndices.addAll(melodicMinor);               break; //Melodic Minor
//                 case "m7"  : case "min7"      : noteIndices.addAll(dorian);                     break; //Dorian
//                 case "+M7" : case "augmaj7"   : noteIndices.addAll(Arrays.asList(0,4,8,11));    break;
//                 case "+7"  : case "aug7"   :case "7+"   : noteIndices.addAll(Arrays.asList(0,4,8,10));    break;
//                 case "m7b5": case "h7"        : noteIndices.addAll( mmUpm3 );                   break; 
//                 case "o7"  : case "dim7"      : noteIndices.addAll(diminished);                 break; //Diminished
//                 case "7b5" : case "7dim5"     : noteIndices.addAll(Arrays.asList(0,4,6,10));    break;
//                 case "7b9" : case "7dim9"     : noteIndices.addAll(diminishedUpHs);             break;
//                 case "M9"  : case "maj9"      : noteIndices.addAll(Arrays.asList(0,4,7,11,2));  break;
//                 case "9"   : case "dom9"      : noteIndices.addAll(mixolydianDom);              break; //Mixolydian(dom)
//                 case "mM9" : case "minmaj9"   : noteIndices.addAll(Arrays.asList(0,3,7,11,2));  break;
//                 case "m9"  : case "min9"      : noteIndices.addAll(melodicMinor);               break; //Melodic Minor
//                 case "+M9" : case "augmaj9"   : noteIndices.addAll(Arrays.asList(0,4,8,11,2));  break;
//                 case "+9"  : case "aug9": case "9+"      : noteIndices.addAll(Arrays.asList(0,4,8,10,2));  break;
//                 case "h9"                     : noteIndices.addAll(Arrays.asList(0,3,6,10,2));  break;
//                 case "hmin9"                  : noteIndices.addAll(Arrays.asList(0,3,6,8,2));   break;   
//                 case "o9"  : case "dim9"      : noteIndices.addAll(diminished);                 break; //Diminished 
//                 case "ob9" : case "dimb9"     : noteIndices.addAll(Arrays.asList(0,3,6,9,1));   break;
//                 case "M11" : case "maj11"     : noteIndices.addAll(Arrays.asList(0,4,7,11,2,5));break;
//                 case "11"  : case "dom11"     : noteIndices.addAll(Arrays.asList(0,4,7,10,2,5));break;
//                 case "mM11": case "minmaj11"  : noteIndices.addAll(Arrays.asList(0,3,7,11,2,5));break;
//                 case "m11" : case "min11"     : noteIndices.addAll(Arrays.asList(0,3,7,10,2,5));break;
//                 case "+M11": case "augmaj11"  : noteIndices.addAll(Arrays.asList(0,4,8,11,2,5));break;
//                 case "+11" : case "aug11" : case "11+"     : noteIndices.addAll(Arrays.asList(0,4,8,10,2,5));break;
//                 case "h11"                    : noteIndices.addAll(Arrays.asList(0,3,6,10,2,5));break;
//                 case "o11" : case "dim11"     : noteIndices.addAll(diminished); noteIndices.add(4);     break; //Diminished
//                 case "M13" : case "maj13"     : noteIndices.addAll(Arrays.asList(0,4,7,11,2,5,9));      break;
//                 case "13"  : case "dom13"     : noteIndices.addAll(mixolydianDom);                      break; //Mixolydian(dom)
//                 case "13b9": case "dom13b9"   : noteIndices.addAll(Arrays.asList(0,4,7,10,1,9));   break;
//                 case "mM13": case "minmaj13"  : noteIndices.addAll(Arrays.asList(0,3,7,11,2,5,9)); break;
//                 case "m13" : case "min13"     : noteIndices.addAll(Arrays.asList(0,3,7,10,2,5,9)); break;
//                 case "+M13": case "augmaj13"  : noteIndices.addAll(Arrays.asList(0,4,8,11,2,5,9)); break;
//                 case "+13" : case "aug13" :case "13+"    : noteIndices.addAll(Arrays.asList(0,4,8,10,2,5,9)); break;
//                 case "h13"                    : noteIndices.addAll(Arrays.asList(0,3,6,10,2,5,9)); break;
//                 case "pentatonicBlues"        : noteIndices.addAll(Arrays.asList(0,3,6,8,10,1));   break;  // Ebb Gb Ab Bb Db Eb
//                 default: break;
//                 }

            }



            for (int i = 0; i< noteIndices.size(); i++) {
        System.err.println("MusicSpecs: commented out non-1.6 code.");
        //notesInChord.add(translateKeyToLetterNote(baseInteger+(int)noteIndices.get(i),false));
            }



        }
        return notesInChord;


    }

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
        default: break;
        }
        // 0 : C
        // 1 : C# Db
        // 2 : D
        // 3 : D# Eb
        // 4 : E
        // 5 : F
        // 6 : F# Gb
        // 7 : G
        // 8 : G# Ab
        // 9 : A
        // 10: A# Bb
        // 11: B
        if (addOctave) {
            int octave = (keyIndex - (keyIndex % 12))/12 - 1;
            noteName = noteName.concat(octave+"");
        }
        return noteName;

    }

    public static int translateNoteToKey(String note) {
        int octave =  -1;
        String letterPart = note;
        if (!note.startsWith(REST_SYMBOL)) {
            octave = Integer.parseInt(note.charAt(note.length()-1)+"");
            letterPart = note.substring(0,note.length());
        } 
        int noteIndex = 0;
        int noteKeyValue = 0;

        System.err.println("MusicSpecs: commented out non-1.6 code.");
//         switch (letterPart.charAt(0)) {
//         case 'A' : noteIndex = 9;  break;
//         case 'B' : noteIndex = 11; break;
//         case 'C' : noteIndex = 0;  break;
//         case 'D' : noteIndex = 2;  break;
//         case 'E' : noteIndex = 4;  break;
//         case 'F' : noteIndex = 5;  break;
//         case 'G' : noteIndex = 7;  break;
//         default: noteIndex = -1; break;
//         }
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

    public static int translateLetterToKeyIndex(String keyLetter) {  
        String letterPart = keyLetter.substring(0,keyLetter.length()); 
        int noteIndex = 0;   
        System.err.println("MusicSpecs: commented out non-1.6 code.");
//         switch (letterPart.charAt(0)) {
//         case 'A' : noteIndex = 9;  break;
//         case 'B' : noteIndex = 11; break;
//         case 'C' : noteIndex = 0;  break;
//         case 'D' : noteIndex = 2;  break;
//         case 'E' : noteIndex = 4;  break;
//         case 'F' : noteIndex = 5;  break;
//         case 'G' : noteIndex = 7;  break;
//         default: noteIndex = -1; break;
//         } 
        if (letterPart.length() >1) {
            if (letterPart.charAt(1) == '#') {
                noteIndex += 1;
            } else if (letterPart.charAt(1) == 'b') {
                noteIndex -= 1;
            }
        } 

        return noteIndex; 
    }
    
    public static final String TERMINATION_CHORD = "T";
    public static final String TERMINATION_NOTE_SYMBOL = "T";
    public static final String REST_SYMBOL = "R";
    public static final int TERMINATION_NOTE_KEY = -100;
    

}
