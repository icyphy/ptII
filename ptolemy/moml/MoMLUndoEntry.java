/* Manages undo/redo actions on a MoML model.

 Copyright (c) 2000-2003 The Regents of the University of California.
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
@ProposedRating Red (nsmyth@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
// Review base URL stuff.
*/

package ptolemy.moml;

import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// MoMLUndoEntry
/**
An entry into the list of actions that can be un-done. If undo/redo
is enabled, an instance of a class derived from this class is created
each time an operation is carried out on a MoML model following its
initial creation.
<p>
This class contains that info which will be common so all undo entries,
and defines methods that each such entry should provide e.g. redo()
<p>
FIXME: this class may do well as an interface...
<p>
@author  Neil Smyth
@version $Id$
@since Ptolemy II 2.1
*/
public class MoMLUndoEntry  {

    /**
     */
    public MoMLUndoEntry(NamedObj context,
            String undoMoML) {
        // First store the context
        _context = context;

        // For now the xml to execute to undo the entry...
        _undoMoML = undoMoML;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Gets the MoML that will undo the entry */
    public String getUndoMoML() {
        return _undoMoML;
    }

    /** Get teh context for the undo
     *  @return the NamedObj which is the context for the undo MoML
     */
    public NamedObj getUndoContext() {
        return _context;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Holds the context in which this undo entry
    // was created.
    private NamedObj _context;

    // Holds the MoML that will undo the executed MoML
    // FIXME: find a better name...
    private String _undoMoML;

}
