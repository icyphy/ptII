/* A GUI widget for renaming an object.

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
@AcceptedRating Red (neuendor@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import javax.swing.BoxLayout;

import ptolemy.gui.Query;
import ptolemy.gui.QueryListener;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.MessageHandler;

//////////////////////////////////////////////////////////////////////////
//// RenameConfigurer
/**
This class is an editor widget to rename an object.

@see Configurer
@author Edward A. Lee
@version $Id$
@since Ptolemy II 1.0
*/

public class RenameConfigurer extends Query
    implements ChangeListener, QueryListener {

    /** Construct a rename configurer for the specified entity.
     *  @param object The entity to configure.
     */
    public RenameConfigurer(NamedObj object) {
        super();
        this.addQueryListener(this);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setTextWidth(25);
        _object = object;
        addLine("New name", "New name", object.getName());
        // By default, names are not shown for ports, and are shown
        // for everything else.  Note that ports are a little confusing,
        // because names are _always_ shown for external ports inside
        // a composite actor.  This dialog determines whether they will
        // be shown on the outside of the composite actor.
        boolean nameShowing = false;
        if (object instanceof Port) {
            // NOTE: If the object is a Port, then Diva used to display
            // the name twice, which looks really bad.
            // In Ptolemy II 2.0beta, we shipped a modified
            // version of this file that does not display the the
            // Show name checkbox
            nameShowing = _object.getAttribute("_showName") != null;
        } else {
            nameShowing = _object.getAttribute("_hideName") == null;
        }
        addCheckBox("Show name", "Show name", nameShowing);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Apply the changes by renaming the object.
     */
    public void apply() {
        if (_changed) {
            String newName = getStringValue("New name");

            // The context for the MoML should be the first container
            // above this object in the hierarchy that defers its
            // MoML definition, or the immediate parent if there is none.
            NamedObj parent = (NamedObj)_object.getContainer();
            String oldName = _object.getName();

            // NOTE: This is awkward, but we need to know what is being
            // renamed to create the right MoML.
            StringBuffer moml = new StringBuffer("<");
            String elementName = "entity";
            if (_object instanceof Port) {
                elementName = "port";
            } else if (_object instanceof Attribute) {
                elementName = "property";
            } else if (_object instanceof Relation) {
                elementName = "relation";
            }
            moml.append(elementName);
            moml.append(" name=\"");
            moml.append(oldName);
            moml.append("\"><rename name=\"");
            moml.append(newName);
            moml.append("\"/>");
            // Remove or show name.
            boolean showName = getBooleanValue("Show name");
            if (_object instanceof Port) {
                if (showName) {
                    moml.append("<property name=\"_showName\" "
                            + "class=\"ptolemy.kernel.util.SingletonAttribute\"/>");
                } else {
                    if (_object.getAttribute("_showName") != null) {
                        moml.append("<deleteProperty name=\"_showName\"/>");
                    }
                }
            } else {
                if (showName) {
                    if (_object.getAttribute("_hideName") != null) {
                        moml.append("<deleteProperty name=\"_hideName\"/>");
                    }
                } else {
                    moml.append("<property name=\"_hideName\" "
                            + "class=\"ptolemy.kernel.util.SingletonAttribute\"/>");
                }
            }
            moml.append("</");
            moml.append(elementName);
            moml.append(">");

            MoMLChangeRequest request = new MoMLChangeRequest(
                    this,            // originator
                    parent,          // context
                    moml.toString(), // MoML code
                    null);           // base

            request.addChangeListener(this);
            request.setUndoable(true);
            parent.requestChange(request);
        }
    }

    /** React to the fact that the change has been successfully executed
     *  by doing nothing.
     *  @param change The change that has been executed.
     */
    public void changeExecuted(ChangeRequest change) {
        // Nothing to do.
    }

    /** React to the fact that the change has failed by reporting it.
     *  @param change The change that was attempted.
     *  @param exception The exception that resulted.
     */
    public void changeFailed(ChangeRequest change, Exception exception) {
        // Ignore if this is not the originator.
        if (change != null && change.getSource() != this) return;
        if (change != null && !change.isErrorReported()) {
            change.setErrorReported(true);
            MessageHandler.error("Rename failed: ", exception);
        }
    }

    /** Called to notify that one of the entries has changed.
     *  This simply sets a flag that enables application of the change
     *  when the apply() method is called.
     *  @param name The name of the entry that changed.
     */
    public void changed(String name) {
        _changed = true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Indicator that the name has changed.
    private boolean _changed = false;

    // The object that this configurer configures.
    private NamedObj _object;
}
