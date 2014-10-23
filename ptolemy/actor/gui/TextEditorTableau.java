/* A tableau representing a text window.

 Copyright (c) 2000-2014 The Regents of the University of California.
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
package ptolemy.actor.gui;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Iterator;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
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
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
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
        this(container, name, null);
    }

    /** Construct a new tableau for the model represented by the given effigy.
     *  @param container The container.
     *  @param name The name.
     *  @param editor The text editor to use, or null to use the default.
     *  @exception IllegalActionException If the container does not accept
     *   this entity (this should not occur).
     *  @exception NameDuplicationException If the name coincides with an
     *   attribute already in the container.
     */
    public TextEditorTableau(TextEffigy container, String name,
            TextEditor editor) throws IllegalActionException,
            NameDuplicationException {
        super(container, name);

        String title = "Unnamed";
        TextEditor frame = editor;

        if (frame == null) {
            frame = new TextEditor(title, container.getDocument());
            // Set the title for the Kepler R actor
            // http://bugzilla.ecoinformatics.org/show_bug.cgi?id=3187
            setTitle(frame.getTitle());
        }

        frame.text.setColumns(80);
        frame.text.setRows(40);
        setFrame(frame);
        frame.setTableau(this);

        // https://chess.eecs.berkeley.edu/bugzilla/show_bug.cgi?id=67 says:
        // "Create a new model and drag in a Const actor. If you close
        // the model at this time, vergil prompts to save the model
        // since there is unsaved modification. However, if you first
        // do View -> XML View, and then close the model, no prompt
        // shows up even if the model should be dirty."
        // So, we don't want to mark the frame as unmodified.
        //
        // The above will mark the text object modified. Reverse this.
        //frame.setModified(false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Make the tableau editable or uneditable.  Notice that this does
     *  not change whether the effigy is modifiable, so other tableaux
     *  on the same effigy may still modify the associated file.
     *  @param flag False to make the tableau uneditable.
     */
    @Override
    public void setEditable(boolean flag) {
        super.setEditable(flag);

        TextEditor editor = (TextEditor) getFrame();

        if (editor.text != null) {
            editor.text.setEditable(flag);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A factory that creates text editor tableaux for Ptolemy models.
     */
    public static class Factory extends TableauFactory {
        /** Create a factory with the given name and container.
         *  @param container The container entity.
         *  @param name The name of the entity.
         *  @exception IllegalActionException If the container is incompatible
         *   with this attribute.
         *  @exception NameDuplicationException If the name coincides with
         *   an attribute already in the container.
         */
        public Factory(NamedObj container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);

            String editorPreference = ".";

            try {
                editorPreference = System.getProperty(
                        "ptolemy.user.texteditor", ".");
            } catch (SecurityException security) {
                // Ignore, we are probably running in a sandbox or as
                // an applet
            }

            Class tableauClass;
            Class effigyClass;

            try {
                if (editorPreference.equals("emacs")) {
                    tableauClass = Class
                            .forName("ptolemy.actor.gui.ExternalTextTableau");
                    effigyClass = Class
                            .forName("ptolemy.actor.gui.ExternalTextEffigy");
                } else {
                    tableauClass = Class
                            .forName("ptolemy.actor.gui.TextEditorTableau");
                    effigyClass = Class.forName("ptolemy.actor.gui.TextEffigy");
                }

                _tableauConstructor = tableauClass.getConstructor(new Class[] {
                        TextEffigy.class, String.class });
                _newTextEffigyText = effigyClass.getMethod("newTextEffigy",
                        new Class[] { CompositeEntity.class, String.class });
                _newTextEffigyURL = effigyClass.getMethod("newTextEffigy",
                        new Class[] { CompositeEntity.class, URL.class,
                        URL.class });
            } catch (ClassNotFoundException ex) {
                throw new IllegalActionException(ex.toString());
            } catch (NoSuchMethodException ex) {
                throw new IllegalActionException(ex.toString());
            }
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** If the specified effigy is a TextEffigy and it
         *  already contains a tableau named
         *  "textTableau", then return that tableau; otherwise, create
         *  a new instance of TextEditorTableau in the specified
         *  effigy, and name it "textTableau" and return that tableau.
         *  If the specified effigy is not an instance of TextEffigy,
         *  but contains an instance of TextEffigy, then open a tableau
         *  for that effigy.  If it is a PtolemyEffigy, then create a
         *  text effigy with the MoML representation of the model.
         *  Finally, if is not a TextEffigy or a PtolemyEffigy,
         *  and it does not contain a TextEffigy, then attempt to
         *  open its URL and display its date by creating a text effigy,
         *  which will then be contained by the specified effigy. If all
         *  of this fails, then do not create a tableau and return null.
         *  It is the responsibility of callers of this method to check the
         *  return value and call show().
         *
         *  @param effigy The effigy.
         *  @return A text editor tableau, or null if one cannot be
         *    found or created.
         *  @exception Exception If the factory should be able to create a
         *   tableau for the effigy, but something goes wrong.
         */
        @Override
        public Tableau createTableau(Effigy effigy) throws Exception {
            if (effigy instanceof TextEffigy) {
                // First see whether the effigy already contains a
                // TextEditorTableau with the appropriate name.
                TextEditorTableau tableau = (TextEditorTableau) effigy
                        .getEntity("textTableau");

                if (tableau == null) {
                    tableau = (TextEditorTableau) _tableauConstructor
                            .newInstance(new Object[] { effigy, "textTableau" });
                }

                URL url = effigy.uri.getURL();
                if (url != null) {
                    // Set the identifier so that if we start vergil and
                    // do File -> New -> Text Editor, type some text, Save
                    // then the title changes from Unnames to the name of the file.
                    effigy.identifier.setExpression(url.toExternalForm());
                }

                tableau.setEditable(effigy.isModifiable());
                return tableau;
            } else {
                // The effigy is not an instance of TextEffigy.
                // See whether it contains an instance of TextEffigy
                // named "textEffigy", and if it does return that instance.
                Iterator effigies = effigy.entityList(TextEffigy.class)
                        .iterator();

                while (effigies.hasNext()) {
                    TextEffigy textEffigy = (TextEffigy) effigies.next();

                    if (textEffigy.getName().equals("textEffigy")) {
                        return createTableau(textEffigy);
                    }
                }

                // It does not contain an instance of TextEffigy with
                // the name "textEffigy".
                // Attempt to use it's url attribute and create a new
                // instance of TextEffigy contained by the specified one.
                URL url = effigy.uri.getURL();
                TextEffigy textEffigy;

                if (effigy instanceof PtolemyEffigy) {
                    // NOTE: It seems unfortunate here to have
                    // to distinctly support MoML.  Would it make
                    // sense for the Effigy base class to have a method
                    // that gives a textual description of the data?
                    String moml = ((PtolemyEffigy) effigy).getModel()
                            .exportMoML();
                    textEffigy = (TextEffigy) _newTextEffigyText.invoke(null,
                            new Object[] { effigy, moml });

                    // FIXME: Eventually, it would be nice that this be
                    // editable if the PtolemyEffigy is modifiable.
                    // But this requires having an "apply" button.
                    textEffigy.setModifiable(false);
                    textEffigy.setName("textEffigy");
                } else {
                    // The View Source choice of the HTMLViewer runs this code.
                    textEffigy = (TextEffigy) _newTextEffigyURL.invoke(null,
                            new Object[] { effigy, url, url });
                    textEffigy.setName("textEffigy");
                }

                TextEditorTableau textTableau = (TextEditorTableau) createTableau(textEffigy);

                if (url != null) {
                    // A NullPointerException was reported here, see
                    // http://bugzilla.ecoinformatics.org/show_bug.cgi?id=5446
                    textEffigy.identifier.setExpression(url.toExternalForm());
                }
                return textTableau;
            }
        }

        private Constructor _tableauConstructor;

        private Method _newTextEffigyText;

        private Method _newTextEffigyURL;
    }
}
