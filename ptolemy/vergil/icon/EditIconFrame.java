/* An icon editor frame for Ptolemy models.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

package ptolemy.vergil.icon;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.net.URL;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.DebugListenerTableau;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TextEffigy;
import ptolemy.gui.ComponentDialog;
import ptolemy.gui.Query;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.LibraryAttribute;
import ptolemy.moml.MoMLParser;
import ptolemy.util.CancelException;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.actor.ActorEditorGraphController;
import ptolemy.vergil.actor.ActorGraphModel;
import ptolemy.vergil.basic.BasicGraphFrame;
import diva.canvas.FigureLayer;
import diva.canvas.JCanvas;
import diva.canvas.toolbox.BasicRectangle;
import diva.graph.GraphPane;

//////////////////////////////////////////////////////////////////////////
//// EditIconFrame
/**
This is an icon editor frame for Ptolemy II models.
FIXME: More information.

@author  Edward A. Lee
@version $Id$
*/
public class EditIconFrame extends BasicGraphFrame {

    /** Construct a frame to edit the specified icon.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  This is typically done by calling show() on the controlling tableau.
     *  This constructor results in a frame that obtains its library
     *  either from the model (if it has one) or the default library defined
     *  in the configuration.
     *  FIXME: Check whether the above is right.
     *  @see Tableau#show()
     *  @param icon The icon to put in this frame.
     *  @param tableau The tableau responsible for this frame.
     */
    public EditIconFrame(EditorIcon icon, Tableau tableau) {
        this(icon, tableau, null);
    }

    /** Construct a frame to edit the specified icon.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  This is typically done by calling show() on the controlling tableau.
     *  This constructor results in a graph frame that obtains its library
     *  either from the model (if it has one), or the <i>defaultLibrary</i>
     *  argument (if it is non-null), or the default library defined
     *  in the configuration.
     *  @see Tableau#show()
     *  @param entity The model to put in this frame.
     *  @param tableau The tableau responsible for this frame.
     *  @param defaultLibrary An attribute specifying the default library
     *   to use if the model does not have a library.
     */
    public EditIconFrame(
            EditorIcon entity,
            Tableau tableau,
            LibraryAttribute defaultLibrary) {
        super(entity, tableau, defaultLibrary);

        // Override the default help file.
        helpFile = "ptolemy/configs/doc/vergilGraphEditorHelp.htm";
        
        zoomReset();
        
        _drawReferenceBox();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Set zoom to the nominal.  This overrides the base class to set
     *  a zoom factor and center more appropriate for editing icons.
     */
    public void zoomReset() {
        JCanvas canvas = _jgraph.getGraphPane().getCanvas();
        AffineTransform current =
            canvas.getCanvasPane().getTransformContext().getTransform();
        current.setToScale(_ZOOM_SCALE, _ZOOM_SCALE);
        canvas.getCanvasPane().setTransform(current);
        setCenter(new Point2D.Double(0.0, 0.0));
        if (_graphPanner != null) {
            _graphPanner.repaint();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the menus that are used by this frame.
     *  It is essential that _createGraphPane() be called before this.
     */
    protected void _addMenus() {
        super._addMenus();

        // Add any commands to graph menu and toolbar that the controller
        // wants in the graph menu and toolbar.
        _graphMenu.addSeparator();
        _controller.addToMenuAndToolbar(_graphMenu, _toolbar);

        // Add debug menu.
        JMenuItem[] debugMenuItems = {
            // FIXME: Wrong menu items.
            new JMenuItem("Listen to Director", KeyEvent.VK_L),
            new JMenuItem("Animate Execution", KeyEvent.VK_A),
            new JMenuItem("Stop Animating", KeyEvent.VK_S),
        };
        // NOTE: This has to be initialized here rather than
        // statically because this method is called by the constructor
        // of the base class, and static initializers have not yet
        // been run.
        _debugMenu = new JMenu("Debug");
        _debugMenu.setMnemonic(KeyEvent.VK_D);
        DebugMenuListener debugMenuListener = new DebugMenuListener();
        // Set the action command and listener for each menu item.
        for (int i = 0; i < debugMenuItems.length; i++) {
            debugMenuItems[i].setActionCommand(debugMenuItems[i].getText());
            debugMenuItems[i].addActionListener(debugMenuListener);
            _debugMenu.add(debugMenuItems[i]);
        }
        _menubar.add(_debugMenu);
    }

    /** Create the default library to use if an entity has no
     *  LibraryAttribute.  Note that this is called in the
     *  constructor and therefore overrides in subclasses
     *  should not refer to any members that may not have been 
     *  initialized.  This method overrides the base class to
     *  look for a library called "icon library" in the
     *  configuration. If there is no such library, then
     *  it provides a simple default library, created in
     *  the specified workspace.
     *  @param workspace The workspace in which to create
     *   the library, if one needs to be created.
     *  @return The new library, or null if there is no
     *   configuration.
     */
    protected CompositeEntity _createDefaultLibrary(Workspace workspace) {
        Configuration configuration = getConfiguration();
        if (configuration != null) {
            CompositeEntity result = (CompositeEntity)
                    configuration.getEntity("icon editor library");
            if (result == null) {
                // Create a default library by directly reading the
                // default XML description.
                URL source = getClass().getClassLoader().getResource(
                        "ptolemy/vergil/kernel/attributes/iconEditorLibrary.xml");
                MoMLParser parser = new MoMLParser(workspace);
                try {
                    result = (CompositeEntity)parser.parse(null, source);
                } catch (Exception e) {
                    throw new InternalErrorException(
                    "Unable to open default icon editor library: " + e);
                }
            }
            return result;
        } else {
            return null;
        }
    }

    /** Create a new graph pane. Note that this method is called in
     *  constructor of the base class, so it must be careful to not reference
     *  local variables that may not have yet been created.
     */
    protected GraphPane _createGraphPane() {
        // FIXME: This is the wrong controller to create.
        _controller = new ActorEditorGraphController();
        _controller.setConfiguration(getConfiguration());
        _controller.setFrame(this);
        final ActorGraphModel graphModel = new ActorGraphModel(getModel());
        return new GraphPane(_controller, graphModel);
    }
    
    /** Draw a reference box with the default icon size, 60x40.
     */
    protected void _drawReferenceBox() {
        // The background layer is a FigureLayer, despite the fact that
        // getBackgroundLayer() only returns a CanvasLayer.
        FigureLayer layer = (FigureLayer)_jgraph.getGraphPane().getBackgroundLayer();
        layer.setVisible(true);
        BasicRectangle reference = new BasicRectangle(-30.0, -20.0, 60.0, 40.0, 0.1f);
        reference.setStrokePaint(Color.BLUE);
        layer.add(reference);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Debug menu for this frame. */
    protected JMenu _debugMenu;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private ActorEditorGraphController _controller;

    // The delay time specified that last time animation was set.
    private long _lastDelayTime = 0;
    
    // The default zoom scale.
    private double _ZOOM_SCALE = 4.0;

    ///////////////////////////////////////////////////////////////////
    ////                     public inner classes                  ////

    // NOTE: The following class is very similar to the inner class
    // in FSMGraphFrame.  Is there some way to merge these?
    // There seem to be enough differences that this may be hard.

    /** Listener for debug menu commands. */
    public class DebugMenuListener implements ActionListener {

        /** React to a menu command. */
        public void actionPerformed(ActionEvent e) {
            JMenuItem target = (JMenuItem)e.getSource();
            String actionCommand = target.getActionCommand();
            try {
                if (actionCommand.equals("Listen to Director")) {
                    NamedObj model = getModel();
                    boolean success = false;
                    if (model instanceof Actor) {
                        Director director = ((Actor)model).getDirector();
                        if (director != null) {
                            Effigy effigy = (Effigy)getTableau().getContainer();
                            // Create a new text effigy inside this one.
                            Effigy textEffigy = new TextEffigy(effigy,
                                    effigy.uniqueName("debug listener"));
                            DebugListenerTableau tableau =
                                new DebugListenerTableau(textEffigy,
                                        textEffigy.uniqueName("debugListener"));
                            tableau.setDebuggable(director);
                            success = true;
                        }
                    }
                    if (!success) {
                        MessageHandler.error("No director to listen to!");
                    }
                } else if (actionCommand.equals("Animate Execution")) {
                    // To support animation, add a listener to the
                    // first director found above in the hierarchy.
                    // NOTE: This doesn't properly support all
                    // hierarchy.  Insides of transparent composite
                    // actors do not get animated if they are classes
                    // rather than instances.
                    NamedObj model = getModel();
                    if (model instanceof Actor) {
                        // Dialog to ask for a delay time.
                        Query query = new Query();
                        query.addLine("delay",
                                "Time (in ms) to hold highlight",
                                Long.toString(_lastDelayTime));
                        ComponentDialog dialog = new ComponentDialog(
                                EditIconFrame.this,
                                "Delay for Animation",
                                query);
                        if (dialog.buttonPressed().equals("OK")) {
                            try {
                                _lastDelayTime = Long.parseLong(
                                        query.getStringValue("delay"));
                                _controller.setAnimationDelay(_lastDelayTime);
                                Director director
                                    = ((Actor)model).getDirector();
                                while (director == null
                                        && model instanceof Actor) {
                                    model = (NamedObj)model.getContainer();
                                    if (model instanceof Actor) {
                                        director = ((Actor)model).getDirector();
                                    }
                                }
                                if (director != null
                                        && _listeningTo != director) {
                                    if (_listeningTo != null) {
                                        _listeningTo.removeDebugListener(
                                                _controller);
                                    }
                                    director.addDebugListener(_controller);
                                    _listeningTo = director;
                                }
                            } catch (NumberFormatException ex) {
                                MessageHandler.error(
                                        "Invalid time, which is required "
                                        + "to be an integer", ex);
                            }
                        } else {
                            MessageHandler.error(
                                    "Cannot find the director. Possibly this "
                                    + "is because this is a class, not an "
                                    + "instance.");
                        }
                    } else {
                        MessageHandler.error(
                                "Model is not an actor. Cannot animate.");
                    }
                } else if (actionCommand.equals("Stop Animating")) {
                    if (_listeningTo != null) {
                        _listeningTo.removeDebugListener(_controller);
                        _controller.clearAnimation();
                        _listeningTo = null;
                    }
                }
            } catch (KernelException ex) {
                try {
                    MessageHandler.warning(
                            "Failed to create debug listener: " + ex);
                } catch (CancelException exception) {}
            }
        }

        private Director _listeningTo;
    }
}
