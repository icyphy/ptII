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

import ptolemy.gui.Top;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.net.URL;
import java.util.List;
import javax.swing.JTextArea;

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
    public TextEditorTableau(TextEffigy container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        String title = "Unnamed";
        TextEditor frame = new TextEditor(title, container.getDocument());
	frame.text.setColumns(80);
	frame.text.setRows(40);
	setFrame(frame);
	frame.setTableau(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Make the tableau editable or uneditable.
     *  @param flag False to make the tableau uneditable.
     */
    public void setEditable(boolean flag) {
        TextEditor editor = (TextEditor)getFrame();
        if (editor.text != null) {
            editor.text.setEditable(false);
            super.setEditable(flag);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A factory that creates text editor tableaux for Ptolemy models.
     */
    public static class Factory extends TableauFactory {

	/** Create an factory with the given name and container.
	 *  The container argument must not be null, or a
	 *  NullPointerException will be thrown.  This entity will use the
	 *  workspace of the container for synchronization and version counts.
	 *  If the name argument is null, 
	 *  then the name is set to the empty string.
	 *  Increment the version of the workspace.
	 *  @param container The container entity.
	 *  @param name The name of the entity.
	 *  @exception IllegalActionException If the container is incompatible
	 *   with this entity.
	 *  @exception NameDuplicationException If the name coincides with
	 *   an entity already in the container.
	 */
	public Factory(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
	    super(container, name);
	}

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

	/** If the specified effigy already contains a tableau named
         *  "textTableau", then show it; otherwise, create a new instance
         *  of TextEditorTableau in the specified effigy, and name it
         *  "textTableau".  If the specified effigy is not an instance of
         *  TextEffigy, then do not create a tableau and return null.
	 *  @param effigy The effigy.
	 *  @return A new text editor tableau if the effigy is a TextEffigy,
	 *    or null otherwise.
         *  @exception Exception If the factory should be able to create a
         *   tableau for the effigy, but something goes wrong.
	 */
	public Tableau createTableau(Effigy effigy) throws Exception {
	    if(effigy instanceof TextEffigy) {
                // First see whether the effigy already contains a
                // TextEditorTableau with the appropriate name.
                TextEditorTableau tableau =
                        (TextEditorTableau)effigy.getEntity("textTableau");
                if (tableau == null) {
                    tableau = new TextEditorTableau(
                             (TextEffigy)effigy, "textTableau");
                }
                tableau.show();
                return tableau;
	    } else {
                // The effigy is not an instance of TextEffigy.
                // See whether it contains an instance of TextEffigy.
                List effigies = effigy.entityList(TextEffigy.class);
                if (effigies.size() > 0) {
                    TextEffigy textEffigy = (TextEffigy)effigies.get(0);
                    return createTableau(textEffigy);
                } else {
                    // It does not contain an instance of TextEffigy.
                    // Attempt to use it's url attribute and create a new
                    // instance of TextEffigy contained by the specified one.
                    URL url = effigy.url.getURL();
                    TextEffigy textEffigy = TextEffigy.newTextEffigy(
                            effigy, url, url);
                    Tableau textTableau = createTableau(textEffigy);
                    textTableau.setEditable(false);
                    return textTableau;
                }
            }
	}
    }
}
