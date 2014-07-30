/* The main content frame of the Homer UI designer.

 Copyright (c) 2011-2014 The Regents of the University of California.
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
package ptolemy.homer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.netbeans.api.visual.widget.Scene;

import ptolemy.actor.gui.style.NotEditableLineStyle;
import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.homer.HomerApplication;
import ptolemy.homer.kernel.HomerConstants;
import ptolemy.homer.kernel.HomerMultiContent;
import ptolemy.homer.kernel.HomerWidgetElement;
import ptolemy.homer.kernel.LayoutFileOperations;
import ptolemy.homer.kernel.LayoutParser.ScreenOrientation;
import ptolemy.homer.kernel.PositionableElement;
import ptolemy.homer.kernel.TabDefinition;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.actor.ActorEditorGraphController;
import ptolemy.vergil.actor.ActorGraphModel;
import ptolemy.vergil.basic.BasicGraphPane;
import diva.graph.JGraph;
import diva.gui.toolbox.JCanvasPanner;

///////////////////////////////////////////////////////////////////
//// HomerMainFrame

/** The container window for the UI designer that maintains the palette of
 *  placeable elements of the model, widget references, and the tabs/scene placement.
 *
 *  @author Anar Huseynov
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (ahuseyno)
 *  @Pt.AcceptedRating Red (ahuseyno)
 */
@SuppressWarnings("serial")
public class HomerMainFrame extends JFrame {

    ///////////////////////////////////////////////////////////////////
    ////                         constructor                       ////

    /** Create the UI designer frame.
     *  @param application The application hosting this frame.
     */
    public HomerMainFrame(HomerApplication application) {
        setTitle("Homer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(_DEFAULT_BOUNDS, _DEFAULT_BOUNDS, _DEFAULT_FRAME_WIDTH,
                _DEFAULT_FRAME_HEIGHT);

        _application = application;
        _menu = new HomerMenu(this);
        _initializeFrame();

        setJMenuBar(_menu.getMenuBar());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a label to the scene.
     *  @param panel The panel on which to add the label.
     *  @param label The label text.
     *  @param dimension The dimensions of the label.
     *  @param point The position to place the label.
     *  @exception IllegalActionException  If the attribute is not of an
     *  acceptable attribute for the container, or if the container is not
     *  an instance of Settable.
     *  @exception NameDuplicationException If the name coincides with an
     *  attribute already in the container.
     */
    public void addLabel(TabScenePanel panel, String label,
            Dimension dimension, Point point) throws IllegalActionException,
            NameDuplicationException {

        Attribute tabsNode = _topLevelActor
                .getAttribute(HomerConstants.TABS_NODE);
        Attribute tabNode = tabsNode.getAttribute(panel.getTag());

        Parameter stringAttribute = new Parameter(tabNode,
                tabNode.uniqueName("label"));
        new NotEditableLineStyle(stringAttribute, "_style").setPersistent(true);

        stringAttribute.setVisibility(Settable.EXPERT);
        stringAttribute.setExpression(label);
        stringAttribute.setPersistent(true);

        addVisualNamedObject(panel, stringAttribute, dimension, point);
    }

    /** Add a non-visual NamedObj item to the panel.
     *  @param object The NamedObj to be added to the list.
     */
    public void addNonVisualNamedObject(NamedObj object) {
        _contents.add(object);
    }

    /** Add a tab to the screen with the given name.
     *  @param tabName The name of the tab.
     */
    public void addTab(String tabName) {
        if (_topLevelActor == null) {
            return;
        }

        try {
            _contents.addTab(_topLevelActor, tabName);
        } catch (NameDuplicationException e) {
            MessageHandler.error(e.getMessage(), e);
        } catch (IllegalActionException e) {
            MessageHandler.error(e.getMessage(), e);
        }
    }

    /** Add a tab to the screen with the given name and tag.
     *  @param tabTag The tag of the tab.
     *  @param tabName The name of the tab.
     */
    public void addTab(String tabTag, String tabName) {
        if (_topLevelActor == null) {
            return;
        }

        try {
            _contents.addTab(_topLevelActor, tabTag, tabName);
        } catch (IllegalActionException e) {
            MessageHandler.error(e.getMessage(), e);
        } catch (NameDuplicationException e) {
            MessageHandler.error(e.getMessage(), e);
        }
    }

    /** Add a visual NamedObj item to the panel.
     *  @param tag The tag associated with the element.
     *  @param element The NamedObj to be added.
     *  @exception IllegalActionException If the content area is not set.
     */
    public void addVisualNamedObject(String tag, HomerWidgetElement element)
            throws IllegalActionException {
        _contents.addElement(tag, element);
    }

    /** Add a visual NamedObj item to the panel.
     *  @param panel The target panel.
     *  @param object The NamedObj to be added to the list.
     *  @param dimension The size of the widget.
     *  @param point Location on the scene.
     *  @exception IllegalActionException If the appropriate widget cannot be loaded.
     *  @exception NameDuplicationException If the NamedObj duplicates a name of
     *  an item already on the scene.
     */
    public void addVisualNamedObject(TabScenePanel panel, NamedObj object,
            Dimension dimension, Point point) throws IllegalActionException,
            NameDuplicationException {
        if (point == null) {
            throw new IllegalActionException(
                    "Cannot create visual representation without the x, y coordinates.");
        }

        HomerWidgetElement element = new HomerWidgetElement(object,
                panel.getContent());
        if (dimension == null) {
            dimension = new Dimension(0, 0);
        }

        element.setLocation((int) point.getX(), (int) point.getY(),
                (int) dimension.getWidth(), (int) dimension.getHeight());
        element.setTab(panel.getTag());
        addVisualNamedObject(panel.getTag(), element);
    }

    /** See if the multi-content already has the NamedObj.
     *  @param key The NamedObj to check existence.
     *  @return If the NamedObj is in the content.
     */
    public boolean contains(NamedObj key) {
        return _contents.contains(key);
    }

    /** Get all tab definitions.
     *  @return The tab definitions of the window.
     */
    public ArrayList<TabDefinition> getAllTabs() {
        return _contents.getAllTabs();
    }

    /** Get the current layout file URL.
     *  @return The current layout file URL.
     */
    public URL getLayoutURL() {
        if (_layoutURL == null) {
            return null;
        }
        if (_layoutURL.getFile().equals("")) {
            return null;
        }

        return _layoutURL;
    }

    /** Get the model URL.
     *  @return The model URL.
     */
    public URL getModelURL() {
        try {
            if (!new File(_modelURL.toURI()).canRead()) {
                return null;
            }
        } catch (URISyntaxException e) {
            return null;
        }
        return _modelURL;
    }

    /** Get the selected screen orientation.
     *  @return The screen orientation.
     *  @see #setOrientation(ptolemy.homer.kernel.LayoutParser.ScreenOrientation)
     */
    public ScreenOrientation getOrientation() {
        if (_screenPanel.getSceneTabs().getPreferredSize().height > _screenPanel
                .getSceneTabs().getPreferredSize().width) {
            return ScreenOrientation.PORTRAIT;
        } else {
            return ScreenOrientation.LANDSCAPE;
        }
    }

    /** Get the set of references to on-screen remote objects.
     *  @return The set of remote object references.
     */
    public HashSet<NamedObj> getRemoteObjectSet() {
        return _contents.getRemoteElements();
    }

    /** Get the screen size.
     *  @return The size of the scene.
     *  @see #setScreenSize(Dimension)
     */
    public Dimension getScreenSize() {
        return _screenPanel.getPreferredSize();
    }

    /** Get the tabbed layout scene.
     *  @return The reference to the tabbed area of the screen.
     */
    public TabbedLayoutScene getTabbedLayoutScene() {
        return _screenPanel;
    }

    /** Get the scene on the tab.
     *  @param tabTag The tag of the tab being retrieved.
     *  @return The scene on the selected tab.
     */
    public Scene getTabContent(String tabTag) {
        return (Scene) _contents.getContent(tabTag);
    }

    /** Prepare the scene for creating a new layout and prompt the user for
     *  file selection.
     *  @param modelURL The url of the model file to be opened.
     */
    public void newLayout(URL modelURL) {
        _contents.clear();
        _modelURL = modelURL;
        _layoutURL = null;

        try {
            _topLevelActor = LayoutFileOperations.openModelFile(modelURL);
            _namedObjectTreePanel.setCompositeEntity(_topLevelActor);
            _initializeGraphPreview(_topLevelActor);
            addTab("Default");
        } catch (IllegalActionException e) {
            MessageHandler.error(e.getMessage(), e);
        } catch (Exception e) {
            MessageHandler.error(e.getMessage(), e);
        }

    }

    /** Prepare the scene for creating a new layout and prompt the user for
     *  file selection.
     *  @param modelURL The url of the model file to be opened.
     *  @param layoutURL The url of the layout file to be opened.
     */
    public void openLayout(URL modelURL, URL layoutURL) {
        _contents.clear();
        _modelURL = modelURL;
        _layoutURL = layoutURL;

        try {
            _topLevelActor = LayoutFileOperations.open(this, modelURL,
                    layoutURL);
            LayoutFileOperations.parseModel(this);

            // Get the screen orientation.
            StringAttribute orientation = (StringAttribute) _topLevelActor
                    .getAttribute(HomerConstants.ORIENTATION_NODE);
            if (orientation != null) {
                if (orientation.getExpression().equals("landscape")) {
                    _menu.setOrientation(ScreenOrientation.LANDSCAPE);
                } else if (orientation.getExpression().equals("portrait")) {
                    _menu.setOrientation(ScreenOrientation.PORTRAIT);
                }
            }

            // Get the window properties and sizing.
            Parameter screenSize = (Parameter) _topLevelActor
                    .getAttribute(HomerConstants.SCREEN_SIZE);
            if (screenSize != null) {
                ArrayToken token = (ArrayToken) screenSize.getToken();
                if (token != null) {
                    Dimension dimensions = new Dimension(
                            ((IntToken) token.getElement(0)).intValue(),
                            ((IntToken) token.getElement(1)).intValue());

                    // Set the window size according to attribute.
                    _screenPanel.getSceneTabs().setPreferredSize(dimensions);
                }
            }

            _namedObjectTreePanel.setCompositeEntity(_topLevelActor);
            _initializeGraphPreview(_topLevelActor);
        } catch (IllegalActionException e) {
            MessageHandler.error(e.getMessage(), e);
        } catch (NameDuplicationException e) {
            MessageHandler.error(e.getMessage(), e);
            e.printStackTrace();
        } catch (CloneNotSupportedException e) {
            MessageHandler.error(e.getMessage(), e);
        } catch (Exception e) {
            MessageHandler.error(e.getMessage(), e);
        }
    }

    /** Remove the NamedObj from the widget map and list of remote objects.
     *  @param object The NamedObj item to be removed.
     */
    public void remove(NamedObj object) {
        PositionableElement element = _contents.getElement(object);
        if (element != null) {
            _contents.removeElement(element);
        } else {
            _contents.remove(object);
        }
    }

    /** Remove the tab at the given index.
     *  @param index The tab index to be removed.
     */
    public void removeTab(int index) {
        _contents.removeTab(index);
    }

    /** Remove the visual named object from the scene.
     *  @param element The screen element to be removed.
     */
    public void removeVisualNamedObject(PositionableElement element) {
        _contents.removeElement(element);

        // Check if this is a label widget contained within a tab.
        NamedObj object = element.getElement();
        if (isLabelWidget(object)) {
            // Remove the label from the container.
            try {
                ((Attribute) object).setContainer(null);
            } catch (IllegalActionException e) {
                // can't happen since we are removing it
                MessageHandler.error(e.getMessage(), e);
            } catch (NameDuplicationException e) {
                // can't happen since we are removing it
                MessageHandler.error(e.getMessage(), e);
            }
        }
    }

    /** Save the layout file.
     *  @param layoutFile The target file for the "Save As" operation.
     */
    public void saveLayoutAs(File layoutFile) {
        try {
            _layoutURL = layoutFile.toURI().toURL();
        } catch (MalformedURLException e) {
            MessageHandler.error(e.getMessage(), e);
        }
        LayoutFileOperations.saveAs(this, layoutFile);
    }

    /** Set the orientation of the scene.
     *  @param orientation The orientation of the scene.
     *  @see #getOrientation()
     */
    public void setOrientation(ScreenOrientation orientation) {

        TabbedLayoutScene scene = getTabbedLayoutScene();
        if (scene != null) {
            double height = scene.getSceneTabs().getPreferredSize().getHeight();
            double width = scene.getSceneTabs().getPreferredSize().getWidth();

            // If in the opposite orientation, invert dimensions.
            if (orientation == ScreenOrientation.LANDSCAPE) {
                if (height >= width) {
                    scene.getSceneTabs().setPreferredSize(
                            new Dimension((int) height, (int) width));
                }
            } else if (orientation == ScreenOrientation.PORTRAIT) {
                if (width >= height) {
                    scene.getSceneTabs().setPreferredSize(
                            new Dimension((int) height, (int) width));
                }
            }

            //_orientation = orientation;
            scene.revalidate();
        }
    }

    /** Set the screen size.
     *  @param dimension The screen size.
     *  @see #getScreenSize()
     */
    public void setScreenSize(Dimension dimension) {
        if (dimension != null) {
            if (getOrientation() == ScreenOrientation.LANDSCAPE) {
                if (dimension.width > dimension.height) {
                    _screenPanel.getSceneTabs().setPreferredSize(dimension);
                } else {
                    // Invert height and width.
                    _screenPanel.getSceneTabs().setPreferredSize(
                            new Dimension(dimension.height, dimension.width));
                }
            } else if (getOrientation() == ScreenOrientation.PORTRAIT) {
                if (dimension.height > dimension.width) {
                    _screenPanel.getSceneTabs().setPreferredSize(dimension);
                } else {
                    // Invert height and width.
                    _screenPanel.getSceneTabs().setPreferredSize(
                            new Dimension(dimension.height, dimension.width));
                }
            }

            _screenPanel.revalidate();
        }
    }

    /** Set the tab title.
     *  @param position The tab index being changed.
     *  @param text The new tab text.
     */
    public void setTabTitleAt(int position, String text) {
        try {
            _contents.setNameAt(position, text);
        } catch (IllegalActionException e) {
            MessageHandler.error(e.getMessage(), e);
        }
    }

    /** Return if the object argument is a label widget.
     *  A label widget is an object that is an Attribute that contains
     *  a {@link ptolemy.homer.kernel.HomerConstants#TAB_NODE} and the
     *  value of the TAB_NODE is the name of the container of the
     *  object.
     *  @param object The object to be checked.
     *  @return If the value of the object argument is a label widget.
     */
    public static boolean isLabelWidget(NamedObj object) {
        Attribute tab = object.getAttribute(HomerConstants.TAB_NODE);
        if (object instanceof Attribute
                && tab instanceof Settable
                && ((Settable) tab).getExpression().equals(
                        object.getContainer().getName())) {
            return true;
        }

        return false;
    }

    /** Get the top level actor.
     *  @return The top level actor.
     */
    public CompositeEntity getTopLevelActor() {
        return _topLevelActor;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize the default look of the frame.
     */
    private void _initializeFrame() {
        _contents = new HomerMultiContent(new TabScenePanel(this));

        _contentPane = new JPanel();
        _contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        _contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(_contentPane);

        _namedObjectTreePanel = new NamedObjectTree();
        _namedObjectTreePanel.setBorder(new TitledBorder(new EtchedBorder(
                EtchedBorder.LOWERED, null, null), "Model Elements",
                TitledBorder.LEADING, TitledBorder.TOP, null,
                new Color(0, 0, 0)));
        _namedObjectTreePanel.setPreferredSize(new Dimension(_SIDEBAR_WIDTH,
                _DUMMY_HEIGHT));
        _contentPane.add(_namedObjectTreePanel, BorderLayout.WEST);

        JPanel pnlEast = new JPanel();
        pnlEast.setPreferredSize(new Dimension(_SIDEBAR_WIDTH, _DUMMY_HEIGHT));
        pnlEast.setLayout(new BorderLayout(0, 0));
        _contentPane.add(pnlEast, BorderLayout.EAST);

        _graphPanel = new JPanel();
        _graphPanel.setLayout(new BorderLayout());
        _graphPanel.setBorder(new TitledBorder(null, "Graph Preview",
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        _graphPanel.setPreferredSize(new Dimension(_SIDEBAR_WIDTH,
                _GRAPH_HEIGHT));
        pnlEast.add(_graphPanel, BorderLayout.NORTH);

        _remoteObjectsPanel = new RemoteObjectList(this);
        _remoteObjectsPanel.setBorder(new TitledBorder(null, "Layout Elements",
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        pnlEast.add(_remoteObjectsPanel, BorderLayout.CENTER);

        _screenPanel = new TabbedLayoutScene(this);
        _screenPanel.getSceneTabs().setPreferredSize(
                new Dimension(_DEFAULT_SCENE_WIDTH, _DEFAULT_SCENE_HEIGHT));

        JScrollPane scroller = new JScrollPane();
        scroller.setBorder(new TitledBorder(null, "Screen Layout",
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        scroller.setViewportView(_screenPanel);

        _contentPane.add(scroller, BorderLayout.CENTER);
        _contents.addListener(_remoteObjectsPanel);
        _contents.addListener(_screenPanel);

        addTab("Default");
    }

    /** Initialize the graph preview frame with an image of the actor graph.
     *  @param topLevelActor The top level actor of the parsed model.
     */
    private void _initializeGraphPreview(CompositeEntity topLevelActor) {
        ActorEditorGraphController controller = new ActorEditorGraphController();
        controller.setConfiguration(_application.getConfiguration());

        _graphPanel.removeAll();
        _graphPanel.add(
                new JCanvasPanner(new JGraph(new BasicGraphPane(controller,
                        new ActorGraphModel(topLevelActor), topLevelActor))),
                        BorderLayout.CENTER);
        _graphPanel.revalidate();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The default bounds applied to this window.
     */
    private static final int _DEFAULT_BOUNDS = 100;

    /** The initial scene height.
     */
    private static final int _DEFAULT_FRAME_HEIGHT = 700;

    /** The initial scene width.
     */
    private static final int _DEFAULT_FRAME_WIDTH = 1200;

    /** The initial height of the scene.
     */
    private static final int _DEFAULT_SCENE_HEIGHT = 400;

    /** The initial width of the scene.
     */
    private static final int _DEFAULT_SCENE_WIDTH = 600;

    /** The dummy initial height parameter for frame sizing.
     */
    private static final int _DUMMY_HEIGHT = 10;

    /** The height of the actor graph image.
     */
    private static final int _GRAPH_HEIGHT = 150;

    /** The width of the east screen panel where the image and remote object list reside.
     */
    private static final int _SIDEBAR_WIDTH = 250;

    /** The host application of this frame.
     */
    private HomerApplication _application;

    /** The main content pane of the frame.
     */
    private JPanel _contentPane;

    /** The underlying multicontent of the screen.
     */
    private HomerMultiContent _contents;

    /** The actor graph panel that provides a visual representation of the model.
     */
    private JPanel _graphPanel;

    /** The current layout file URL.
     */
    private URL _layoutURL;

    /** The Homer menu bar.
     */
    private HomerMenu _menu;

    /** The current model file URL.
     */
    private URL _modelURL;

    /** The tree containing all elements of the model and sub-models.
     */
    private NamedObjectTree _namedObjectTreePanel;

    /** The orientation of the scene panels.
     */
    //private ScreenOrientation _orientation;

    /** The list of remote objects included as part of the layout file.
     */
    private RemoteObjectList _remoteObjectsPanel;

    /** The tabbed area onto which the user can drop widgets.
     */
    private TabbedLayoutScene _screenPanel;

    /**
     * Merged top level actor.
     */
    private CompositeEntity _topLevelActor;
}
