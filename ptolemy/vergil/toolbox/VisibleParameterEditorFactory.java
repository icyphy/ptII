/* An attribute that creates an editor dialog to edit the value of a parameter.

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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.toolbox;

import ptolemy.actor.gui.EditorFactory;
import ptolemy.actor.gui.PtolemyQuery;
import ptolemy.gui.ComponentDialog;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.StringUtilities;

import java.awt.Component;
import java.awt.Frame;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

//////////////////////////////////////////////////////////////////////////
//// VisibleParameterEditorFactory
/**
If this class is contained by a settable attribute, then double
clicking on that attribute will invoke an editor for only that
parameter.  This is contrary to the the double-click action for most
objects, which edits the parameters they contain..  This class is
contained by visible parameters in the Vergil utilities library.

@author Steve Neuendorffer
@version $Id$
@since Ptolemy II 2.0
*/

public class VisibleParameterEditorFactory extends EditorFactory {

    /** Construct a factory with the specified container and name.
     *  @param container The container.
     *  @param name The name of the factory.
     *  @exception IllegalActionException If the factory is not of an
     *   acceptable attribute for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public VisibleParameterEditorFactory(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create an editor for configuring the specified object.
     */
    public void createEditor(final NamedObj object, Frame parent) {
        ComponentDialog dialog = new ComponentDialog(
                parent, "Edit Parameter " + object.getName(),
                createEditorPane());

        // If we were canceled, then restore the old value.
        if (dialog.buttonPressed().equals("Cancel")) {
            SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        NamedObj parent = (NamedObj)object.getContainer();
                        String moml = "<property name=\""
                            + object.getName()
                            + "\" value=\""
                            + StringUtilities.escapeForXML(_oldExpression)
                            + "\"/>";
                        MoMLChangeRequest request = new MoMLChangeRequest(
                                this,         // originator
                                parent,       // context
                                moml,         // MoML code
                                null);        // base
                        object.requestChange(request);
                    }
                });
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new widget for configuring the container.  This
     *  class returns a new PtolemyQuery that references the container,
     *  assuming that that container is a settable attribute.
     *  @return A new widget for configuring the container.
     */
    public Component createEditorPane() {
        NamedObj object = (NamedObj)getContainer();
        PtolemyQuery query = new PtolemyQuery(object);
        query.setTextWidth(25);

        if (object instanceof Settable) {
            Settable parameter = (Settable)object;
            _oldExpression = parameter.getExpression();
            query.addStyledEntry(parameter);
            return query;
        } else {
            return new JLabel(object.getName() +
                    " is not a settable attribute!");
        }
    }

    String _oldExpression = "";
}
