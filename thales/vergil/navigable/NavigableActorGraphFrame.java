/*
Created on 01 sept. 2003

Copyright (c) 2003 THALES.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

IN NO EVENT SHALL THALES BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE
OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THALES HAS BEEN
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

THALES SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS"
BASIS, AND THALES HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT,
UPDATES, ENHANCEMENTS, OR MODIFICATIONS.

@ProposedRating Red (jerome.blanc@thalesgroup.com)
@AcceptedRating
*/
package thales.vergil.navigable;

import java.awt.BorderLayout;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.Manager;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.DebugListenerTableau;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TextEffigy;
import ptolemy.gui.CancelException;
import ptolemy.gui.ComponentDialog;
import ptolemy.gui.MessageHandler;
import ptolemy.gui.Query;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.LibraryAttribute;
import ptolemy.vergil.VergilApplication;
import ptolemy.vergil.actor.ActorEditorGraphController;
import ptolemy.vergil.actor.ActorGraphModel;
import ptolemy.vergil.basic.ExtendedGraphFrame;

import thales.actor.gui.NavigableEffigy;
import thales.vergil.SingleWindowApplication;

import diva.canvas.Figure;
import diva.canvas.interactor.SelectionModel;
import diva.graph.GraphController;
import diva.graph.GraphModel;
import diva.graph.GraphPane;
import diva.graph.GraphUtilities;

//////////////////////////////////////////////////////////////////////////
//// NavigableActorGraphFrame
/**
<p>Titre : NavigableActorGraphFrame</p>
<p>Description : This is a simple copy of the actuel ActorGraphFrame
with additional functionalities for the navigation.</p>
<p>Société : Thales Research and technology</p>

@author Jérôme Blanc & Benoit Masson
01 sept. 2003
@since Ptolemy II 3.1
*/
public class NavigableActorGraphFrame extends ExtendedGraphFrame {

    /** Construct a frame associated with the specified Ptolemy II model.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  This is typically done by calling show() on the controlling tableau.
     *  This constructor results in a graph frame that obtains its library
     *  either from the model (if it has one) or the default library defined
     *  in the configuration.
     *  @see Tableau#show()
     *  @param entity The model to put in this frame.
     *  @param tableau The tableau responsible for this frame.
     */
    public NavigableActorGraphFrame(CompositeEntity entity, Tableau tableau) {
        this(entity, tableau, null);
    }

    /** Construct a frame associated with the specified Ptolemy II model.
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
    public NavigableActorGraphFrame(
            CompositeEntity entity,
            Tableau tableau,
            LibraryAttribute defaultLibrary) {
        super(entity, tableau, defaultLibrary);

        // Override the default help file.
        helpFile = "ptolemy/configs/doc/vergilGraphEditorHelp.htm";

        diva.gui.GUIUtilities.addToolBarButton(_toolbar, _upAction);

        URL img =
            getClass().getResource("/thales/vergil/navigable/img/zoomin.gif");
        if (img != null) {
            ImageIcon icon = new ImageIcon(img);
            ((JButton) _toolbar.getComponent(0)).setIcon(icon);
        }
        img =
            getClass().getResource(
                    "/thales/vergil/navigable/img/zoomreset.gif");
        if (img != null) {
            ImageIcon icon = new ImageIcon(img);
            ((JButton) _toolbar.getComponent(1)).setIcon(icon);
        }
        img =
            getClass().getResource("/thales/vergil/navigable/img/zoomfit.gif");
        if (img != null) {
            ImageIcon icon = new ImageIcon(img);
            ((JButton) _toolbar.getComponent(2)).setIcon(icon);
        }
        img =
            getClass().getResource("/thales/vergil/navigable/img/zoomout.gif");
        if (img != null) {
            ImageIcon icon = new ImageIcon(img);
            ((JButton) _toolbar.getComponent(3)).setIcon(icon);
        }
        img =
            getClass().getResource(
                    "/thales/vergil/navigable/img/fullScreen.gif");
        if (img != null) {
            ImageIcon icon = new ImageIcon(img);
            ((JButton) _toolbar.getComponent(4)).setIcon(icon);
        }

        _palettePane.remove(_libraryScrollPane);
        _palettePane.remove(_graphPanner);

        JScrollPane _treeVergil = new JScrollPane();
        NavigationTreeModel treeModel = null;
        Effigy effigy = getEffigy();
        if (effigy instanceof NavigableEffigy) {
            treeModel = ((NavigableEffigy) effigy).getNavigationModel();
        }

        _tree = new NavigationPTree(treeModel);
        _treeVergil.getViewport().add(_tree);
        _tabbedPalette.add("Navigation", _treeVergil);

        _tabbedPalette.add("Palette", _libraryScrollPane);

        _palettePane.add(_tabbedPalette, BorderLayout.CENTER);
        _palettePane.add(_graphPanner, BorderLayout.SOUTH);
    }

    //private members
    NavigationPTree _tree;

    //Accessor
    public NavigationPTree getTree() {
        return _tree;
    }

    /** Get the currently selected objects from this document, if any,
     *  and place them on the clipboard in MoML format.
     */
    /**
     * THALES CORRECTION, for some reasons, the relation can appear
     * into the namedObjSet before the copied entities ... so when the
     * paste action occurs, relations are rendered as diamonds ....
     * to avoid this silly things, the MoML export at the end of
     * the method is ordered as follow :
     * entities, relation and links to finish.
     */
    public void copy() {
        Clipboard clipboard =
            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
        GraphPane graphPane = _jgraph.getGraphPane();
        GraphController controller =
            (GraphController) graphPane.getGraphController();
        SelectionModel model = controller.getSelectionModel();
        GraphModel graphModel = controller.getGraphModel();
        Object selection[] = model.getSelectionAsArray();
        // A set, because some objects may represent the same
        // ptolemy object.
        HashSet namedObjSet = new HashSet();
        HashSet nodeSet = new HashSet();
        // First get all the nodes.
        for (int i = 0; i < selection.length; i++) {
            if (selection[i] instanceof Figure) {
                Object userObject = ((Figure) selection[i]).getUserObject();
                if (graphModel.isNode(userObject)) {
                    nodeSet.add(userObject);
                    NamedObj actual =
                        (NamedObj) graphModel.getSemanticObject(userObject);
                    namedObjSet.add(actual);
                }
            }
        }
        for (int i = 0; i < selection.length; i++) {
            if (selection[i] instanceof Figure) {
                Object userObject = ((Figure) selection[i]).getUserObject();
                if (graphModel.isEdge(userObject)) {
                    // Check to see if the head and tail are both being
                    // copied.  Only if so, do we actually take the edge.
                    Object head = graphModel.getHead(userObject);
                    Object tail = graphModel.getTail(userObject);
                    boolean headOK = nodeSet.contains(head);
                    boolean tailOK = nodeSet.contains(tail);
                    Iterator objects = nodeSet.iterator();
                    while (!(headOK && tailOK) && objects.hasNext()) {
                        Object object = objects.next();
                        if (!headOK
                                && GraphUtilities.isContainedNode(
                                        head,
                                        object,
                                        graphModel)) {
                            headOK = true;
                        }
                        if (!tailOK
                                && GraphUtilities.isContainedNode(
                                        tail,
                                        object,
                                        graphModel)) {
                            tailOK = true;
                        }
                    }
                    if (headOK && tailOK) {
                        NamedObj actual =
                            (NamedObj) graphModel.getSemanticObject(userObject);
                        namedObjSet.add(actual);
                    }
                }
            }
        }
        StringWriter buffer = new StringWriter();
        try {
            //THALES CORRECTION: entities fisrt
            Iterator elements = namedObjSet.iterator();
            while (elements.hasNext()) {
                NamedObj element = (NamedObj) elements.next();
                                // first level to avoid obnoxiousness with
                                // toplevel translations.
                if (!(element instanceof Relation)) {
                    element.exportMoML(buffer, 1);
                }
            }
            //THALES CORRECTION: relations in second
            elements = namedObjSet.iterator();
            while (elements.hasNext()) {
                NamedObj element = (NamedObj) elements.next();
                                // first level to avoid obnoxiousness with
                                // toplevel translations.
                if (element instanceof Relation) {
                    element.exportMoML(buffer, 1);
                }
            }
            CompositeEntity container = (CompositeEntity) graphModel.getRoot();
            buffer.write(container.exportLinks(1, namedObjSet));

            // The code below does not use a PtolemyTransferable,
            // to work around
            // a bug in the JDK that should be fixed as of jdk1.3.1.  The bug
            // is that cut and paste through the system clipboard to native
            // applications doesn't work unless you use string selection.
            clipboard.setContents(new StringSelection(buffer.toString()), this);
        } catch (Exception ex) {
            MessageHandler.error("Copy failed", ex);
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
        JMenuItem[] debugMenuItems =
        {
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

    /** If the ptolemy model associated with this frame is a top-level
     *  composite actor, use its manager to stop it.
     *  Remove the listeners that this frame registered with the ptolemy
     *  model. Also remove the listeners our graph model has created.
     *  @return True if the close completes, and false otherwise.
     */
    protected boolean _close() {
        CompositeEntity ptModel = getModel();
        if (ptModel instanceof CompositeActor
                && ptModel.getContainer() == null) {
            CompositeActor ptActorModel = (CompositeActor) ptModel;
            Manager manager = ptActorModel.getManager();
            if (manager != null) {
                manager.stop();
            }
        }

        //THALES CORRECTION
        //In the TableauFrame class, if the topLevel effigy has been modified
        //and if you have other Tableaux opened, nothing is saved
        //and all the opened Tableaux are closed ... so your modifications are loosed

        //To avoid this, we force the topLevel effigy (wich is a NavigableEffigy for us)
        //to answer 1 at the numberOfOpenTableaux in this case (and only this one)
        //we hope that this bug will be fixed in a future version of Ptolemy
        //to stop this silly thing ;)

        Effigy topEffigy = getEffigy().topEffigy();
        if (topEffigy instanceof NavigableEffigy
                && topEffigy.numberOfOpenTableaux() > 1
                && topEffigy.equals(getEffigy())) {
            ((NavigableEffigy) topEffigy).lieAtNumberOfOpendTableaux();
        }
        //END THALES ADDON

        return super._close();
    }

    /** Create a new graph pane. Note that this method is called in
     *  constructor of the base class, so it must be careful to not reference
     *  local variables that may not have yet been created.
     */
    protected GraphPane _createGraphPane() {
        _controller = new ActorEditorGraphController();
        _controller.setConfiguration(getConfiguration());
        _controller.setFrame(this);
        final ActorGraphModel graphModel = new ActorGraphModel(getModel());
        return new GraphPane(_controller, graphModel);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Debug menu for this frame. */
    protected JMenu _debugMenu;

    protected JTabbedPane _tabbedPalette = new JTabbedPane(JTabbedPane.BOTTOM);

    protected UpAction _upAction = new UpAction();

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    protected ActorEditorGraphController _controller;

    // The delay time specified that last time animation was set.
    private long _lastDelayTime = 0;

    ///////////////////////////////////////////////////////////////////
    ////                     public inner classes                  ////

    public class UpAction extends AbstractAction {
        public UpAction() {
            super("Up");
            // Load the image by using the absolute path to the gif.
            // Using a relative location should work, but it does not.
            // Use the resource locator of the class.
            URL img =
                getClass().getResource("/thales/vergil/navigable/img/Up.gif");
            if (img != null) {
                ImageIcon icon = new ImageIcon(img);
                putValue(diva.gui.GUIUtilities.LARGE_ICON, icon);
            }
            putValue("tooltip", "Up");
            putValue(
                    diva.gui.GUIUtilities.ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(
                            KeyEvent.VK_EQUALS,
                            java.awt.Event.CTRL_MASK));
            putValue(
                    diva.gui.GUIUtilities.MNEMONIC_KEY,
                    new Integer(KeyEvent.VK_M));
        }

        public void actionPerformed(ActionEvent e) {
            NamedObj toOpen = (NamedObj) getModel().getContainer();
            if (toOpen != null) {
                Configuration configuration =
                    SingleWindowApplication._mainFrame.getConfiguration();
                try {
                    configuration.openModel(toOpen);
                } catch (IllegalActionException e1) {
                    e1.printStackTrace();
                } catch (NameDuplicationException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    // NOTE: The following class is very similar to the inner class
    // in FSMGraphFrame.  Is there some way to merge these?
    // There seem to be enough differences that this may be hard.

    /** Listener for debug menu commands. */
    public class DebugMenuListener implements ActionListener {

        /** React to a menu command. */
        public void actionPerformed(ActionEvent e) {
            JMenuItem target = (JMenuItem) e.getSource();
            String actionCommand = target.getActionCommand();
            try {
                if (actionCommand.equals("Listen to Director")) {
                    NamedObj model = getModel();
                    boolean success = false;
                    if (model instanceof Actor) {
                        Director director = ((Actor) model).getDirector();
                        if (director != null) {
                            Effigy effigy =
                                (Effigy) getTableau().getContainer();
                            // Create a new text effigy inside this one.
                            Effigy textEffigy =
                                new TextEffigy(
                                        effigy,
                                        effigy.uniqueName("debug listener"));
                            DebugListenerTableau tableau =
                                new DebugListenerTableau(
                                        textEffigy,
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
                        query.addLine(
                                "delay",
                                "Time (in ms) to hold highlight",
                                Long.toString(_lastDelayTime));
                        ComponentDialog dialog =
                            new ComponentDialog(
                                    NavigableActorGraphFrame.this,
                                    "Delay for Animation",
                                    query);
                        if (dialog.buttonPressed().equals("OK")) {
                            try {
                                _lastDelayTime =
                                    Long.parseLong(
                                            query.getStringValue("delay"));
                                _controller.setAnimationDelay(_lastDelayTime);
                                Director director =
                                    ((Actor) model).getDirector();
                                while (director == null
                                        && model instanceof Actor) {
                                    model = (NamedObj) model.getContainer();
                                    if (model instanceof Actor) {
                                        director =
                                            ((Actor) model).getDirector();
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
                                        + "to be an integer",
                                        ex);
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
                } catch (CancelException exception) {
                }
            }
        }

        private Director _listeningTo;
    }
}
