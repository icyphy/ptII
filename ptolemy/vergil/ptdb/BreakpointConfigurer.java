/* A GUI widget for configuring breakpoints.

 Copyright (c) 1998-2002 The Regents of the University of California.
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

@ProposedRating Red (celaine@eecs.berkeley.edu)
@AcceptedRating Red (celaine@eecs.berkeley.edu)
*/

package ptolemy.vergil.ptdb;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.DebugListener;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.TransientSingletonConfigurableAttribute;
import ptolemy.gui.Query;
import ptolemy.gui.QueryListener;
import ptolemy.vergil.basic.BasicGraphController;
import ptolemy.vergil.basic.BasicGraphFrame;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import javax.swing.BoxLayout;

//////////////////////////////////////////////////////////////////////////
//// BreakpointConfigurer
/**
This class is an editor to configure the ports of an object.
It supports setting their input, output, and multiport properties,
and adding and removing ports.  Only ports that extend the TypedIOPort
class are listed, since more primitive ports cannot be configured
in this way.

@see ptolemy.actor.gui.PortConfigurer

@author Elaine Cheong
@version $Id$
*/

public class BreakpointConfigurer extends Query implements QueryListener {

    /** Construct a port configurer for the specified entity.
     *  @param object The entity to configure.
     */
    public BreakpointConfigurer(Entity object, BasicGraphController graphController) {
        super();
	this.addQueryListener(this);

	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	setTextWidth(15);

        _object = object;
        _graphController = graphController;

        // Temporary variable for holding which (before and/or after)
        // firing event breakpoint options have already been selected.
        Set selectedValues;

        // prefire
        selectedValues = new HashSet();
        if ((NamedObj)object.getAttribute("BEFORE_PREFIRE") != null) {
            selectedValues.add("before");
        }
        if ((NamedObj)object.getAttribute("AFTER_PREFIRE") != null)
            selectedValues.add("after");
        addSelectButtons("prefire", "prefire", _optionsArray, selectedValues);

        // fire
        selectedValues = new HashSet();
        if ((NamedObj)object.getAttribute("BEFORE_FIRE") != null) {
            selectedValues.add("before");
        }
        if ((NamedObj)object.getAttribute("AFTER_FIRE") != null)
            selectedValues.add("after");
        addSelectButtons("fire", "fire", _optionsArray, selectedValues);

        // postfire
        selectedValues = new HashSet();
        if ((NamedObj)object.getAttribute("BEFORE_POSTFIRE") != null) {
            selectedValues.add("before");
        }
        if ((NamedObj)object.getAttribute("AFTER_POSTFIRE") != null)
            selectedValues.add("after");
        addSelectButtons("postfire", "postfire", _optionsArray, selectedValues);

        // iterate
        selectedValues = new HashSet();
        if ((NamedObj)object.getAttribute("BEFORE_ITERATE") != null) {
            selectedValues.add("before");
        }
        if ((NamedObj)object.getAttribute("AFTER_ITERATE") != null)
            selectedValues.add("after");
        addSelectButtons("iterate", "iterate", _optionsArray, selectedValues);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Apply the changes by configuring the breakpoints that have changed.
     *
     *  FIXME: how to deal with NameDuplicationException,
     *  IllegalActionException?
     */
    public void apply() {
        Iterator items = _changed.iterator();
        boolean noneSelected = true;
        try {
            // For each firing event breakpoint that has changed,
            // process new selection.
            while (items.hasNext()) {
                String item = (String)items.next();
                String value = stringValue(item);

                // First, parse the value, which may be a comma-separated list.
                Set selectedValues = new HashSet();
                StringTokenizer tokenizer = new StringTokenizer(value, ",");
                while (tokenizer.hasMoreTokens()) {
                    selectedValues.add(tokenizer.nextToken().trim());
                }

                // selectedValues now contains either nothing, or
                // "before" and/or "after".
                if (selectedValues.contains("before")) {
                    noneSelected = false;
                    TransientSingletonConfigurableAttribute attribute =
                        new TransientSingletonConfigurableAttribute(_object,
                                new String("BEFORE_" + item.toUpperCase()));
                } else {
                    Attribute oldAttribute = _object.getAttribute(
                            new String("BEFORE_" + item.toUpperCase()));
                    if (oldAttribute != null) {
                        // remove the attribute
                        oldAttribute.setContainer(null);
                    }
                }
                if (selectedValues.contains("after")) {
                    noneSelected = false;
                    TransientSingletonConfigurableAttribute attribute =
                        new TransientSingletonConfigurableAttribute(_object,
                                new String("AFTER_" + item.toUpperCase()));
                } else {
                    Attribute oldAttribute = _object.getAttribute(
                            new String("AFTER_" + item.toUpperCase()));
                    if (oldAttribute != null) {
                        // remove the attribute
                        oldAttribute.setContainer(null);
                    }
                }
            }

            // Check if there is already a DebugListener for this _object.
            DebugListener listener = (DebugListener) _object.getAttribute("DebugController");
            if (listener == null) {
                // Register a new DebugListener with the director.
                BasicGraphFrame frame = _graphController.getFrame();
                DebugController debugController =
                    new DebugController(_object, _graphController);

                Director director = ((Actor)frame.getModel()).getDirector();
                if (director != null) {
                    director.addDebugListener(debugController);
                }
            } else {
                // Remove debug listener if there are no longer any
                // breakpoints for this _object
                if (noneSelected) {
                    BasicGraphFrame frame = _graphController.getFrame();
                    Director director = ((Actor)frame.getModel()).getDirector();
                    if (director != null) {
                        director.removeDebugListener(listener);
                        ((Attribute)listener).setContainer(null);
                    }
                }
            }

        } catch (NameDuplicationException e1) {
            // do nothing
            // FIXME: should we do something here?
        } catch (IllegalActionException e2) {
            // do nothing
            // FIXME: should we do something here?
        }
    }

    /** Called to notify that one of the entries has changed.
     *  This simply sets a flag that enables application of the change
     *  when the apply() method is called.
     *  @param name The name of the entry that changed.
     */
    public void changed(String name) {
        _changed.add(name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The set of names of breakpoint options (before/after firing
    // event) that have changed.
    private Set _changed = new HashSet();

    // The object that this configurer configures.
    private Entity _object;

    // The possible configurations for a firing event breakpoint.
    private String[] _optionsArray = {"before", "after"};

    // The GraphController associated with _object.
    private BasicGraphController _graphController;
}
