/* The node controller for objects with icons.

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
package ptolemy.vergil.basic;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
 
import org.ptolemy.ssm.MirrorDecorator;
import org.ptolemy.ssm.MirrorDecoratorAttributes;

import ptolemy.actor.ExecutionAttributes; 
import ptolemy.actor.gui.ColorAttribute;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.Decorator;
import ptolemy.kernel.util.DecoratorAttributes;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.vergil.icon.EditorIcon;
import ptolemy.vergil.icon.XMLIcon;
import ptolemy.vergil.kernel.AnimationRenderer;
import ptolemy.vergil.kernel.AttributeController;
import ptolemy.vergil.kernel.ShadowRenderer;
import diva.canvas.Figure;
import diva.canvas.toolbox.SVGUtilities;
import diva.graph.GraphController;
import diva.graph.GraphModel;
import diva.graph.NodeRenderer;

///////////////////////////////////////////////////////////////////
//// IconController

/**
 This class provides interaction with nodes that represent Ptolemy II
 objects that are represented on screen as icons, such as attributes
 and entities.   It provides a double click binding to edit the parameters
 of the node, and a context menu containing a command to edit parameters
 ("Configure"). This adds to the base class the ability to render an
 icon for the object being controlled, where the icon is specified
 by a contained attribute of class EditorIcon (typically, but not
 necessarily named "_icon").

 @author Steve Neuendorffer and Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class IconController extends ParameterizedNodeController {
    /** Create a controller associated with the specified graph
     *  controller.
     *  @param controller The associated graph controller.
     */
    public IconController(GraphController controller) {
        super(controller);
        setNodeRenderer(new IconRenderer());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Map used to keep track of icons that have been created
     *  but not yet assigned to a container.
     */
    private static Map _iconsPendingContainer = new HashMap();

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** An icon renderer. */
    public class IconRenderer implements NodeRenderer {
        // FindBugs wants this static, but adding a constructor that takes
        // a GraphModel does not work.  If you create a model that has
        // a composite and then save it, dragging around the composite
        // results in duplicate composites.

        /**  Render a visual representation of the given node. If the
         * StringAttribute _color of the node is set then use that color to
         * highlight the node. If the StringAttribute _explanation of the node
         * is set then use it to set the tooltip.
         * @see diva.graph.NodeRenderer#render(java.lang.Object)
         */
        @Override
        public Figure render(Object n) {
            Locatable location = (Locatable) n;
            final NamedObj object = location.getContainer();

            // NOTE: this code is similar to that in PtolemyTreeCellRenderer
            Figure result = null;

            try {
                List iconList = object.attributeList(EditorIcon.class);

                // Check to see whether there is an icon that has been created,
                // but not inserted.
                if (iconList.size() == 0) {
                    XMLIcon alreadyCreated = (XMLIcon) _iconsPendingContainer
                            .get(object);

                    if (alreadyCreated != null) {
                        iconList.add(alreadyCreated);
                    }
                }

                // If there are still no icons, then we need to create one.
                if (iconList.size() == 0) {
                    // NOTE: This used to directly create an XMLIcon within
                    // the container "object". However, this is not cosher,
                    // since we may not be able to get write access on the
                    // workspace. We instead use a hack supported by XMLIcon
                    // to create an XMLIcon with no container (this does not
                    // require write access to the workspace), and specify
                    // to it what the container will eventually be. Then
                    // we queue a change request to make that the container.
                    // Further, we have to make a record of the figure, indexed
                    // by the object, in case some other change request is
                    // executed before this gets around to setting the
                    // container.  Otherwise, that second change request
                    // will result in the creation of a second figure.

                    final EditorIcon icon = XMLIcon.getXMLIcon(
                            object.workspace(), "_icon");
                    icon.setContainerToBe(object);
                    icon.setPersistent(false);
                    result = icon.createFigure();

                    // NOTE: Make sure this is done before the change request
                    // below is executed, which may be as early as when it is
                    // requested.
                    _iconsPendingContainer.put(object, icon);

                    // NOTE: Make sure the source of this change request is
                    // the graph model. Otherwise, this change request will
                    // trigger a redraw of the entire graph, which will result
                    // in another call to this very same method, which will
                    // result in creation of yet another figure before this
                    // method even returns!
                    GraphController controller = IconController.this
                            .getController();
                    GraphModel graphModel = controller.getGraphModel();
                    ChangeRequest request = new ChangeRequest(graphModel,
                            "Set the container of a new XMLIcon.") {
                        // NOTE: The KernelException should not be thrown,
                        // but if it is, it will be handled properly.
                        @Override
                        protected void _execute() throws KernelException {
                            _iconsPendingContainer.remove(object);

                            // If the icon already has a container, do nothing.
                            if (icon.getContainer() != null) {
                                return;
                            }

                            // If the container already has an icon, do nothing.
                            if (object.getAttribute("_icon") != null) {
                                return;
                            }

                            icon.setContainer(object);
                        }
                    };

                    request.setPersistent(false);
                    object.requestChange(request);
                } else if (iconList.size() >= 1) {
                    // Use only the last icon in the list.
                    EditorIcon icon = (EditorIcon) iconList
                            .get(iconList.size() - 1);
                    result = icon.createFigure();
                }
            } catch (KernelException ex) {
                throw new InternalErrorException(null, ex,
                        "Could not create icon " + "in " + object + " even "
                                + "though one did not previously exist.");
            }

            if (result == null) {
                throw new InternalErrorException("Failed to create icon.");
            } else {
                result.setToolTipText(object.getClassName());
            }

            // Check to see if it has
            // attributes that specify its color or an explanation.
            // Old way to specify a color.
            try {
                StringAttribute colorAttr = (StringAttribute) object
                        .getAttribute("_color", StringAttribute.class);
                if (colorAttr != null) {
                    String color = colorAttr.getExpression();
                    AnimationRenderer animationRenderer = new AnimationRenderer(
                            SVGUtilities.getColor(color));
                    animationRenderer.renderSelected(result);
                }
            } catch (IllegalActionException e) {
                // Ignore
            }

            // New way to specify a highlight color.
            AttributeController.renderHighlight(object, result);

            try {
                // clear highlighting
                Attribute highlightColor = object
                        .getAttribute("_decoratorHighlightColor");
                if (highlightColor != null) {
                    object.removeAttribute(highlightColor);
                }

                List<Decorator> decorators = new ArrayList();
                decorators.addAll(object.decorators());

                for (Decorator decorator : decorators) {
                    DecoratorAttributes decoratorAttributes = object
                            .getDecoratorAttributes(decorator);
                    boolean validExecutionAspectFound = 
                            (decoratorAttributes instanceof ExecutionAttributes) &&
                            decoratorAttributes.getDecorator() != null
                            && ((ExecutionAttributes) decoratorAttributes)
                            .enabled();
                    boolean validMirrorDecoratorFound = 
                            (decoratorAttributes instanceof MirrorDecoratorAttributes) &&
                            decoratorAttributes.getDecorator() != null
                            && ((MirrorDecoratorAttributes) decoratorAttributes)
                            .enabled();
                    boolean mirrorDecoratorDisabled = (decoratorAttributes instanceof MirrorDecoratorAttributes) &&
                            decoratorAttributes.getDecorator() != null;

                    if (validExecutionAspectFound || validMirrorDecoratorFound) { 
                        try {
                            // not highlighting measurement models because they
                            // are decorators themselves.
                            if (!(object instanceof Decorator) && object
                                    .getAttribute("_decoratorHighlightColor") == null) {
                                highlightColor = new ColorAttribute(object,
                                        "_decoratorHighlightColor");
                                Attribute attribute = ((NamedObj) decorator)
                                        .getAttribute("decoratorHighlightColor");
                                String colorExpression = "{0.5, 0.5, 0.5, 0.5}";
                                if (attribute != null) {
                                    colorExpression = (((ColorAttribute) attribute)
                                            .getToken()).toString();
                                }
                                ((ColorAttribute) highlightColor)
                                .setExpression(colorExpression);
                            }
                        } catch (NameDuplicationException e) {
                            // Not gonna happen.
                        } 
                        // check if the decorator itself has been highlighted
                        // if not, highlight.
                        if (validMirrorDecoratorFound) {
                            if (((MirrorDecorator)decorator)
                                    .getAttribute("_highlightColor") == null) {
                                try {
                                    Attribute attribute = ((NamedObj) decorator)
                                            .getAttribute("decoratorHighlightColor");
                                    highlightColor = new ColorAttribute((MirrorDecorator)decorator,
                                            "_highlightColor"); 
                                    ((Parameter)highlightColor).setVisibility(Settable.EXPERT);
                                    String colorExpression = "{0.5, 0.5, 0.5, 0.5}";
                                    if (attribute != null) {
                                        colorExpression = (((ColorAttribute) attribute)
                                                .getToken()).toString();
                                    }
                                    ((ColorAttribute) highlightColor)
                                    .setExpression(colorExpression);  
                                } catch (NameDuplicationException e) {
                                    // Should not happen.
                                } 
                            }
                        }
                    } else if (mirrorDecoratorDisabled) {
                        Attribute mda = ((MirrorDecorator)decorator)
                                .getAttribute("_highlightColor");
                        if (mda != null) {
                            ((MirrorDecorator)decorator).removeAttribute(mda);
                    } 
                    }
                }

            } catch (IllegalActionException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } 

            AttributeController.renderDecoratorHighlight(object, result);

            // If a shadow is specified, render it now.
            // The shadow attribute can be contained by the container
            // so that it is applied to all icons corresponding to Entity
            // objects (not attributes). This can be overridden for each
            // object (including attributes) by providing an individual
            // shadow specification. An empty color results in no shadow.
            try {
                // If the object itself has a shadow specification, use that.
                ColorAttribute shadowAttribute = (ColorAttribute) object
                        .getAttribute("_shadowColor", ColorAttribute.class);
                if (shadowAttribute != null) {
                    if (!shadowAttribute.getExpression().trim().equals("")) {
                        Color color = shadowAttribute.asColor();
                        // FIXME: How to set the size of the shadow?
                        ShadowRenderer animationRenderer = new ShadowRenderer(
                                color);
                        animationRenderer.renderSelected(result);
                    }
                } else if (object instanceof Entity) {
                    // If the container has a shadow specification, use that.
                    NamedObj container = object.getContainer();
                    if (container != null) {
                        shadowAttribute = (ColorAttribute) container
                                .getAttribute("_shadowColor",
                                        ColorAttribute.class);
                        if (shadowAttribute != null
                                && !shadowAttribute.getExpression().trim()
                                .equals("")) {
                            Color color = shadowAttribute.asColor();
                            // FIXME: How to set the size of the shadow?
                            ShadowRenderer animationRenderer = new ShadowRenderer(
                                    color);
                            animationRenderer.renderSelected(result);
                        }
                    }
                }
            } catch (IllegalActionException e) {
                // Ignore.
            }

            try {
                StringAttribute explanationAttribute = (StringAttribute) object
                        .getAttribute("_explanation", StringAttribute.class);
                if (explanationAttribute != null) {
                    result.setToolTipText(explanationAttribute.getExpression());
                }
            } catch (IllegalActionException e) {
                // Ignore.
            }

            return result;
        }
    }
}
