/* An attribute that causes look inside to open a text editor to
 edit a string attribute in the container.

 Copyright (c) 2003-2014 The Regents of the University of California.
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
package ptolemy.vergil.toolbox;

import java.util.Iterator;

import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFactory;
import ptolemy.actor.gui.TextEditorTableau;
import ptolemy.actor.gui.TextEffigy;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;

///////////////////////////////////////////////////////////////////
//// TextEditorTableauFactory

/**
 This class is an attribute that creates a text editor to edit a specified
 string attribute in the container of this attribute.  It is similar to
 TextEditorConfigureFactory, but instead of opening when the actor is configured,
 it is opened when the user looks inside the actor.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (ptolemy)
 @see TextEditorConfigureFactory
 */
public class TextEditorTableauFactory extends TableauFactory implements
        TextEditorFactory {
    /** Create a factory with the given name and container.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the container is incompatible
     *   with this attribute.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public TextEditorTableauFactory(NamedObj container, String name)
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
    @Override
    public void clear() {
        _editor = null;
    }

    /** Create a tableau for the specified effigy. The tableau will be
     *  created with a new unique name with the specified effigy as its
     *  container.  If this factory cannot create a tableau
     *  for the given effigy (it is not an instance of PtolemyEffigy),
     *  then return null.
     *  @param effigy The component effigy.
     *  @return A tableau for the effigy, or null if one cannot be created.
     *  @exception Exception If the factory should be able to create a
     *   Tableau for the effigy, but something goes wrong.
     */
    @Override
    public Tableau createTableau(Effigy effigy) throws Exception {
        if (!(effigy instanceof PtolemyEffigy)) {
            return null;
        }

        NamedObj object = ((PtolemyEffigy) effigy).getModel();
        Attribute attribute = object
                .getAttribute(attributeName.getExpression());

        if (!(attribute instanceof StringAttribute)) {
            throw new IllegalActionException(object, "Expected "
                    + object.getFullName()
                    + " to contain a StringAttribute named "
                    + attributeName.getExpression() + ", but it does not.");
        }

        // effigy may already contain a texteffigy.
        TextEffigy textEffigy = null;
        Iterator subEffigies = effigy.entityList(TextEffigy.class).iterator();

        while (subEffigies.hasNext()) {
            textEffigy = (TextEffigy) subEffigies.next();
        }

        if (textEffigy == null) {
            textEffigy = TextEffigy.newTextEffigy(effigy,
                    ((StringAttribute) attribute).getExpression());
        }

        // textEffigy may already have a tableau.
        Iterator tableaux = textEffigy.entityList(TextEditorTableau.class)
                .iterator();

        if (tableaux.hasNext()) {
            return (TextEditorTableau) tableaux.next();
        }

        // Need a new tableau, so create an editor for it.
        if (_editor == null) {
            int numberOfRows = ((IntToken) rowsDisplayed.getToken()).intValue();
            int numberOfColumns = ((IntToken) columnsDisplayed.getToken())
                    .intValue();
            _editor = new TextEditorForStringAttributes(this,
                    (StringAttribute) attribute, numberOfRows, numberOfColumns,
                    "Editor for " + attributeName.getExpression() + " of "
                            + getContainer().getFullName());
        }

        TextEditorTableau tableau = new TextEditorTableau(textEffigy,
                "_tableau", _editor);
        return tableau;
    }

    /** Return the current text of the text editor.
     *  @return The current text of the text editor, or null if there
     *   is none.
     */
    @Override
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
