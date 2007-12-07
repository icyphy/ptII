/* An attribute that creates an editor pane to edit a JNIActor

 Copyright (c) 2006 The Regents of the University of California.
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
package jni.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import jni.GenericJNIActor;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.EditParametersDialog;
import ptolemy.actor.gui.EditorFactory;
import ptolemy.gui.ComponentDialog;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// JNIActorEditorFactory

/**
 An editor for GenericJNIActor.

 The editor has two choices: the usual parameter editor and a special
 editor for editing arguments to the native method.

 @author Christopher Brooks, based on AnnotationEditorFactory by Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class JNIActorEditorFactory extends EditorFactory {
    /** Construct a factory with the specified container and name.
     *  @param container The container.
     *  @param name The name of the factory.
     *  @exception IllegalActionException If the factory is not of an
     *   acceptable attribute for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public JNIActorEditorFactory(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create an editor for configuring the specified object.
     */
    public void createEditor(NamedObj object, Frame parent) {
        ComponentDialog dialog = new ComponentDialog(parent,
                "Configure JNI Actor", createEditorPane(), _moreButtons);

        String button = dialog.buttonPressed();

        if (button.equals("Cancel")) {
            return;
        }

        if (button.equals("Configure Parameters")) {
            new EditParametersDialog(parent, object);
            return;
        }

        Configuration configuration = (Configuration) Configuration.findEffigy(
                object.getContainer()).toplevel();

        if (button.equals("Configure Arguments")) {
            if (!(object instanceof GenericJNIActor)) {
                throw new InternalErrorException("Tried to configure an "
                        + "object that is not a GenericJNIActor");
            } else {
                new ArgumentConfigurerDialog(parent, (GenericJNIActor) object,
                        configuration);
                return;
            }
        }

        if (button.equals("Help")) {
            ArgumentConfigurerDialog.help(configuration);
            return;
        }
    }

    /** Return a new widget for configuring the container.
     *  @return A JPanel that is a text editor for editing the annotation text.
     */
    public Component createEditorPane() {
        JPanel panel = new JPanel();
        JTextArea textArea = new JTextArea("To edit the parameters, select "
                + "\"Configure Parameters\".  To edit the native method "
                + "arguments, select \"Configure Arguments\"", 3, 40);
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
            "Configure Arguments", "Help", "Cancel" };

}
