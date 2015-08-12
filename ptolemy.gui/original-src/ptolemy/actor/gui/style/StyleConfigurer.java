/* An Object for changing the style of parameters.

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
package ptolemy.actor.gui.style;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;

import ptolemy.actor.gui.Configurer;
import ptolemy.data.expr.Parameter;
import ptolemy.gui.Query;
import ptolemy.gui.QueryListener;
import ptolemy.gui.Top;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.moml.MoMLChangeRequest;

///////////////////////////////////////////////////////////////////
//// StyleConfigurer

/**
 This class is an editor for the styles of the parameters of an object.
 It allows a user to graphically change the ParameterEditorStyles contained
 within the user settable attributes of a named object.
 It is very similar in spirit and style to Configurer, which edits the actual
 values of the attributes.
 <p>
 The restore() method restores the values of the parameters of the
 object to their values when this object was created.  This can be used
 in a modal dialog to implement a cancel button, which restores
 the styles to those before the dialog was opened.

 @see ptolemy.actor.gui.Configurer
 @see ParameterEditorStyle
 @author Steve Neuendorffer and Edward A. Lee
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (neuendor)
 @Pt.AcceptedRating Yellow (neuendor)
 */
@SuppressWarnings("serial")
public class StyleConfigurer extends Query implements QueryListener {
    /** Construct a configurer for the specified object.
     *  @param object The object to configure.
     *  @exception IllegalActionException If the specified object has
     *   no editor factories, and refuses to acceptable as an attribute
     *   an instance of EditorPaneFactory.
     */
    public StyleConfigurer(NamedObj object) throws IllegalActionException {
        super();
        this.addQueryListener(this);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        _object = object;

        setTextWidth(25);

        try {
            // FIXME this list should not be statically specified.
            // Note that fixing this will probably move the accept method
            // into some sort of factory object (instead of cloning
            // existing styles).
            // NOTE: These styles need to have a container so
            // that exportMoML() doesn't generate XML header information.
            Parameter container = new Parameter();
            _parameterStyles = new ParameterEditorStyle[8];
            _parameterStyles[0] = new LineStyle(container, "Line");
            _parameterStyles[1] = new CheckBoxStyle(container, "Check Box");
            _parameterStyles[2] = new ChoiceStyle(container, "Choice");
            _parameterStyles[3] = new EditableChoiceStyle(container,
                    "EditableChoice");
            _parameterStyles[4] = new TextStyle(container, "Text");
            _parameterStyles[5] = new FileChooserStyle(container, "FileChooser");
            _parameterStyles[6] = new NotEditableLineStyle(container, "Fixed");
            _parameterStyles[7] = new HiddenStyle(container, "Hidden");
        } catch (NameDuplicationException ex) {
            throw new InternalErrorException(ex.getMessage());
        }

        Iterator parameters = object.attributeList(Settable.class).iterator();

        while (parameters.hasNext()) {
            Settable param = (Settable) parameters.next();

            // Skip if the parameter is not visible.
            if (!Configurer.isVisible(_object, param)) {
                continue;
            }

            // Get the current style.
            boolean foundOne = false;
            Iterator styles = ((NamedObj) param).attributeList(
                    ParameterEditorStyle.class).iterator();
            ParameterEditorStyle foundStyle = null;

            while (styles.hasNext()) {
                foundOne = true;
                foundStyle = (ParameterEditorStyle) styles.next();
            }

            List styleList = new ArrayList();

            // The index of the default;
            int defaultIndex = 0;

            _originalExpertMode = _object.getAttribute("_expertMode") != null;
            if (param.getVisibility() == Settable.NOT_EDITABLE
                    && !_originalExpertMode && !foundOne) {
                // If the parameter is set to NOT_EDITABLE visibility and not expert mode
                // then only a fixed style is possible.
                styleList.add("Fixed");
                defaultIndex = 0;
            } else {
                int count = 0;

                // Reduce the list of parameters
                for (int i = 0; i < _parameterStyles.length
                        && _parameterStyles[i] != null; i++) {
                    if (foundOne
                            && _parameterStyles[i].getClass() == foundStyle
                            .getClass()) {
                        defaultIndex = count;

                        if (foundStyle.acceptable(param)) {
                            styleList.add(_parameterStyles[i].getName());
                            count++;
                        }
                    } else if (_parameterStyles[i].acceptable(param)) {
                        styleList.add(_parameterStyles[i].getName());
                        count++;
                    }
                }
            }

            String[] styleArray = (String[]) styleList
                    .toArray(new String[styleList.size()]);

            addChoice(param.getName(), param.getName(), styleArray,
                    styleArray[defaultIndex]);
        }

        // Add the expert mode box.
        addCheckBox("expertMode", "expert mode", _originalExpertMode);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate a change request to apply the changes.
     *  This is called to notify that one of the entries has changed.
     *  The name of the entry is passed as an argument.
     *  @param name The name of the entry.
     */
    @Override
    public void changed(String name) {
        StringBuffer moml = new StringBuffer();

        // Treat the expertMode entry specially.
        if (name.equals("expertMode")) {
            Attribute previousExpert = _object.getAttribute("_expertMode");
            boolean isExpert = previousExpert != null;
            boolean toExpert = getBooleanValue("expertMode");

            if (isExpert != toExpert) {
                if (isExpert) {
                    moml.append("<deleteProperty name=\"_expertMode\"/>");
                } else {
                    moml.append("<property name=\"_expertMode\" "
                            + "class=\"ptolemy.kernel.util.SingletonAttribute\"/>");
                }
            }
        } else {
            // Entry is not expertMode.
            // Figure out which style is being requested.
            ParameterEditorStyle found = null;

            for (int i = 0; i < _parameterStyles.length && found == null; i++) {
                if (getStringValue(name).equals(_parameterStyles[i].getName())) {
                    found = _parameterStyles[i];
                }
            }

            // First remove all pre-existing styles.
            moml.append("<group>");

            Attribute param = _object.getAttribute(name);
            moml.append("<property name=\"" + param.getName() + "\">");

            Iterator styles = param.attributeList(ParameterEditorStyle.class)
                    .iterator();
            boolean foundOne = false;

            while (styles.hasNext()) {
                foundOne = true;

                ParameterEditorStyle style = (ParameterEditorStyle) styles
                        .next();
                moml.append("<deleteProperty name=\"" + style.getName()
                        + "\"/>\n");
            }

            if (foundOne) {
                // Have to close and re-open the context to ensure
                // that deletions occur before additions.
                moml.append("</property>");
                moml.append("<property name=\"" + param.getName() + "\">");
            }

            moml.append("<group name=\"auto\">");
            if (found != null) {
                // Coverity: found could be null if there was an internal error
                // and the parameter style was not found/
                moml.append(found.exportMoML("style"));
            }
            moml.append("</group></property></group>");
        }

        MoMLChangeRequest change = new MoMLChangeRequest(this, _object,
                moml.toString());
        _object.requestChange(change);
    }

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
        Top.deferIfNecessary(new Runnable() {
            @Override
            public void run() {
                // Treat the expertMode entry specially.
                Attribute currentExpert = _object.getAttribute("_expertMode");
                boolean isExpert = currentExpert != null;

                if (isExpert != _originalExpertMode) {
                    try {
                        if (isExpert) {
                            currentExpert.setContainer(null);
                        } else {
                            // FIXME: This won't propagate.
                            new Attribute(_object, "_expertMode");
                        }
                    } catch (KernelException e) {
                        // This should not occur.
                        throw new InternalErrorException(e);
                    }
                }

                // FIXME: This code is nonsensical... _originalValues never
                // gets anything added to it!
                Iterator entries = _originalValues.entrySet().iterator();

                while (entries.hasNext()) {
                    Map.Entry entry = (Map.Entry) entries.next();
                    Settable param = (Settable) _object
                            .getAttribute((String) entry.getKey());

                    try {
                        param.setExpression((String) entry.getValue());
                    } catch (IllegalActionException ex) {
                        throw new InternalErrorException(
                                "Cannot restore style value!");
                    }
                }
            }
        });
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The object that this configurer configures.
    private NamedObj _object;

    // Indicator of what the expert mode was upon entry.
    private boolean _originalExpertMode = false;

    // The original values of the parameters.
    private Map _originalValues = new HashMap();

    // The list of the possible styles.
    private ParameterEditorStyle[] _parameterStyles;
}
