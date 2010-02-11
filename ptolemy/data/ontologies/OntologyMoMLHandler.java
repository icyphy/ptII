/* An attribute that helps an OntologySolver to issue MoML requests and
 make changes to the model.

 Copyright (c) 2006-2010 The Regents of the University of California.
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

import java.util.List;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.gui.ColorAttribute;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;

///////////////////////////////////////////////////////////////////
//// OntologyMoMLHandler

/**
 This is an attribute used by the PropertySolver to issue MoML requests and
 make changes to the model. These changes include addition, update, or deletion
 of property annotations and display of the property results.
 This is designed to be contained by an instance of PropertySolver
 or a subclass of PropertySolver. It contains parameters that allow
 users to configure the display of the property annotation results.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class OntologyMoMLHandler extends Attribute {

    /** Construct an OntologyMoMLHandler with the specified container and name.
     *  @param container The container.
     *  @param name The name of the OntologyMoMLHandler.
     *  @exception IllegalActionException If the OntologyMoMLHandler is not of an
     *   acceptable attribute for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public OntologyMoMLHandler(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        showText = new Parameter(this, "showText");
        showText.setTypeEquals(BaseType.BOOLEAN);
        showText.setExpression("true");

        highlight = new Parameter(this, "highlight");
        highlight.setTypeEquals(BaseType.BOOLEAN);
        highlight.setExpression("true");

        // FIXME: we should check if the container is
        // a PropertySolver.
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /**
     * Indicate whether the _showInfo attributes will be set.
     */
    public Parameter showText;

    /**
     * Indicate whether the _highlightColor attributes will be set.
     */
    public Parameter highlight;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Remove the highlighting and visible annotations
     * for all property-able objects.
     */
    public void clearDisplay() {

        // Get the PropertySolver.
        OntologySolver solver = (OntologySolver) getContainer();
        StringBuffer completeMoML = new StringBuffer("<group>");
        try {
            for (Object propertyable : solver.getAllPropertyables()) {
                if (propertyable instanceof NamedObj) {
                    NamedObj namedObj = (NamedObj) propertyable;

                    String request = "";
                    if (namedObj.getAttribute("_showInfo") != null) {
                        request += "<deleteProperty name=\"_showInfo\"/>";
                    }
                    if (namedObj.getAttribute("_highlightColor") != null) {
                        request += "<deleteProperty name=\"_highlightColor\"/>";
                    }
                    request = _completeHierarchyInMoML(namedObj, request);

                    completeMoML.append(request);
                }
            }
        } catch (IllegalActionException e1) {
            assert false;
        }

        completeMoML.append("</group>");

        _requestChange(completeMoML.toString());

    }

    /**
     * Clear the property annotations of associated with
     * the container solver. This deletes all the trained
     * data, which includes the trained exception attribute,
     * used by regression testing.
     */
    public void clearProperties() {
        // Get the PropertySolver.
        OntologySolver solver = (OntologySolver) getContainer();
        StringBuffer completeMoML = new StringBuffer("<group>");

        try {
            for (Object propertyable : solver.getAllPropertyables()) {
                if (propertyable instanceof NamedObj) {

                    /* FIXME
                    String attributeName = solver.getExtendedUseCaseName();
                    ConceptAttribute attribute = (ConceptAttribute) namedObj
                            .getAttribute(attributeName);

                    if (attribute != null) {
                        String request = "<deleteProperty name=\""
                                + attributeName + "\"/>";
                        request = _completeHierarchyInMoML(namedObj, request);

                        completeMoML.append(request);
                    }
                    */
                }
            }
        } catch (IllegalActionException e) {
            assert false;
        }

        completeMoML.append("</group>");

        _requestChange(completeMoML.toString());
    }

    /** Highlight all property-able objects with
     *  the specified colors for their property values.
     *  @throws IllegalActionException If getting the resolved concept fails.
     */
    public void highlightProperties() throws IllegalActionException {
        // Get the PropertySolver.
        OntologySolver solver = (OntologySolver) getContainer();
        // FIXME: Issuing a distinct change request for each highlight
        // request will be quite inefficient. How to batch the change requests?
        for (Object propertyable : solver.getAllPropertyables()) {
            if (propertyable instanceof NamedObj) {
                Concept concept = solver.getResolvedConcept(propertyable, false);
                if (concept != null) {
                    // Use the color in the concept instance.
                    List<ColorAttribute> colors = concept.attributeList(ColorAttribute.class);
                    if (colors != null && colors.size() > 0) {
                        // ConceptIcon renders the first found ColorAttribute,
                        // so we use that one here as well.
                        ColorAttribute conceptColor = colors.get(0);
                        String request = "<property name=\"_highlightColor\" "
                            + "class=\"ptolemy.actor.gui.ColorAttribute\" value=\""
                            + conceptColor.getExpression()
                            + "\"/>";
                        // FIXME: Really should have a constructor for
                        // MoMLChangeRequest with an extra argument to mark
                        // this as a non-structural change.
                        // Marking this as a non-structural change prevents a
                        // repaint from happening, which is a good idea since we
                        // are going to issue a lot of these change requests.
                        MoMLChangeRequest change = new MoMLChangeRequest(this, (NamedObj)propertyable, request) {
                            public boolean isStructuralChange() {
                                return false;
                            }
                        };
                        ((NamedObj)propertyable).requestChange(change);
                    }
                }
            }
        }
        // Force a single repaint after all the above requests have been processed.
        solver.requestChange(new MoMLChangeRequest(this, solver, "<group/>"));
    }

    /**
     * If the value of the showText parameter is set to
     * true, show all property values visually.
     * Otherwise, do nothing.
     */
    public void showProperties() {
        StringBuffer completeMoML = new StringBuffer("<group>");

        // Get the PropertySolver.
        OntologySolver solver = (OntologySolver) getContainer();
        try {
            for (Object propertyable : solver.getAllPropertyables()) {

                if (propertyable instanceof NamedObj) {
                    NamedObj namedObj = (NamedObj) propertyable;

                    Concept property = solver.getResolvedConcept(namedObj,
                            false);

                    completeMoML.append(_getMoMLShowInfoString(namedObj,
                            property));
                }
            }
        } catch (IllegalActionException e) {
            assert false;
        }
        completeMoML.append("</group>");

        _requestChange(completeMoML.toString());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Wrap the given MoML request string with extra enclosing tags
     * according to the relation of the specified namedObj to the
     * toplevel context. This is used to issue a complete MoML request
     * at the toplevel context.
     * @param namedObj The specified namedObj.
     * @param request The given MoML request.
     */
    private String _completeHierarchyInMoML(NamedObj namedObj, String request) {
        // Extend the MoML request.
        NamedObj momlContainer = namedObj;
        if (momlContainer != null) {

            // We don't need to specify the toplevel because
            // that is the context which we will execute the
            // change request.
            while (momlContainer.getContainer() != null) {
                String type = momlContainer.getElementName();
                request = "<" + type + " name=\"" + momlContainer.getName()
                        + "\" class=\"" + momlContainer.getClassName() + "\">"
                        + request + "</" + type + ">";

                momlContainer = momlContainer.getContainer();
            }
        }
        return request;
    }

    /**
     * Return a MoML request string that creates or updates
     * the _showInfo attribute of the given property-able
     * object, according to the given property value.
     * If the given property is null, this would issue
     * delete request to remove the _showInfo attribute,
     * if there exists any.
     * @param propertyable The given property-able object.
     * @param property The given property.
     */
    private String _getMoMLShowInfoString(NamedObj propertyable,
            Concept property) {

        String request;
        String propertyString;
        if (property != null) {
            propertyString = property.toString();

        } else {
            propertyString = "";
        }

        //StringParameter showAttribute = (StringParameter) propertyable
        //        .getAttribute("_showInfo");

        // Update the _showInfo attribute.
        request = "<property name=\"_showInfo\" class=\"ptolemy.data.expr.StringParameter\" value=\""
                + propertyString + "\"/>";

        request = _completeHierarchyInMoML(propertyable, request);
        return request;

    }

    /**
     * Create and request an undo-able MoMLChangeRequest for the given
     * MoML string. The context of the request is set to the toplevel.
     * @param moml The given moml string that contains the change request.
     */
    private void _requestChange(String moml) {
        // FIXME: we can only undo at the toplevel.
        NamedObj toplevel = toplevel();
        MoMLChangeRequest request = new MoMLChangeRequest(this, toplevel, moml);
        request.setUndoable(true);
        ((TypedCompositeActor) toplevel).requestChange(request);
    }
}
