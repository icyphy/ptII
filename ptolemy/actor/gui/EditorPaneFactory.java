/* An attribute that creates an editor pane to configure its container.

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
import ptolemy.kernel.util.*;
import ptolemy.data.expr.Parameter;
import ptolemy.actor.gui.style.*;

// Java imports.
import java.awt.Component;
import java.util.Iterator;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

//////////////////////////////////////////////////////////////////////////
//// EditorPaneFactory
/**
This is an attribute that can create a pane (called a "configuration
widget") for interactively configuring its container.  This attribute
is used by an instance of Configurer.

In this base class, the createEditorPane() method creates an
instance of PtolemyQuery with one entry for each parameter in
the container. This is the default mechanism
for editing parameters.  Derived classes may override this
method to present radically different interfaces to the user.
For example, a digital filter actor could present a filter
design interface.  A plotter actor could present a panel for
configuring a plot.  A file reader actor could present a file
browser.

@see Configurer
@author Steve Neuendorffer and Edward A. Lee
@version $Id$
*/

public class EditorPaneFactory extends Attribute {

    /** Construct a factory with the specified container and name.
     *  @param container The container.
     *  @param name The name of the factory.
     *  @exception IllegalActionException If the factory is not of an
     *   acceptable attribute for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public EditorPaneFactory(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Return a new widget for configuring the container.
     *  @return A new widget for configuring the container.
     */
    public Component createEditorPane() {
        PtolemyQuery query = new PtolemyQuery();
        query.setTextWidth(25);
        NamedObj container = (NamedObj)getContainer();
        Iterator params
            = container.attributeList(Parameter.class).iterator();
        boolean foundOne = false;
        while (params.hasNext()) {
            foundOne = true;
            Parameter param = (Parameter)params.next();
	    query.addStyledEntry(param);
        }
        if (!foundOne) {
            return new JLabel(container.getName() + " has no parameters.");
        }
        return query;
    }
}
