/* An attribute that creates a text editor to edit a string attribute.

 Copyright (c) 1998-2003 The Regents of the University of California.
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
@AcceptedRating Red (ptolemy@eecs.berkeley.edu)
*/

package ptolemy.vergil.toolbox;

import java.awt.Frame;

import ptolemy.actor.gui.EditorFactory;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.util.MessageHandler;

//////////////////////////////////////////////////////////////////////////
//// TextEditorConfigureFactory
/**
If this class is contained by a actor, then double clicking on that
actor will invoke a text editor that edits the value of a specified
string attribute.  The string attribute must be contained by the
same container as this factory; its name is given by the
<i>attributeName</i> attribute of this factory. The number of
rows and columns displayed are given by the <i>rowsDisplayed</i>
and <i>columnsDisplayed</i> parameters. The default is 80 columns
and 40 rows.
<p>
This attribute is similar to TextEditorTableauFactory, except that
it opens the text editor when the containing actor is configured
(edit parameters), whereas TextEditorTableauFactory opens the text
editor when the user looks inside.
@see TextEditorTableauFactory

@author Edward A. Lee
@version $Id$
*/

public class TextEditorConfigureFactory
    extends EditorFactory
    implements TextEditorFactory {

    /** Construct a factory with the specified container and name.
     *  @param container The container.
     *  @param name The name of the factory.
     *  @exception IllegalActionException If the factory is not of an
     *   acceptable attribute for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public TextEditorConfigureFactory(NamedObj container, String name)
        throws IllegalActionException, NameDuplicationException {
        super(container, name);

        attributeName = new StringAttribute(this, "attributeName");

        columnsDisplayed = new Parameter(this, "columnsDisplayed");
        columnsDisplayed.setTypeEquals(BaseType.INT);
        columnsDisplayed.setExpression("80");

        rowsDisplayed = new Parameter(this, "rowsDisplayed");
        rowsDisplayed.setTypeEquals(BaseType.INT);
        rowsDisplayed.setExpression("40");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The name of the string attribute that is to be edited. */
    public StringAttribute attributeName;

    /** The horizontal size of the display, in columns. This contains
     *  an integer, and defaults to 40.
     */
    public Parameter columnsDisplayed;

    /** The vertical size of the display, in rows. This contains an
     *  integer, and defaults to 10.
     */
    public Parameter rowsDisplayed;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Remove any editor that may have been associated with this object
     *  by a previous call to createEditor().
     */
    public void clear() {
        _editor = null;
    }

    /** Create an editor for editing the string attribute specified
     *  by the <i>attributeName</i> parameter.
     *  @param object The object to configure (which is expected to
     *   be the same as the container of this attribute).
     *  @param parent The frame with respect to which to define the
     *   editor.
     */
    public void createEditor(NamedObj object, Frame parent) {
        if (_editor == null) {
            try {
                StringAttribute attributeToEdit =
                    (StringAttribute) ((NamedObj) getContainer()).getAttribute(
                        attributeName.getExpression(),
                        StringAttribute.class);
                int numberOfRows =
                    ((IntToken) rowsDisplayed.getToken()).intValue();
                int numberOfColumns =
                    ((IntToken) columnsDisplayed.getToken()).intValue();
                _editor =
                    new TextEditorForStringAttributes(
                        this,
                        attributeToEdit,
                        numberOfRows,
                        numberOfColumns,
                        "Editor for "
                            + attributeName.getExpression()
                            + " of "
                            + getContainer().getFullName());
            } catch (IllegalActionException ex) {
                MessageHandler.error(
                    "Cannot get specified string attribute to edit.",
                    ex);
            }
        }
        // Can't just call show() here because after calling pack() and
        // before making visible we need to call adjustFileMenu().
        _editor.pack();
        _editor.adjustFileMenu();
        _editor.centerOnScreen();
        _editor.setVisible(true);
    }
    
    /** Return the current text of the text editor.
     *  @return The current text of the text editor, or null if there
     *   is none.
     */
    public String getText() {
        if (_editor != null) {
            return _editor.text.getText();
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // Keep track of an open editor so that it isn't opened more than
    // once.
    private TextEditorForStringAttributes _editor;
}
