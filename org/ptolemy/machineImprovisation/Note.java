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

/** The Note object.

@author Ilge Akkaya
@version  $Id$
@since Ptolemy II 10.1
@Pt.ProposedRating Red (ilgea)
@Pt.AcceptedRating
*/
public class Note{

    /**
     * Constructs a Note object.
     *
     * @param name  Note name
     */
    public Note(String name) {
        this._name = name;
        this._noteValue = 0.0;
    }
    /**
     * Constructs a Note object.
     *
     * @param name      Note name
     * @param duration  Note duration
     */
    public Note(String name, double duration) {
        this._name = name;
        this._noteValue = duration;
    }

    /**
     * Returns the note duration
     * @return a double indicating the note duration
     */
    public double getDuration() {
        return this._noteValue;
    }

    /**
     * Returns the name of Note object
     * @return the name
     */
    public String getName() {
        return this._name;
    }

    /**
     * Returns the absolute MIDI key index of the note
     * @return MIDI key index
     */
    public int getCompleteIndex() {
        return this._getCompleteKeyIndex(_numericNoteIndex, _octave);
    }

    /**
     * Returns the relative MIDI key index in base 12
     * @return the relative key index
     */
    public int getKeyIndex() {
        return this._numericNoteIndex;
    }

    /**
     * Returns true if the note is a rest.
     * @return true if note is a rest
     */
    public boolean isRest() {
        return _name.startsWith(MusicSpecs.REST_SYMBOL);
    }

    private int _getCompleteKeyIndex(int noteIndex, int octave) {
        return noteIndex + 12*(octave+1);
    }

    private int _numericNoteIndex;
    private int _octave;
    private String _name;
    private double _noteValue;
}
