/* An attribute that creates an editor to configure and run a code generator.

 Copyright (c) 2006 The Regents of the University of California.
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
package ptolemy.data.properties.gui;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;

import ptolemy.actor.gui.ColorAttribute;
import ptolemy.actor.gui.EditParametersDialog;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.properties.Property;
import ptolemy.data.properties.PropertySolver;
import ptolemy.data.properties.token.PropertyTokenSolver;
import ptolemy.data.type.BaseType;
import ptolemy.gui.ComponentDialog;
import ptolemy.gui.Query;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.vergil.basic.NamedObjController;
import ptolemy.vergil.basic.NodeControllerFactory;
import ptolemy.vergil.icon.EditorIcon;
import ptolemy.vergil.kernel.AttributeController;
import ptolemy.vergil.kernel.attributes.RectangleAttribute;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import diva.graph.GraphController;

//////////////////////////////////////////////////////////////////////////
//// PropertyHighlighter

/**
 This is an attribute that creates an editor for configuring and
 running a code generator.  This is designed to be contained by
 an instance of CodeGenerator or a subclass of CodeGenerator.
 It customizes the user interface for "configuring" the code
 generator. This UI will be invoked when you double click on the
 code generator.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 7.0
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class PropertyHighlighter extends NodeControllerFactory {
    
    /** Construct a PropertyHighlighter with the specified container and name.
     *  @param container The container.
     *  @param name The name of the PropertyHighlighter.
     *  @exception IllegalActionException If the PropertyHighlighter is not of an
     *   acceptable attribute for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public PropertyHighlighter(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Hide the name.
        SingletonParameter _hideName = new SingletonParameter(this, "_hideName");
        _hideName.setToken(BooleanToken.TRUE);
        _hideName.setVisibility(Settable.EXPERT);
   
        // The icon.
        _icon = new EditorIcon(this, "_icon");
        RectangleAttribute rectangle = new RectangleAttribute(_icon,
                "rectangle");
        rectangle.width.setExpression("155.0");
        rectangle.height.setExpression("20.0");
        rectangle.fillColor.setExpression("{1.0, 0.7, 0.7, 1.0}");
        
        showText = new Parameter(this, "showText");
        showText.setTypeEquals(BaseType.BOOLEAN);
        showText.setExpression("true");

        highlight = new Parameter(this, "highlight");
        highlight.setTypeEquals(BaseType.BOOLEAN);
        highlight.setExpression("true");
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
        PropertySolver solver = (PropertySolver) getContainer();
        try {        
            List propertyables = solver.getAllPropertyables();
            //solver.reset();
            
            for (Object propertyable : propertyables) {
                if (propertyable instanceof NamedObj) {
                    NamedObj namedObj = (NamedObj) propertyable;
    
                    StringParameter showAttribute = 
                        (StringParameter) namedObj.getAttribute("_showInfo");
            
                    Attribute highlightAttribute = 
                        namedObj.getAttribute("_highlightColor");
                    
    
                        if (highlightAttribute != null) {
                            highlightAttribute.setContainer(null);
                        }
                        
                        if (showAttribute != null) {
                            showAttribute.setContainer(null);
                        }
                }
            }
        } catch (IllegalActionException e1) {
            assert false;
        } catch (NameDuplicationException e1) {
            assert false;
        }
        
        
        // Repaint the GUI.
        getContainer().requestChange(new ChangeRequest(this,
            "Repaint the GUI.") {
            protected void _execute() throws Exception {}
        });        
    }
    
    /**
     * Clear the property annotations of associated with 
     * the container solver.
     */
    public void clearProperties() {
        // Get the PropertySolver.
        PropertySolver solver = (PropertySolver) getContainer();
        try {
            solver.clearProperties();
        } catch (IllegalActionException e) {
            assert false;
        }
    }
        
    /** Return a new node controller.  This base class returns an
     *  instance of IconController.  Derived
     *  classes can return some other class to customize the
     *  context menu.
     *  @param controller The associated graph controller.
     *  @return A new node controller.
     */
    public NamedObjController create(GraphController controller) {
        super.create(controller);
        //return new ConfigureHighlightController(controller);
        return new HighlighterController(controller);
    }

    /** Create an editor for configuring the specified object with the
     *  specified parent window.
     *  @param object The object to configure.
     *  @param parent The parent window, or null if there is none.
     */
    public void createEditor(NamedObj object, Frame parent) {
        // This is always used to configure the container, so
        // we just use that.
        PropertySolver solver = (PropertySolver) getContainer();
        try {
            solver.workspace().getWriteAccess();
            
            solver.resolveProperties(true);

            solver.checkErrors();
            
            solver.displayProperties();

            solver.workspace().doneWriting();
            
        } catch (KernelException e) {
            throw new InternalErrorException(e);
        } finally {
            solver.getSharedUtilities().resetAll();
        }
    }
    
    /**
     * Highlight all property-able objects with
     * the specified colors for their property values,
     * if the highlight parameter value is true.
     * Otherwise, do nothing.
     */
    public void highlightProperties() {
        try {
            if ((BooleanToken) highlight.getToken()
                     == BooleanToken.TRUE) {
                _highlightProperties();
            }
        } catch (IllegalActionException ex) {
            // Silently, do nothing.
        }    
    }
    
    /**
     * If the value of the highlight parameter is set to
     * true, highlight the given property-able object with
     * the specified color associated with the given
     * property, if there exists any.
     * @param propertyable The given property-able object.
     * @param property The given property.
     * @exception IllegalActionException Thrown if an error
     * occurs when creating or setting the value for the 
     * highlightColor attribute in the property-able object.
     */
    public void highlightProperty(NamedObj propertyable, Property property) 
    throws IllegalActionException {
        if (highlight.getToken() == BooleanToken.TRUE) {
            _highlightProperty(propertyable, property);            
        }
    }
    
    /**
     * If the value of the showText parameter is set to
     * true, show all property values visually. 
     * Otherwise, do nothing.
     */
    public void showProperties() {
        if (showText.getExpression().equals("true")) {
            _showProperties();
        }
    }
    
    /**
     * If the value of the showText parameter is true,
     * show the given property value for the given
     * property-able object. If the property is not null,
     * this looks for the _showInfo parameter in the 
     * property-able object. Create a new _showInfo
     * StringParameter, if there does not already exists one.
     * Set its value to the given property value. If the
     * given property is null, this removes the _showInfo
     * parameter from the property-able object. 
     * @param propertyable The given property-able object.
     * @param property The given property.
     * @exception IllegalActionException Thrown if an error
     * occurs when creating or setting the value for the 
     * _showInfo parameter in the property-able object.
     */
    public void showProperty(NamedObj propertyable, Property property) 
            throws IllegalActionException {
        if (showText.getToken() == BooleanToken.TRUE) {
            _showProperty(propertyable, property);
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    
    private class EditHighlightDialog extends EditParametersDialog  {
        
        /** Construct a dialog with the specified owner and target.
         *  A "Commit" and a "Cancel" button are added to the dialog.
         *  The dialog is placed relative to the owner.
         *  @param owner The object that, per the user, appears to be
         *   generating the dialog.
         *  @param target The object whose parameters are being edited.
         *  @param label The label for the dialog box.
         */
        public EditHighlightDialog(Frame owner, NamedObj target, String label) {
            super(owner, target, label);            
        }
        
        /** Open a dialog to add a new parameter.
         *  @param message A message to place at the top, or null if none.
         *  @param name The default name.
         *  @param defValue The default value.
         *  @param className The default class name.
         *  @return The dialog that is created.
         */
        protected ComponentDialog _openAddDialog(String message, String name,
                String defValue, String className) {
            // Create a new dialog to add a parameter, then open a new
            // EditParametersDialog.
            _query = new Query();
    
            if (message != null) {
                _query.setMessage(message);
            }
    
            _query.addLine("name", "Name", name);
            
            ComponentDialog dialog = new ComponentDialog(_owner,
                    "Add a new parameter to " + _target.getFullName(), _query, null);
    
            // If the OK button was pressed, then queue a mutation
            // to create the parameter.
            // A blank property name is interpreted as a cancel.
            String newName = _query.getStringValue("name");
            
            if (dialog.buttonPressed().equals("OK") && !newName.equals("")) {
                String moml = "<property name=\"" + newName + "\" value=\""
                        + "property value\" class=\""
                        + "ptolemy.kernel.util.StringAttribute" + "\"/>";
                
                MoMLChangeRequest request = new MoMLChangeRequest(this, _target,
                        moml);
                request.setUndoable(true);
                _target.requestChange(request);
    
                moml = "<property name=\"" + newName + "HighlightColor\" value=\""
                + "{1.0, 0.0, 0.0, 1.0}\" class=\""
                + "ptolemy.actor.gui.ColorAttribute" + "\"/>";
    
                _target.addChangeListener(this);
    
                request = new MoMLChangeRequest(this, _target, moml);
                request.setUndoable(true);
                _target.requestChange(request);
            }
            
            return dialog;
        }
    }


    /** The controller that adds commands to the context menu.
     */
    private class HighlighterController extends AttributeController {
    
        /** Create a DependencyController that is associated with a controller.
         *  @param controller The controller.
         */
        public HighlighterController(GraphController controller) {
            super(controller);
    
            ClearProperty clearProperty = new ClearProperty();
            _menuFactory.addMenuItemFactory(new MenuActionFactory(clearProperty));
    
            ClearDisplay clearDisplay = new ClearDisplay();
            _menuFactory.addMenuItemFactory(new MenuActionFactory(clearDisplay));
    
            ShowProperty showProperty = new ShowProperty();
            _menuFactory.addMenuItemFactory(new MenuActionFactory(showProperty));
    
            HighlightProperty highlightProperty = new HighlightProperty();
            _menuFactory.addMenuItemFactory(new MenuActionFactory(highlightProperty));
    
            ConfigureHighlightAction highlight = new ConfigureHighlightAction();
            _configureMenuFactory.addAction(highlight, "Configure");
        }
    }


    /** The action for the commands added to the context menu.
     */
    private class ClearDisplay extends FigureAction {
        public ClearDisplay() {
            super("Clear Property Display");
        }
        
        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);
            clearDisplay();
        }
    }

    /** The action for the commands added to the context menu.
     */
    private class ClearProperty extends FigureAction {
        public ClearProperty() {
            super("Clear Property");
        }
        
        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);
            clearProperties();
        }
    }


    /** The action for the commands added to the configure menu.
     */
    private class ConfigureHighlightAction extends FigureAction {
        
        public ConfigureHighlightAction() {
            super("Property Display");
        }
    
        /**
         * Open the dialog for configuring the highlight color
         * for property values. 
         */
        public void actionPerformed(ActionEvent e) {
                // Determine which entity was selected for the look inside action.
                super.actionPerformed(e);
    
                NamedObj target = PropertyHighlighter.this;
                
                // Create a dialog for configuring the object.
                // First, identify the top parent frame.
                Frame parent = getFrame();
                
                _openDialog(parent, target);
        }
        
        
        /** Open an edit parameters dialog.  This is a modal dialog, so
         *  this method returns only after the dialog has been dismissed.
         *  @param parent A frame to serve as a parent for the dialog, or
         *  null if there is none.
         *  @param target The object whose parameters are to be edited.
         *  @param event The action event that triggered this, or null if
         *   none.
         */
        private void _openDialog(Frame parent, NamedObj target) {
            new EditHighlightDialog(parent, target, "Highlight properties");
        }
    }


    /** The action for the commands added to the context menu.
     */
    private class HighlightProperty extends FigureAction {
        public HighlightProperty() {
            super("Highlight Property");
        }
        
        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);
            _highlightProperties();
    
            // Repaint the GUI.
            getContainer().requestChange(new ChangeRequest(this,
                "Repaint the GUI.") {
                protected void _execute() throws Exception {}
            });        
        }
    }


    /** The action for the commands added to the context menu.
     */
    private class ShowProperty extends FigureAction {
        public ShowProperty() {
            super("Show Property");
        }
        
        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);
            _showProperties();
    
            // Repaint the GUI.
            getContainer().requestChange(new ChangeRequest(this,
                "Repaint the GUI.") {
                protected void _execute() throws Exception {}
            });        
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Highlight all property-able objects with
     * the specified colors for their property values.
     */
    private void _highlightProperties() {
        // Get the PropertySolver.
        PropertySolver solver = (PropertySolver) getContainer();
        try {
            Iterator propertyables = solver.getAllPropertyables().iterator();

            while (propertyables.hasNext()) {
                Object propertyableObject = propertyables.next();

                if (propertyableObject instanceof NamedObj) {
                    NamedObj namedObj = (NamedObj) propertyableObject;

                    Property property = solver.getResolvedProperty(namedObj, false);

                    _highlightProperty(namedObj, property);
                }
            }
        } catch (IllegalActionException ex) {
            assert false;
        }
    }

    
    /**
     * @param namedObj
     * @param property
     * @throws IllegalActionException
     */
    private void _highlightProperty(NamedObj namedObj, Property property) throws IllegalActionException {
        String propertyString;
        if (property != null) {
            propertyString = property.toString();
    
        } else if (getContainer() instanceof PropertyTokenSolver) {
            propertyString = "";//Token.NIL.toString();
        } else {
            propertyString = "";
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
                            (ColorAttribute) namedObj.getAttribute("_highlightColor");
                        
                        if (highlightAttribute == null) {
                            try {
                                highlightAttribute = new ColorAttribute(namedObj, "_highlightColor");
                            } catch (NameDuplicationException e) {
                                // This shouldn't happen. If another attribute 
                                // has the same name, we should find it before.
                                assert false;
                            }
                        }
                        highlightAttribute.setExpression(colorAttribute.getExpression());
                    }
                }
            }
        }
    }
    
    /**
     * Show all property values visually.
     */
    private void _showProperties() {
        // Get the PropertySolver.
        PropertySolver solver = (PropertySolver) getContainer();
        try {
            Iterator propertyables = solver.getAllPropertyables().iterator();

            while (propertyables.hasNext()) {
                Object propertyableObject = propertyables.next();

                if (propertyableObject instanceof NamedObj) {
                    NamedObj namedObj = (NamedObj) propertyableObject;

                    Property property = solver.getResolvedProperty(namedObj, false);

                    _showProperty(namedObj, property);
                }
            }
        } catch (IllegalActionException e) {
            assert false;
        }
    }

    /**
     * Show the given property value for the given
     * property-able object.
     * @param namedObj The given property-able object.
     * @param property The given property.
     * @exception IllegalActionException Thrown if
     *  there is an error in creating the visual
     *  attribute and/or setting its value for the 
     *  property-able object.
     */
    private void _showProperty(NamedObj namedObj, Property property)
    throws IllegalActionException {
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
        
        // Update the _showInfo attribute.
        StringParameter showAttribute = 
            (StringParameter) namedObj.getAttribute("_showInfo");

        try {
            // Remove the showInfo attribute if we don't have
            // any property to display.
            if (property == null && showAttribute != null) {
                showAttribute.setContainer(null);
                return;
                
            } else if (showAttribute == null) {
                showAttribute = new StringParameter(namedObj, "_showInfo");
            }
        } catch (NameDuplicationException e) {
            // This shouldn't happen. If another attribute 
            // has the same name, we should find it before.
            assert false;
        }
        showAttribute.setToken(propertyString);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The visual icon.
     */
    private EditorIcon _icon;
}
