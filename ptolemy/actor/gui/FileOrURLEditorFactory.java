/* An editor factory for an object that has a fileOrURL parameter.

 Copyright (c) 2006-2014 The Regents of the University of California.
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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.gui.ComponentDialog;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// FileOrURLEditorFactory

/**
 An editor factory for an attribute that has a fileOrURL parameter.

 The editor has two choices: the usual parameter editor and a file editor
 that edits the fileOrURL parameter.

 @author Christopher Brooks, based on AnnotationEditorFactory by Edward A. Lee
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class FileOrURLEditorFactory extends EditorFactory {
    /** Construct a factory with the specified container and name.
     *  @param container The container.
     *  @param name The name of the factory.
     *  @exception IllegalActionException If the factory is not of an
     *   acceptable attribute for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public FileOrURLEditorFactory(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create an editor for configuring the specified object.
     */
    @Override
    public void createEditor(NamedObj object, Frame parent) {

        ComponentDialog dialog = null;

        FileParameter helpFileParameter = (FileParameter) object
                .getAttribute("help");

        String[] buttons = _moreButtons;
        if (helpFileParameter == null) {
            // Do not include the help button.
            buttons = _moreButtonsNoHelp;
        }

        dialog = new ComponentDialog(parent, "Edit Parameters or File",
                createEditorPane(), buttons);

        String button = dialog.buttonPressed();

        if (button.equals("Cancel")) {
            return;
        }

        if (button.equals(_moreButtons[0])) {
            new EditParametersDialog(parent, object);
            return;
        }

        Configuration configuration = (Configuration) Configuration.findEffigy(
                object.getContainer()).toplevel();

        if (button.equals(_moreButtons[1])) {
            FileParameter fileOrURLParameter = (FileParameter) object
                    .getAttribute("fileOrURL");
            if (fileOrURLParameter == null) {
                throw new InternalErrorException(object, null,
                        "No \"fileOrURL\" attribute.");
            } else {
                try {
                    if (fileOrURLParameter.asURL() == null) {
                        ModelDirectory directory = configuration.getDirectory();

                        StringParameter initialDefaultContentsParameter = (StringParameter) object
                                .getAttribute("initialDefaultContents");
                        String defaultText = "";
                        if (initialDefaultContentsParameter != null) {
                            defaultText = initialDefaultContentsParameter
                                    .getExpression();
                        }
                        Effigy effigy = TextEffigy.newTextEffigy(directory,
                                defaultText);
                        configuration.createPrimaryTableau(effigy);

                        //                         EffigyFactory textEffigyFactory = new TextEffigy.Factory(directory, "effigyFactory");
                        //                         Tableau tableau = configuration.openModel(
                        //                                 null, null, "Unnamed",
                        //                                 textEffigyFactory);
                        //                         Effigy effigy = (Effigy) tableau.getContainer();
                        //                         effigy.masterEffigy().setModifiable(true);
                        //                         System.out.println("FileOrURLEditorFactory: modifiable"
                        //                                            + effigy.isModifiable() + " " + effigy);

                    } else {
                        URL fileURL = fileOrURLParameter.asFile().toURI()
                                .toURL();
                        configuration.openModel(null, fileURL,
                                fileURL.toExternalForm());
                    }
                } catch (Exception ex) {
                    throw new InternalErrorException(object, ex,
                            "Failed to open \"" + fileOrURLParameter + "\"");
                }
                return;
            }
        }

        if (button.equals(_moreButtons[2])) {
            try {
                if (helpFileParameter != null) {
                    URL fileURL = helpFileParameter.asFile().toURI().toURL();
                    configuration.openModel(null, fileURL,
                            fileURL.toExternalForm());
                } else {
                    throw new InternalErrorException("No help parameter?");
                }
            } catch (Exception ex) {
                throw new InternalErrorException(object, ex,
                        "Failed to open \"" + helpFileParameter + "\"");
            }
            return;
        }
    }

    /** Return a new widget for configuring the container.
     *  @return A JPanel that is a text editor for editing the annotation text.
     */
    public Component createEditorPane() {
        JPanel panel = new JPanel();
        JTextArea textArea = new JTextArea(
                "To specify the name of the file that contains the parameters, select "
                        + "\""
                        + _moreButtons[0]
                                + "\".  To edit the file itself that contains the parameters,"
                                + " select \"" + _moreButtons[1] + "\".", 3, 40);
        textArea.setEditable(false);
        textArea.setBorder(BorderFactory.createEtchedBorder());
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JScrollPane pane = new JScrollPane(textArea);

        // NOTE: Should the size be hardwired here?
        pane.setPreferredSize(new Dimension(400, 100));
        panel.add(pane);

        return panel;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // Button labels.
    private static String[] _moreButtons = { "Configure Parameters",
        "Edit File", "Help", "Cancel" };

    private static String[] _moreButtonsNoHelp = { "Configure Parameters",
        "Edit File", "Cancel" };

}
