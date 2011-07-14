/* TODO
 Copyright (c) 2011 The Regents of the University of California.
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
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.netbeans.api.visual.widget.Widget;

import ptolemy.homer.gui.tree.NamedObjectTree;
import ptolemy.homer.kernel.HomerLocation;
import ptolemy.homer.kernel.LayoutFileOperations;
import ptolemy.homer.kernel.WidgetLoader;
import ptolemy.homer.widgets.NamedObjectWidgetInterface;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;

//////////////////////////////////////////////////////////////////////////
//// UIDesignerFrame

/** The container window for the UI designer that maintains the palette of
 *  placeable elements of the model, widget references, and the tabs/scene placement.
 *  
 *  @author Anar Huseynov
 *  @version $Id$ 
 *  @since Ptolemy II 8.1
 *  @Pt.ProposedRating Red (ahuseyno)
 *  @Pt.AcceptedRating Red (ahuseyno)
 */
public class UIDesignerFrame extends JFrame {

    ///////////////////////////////////////////////////////////////////
    ////                         constructor                       ////

    /** Create the UI designer frame.
     */
    public UIDesignerFrame() {
        setTitle("UI Designer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 800, 600);

        _initializeFrame();
        setJMenuBar(new HomerMenu(this).getMenuBar());

        newLayout(this.getClass().getResource(
                "/ptserver/test/junit/SoundSpectrum.xml"));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a non-visual NamedObj item to the panel.
     *  @param object The NamedObj to be added to the list.
     */
    public void addNonVisualNamedObject(NamedObj object) {
        _remoteObjectSet.add(object);
        _pnlRemoteObjects.addItem(object);
    }

    /** Add a visual NamedObj item to the panel.
     *  @param panel The target panel.
     *  @param object The NamedObj to be added to the list.
     *  @param dimension The size of the widget.
     *  @param location Location on the scene.
     *  @exception IllegalActionException If the appropriate widget cannot be loaded.
     *  @exception NameDuplicationException If the NamedObj duplicates a name of
     *  an item already on the scene.
     */
    public void addVisualNamedObject(TabScenePanel panel, NamedObj object,
            Dimension dimension, Point location) throws IllegalActionException,
            NameDuplicationException {
        Class<? extends NamedObj> namedObjectWidgetClass = object.getClass();
        // Not needed any more since AttributeStyleWidget will handle all attributes
        //        if (object instanceof Attribute) {
        //            List<ParameterEditorStyle> attributeList = object
        //                    .attributeList(ParameterEditorStyle.class);
        //            if (!attributeList.isEmpty()) {
        //                namedObjectWidgetClass = attributeList.get(0).getClass();
        //            }
        //        }
        NamedObjectWidgetInterface widget = (NamedObjectWidgetInterface) WidgetLoader
                .loadWidget(panel.getScene(), object, namedObjectWidgetClass);
        _widgetMap.put(object, widget);
        _remoteObjectSet.add(object);
        _widgetTabMap.put(widget, panel);
        _pnlRemoteObjects.addItem(object);

        if (dimension != null && dimension.getWidth() > 0
                && dimension.getHeight() > 0) {
            ((Widget) widget).getBounds().setSize(dimension);
        }
        panel.addWidget((Widget) widget, location);
    }

    /** Add a visual NamedObj item to the panel.
     *  @param panel The target panel.
     *  @param object The NamedObj to be added to the list.
     *  @param location Location on the scene.
     *  @exception IllegalActionException If the appropriate widget cannot be loaded.
     *  @exception NameDuplicationException If the NamedObj duplicates a name of
     *  an item already on the scene.
     */
    public void addVisualNamedObject(TabScenePanel panel, NamedObj object,
            HomerLocation location) throws IllegalActionException,
            NameDuplicationException {
        Dimension dimenstion = new Dimension(location.getWidth(),
                location.getHeight());
        Point point = new Point(location.getX(), location.getY());
        addVisualNamedObject(panel, object, dimenstion, point);
    }

    /** Get the set of references to on-screen remote objects.
     *  @return The set of remote object references.
     */
    public HashSet<NamedObj> getRemoteObjectSet() {
        return _remoteObjectSet;
    }

    /** Save the layout file.
     *  @param layoutFile The target file for the "Save As" operation.
     */
    public void saveLayoutAs(File layoutFile) {
        LayoutFileOperations.saveAs(this, layoutFile);
    }

    /** Get the model URL.
     *  @return The model URL.
     */
    public URL getModelURL() {
        return _modelURL;
    }

    /** Get the tabbed layout scene.
     *  @return The reference to the tabbed area of the screen.
     */
    public TabbedLayoutScene getTabbedLayoutScene() {
        return _pnlScreen;
    }

    /** Get the widget-to-NamedObj mapping.
     *  @return The mapping of widgets to their respective NamedObj items.
     */
    public HashMap<NamedObj, NamedObjectWidgetInterface> getWidgetMap() {
        return _widgetMap;
    }

    /** Get the widget-to-tab mapping.
     *  @return The mapping of widgets to their respective tabs.
     */
    public HashMap<NamedObjectWidgetInterface, TabScenePanel> getWidgetTabMap() {
        return _widgetTabMap;
    }

    /** Prepare the scene for creating a new layout and prompt the user for
     *  file selection.
     *  @param modelURL The url of the model file to be opened.
     */
    public void newLayout(URL modelURL) {
        _widgetMap.clear();
        _widgetTabMap.clear();
        _remoteObjectSet.clear();
        _pnlScreen.clear();
        _modelURL = modelURL;

        try {
            _pnlNamedObjectTree.setCompositeEntity(LayoutFileOperations
                    .openModelFile(modelURL));
        } catch (IllegalActionException e) {
            MessageHandler.error(e.getMessage(), e);
        }
    }

    /** Prepare the scene for creating a new layout and prompt the user for
     *  file selection.
     *  @param modelURL The url of the model file to be opened.
     */
    public void openLayout(URL modelURL, URL layoutURL) {
        _widgetMap.clear();
        _widgetTabMap.clear();
        _remoteObjectSet.clear();
        _pnlScreen.clear();
        _modelURL = modelURL;

        try {
            _pnlNamedObjectTree.setCompositeEntity(LayoutFileOperations
                    .openModelFile(modelURL));
        } catch (IllegalActionException e) {
            MessageHandler.error(e.getMessage(), e);
        }
    }

    /** Remove the NamedObj from the widget map and list of remote objects.
     *  @param object The NamedObj item to be removed.
     */
    public void removeNamedObject(NamedObj object) {

        // Remove it from the scene.
        NamedObjectWidgetInterface widget = _widgetMap.get(object);
        if (widget != null) {
            _widgetTabMap.get(widget).removeWidget((Widget) widget);
            _widgetTabMap.remove(widget);
            _widgetMap.remove(object);
        }

        // Remove it from remote objects.
        if (_remoteObjectSet.contains(object)) {
            _pnlRemoteObjects.removeItem(object);
            _remoteObjectSet.remove(object);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize the default look of the frame.
     */
    private void _initializeFrame() {
        _contentPane = new JPanel();
        _contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        _contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(_contentPane);

        _pnlNamedObjectTree = new NamedObjectTree();
        _pnlNamedObjectTree.setBorder(new TitledBorder(new EtchedBorder(
                EtchedBorder.LOWERED, null, null), "Named Object Tree",
                TitledBorder.LEADING, TitledBorder.TOP, null,
                new Color(0, 0, 0)));
        _pnlNamedObjectTree.setPreferredSize(new Dimension(250, 10));
        _contentPane.add(_pnlNamedObjectTree, BorderLayout.WEST);

        JPanel pnlEast = new JPanel();
        pnlEast.setPreferredSize(new Dimension(200, 10));
        _contentPane.add(pnlEast, BorderLayout.EAST);
        pnlEast.setLayout(new BorderLayout(0, 0));

        JPanel pnlModelImage = new JPanel();
        pnlModelImage.setBorder(new TitledBorder(null, "Graph Preview",
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        pnlModelImage.setPreferredSize(new Dimension(10, 150));
        pnlEast.add(pnlModelImage, BorderLayout.NORTH);

        _pnlRemoteObjects = new RemoteObjectList();
        _pnlRemoteObjects.setMainFrame(this);
        _pnlRemoteObjects.setBorder(new TitledBorder(null,
                "Remote Named Objects", TitledBorder.LEADING, TitledBorder.TOP,
                null, null));
        pnlEast.add(_pnlRemoteObjects, BorderLayout.CENTER);

        _spnScreen = new JScrollPane();
        _contentPane.add(_spnScreen, BorderLayout.CENTER);

        _pnlScreen = new TabbedLayoutScene();
        _pnlScreen.setMainFrame(this);
        _pnlScreen.addTab("Default");
        _pnlScreen.selectTab(0);
        _spnScreen.setViewportView(_pnlScreen);
        _pnlScreen.getSceneTabs().setPreferredSize(new Dimension(600, 400));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private JPanel _contentPane;
    private JScrollPane _spnScreen;
    private NamedObjectTree _pnlNamedObjectTree;
    private TabbedLayoutScene _pnlScreen;
    private HashMap<NamedObj, NamedObjectWidgetInterface> _widgetMap = new HashMap<NamedObj, NamedObjectWidgetInterface>();
    private HashMap<NamedObjectWidgetInterface, TabScenePanel> _widgetTabMap = new HashMap<NamedObjectWidgetInterface, TabScenePanel>();
    private HashSet<NamedObj> _remoteObjectSet = new HashSet<NamedObj>();
    private RemoteObjectList _pnlRemoteObjects;
    private URL _modelURL;
}
