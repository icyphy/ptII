/* An editor for Ptolemy II objects.

 Copyright (c) 1998-1999 The Regents of the University of California.
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
import ptolemy.kernel.util.*;
import ptolemy.data.expr.Parameter;

// Java imports.
import java.awt.Component;
import java.util.HashMap;
import java.util.Iterator;
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

The restore() method restores the values of the parameters of the
object to their values when this object was created.  This can be used
in a modal dialog to implement a cancel button, which restores
the parameter values to those before the dialog was opened.

@see EditorPaneFactory
@author Steve Neuendorffer and Edward A. Lee
@version $Id$
*/

public class Configurer extends JPanel {

    /** Construct a configurer for the specified object.
     *  @param object The object to configure.
     *  @exception IllegalActionException If the specified object has
     *   no editor factories, and refuses to accept as an attribute
     *   an instance of EditorPaneFactory.
     */
    public Configurer(NamedObj object) throws IllegalActionException {
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
            add(editor.createEditorPane());
        }
        if (!foundOne) {
            try {
                EditorPaneFactory editor = new EditorPaneFactory(object,
                        object.uniqueName("editorFactory"));
                add(editor.createEditorPane());
            } catch (NameDuplicationException ex) {
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

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The object that this configurer configures.
    private NamedObj _object;

    // The original values of the parameters.
    private Map _originalValues = new HashMap();
}
