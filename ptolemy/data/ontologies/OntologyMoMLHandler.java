/* An attribute that helps an OntologySolver to issue MoML requests and
 make changes to the model.

 Copyright (c) 2006-2014 The Regents of the University of California.
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
package ptolemy.data.ontologies;

import java.util.Set;

import ptolemy.actor.gui.ColorAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;

///////////////////////////////////////////////////////////////////
//// OntologyMoMLHandler

/** This is an attribute used by the OntologySolver to issue MoML requests and
 *  make changes to the model. These changes include addition, update, or deletion
 *  of concept annotations and display of the concept results.
 *  This is designed to be contained by an instance of OntologySolver
 *  or a subclass of OntologySolver. It contains parameters that allow
 *  users to configure the display of the concept annotation results.
 *
 *  @author Ben Lickly, Man-Kit Leung
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (mankit)
 *  @Pt.AcceptedRating Red (mankit)
 */
public class OntologyMoMLHandler extends Attribute {

    /** Construct an OntologyMoMLHandler with the specified container and name.
     *  @param container The container which should be an instance of OntologySolver.
     *  @param name The name of the OntologyMoMLHandler.
     *  @exception IllegalActionException If the OntologyMoMLHandler is not of an
     *   acceptable attribute for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public OntologyMoMLHandler(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Remove the highlighting and visible annotations
     *  for all property-able objects.
     *  @param colors True if the highlight colors should be cleared.
     *  @param text True if the ontology concept annotation text should be cleared.
     *  @exception IllegalActionException If getting the resolved concept fails.
     */
    public void clearDisplay(boolean colors, boolean text)
            throws IllegalActionException {
        if (colors || text) {
            // Get the OntologySolver.
            OntologySolver solver = (OntologySolver) getContainer();
            for (Object propertyable : solver.getAllPropertyables()) {
                if (propertyable instanceof NamedObj) {
                    Concept concept = solver.getConcept(propertyable);
                    if (concept != null
                            || ((NamedObj) propertyable)
                            .getAttribute("_showInfo") != null
                            && colors
                            || ((NamedObj) propertyable)
                            .getAttribute("_highlightColor") != null
                            && text) {
                        String request = "<group>";
                        if (((NamedObj) propertyable).getAttribute("_showInfo") != null
                                && text) {
                            request += "<deleteProperty name=\"_showInfo\"/>";
                        }
                        if (((NamedObj) propertyable)
                                .getAttribute("_highlightColor") != null
                                && colors) {
                            request += "<deleteProperty name=\"_highlightColor\"/>";
                        }
                        request += "</group>";
                        MoMLChangeRequest change = new MoMLChangeRequest(this,
                                (NamedObj) propertyable, request, false);
                        ((NamedObj) propertyable).requestChange(change);
                    }
                }
            }

            // Force a single repaint after all the above requests have been processed.
            solver.requestChange(new MoMLChangeRequest(this, solver, "<group/>"));
        }
    }

    /** Highlight all property-able objects with
     *  the specified colors for their property values.
     *  @exception IllegalActionException If getting the resolved concept fails.
     */
    public void highlightConcepts() throws IllegalActionException {
        // Get the PropertySolver.
        OntologySolver solver = (OntologySolver) getContainer();
        highlightConcepts(solver.getAllPropertyables());
    }

    /** Highlight concepts that have already been resolved, but do not run solver.
     *  Otherwise, do nothing.
     *  @param objects The set of objects to highlight.
     *  @exception IllegalActionException Thrown if there is an error getting the
     *   colors for the resolved concept values.
     */
    public void highlightConcepts(Set<Object> objects)
            throws IllegalActionException {
        if (objects != null) {
            // Get the PropertySolver.
            OntologySolver solver = (OntologySolver) getContainer();

            for (Object object : objects) {
                if (object instanceof NamedObj) {
                    Concept concept = solver.getConcept(object);
                    if (concept != null) {
                        ColorAttribute conceptColor = concept.getColor();
                        if (conceptColor != null) {
                            String request = "<property name=\"_highlightColor\" "
                                    + "class=\"ptolemy.actor.gui.ColorAttribute\" value=\""
                                    + conceptColor.getExpression() + "\"/>";
                            MoMLChangeRequest change = new MoMLChangeRequest(
                                    this, (NamedObj) object, request, false);
                            ((NamedObj) object).requestChange(change);
                        }
                    }
                }
            }
            // Force a single repaint after all the above requests have been processed.
            solver.requestChange(new MoMLChangeRequest(this, solver, "<group/>"));
        }
    }

    /** Show all concept values as text annotations on each model element.
     *  @exception IllegalActionException If getting the resolved concept fails.
     */
    public void showConceptAnnotations() throws IllegalActionException {
        // Get the PropertySolver.
        OntologySolver solver = (OntologySolver) getContainer();
        for (Object propertyable : solver.getAllPropertyables()) {
            if (propertyable instanceof NamedObj) {
                Concept concept = solver.getConcept(propertyable);
                if (concept != null) {
                    String request = "<property name=\"_showInfo\" class=\"ptolemy.data.expr.StringParameter\" value=\""
                            + concept.toString() + "\"/>";
                    MoMLChangeRequest change = new MoMLChangeRequest(this,
                            (NamedObj) propertyable, request, false);
                    ((NamedObj) propertyable).requestChange(change);
                }
            }
        }
        // Force a single repaint after all the above requests have been processed.
        solver.requestChange(new MoMLChangeRequest(this, solver, "<group/>"));
    }
}
