/* A tableau representing a text window.

 Copyright (c) 1999 The Regents of the University of California.
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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// TextEditorTableau
/**
A tableau representing a text window. The constructor of this
class creates the window. The text window itself is an instance
of TextEditor, and can be accessed using the getFrame() method.
As with other tableaux, this is an entity that is contained by
an effigy of a model.
There can be any number of instances of this class in an effigy.

@author  Steve Neuendorffer and Edward A. Lee
@version $Id$
@see Effigy
*/
public class TextEditorTableau extends Tableau {

    /** Construct a new tableau for the model represented by the given effigy.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the container does not accept
     *   this entity (this should not occur).
     *  @exception NameDuplicationException If the name coincides with an 
     *   attribute already in the container.
     */
    public TextEditorTableau(Effigy container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        TextEditor frame = new TextEditor();
	frame.text.setColumns(80);
	frame.text.setRows(20);
	setFrame(frame);
	frame.setTableau(this);
	frame.setVisible(true);
	frame.pack();
    }
}
