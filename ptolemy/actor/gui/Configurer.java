/* An editor for Ptolemy II objects.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

package ptolemy.actor.gui;

// Ptolemy imports.
import ptolemy.gui.CloseListener;
import ptolemy.kernel.util.*;
import ptolemy.data.expr.Parameter;

// Java imports.
import java.awt.Component;
import java.awt.Window;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

//////////////////////////////////////////////////////////////////////////
//// Configurer
/**
This class is an editor for the parameters of an object.  It may consist
of more than one editor panel.  If the object has any attributes that
are instances of EditorPaneFactory, then the panes made by those
factories are stacked vertically in this panel.  Otherwise, a
single instance of EditorPaneFactory is created and used to
construct an editor.
<p>
The restore() method restores the values of the parameters of the
object to their values when this object was created.  This can be used
in a modal dialog to implement a cancel button, which restores
the parameter values to those before the dialog was opened.

@see EditorPaneFactory
@author Steve Neuendorffer and Edward A. Lee
@version $Id$
*/

public class Configurer extends JPanel implements CloseListener {

    /** Construct a configurer for the specified object.
     *  @param object The object to configure.
     */
    public Configurer(NamedObj object) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        _object = object;
        Iterator params
            = object.attributeList(Parameter.class).iterator();
        while (params.hasNext()) {
            Parameter param = (Parameter)params.next();
            _originalValues.put(param.getName(), param.stringRepresentation());
        }

        boolean foundOne = false;
        Iterator editors
            = object.attributeList(EditorPaneFactory.class).iterator();
        while (editors.hasNext()) {
            foundOne = true;
            EditorPaneFactory editor = (EditorPaneFactory)editors.next();
            Component pane = editor.createEditorPane();
            add(pane);
            if (pane instanceof CloseListener) {
                _closeListeners.add(pane);
            }
        }
        if (!foundOne) {
            // FIXME: I believe this is where we get an error if we
            // try to edit parameters in an already executing model,
            // unless the editor pane has already been created.  EAL.
            try {
                EditorPaneFactory editor = new EditorPaneFactory(object,
                        object.uniqueName("editorFactory"));
                Component pane = editor.createEditorPane();
                add(pane);
                if (pane instanceof CloseListener) {
                    _closeListeners.add(pane);
                }
            } catch (NameDuplicationException ex) {
                throw new InternalErrorException(ex.toString());
            } catch (IllegalActionException ex) {
                // This is thrown only if the object refuses to
                // accept the attribute.  But there is no reason for
                // an object to refuse this attribute.
                throw new InternalErrorException(ex.toString());
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Request restoration of the parameter values to what they
     *  were when this object was created.  The actual restoration
     *  occurs later, in the UI thread, in order to allow all pending
     *  changes to the parameter values to be processed first.
     */
    public void restore() {
        // This is done in the UI thread in order to
        // ensure that all pending UI events have been
        // processed.  In particular, some of these events
        // may trigger notification of new parameter values,
        // which must not be allowed to occur after this
        // restore is done.  In particular, the default
        // parameter editor has lines where notification
        // of updates occurs when the line loses focus.
        // That notification occurs some time after the
        // window is destroyed.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Iterator entries = _originalValues.entrySet().iterator();
                while (entries.hasNext()) {
                    Map.Entry entry = (Map.Entry)entries.next();
                    Parameter param =
                        (Parameter)_object.getAttribute((String)entry.getKey());
                    param.setExpression((String)entry.getValue());
                }
            }
        });
    }

    /** Notify any panels in this configurer that implement the
     *  CloseListener interface that the specified window has closed.
     *  The second argument, if non-null, gives the name of the button
     *  that was used to close the window.
     *  @param window The window that closed.
     *  @param button The name of the button that was used to close the window.
     */
    public void windowClosed(Window window, String button) {
        Iterator listeners = _closeListeners.iterator();
        while(listeners.hasNext()) {
            CloseListener listener = (CloseListener)listeners.next();
            listener.windowClosed(window, button);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // A list of panels in this configurer that implement CloseListener,
    // if there are any.
    private List _closeListeners = new LinkedList();

    // The object that this configurer configures.
    private NamedObj _object;

    // The original values of the parameters.
    private Map _originalValues = new HashMap();
}
