/* An attribute that helps a PropertySolver to issue MoML requests and
 make changes to the model.

 Copyright (c) 2006-2009 The Regents of the University of California.
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
package ptolemy.data.properties;

import java.util.List;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.gui.ColorAttribute;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.properties.token.PropertyTokenSolver;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.moml.MoMLChangeRequest;

//////////////////////////////////////////////////////////////////////////
//// PropertyHighlighter

/**
 This is an attribute used by the PropertySolver to issue MoML requests and
 make changes to the model. These changes include addition, update, or deletion
 of property annotations and display of the property results.
 This is designed to be contained by an instance of PropertySolver 
 or a subclass of PropertySolver. It contains parameters that allow 
 users to configure the display of the property annotation results.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class PropertyMoMLHandler extends Attribute {

    /** Construct a PropertyMoMLHandler with the specified container and name.
     *  @param container The container.
     *  @param name The name of the PropertyMoMLHandler.
     *  @exception IllegalActionException If the PropertyMoMLHandler is not of an
     *   acceptable attribute for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public PropertyMoMLHandler(NamedObj container, String name)
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
     * Clear every manual annotated constraints associated with the 
     * solver use-case. Each of these constraints is an
     * AnnotationAttribute in the model.
     * @exception IllegalActionException Thrown if an error occurs
     *  when removing the annotation attributes.
     */
    public void clearAnnotations() throws IllegalActionException {
        PropertySolver solver = (PropertySolver) getContainer();

        StringBuffer completeMoML = new StringBuffer("<group>");

        for (PropertyHelper helper : solver.getAllHelpers()) {
            if (helper.getComponent() instanceof NamedObj) {
                NamedObj namedObj = (NamedObj) helper.getComponent();

                for (AnnotationAttribute attribute : (List<AnnotationAttribute>) namedObj
                        .attributeList(AnnotationAttribute.class)) {

                    if (solver.isIdentifiable(attribute.getUseCaseIdentifier())) {

                        String request = "<deleteProperty name=\"" + attribute.getName() + "\"/>";
                        request = _completeHierarchyInMoML(namedObj, request);

                        completeMoML.append(request);
                    }
                }
            }
        }
        completeMoML.append("</group>");
        _requestChange(completeMoML.toString());
    }

    /**
     * Remove the highlighting and visible annotations
     * for all property-able objects.
     */
    public void clearDisplay() {

        // Get the PropertySolver.
        PropertySolver solver = (PropertySolver) getContainer();
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
        PropertySolver solver = (PropertySolver) getContainer();
        StringBuffer completeMoML = new StringBuffer("<group>");

        try {
            for (Object propertyable : solver.getAllPropertyables()) {
                if (propertyable instanceof NamedObj) {
                    NamedObj namedObj = (NamedObj) propertyable;

                    String attributeName = solver.getExtendedUseCaseName();
                    PropertyAttribute attribute = (PropertyAttribute) namedObj
                    .getAttribute(attributeName);

                    if (attribute != null) {
                        String request = "<deleteProperty name=\"" + attributeName + "\"/>";
                        request = _completeHierarchyInMoML(namedObj, request);

                        completeMoML.append(request);
                    }
                }
            }
        } catch (IllegalActionException e) {
            assert false;
        }

        // Delete the trained exception attribute.
        Attribute trainedException = solver.getTrainedExceptionAttribute();
        if (trainedException != null) {
            String request = "<deleteProperty name=\"" +
            solver.getTrainedExceptionAttributeName() + "\"/>";
            request = _completeHierarchyInMoML(solver, request);
            completeMoML.append(request);
        }

        completeMoML.append("</group>");

        _requestChange(completeMoML.toString());
    }

    /**
     * Highlight all property-able objects with
     * the specified colors for their property values,
     * if the highlight parameter value is true.
     * Otherwise, do nothing.
     */
    public void highlightProperties() {
        StringBuffer completeMoML = new StringBuffer("<group>");

        // Get the PropertySolver.
        PropertySolver solver = (PropertySolver) getContainer();
        try {
            for (Object propertyable : solver.getAllPropertyables()) {

                if (propertyable instanceof NamedObj) {
                    NamedObj namedObj = (NamedObj) propertyable;

                    Property property = solver.getResolvedProperty(namedObj, false);

                    completeMoML.append(_getMoMLHighlightString(namedObj, property));
                }
            }
        } catch (IllegalActionException ex) {
            assert false;
        }
        completeMoML.append("</group>");

        _requestChange(completeMoML.toString());
    }

    /**
     * If the value of the showText parameter is set to
     * true, show all property values visually.
     * Otherwise, do nothing.
     */
    public void showProperties() {
        StringBuffer completeMoML = new StringBuffer("<group>");

        // Get the PropertySolver.
        PropertySolver solver = (PropertySolver) getContainer();
        try {
            for (Object propertyable : solver.getAllPropertyables()) {

                if (propertyable instanceof NamedObj) {
                    NamedObj namedObj = (NamedObj) propertyable;

                    Property property = solver.getResolvedProperty(namedObj, false);

                    completeMoML.append(_getMoMLShowInfoString(namedObj, property));
                }
            }
        } catch (IllegalActionException e) {
            assert false;
        }
        completeMoML.append("</group>");

        _requestChange(completeMoML.toString());
    }

    ///////////////////////////////////////////////////////////////////
    ////                   private methods                         ////

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
                request = "<" + type + " name=\"" + momlContainer.getName() +
                "\" class=\"" + momlContainer.getClassName() + "\">" +
                request + "</" + type + ">";

                momlContainer = momlContainer.getContainer();
            }
        }
        return request;
    }

    /**
     * Return a MoML request string that create or update
     * the _highlightColor attribute of the given property-able
     * object, according to the given property value. 
     * If the given property is null, this would issue
     * delete request to remove the _highlightColor attribute, 
     * if there exists any.
     * @param propertyable The given property-able object.
     * @param property The given property.
     */
    private String _getMoMLHighlightString(NamedObj propertyable,
            Property property) {

        String request;
        String propertyString;
        if (property != null) {
            propertyString = property.toString();

        } else if (getContainer() instanceof PropertyTokenSolver) {
            propertyString = "";//Token.NIL.toString();
        } else {
            propertyString = "";
        }

        if (property != null && property.getColor().length() > 0) {
            request = "<property name=\"_highlightColor\" " +
            "class=\"ptolemy.actor.gui.ColorAttribute\" value=\"" +
            property.getColor() + "\"/>";
            request = _completeHierarchyInMoML(propertyable, request);
            return request;
        } 


        //Highlight Propertyable namedObj's.
        for (ColorAttribute colorAttribute : (List<ColorAttribute>)
                attributeList(ColorAttribute.class)) {

            String colorAttrName = colorAttribute.getName();
            if (colorAttrName.endsWith("HighlightColor")) {

                String propertyAttrName = colorAttrName
                .substring(0, colorAttrName.length() - 14);

                Attribute attribute = getAttribute(propertyAttrName);

                if (attribute != null && attribute instanceof StringAttribute) {

                    String propertyToHighlight = ((StringAttribute) attribute).getExpression();

                    if (propertyToHighlight.equals(propertyString)) {

                        ColorAttribute highlightAttribute =
                            (ColorAttribute) propertyable.getAttribute("_highlightColor");
                        
                        if (property == null && highlightAttribute != null) {
                            // Remove the _highlightColor attribute if we don't have
                            // any property to display.
                            request = "<deleteProperty name=\"_highlightColor\"/>";
                        } else {
                            request = "<property name=\"_highlightColor\" " +
                            "class=\"ptolemy.actor.gui.ColorAttribute\" value=\"" +
                            colorAttribute.getExpression() + "\"/>";
                        }
                        request = _completeHierarchyInMoML(propertyable, request);
                        return request;
                    }
                }
            }
        }

        return "";
    }

    /**
     * Return a MoML request string that create or update
     * the _showInfo attribute of the given property-able
     * object, according to the given property value. 
     * If the given property is null, this would issue
     * delete request to remove the _showInfo attribute, 
     * if there exists any.
     * @param propertyable The given property-able object.
     * @param property The given property.
     */
    private String _getMoMLShowInfoString(NamedObj propertyable, Property property) {

        String request;
        String propertyString;
        if (property != null) {
            propertyString = property.toString();

        } else if (getContainer() instanceof PropertyTokenSolver) {
            // FIXME: If we set propertyString to NIL, then
            // we will have nils everywhere when we don't
            // have any resolved properties.
            propertyString = ""; //Token.NIL.toString();
        } else {
            propertyString = "";
        }

        StringParameter showAttribute =
            (StringParameter) propertyable.getAttribute("_showInfo");

        if (property == null && showAttribute != null) {
            // Remove the showInfo attribute if we don't have
            // any property to display.
            request = "<deleteProperty name=\"_showInfo\"/>";

        } else {
            // Update the _showInfo attribute.
            request = "<property name=\"_showInfo\" class=\"ptolemy.data.expr.StringParameter\" value=\"" +
            propertyString +
            "\"/>";
        }

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
        ((TypedCompositeActor)toplevel).requestChange(request);
    }
}
