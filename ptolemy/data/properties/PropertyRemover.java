/*
 * Below is the copyright agreement for the Ptolemy II system.
 * 
 * Copyright (c) 2008-2009 The Regents of the University of California. All
 * rights reserved.
 * 
 * Permission is hereby granted, without written agreement and without license
 * or royalty fees, to use, copy, modify, and distribute this software and its
 * documentation for any purpose, provided that the above copyright notice and
 * the following two paragraphs appear in all copies of this software.
 * 
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN
 * "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 */
package ptolemy.data.properties;

import java.util.Iterator;

import ptolemy.actor.parameters.SharedParameter;
import ptolemy.data.ObjectToken;
import ptolemy.data.properties.gui.PropertyDisplayGUIFactory;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

/**
 * PropertyRemover class.
 *
 * @author mankit
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class PropertyRemover extends Attribute {

    public PropertyRemover(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-50\" y=\"-20\" width=\"120\" height=\"40\" "
                + "style=\"fill:white\"/>" + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:black\">"
                + "Double click to\nRemove Properties</text></svg>");

        new PropertyDisplayGUIFactory(this, "_portValueSolverGUIFactory");

        sharedUtilitiesWrapper = new SharedParameter(this,
                "sharedUtilitiesWrapper", PropertySolver.class);

        // Create a new shared utilities object (only once).
        if (sharedUtilitiesWrapper.getExpression().length() == 0) {
            sharedUtilitiesWrapper.setToken(new ObjectToken(
                    new SharedUtilities()));
        }
        _sharedUtilities = (SharedUtilities) ((ObjectToken) sharedUtilitiesWrapper
                .getToken()).getValue();
    }

    public void removeProperties(CompositeEntity component)
            throws IllegalActionException {

        Iterator solvers = PropertySolverBase.getAllSolvers(
                sharedUtilitiesWrapper).iterator();

        while (solvers.hasNext()) {
            PropertySolver solver = (PropertySolver) solvers.next();

            // Clear trained exception.
            Attribute trainedException = solver.getTrainedExceptionAttribute();
            if (trainedException != null) {
                try {
                    trainedException.setContainer(null);

                } catch (NameDuplicationException ex) {
                    assert false;
                }
            }

            PropertyHelper adapter = solver.getHelper(component);
            removeProperties(adapter);
        }

        // Update the GUI.
        requestChange(new ChangeRequest(this, "Repaint the GUI.") {
            protected void _execute() throws Exception {
            }
        });
        // PropertyLattice.resetAll();
    }

    public void removeProperties(PropertyHelper adapter)
            throws IllegalActionException {
        Iterator propertyables = adapter.getPropertyables(NamedObj.class)
                .iterator();

        while (propertyables.hasNext()) {
            NamedObj propertyable = (NamedObj) propertyables.next();
            _removePropertyAttributes(propertyable);
        }

        // Recursive case.
        Iterator subHelpers = adapter._getSubHelpers().iterator();

        while (subHelpers.hasNext()) {
            PropertyHelper subHelper = (PropertyHelper) subHelpers.next();
            removeProperties(subHelper);
        }
    }

    private void _removePropertyAttributes(NamedObj namedObj)
            throws IllegalActionException {
        Iterator attributeIterator = namedObj.attributeList(
                PropertyAttribute.class).iterator();

        Attribute attribute;
        while (attributeIterator.hasNext()) {
            attribute = (Attribute) attributeIterator.next();

            try {
                attribute.setContainer(null);
            } catch (NameDuplicationException e) {
                assert false;
            }
            //String moml = "<deleteProperty name=\"" + attribute.getName() + "\"/>";
            //namedObj.requestChange(new MoMLChangeRequest(this, namedObj, moml));
        }

        attribute = namedObj.getAttribute("_showInfo");
        if (attribute != null) {

            try {
                attribute.setContainer(null);
            } catch (NameDuplicationException e) {
                assert false;
            }
            //String moml = "<deleteProperty name=\"_showInfo\"/>";
            //namedObj.requestChange(new MoMLChangeRequest(this, namedObj, moml));
        }

        attribute = namedObj.getAttribute("_highlightColor");
        if (attribute != null) {

            try {
                attribute.setContainer(null);
            } catch (NameDuplicationException e) {
                assert false;
            }
            //String moml = "<deleteProperty name=\"" + attribute.getName() + "\"/>";
            //namedObj.requestChange(new MoMLChangeRequest(this, namedObj, moml));
        }
    }

    public SharedParameter sharedUtilitiesWrapper;

    protected SharedUtilities _sharedUtilities;
}
