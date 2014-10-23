/* A top-level frame for editing the layout of a customizable run control panel.

 Copyright (c) 2007-2014 The Regents of the University of California.
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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JTabbedPane;

import org.mlc.swing.layout.ContainerLayout;
import org.mlc.swing.layout.LayoutConstraintsManager;
import org.mlc.swing.layout.LayoutFrame;
import org.mlc.swing.layout.MultiContainerFrame;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ConfigurableAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.util.CancelException;
import ptolemy.util.MessageHandler;

///////////////////////////////////////////////////////////////////
//// RunLayoutFrame

/**
 A top-level frame for editing the layout of a customizable run control panel.
 <p>
 This is based on the LayoutFrame class by Michael Connor (mlconnor&#064;yahoo.com).

 @see LayoutFrame
 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 */
@SuppressWarnings("serial")
public class RunLayoutFrame extends TableauFrame implements MultiContainerFrame {
    /** Construct a frame to control the specified Ptolemy II model.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  This is typically accomplished by calling show() on
     *  enclosing tableau.
     *  @param model The model to put in this frame, or null if none.
     *  @param tableau The tableau responsible for this frame.
     *  @param pane The run pane whose layout is being edited.
     *  @exception IllegalActionException If the XML to be parsed has errors.
     */
    public RunLayoutFrame(CompositeActor model, Tableau tableau,
            CustomizableRunPane pane) throws IllegalActionException {
        super(tableau);
        _model = model;
        _constraintsManager = pane.getLayoutConstraintsManager();
        _pane = pane;

        List<ContainerLayout> layouts = _constraintsManager.getLayouts();
        for (int index = 0; index < layouts.size(); index++) {
            ContainerLayout containerLayout = layouts.get(index);
            Container container = _constraintsManager
                    .getContainer(containerLayout);
            if (container == null) {
                // If data is malformed, issue a warning and proceed.
                try {
                    MessageHandler
                    .warning("A container with name "
                            + containerLayout.getName()
                            + " was found in the contstraints file but was not found in the container");
                } catch (CancelException ex) {
                    throw new IllegalActionException(model, "Canceled");
                }

            } else {
                addContainerLayout(containerLayout, container);
            }
        }

        getContentPane().setLayout(new BorderLayout(3, 3));
        getContentPane().add(_tabs, BorderLayout.CENTER);

        pack();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a container with the specified name.
     *  @param name The name of the container.
     *  @param container The container.
     */
    @Override
    public void addContainer(String name, Container container) {
        // check to see if another panel with this name already exists
        ContainerLayout layout = _constraintsManager.getContainerLayout(name);
        if (layout != null) {
            throw new IllegalArgumentException("A container with name " + name
                    + " already exists");
        }

        layout = new ContainerLayout(name, "pref", "pref");
        _constraintsManager.addLayout(layout);
        container.setLayout(layout);
        _newLayouts.add(layout);

        addContainerLayout(layout, container);
    }

    /** Return true if the frame has a container with the specified name.
     *  @param name The name of the container.
     *  @return true if the frame has a container with the specified name.
     */
    @Override
    public boolean hasContainer(String name) {
        return _constraintsManager.getContainerLayout(name) != null;
    }

    /** Remove the container with the specified name.
     *  This may throw an InternalErrorException if the container does not exist.
     *  @param name The name of the container.
     */
    @Override
    public void removeContainer(String name) {
        ContainerLayout layout = _constraintsManager.getContainerLayout(name);
        if (layout == null) {
            throw new InternalErrorException("Container " + name
                    + " does not exist");
        }
        // Also have to remove any contained containers!
        Container container = _constraintsManager.getContainer(layout);
        Component[] components = container.getComponents();
        for (Component component2 : components) {
            if (component2 instanceof Container) {
                String componentName = layout.getComponentName(component2);
                if (hasContainer(componentName)) {
                    removeContainer(componentName);
                }
            }
        }
        _constraintsManager.removeLayout(layout);
        PtolemyFormEditor editor = _editors.get(layout);
        _tabs.remove(editor);
        _newLayouts.remove(layout);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Close the window.  This overrides the base class to record
     *  the modified layout, if it has been modified.
     *  @return False if the user cancels on a save query.
     */
    @Override
    protected boolean _close() {
        if (super._close()) {
            // Save the XML.
            // FIXME: Do this only if the layout has been modified.
            String xml = _constraintsManager.getXML();
            Attribute layoutAttribute = _model
                    .getAttribute("_runLayoutAttribute");
            if (layoutAttribute == null) {
                try {
                    layoutAttribute = new ConfigurableAttribute(_model,
                            "_runLayoutAttribute");
                } catch (KernelException e) {
                    throw new InternalErrorException(e);
                }
            }
            if (!(layoutAttribute instanceof ConfigurableAttribute)) {
                MessageHandler.error("Model contains an attribute named "
                        + "'_runLayoutAttribute' that is not the right class "
                        + "to save a run layout in: " + _model.getFullName());
            }
            try {
                ((ConfigurableAttribute) layoutAttribute).configure(null, null,
                        xml);
            } catch (Exception e) {
                throw new InternalErrorException(e);
            }
            return true;
        }
        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The run pane whose layout is being edited. */
    protected CustomizableRunPane _pane;

    /** The associated model. */
    protected CompositeActor _model;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Add an instance of PtolemyFormEditor for the specified containerLayout
     *  to the specified container.
     *  @param containerLayout The layout to edit.
     *  @param container The container into which to put the editor.
     */
    private void addContainerLayout(ContainerLayout containerLayout,
            Container container) {
        PtolemyFormEditor formEditor = new PtolemyFormEditor(this,
                containerLayout, container);
        _editors.put(containerLayout, formEditor);
        _tabs.addTab(containerLayout.getName(), formEditor);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The constraints manager being edited. */
    private LayoutConstraintsManager _constraintsManager;

    /** Set of editors indexed by layout. */
    private Map<ContainerLayout, PtolemyFormEditor> _editors = new HashMap<ContainerLayout, PtolemyFormEditor>();

    /** A list of new layouts. */
    private List<ContainerLayout> _newLayouts = new ArrayList<ContainerLayout>();

    /** Tabbed pane for showing nested layouts. */
    private JTabbedPane _tabs = new JTabbedPane();
}
