/* A GUI widget for renaming an object.

 Copyright (c) 1998-2014 The Regents of the University of California.
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

import javax.swing.BoxLayout;

import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.gui.Query;
import ptolemy.gui.QueryListener;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.AbstractSettableAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// RenameConfigurer

/**
 This class is an editor widget to rename an object.

 @see Configurer
 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (neuendor)
 */
@SuppressWarnings("serial")
public class RenameConfigurer extends Query implements ChangeListener,
QueryListener {
    /** Construct a rename configurer for the specified entity.
     *  @param object The entity to configure.
     */
    public RenameConfigurer(NamedObj object) {
        super();
        this.addQueryListener(this);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setTextWidth(25);
        _object = object;
        addLine(_NAME_LABEL, _NAME_LABEL, object.getName());
        addTextArea(_DISPLAY_NAME_LABEL, _DISPLAY_NAME_LABEL,
                object.getDisplayName());

        // By default, names are not shown for ports, and are shown
        // for everything else.  Note that ports are a little confusing,
        // because names are _always_ shown for external ports inside
        // a composite actor.  This dialog determines whether they will
        // be shown on the outside of the composite actor.
        boolean nameShowing = false;

        if (object instanceof Port) {
            nameShowing = _isPropertySet(_object, "_showName");
        } else {
            nameShowing = !_isPropertySet(_object, "_hideName");
        }

        // Don't include the Show name check box for
        // AbstractSettableAttributes We choose
        // AbstractSettableAttribute because we don't want the
        // "Show name" check box for RequireVersion, which eventually
        // extends AbstractSettableAttribute, but we do want the
        // "Show name" check box for Director, which extends Attribute.
        // See
        // http://bugzilla.ecoinformatics.org/show_bug.cgi?id=3363
        if (!(object instanceof AbstractSettableAttribute)) {
            addCheckBox("Show name", "Show name", nameShowing);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Apply the changes by renaming the object.
     */
    public void apply() {
        if (_changed) {
            String newName = StringUtilities
                    .escapeForXML(getStringValue(_NAME_LABEL));
            String displayName = StringUtilities
                    .escapeForXML(getStringValue(_DISPLAY_NAME_LABEL));

            NamedObj parent = _object.getContainer();
            if (parent == null) {
                // Hitting F2 in an empty model and renaming the canvas can result in a NPE.
                // See https://chess.eecs.berkeley.edu/bugzilla/show_bug.cgi?id=355
                MessageHandler
                .message("Please save the model before changing the name.");
                return;
            }
            String oldName = StringUtilities.escapeForXML(_object.getName());
            String oldDisplayName = StringUtilities.escapeForXML(_object
                    .getDisplayName());

            StringBuffer moml = new StringBuffer("<");
            String elementName = _object.getElementName();
            moml.append(elementName);
            moml.append(" name=\"");
            moml.append(oldName);
            moml.append("\">");
            if (!oldName.equals(newName)) {
                moml.append("<rename name=\"");
                moml.append(newName);
                moml.append("\"/>");
            }
            if (!oldDisplayName.equals(displayName)) {
                moml.append("<display name=\"");
                moml.append(displayName);
                moml.append("\"/>");
            }

            // Don't include the Show name check box for AbstractSettableAttributes
            // http://bugzilla.ecoinformatics.org/show_bug.cgi?id=3363
            if (!(_object instanceof AbstractSettableAttribute)) {
                // Remove or show name.
                boolean showName = getBooleanValue("Show name");

                if (_object instanceof Port) {
                    boolean previousShowName = _isPropertySet(_object,
                            "_showName");

                    if (showName != previousShowName) {
                        if (showName) {
                            moml.append("<property name=\"_showName\" "
                                    + "class=\"ptolemy.data.expr.SingletonParameter\""
                                    + " value=\"true\"/>");
                        } else {
                            if (!(_object.getAttribute("_showName") instanceof Parameter)) {
                                moml.append("<deleteProperty name=\"_showName\"/>");
                            } else {
                                moml.append("<property name=\"_showName\" "
                                        + "class=\"ptolemy.data.expr.SingletonParameter\""
                                        + " value=\"false\"/>");
                            }
                        }
                    }
                } else {
                    boolean previousShowName = !_isPropertySet(_object,
                            "_hideName");

                    if (showName != previousShowName) {
                        if (showName) {
                            if (!(_object.getAttribute("_hideName") instanceof Parameter)) {
                                moml.append("<deleteProperty name=\"_hideName\"/>");
                            } else {
                                moml.append("<property name=\"_hideName\" "
                                        + "class=\"ptolemy.data.expr.SingletonParameter\""
                                        + " value=\"false\"/>");
                            }
                        } else {
                            moml.append("<property name=\"_hideName\" "
                                    + "class=\"ptolemy.data.expr.SingletonParameter\""
                                    + " value=\"true\"/>");
                        }
                    }
                }
            }

            moml.append("</");
            moml.append(elementName);
            moml.append(">");

            MoMLChangeRequest request = new MoMLChangeRequest(this, // originator
                    parent, // context
                    moml.toString(), // MoML code
                    null); // base

            request.addChangeListener(this);
            request.setUndoable(true);
            parent.requestChange(request);
        }
    }

    /** React to the fact that the change has been successfully executed
     *  by doing nothing.
     *  @param change The change that has been executed.
     */
    @Override
    public void changeExecuted(ChangeRequest change) {
        // Nothing to do.
    }

    /** React to the fact that the change has failed by reporting it.
     *  @param change The change that was attempted.
     *  @param exception The exception that resulted.
     */
    @Override
    public void changeFailed(ChangeRequest change, Exception exception) {
        // Ignore if this is not the originator.
        if (change != null && change.getSource() != this) {
            return;
        }

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
    @Override
    public void changed(String name) {
        _changed = true;
    }

    /**
     * Get the object upon which this RenameConfigurer is operating.
     * @return The object.
     */
    public NamedObj getObject() {
        // This method makes it possible to determine which object is being
        // renamed.  This is especially useful when dealing with the
        // MoMLChangeRequest generated by this class from other classes.
        return _object;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return true if the property of the specified name is set for
     *  the specified object. A property is specified if the specified
     *  object contains an attribute with the specified name and that
     *  attribute is either not a boolean-valued parameter, or it is
     *  a boolean-valued parameter with value true.
     *  @param object The object.
     *  @param name The property name.
     *  @return True if the property is set.
     */
    private boolean _isPropertySet(NamedObj object, String name) {
        Attribute attribute = object.getAttribute(name);

        if (attribute == null) {
            return false;
        }

        if (attribute instanceof Parameter) {
            try {
                Token token = ((Parameter) attribute).getToken();

                if (token instanceof BooleanToken) {
                    if (!((BooleanToken) token).booleanValue()) {
                        return false;
                    }
                }
            } catch (IllegalActionException e) {
                // Ignore, using default of true.
            }
        }

        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Indicator that the name has changed.
    private boolean _changed = false;

    // The object that this configurer configures.
    private NamedObj _object;

    private static String _NAME_LABEL = "Name";
    private static String _DISPLAY_NAME_LABEL = "Display name";
}
