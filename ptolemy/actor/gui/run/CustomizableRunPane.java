/* A panel containing customizable controls for a Ptolemy II model.

 Copyright (c) 1998-2006 The Regents of the University of California.
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
package ptolemy.actor.gui.run;

import java.awt.Component;
import java.awt.LayoutManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.mlc.swing.layout.LayoutConstraintsManager;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.actor.gui.Configurer;
import ptolemy.actor.gui.Placeable;
import ptolemy.gui.CloseListener;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ConfigurableAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;

import com.jgoodies.forms.factories.DefaultComponentFactory;

//////////////////////////////////////////////////////////////////////////
//// CustomizableRunPane

/**

 A panel for interacting with an executing Ptolemy II model.
 This panel can be customized by inserting
 
 FIXME: more
 
 @see Placeable
 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 6.2
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class CustomizableRunPane extends JPanel implements CloseListener {
    
    /** Construct a panel for interacting with the specified Ptolemy II model.
     *  @param model The model to control.
     *  @param xml The XML specification of the layout, or null to use the default.
     *  @exception IllegalActionException If the XML to be parsed has errors.
     */
    public CustomizableRunPane(CompositeActor model, String xml) throws IllegalActionException {
        super();
        
        _model = model;
        
        Attribute layoutAttribute = _model.getAttribute("_runLayoutAttribute");
        if (layoutAttribute instanceof ConfigurableAttribute) {
            try {
                xml = ((ConfigurableAttribute)layoutAttribute).value();
            } catch (IOException e) {
                throw new InternalErrorException(e);
            }
        }
        
        if (xml == null) {
            xml = _defaultLayout();
        }
        // Parse the XML
        InputStream stream;
        try {
            stream = new ByteArrayInputStream(xml.toString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e1) {
            throw new InternalErrorException(e1);
        }
        Document dataDocument = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // Without the following, then carriage returns in the XML
            // mess up the parsing, incredibly enough!
            factory.setIgnoringElementContentWhitespace(true);
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            dataDocument = documentBuilder.parse(stream);
        } catch (Exception e) {
            throw new IllegalActionException(model, e, "Unable to parse layout specification.");
        }
        Node root = dataDocument.getDocumentElement();
        _layoutConstraintsManager =
                LayoutConstraintsManager.getLayoutConstraintsManager(root);
        
        LayoutManager layout = _layoutConstraintsManager.createLayout("top", this);
        this.setLayout(layout);
        
        // Walk through the XML, creating an interface as specified in it.
        // This assumes a very specific structure to the XML.
        NodeList components = root.getChildNodes();
        // Go through the subpanels.
        for (int i = 0; i < components.getLength(); i++) {
            Node subpanelNode = components.item(i);
            if (!subpanelNode.getNodeName().equals("container")) {
                continue;
            }
            String subpanelName = subpanelNode.getAttributes().getNamedItem("name").getNodeValue();
            if (subpanelName.equals("top")) {
                NodeList nodeList = components.item(i).getChildNodes();
                for (int j = 0; j < nodeList.getLength(); j++) {
                    Node node = nodeList.item(j);
                    // The attributes will be null if the node represents the contents text.
                    if (node.getAttributes() != null) {
                        String name = node.getAttributes().getNamedItem("name").getNodeValue();
                        _addComponent(name, this);
                    }
                }
            } else {
                if (_subpanels == null) {
                    throw new IllegalActionException(_model, 
                            "Panel 'top' is required to be first. Found instead: " + subpanelName);
                }
                JPanel panel = _subpanels.get(subpanelName);
                if (panel == null) {
                    throw new IllegalActionException(_model, 
                            "No layout matching subpanel: " + subpanelName);
                }
                NodeList nodeList = subpanelNode.getChildNodes();
                for (int j = 0; j < nodeList.getLength(); j++) {
                    Node node = nodeList.item(j);
                    // The attributes will be null if the node represents the contents text.
                    if (node.getAttributes() != null) {
                        String name = node.getAttributes().getNamedItem("name").getNodeValue();
                        _addComponent(name, panel);
                    }
                }   
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Return the layout constraints manager for this pane.
     *  @return A layout constraints manager.
     */
    public LayoutConstraintsManager getLayoutConstraintsManager() {
        return _layoutConstraintsManager;
    }
    
    /** If the model has a manager and is executing, then
     *  pause execution by calling the pause() method of the manager.
     *  If there is no manager, do nothing.
     */
    public void pauseRun() {
        Manager manager = _model.getManager();
        if (manager != null) {
            manager.pause();
        }
    }
    
    /** If the model has a manager and is executing, then
     *  resume execution by calling the resume() method of the manager.
     *  If there is no manager, do nothing.
     */
    public void resumeRun() {
        Manager manager = _model.getManager();
        if (manager != null) {
            manager.resume();
        }
    }

    /** If the model has a manager and is not already running,
     *  then execute the model in a new thread.  Otherwise, do nothing.
     */
    public void startRun() {
        Manager manager = _model.getManager();
        if (manager != null) {
            try {
                manager.startRun();
            } catch (IllegalActionException ex) {
                // Model is already running.  Ignore.
            }
        }
    }

    /** If the model has a manager and is executing, then
     *  stop execution by calling the stop() method of the manager.
     *  If there is no manager, do nothing.
     */
    public void stopRun() {
        Manager manager = _model.getManager();
        if (manager != null) {
            manager.stop();
        }
    }

    /** Notify the contained instances of PtolemyQuery that the window
     *  has been closed, and remove all Placeable displays by calling
     *  place(null).  This method is called if this pane is contained
     *  within a container that supports such notification.
     *  @param window The window that closed.
     *  @param button The name of the button that was used to close the window.
     */
    public void windowClosed(Window window, String button) {
        // FIXME: This is not getting invoked. Need to override
        // TableauFrame above with an override to _close().
        // Better yet, should use ModelFrame instead of TableauFrame,
        // but then ModelPane needs to be converted to an interface
        // so that this pane can be used instead.
        if (_directorQuery != null) {
            _directorQuery.windowClosed(window, button);
        }

        if (_parameterQuery != null) {
            _parameterQuery.windowClosed(window, button);
        }

        if (_model != null) {
            _closeDisplays();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Add a component with the specified name. The name is of the form
     *  TYPE:DETAIL, where TYPE defines the type of component to add
     *  and DETAIL specifies details.
     *  @param name The name.
     *  @param panel The panel into which to add the component.
     *  @exception IllegalActionException If there is an error in the XML.
     */
    private void _addComponent(String name, JPanel panel) throws IllegalActionException {
        // Figure out what type of component to create.
        if (name.startsWith("Placeable:")) {
            // Display of an actor that implements the Placeable
            // interface is given with "Placeable:NAME" where
            // NAME is the name of the actor relative to the model.
            String actorName = name.substring(10);
            ComponentEntity entity = _model.getEntity(actorName);
            if (!(entity instanceof Placeable)) {
                throw new IllegalActionException(_model,
                       "Entity that does not implement Placeable is specified in a display.");
            }
            // Regrettably, it seems we need an intermediate JPanel.
            JPanel dummy = new JPanel();
            ((Placeable) entity).place(dummy);
            panel.add(dummy, name);
        } else if (name.startsWith("Separator:")) {
            // Separator is given with "Separator:TEXT" where
            // TEXT is the text of the separator.
            String text = name.substring(10);
            Component separator = DefaultComponentFactory.getInstance().createSeparator(text);
            panel.add(separator, name);
        } else if (name.startsWith("GoButton:")) {
            // Go button is specified with "GoButton:TEXT", where TEXT
            // is the text displayed in the button.
            String text = name.substring(9);
            JButton goButton = new JButton(text);
            goButton.setToolTipText("Execute the model");
            panel.add(goButton, name);
            goButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    startRun();
                }
            });
        } else if (name.startsWith("PauseButton:")) {
            // Pause button is specified with "PauseButton:TEXT", where TEXT
            // is the text displayed in the button.
            String text = name.substring(12);
            JButton pauseButton = new JButton(text);
            pauseButton.setToolTipText("Pause the model");
            panel.add(pauseButton, name);
            pauseButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    pauseRun();
                }
            });
        } else if (name.startsWith("ResumeButton:")) {
            // Resume button is specified with "ResumeButton:TEXT", where TEXT
            // is the text displayed in the button.
            String text = name.substring(13);
            JButton button = new JButton(text);
            button.setToolTipText("Resume the model");
            panel.add(button, name);
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    resumeRun();
                }
            });
        } else if (name.startsWith("StopButton:")) {
            // Stop button is specified with "StopButton:TEXT", where TEXT
            // is the text displayed in the button.
            String text = name.substring(11);
            JButton button = new JButton(text);
            button.setToolTipText("Stop the model");
            panel.add(button, name);
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    stopRun();
                }
            });
        } else if (name.equals("ConfigureTopLevel")) {
            // A parameter editor for top-level parameters
            _parameterQuery = new Configurer(_model);
            panel.add(_parameterQuery, name);
        } else if (name.equals("ConfigureDirector")) {
            // A parameter editor for the director
            _directorQuery = new Configurer(_model.getDirector());
            panel.add(_directorQuery, name);
        } else if (name.startsWith("ConfigureEntity:")) {
            // A parameter editor for an entity is specified with
            // "ConfigureEntity:NAME", where NAME is the name of
            // the entity to configure relative to the top level.
            String entityName = name.substring(16);
            ComponentEntity entity = _model.getEntity(entityName);
            if (entity == null) {
                throw new IllegalActionException(_model,
                        "Nonexistent entity: " + entityName);
            }
            Configurer configurer = new Configurer(entity);
            panel.add(configurer, name);
        } else if (name.startsWith("Subpanel:")) {
            // Subpanel is specified with "Subpanel:NAME", where NAME
            // is the name of the the subpanel.
            JPanel subpanel = new JPanel();
            LayoutManager layout = _layoutConstraintsManager.createLayout(name, subpanel);
            subpanel.setLayout(layout);
            panel.add(subpanel, name);
            if (_subpanels == null) {
                _subpanels = new HashMap<String,JPanel>();
            }
            _subpanels.put(name, subpanel);
        } else {
            throw new IllegalActionException(_model,
                    "Unrecognized entry in control panel layout: " + name);
        }
    }
    /** Close any open displays by calling place(null).
     */
    private void _closeDisplays() {
        if (_model != null) {
            Iterator atomicEntities = _model.allAtomicEntityList().iterator();

            while (atomicEntities.hasNext()) {
                Object object = atomicEntities.next();

                if (object instanceof Placeable) {
                    ((Placeable) object).place(null);
                }
            }
        }
    }
    
    /** Create a default layout for the associated model. */
    private String _defaultLayout() {
        StringBuffer xml = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<containers>\n");
        
        // Top-level panel has two columns and one row.
        xml.append("<container name=\"top\" " +
                "columnSpecs=\"default,3dlu,default:grow\" " +
                "rowSpecs=\"default\">\n");
        xml.append("<cellconstraints name=\"Subpanel:ControlPanel\" gridX=\"1\" gridY=\"1\" " +
                "gridWidth=\"1\" gridHeight=\"1\" horizontalAlignment=\"default\" " +
                "verticalAlignment=\"top\" topInset=\"0\" bottomInset=\"0\" " +
                "rightInset=\"0\" leftInset=\"0\"/>\n");
        xml.append("<cellconstraints name=\"Subpanel:PlaceablePanel\" gridX=\"3\" gridY=\"1\" " +
                "gridWidth=\"1\" gridHeight=\"1\" horizontalAlignment=\"default\" " +
                "verticalAlignment=\"top\" topInset=\"0\" bottomInset=\"0\" " +
                "rightInset=\"0\" leftInset=\"0\"/>\n");
        xml.append("</container>\n");
        
        // Subpanel with the run control buttons, the top-level parameters,
        // and the director parameters.
        xml.append("<container name=\"Subpanel:ControlPanel\" " +
                "columnSpecs=\"default\" " +
                "rowSpecs=\"default,5dlu,default,5dlu,default,5dlu,default,5dlu," +
                "default,5dlu,default,5dlu,default\">\n");
        xml.append("<cellconstraints name=\"Separator:Run Control\" gridX=\"1\" gridY=\"1\" " +
                "gridWidth=\"1\" gridHeight=\"1\" " +
                "horizontalAlignment=\"default\" verticalAlignment=\"default\" " +
                "topInset=\"0\" bottomInset=\"0\" rightInset=\"0\" leftInset=\"0\"/>\n");
        xml.append("<cellconstraints name=\"Subpanel:Run Control\" gridX=\"1\" gridY=\"3\" " +
                "gridWidth=\"1\" gridHeight=\"1\" " +
                "horizontalAlignment=\"default\" verticalAlignment=\"top\" " +
                "topInset=\"0\" bottomInset=\"0\" rightInset=\"0\" leftInset=\"0\"/>\n");
        // Place a configurer for the top-level settables here.
        xml.append("<cellconstraints name=\"Separator:Top-Level Parameters\" gridX=\"1\" gridY=\"5\" " +
                "gridWidth=\"1\" gridHeight=\"1\" " +
                "horizontalAlignment=\"default\" verticalAlignment=\"default\" " +
                "topInset=\"0\" bottomInset=\"0\" rightInset=\"0\" leftInset=\"0\"/>\n");
        xml.append("<cellconstraints name=\"ConfigureTopLevel\" gridX=\"1\" gridY=\"7\" " +
                "gridWidth=\"1\" gridHeight=\"1\" horizontalAlignment=\"default\" " +
                "verticalAlignment=\"top\" topInset=\"0\" bottomInset=\"0\" " +
                "rightInset=\"0\" leftInset=\"0\"/>\n");        
        // Place a configurer for the director settables here.
        xml.append("<cellconstraints name=\"Separator:Director Parameters\" gridX=\"1\" gridY=\"9\" " +
                "gridWidth=\"1\" gridHeight=\"1\" " +
                "horizontalAlignment=\"default\" verticalAlignment=\"default\" " +
                "topInset=\"0\" bottomInset=\"0\" rightInset=\"0\" leftInset=\"0\"/>\n");
        xml.append("<cellconstraints name=\"ConfigureDirector\" gridX=\"1\" gridY=\"11\" " +
                "gridWidth=\"1\" gridHeight=\"1\" horizontalAlignment=\"default\" " +
                "verticalAlignment=\"top\" topInset=\"0\" bottomInset=\"0\" " +
                "rightInset=\"0\" leftInset=\"0\"/>\n");        
        xml.append("</container>\n");

        // Subpanel with the run control buttons.
        xml.append("<container name=\"Subpanel:Run Control\" " +
                "columnSpecs=\"default,3dlu,default,3dlu,default,3dlu,default\" " +
                "rowSpecs=\"default\">\n");
        xml.append("<cellconstraints name=\"GoButton:Execute\" gridX=\"1\" gridY=\"1\" " +
                "gridWidth=\"1\" gridHeight=\"1\" " +
                "horizontalAlignment=\"default\" verticalAlignment=\"default\" " +
                "topInset=\"0\" bottomInset=\"0\" rightInset=\"0\" leftInset=\"0\"/>\n");
        xml.append("<cellconstraints name=\"PauseButton:Pause\" gridX=\"3\" gridY=\"1\" " +
                "gridWidth=\"1\" gridHeight=\"1\" " +
                "horizontalAlignment=\"default\" verticalAlignment=\"default\" " +
                "topInset=\"0\" bottomInset=\"0\" rightInset=\"0\" leftInset=\"0\"/>\n");
        xml.append("<cellconstraints name=\"ResumeButton:Resume\" gridX=\"5\" gridY=\"1\" " +
                "gridWidth=\"1\" gridHeight=\"1\" " +
                "horizontalAlignment=\"default\" verticalAlignment=\"default\" " +
                "topInset=\"0\" bottomInset=\"0\" rightInset=\"0\" leftInset=\"0\"/>\n");
        xml.append("<cellconstraints name=\"StopButton:Stop\" gridX=\"7\" gridY=\"1\" " +
                "gridWidth=\"1\" gridHeight=\"1\" " +
                "horizontalAlignment=\"default\" verticalAlignment=\"default\" " +
                "topInset=\"0\" bottomInset=\"0\" rightInset=\"0\" leftInset=\"0\"/>\n");
        xml.append("</container>\n");

        // Subpanel for each object that implements Placeable.
        xml.append("<container name=\"Subpanel:PlaceablePanel\" ");
        xml.append("columnSpecs=\"default,3dlu,default,3dlu,default,3dlu,default\" ");
        // FIXME: Need some way to resize plot windows here...
        xml.append("rowSpecs=\"default");                        
        StringBuffer constraints = new StringBuffer();
        if (_model != null) {
            Iterator atomicEntities = _model.allAtomicEntityList().iterator();
            int row = 1;
            while (atomicEntities.hasNext()) {
                Object object = atomicEntities.next();
                if (object instanceof Placeable) {
                    if (row > 1) {
                        xml.append(",5dlu,default");
                    }
                    constraints.append("<cellconstraints name=\"Placeable:");
                    constraints.append(((NamedObj)object).getName(_model));
                    constraints.append("\" gridX=\"3\" gridY=\"");
                    constraints.append(row);
                    constraints.append("\" gridWidth=\"1\" gridHeight=\"1\" horizontalAlignment=\"default\" " +
                            "verticalAlignment=\"default\" topInset=\"0\" bottomInset=\"0\" " +
                            "rightInset=\"0\" leftInset=\"0\"/>\n");
                    row = row + 2;
                }
            }
        }
        // End the row specs.
        xml.append("\">\n");                        
        // Add the constraints.
        xml.append(constraints);
        xml.append("</container>\n");

        xml.append("</containers>\n");
        return xml.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The query box for the director parameters. */
    private Configurer _directorQuery;

    /** The layout constraint manager. */
    private LayoutConstraintsManager _layoutConstraintsManager;

    /** The associated model. */
    private CompositeActor _model;
    
    /** The query box for the top-level parameters. */
    private Configurer _parameterQuery;
    
    /** A collection of subpanels. */
    private HashMap<String,JPanel> _subpanels;
}
