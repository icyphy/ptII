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

import ptolemy.homer.gui.tree.NamedObjectTree;
import ptolemy.homer.kernel.HomerMultiContent;
import ptolemy.homer.kernel.HomerWidgetElement;
import ptolemy.homer.kernel.LayoutFileOperations;
import ptolemy.homer.kernel.PositionableElement;
import ptolemy.homer.kernel.TabDefinition;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;
import ptserver.util.ServerUtility;

//////////////////////////////////////////////////////////////////////////
//// HomerMainFrame

/** The container window for the UI designer that maintains the palette of
 *  placeable elements of the model, widget references, and the tabs/scene placement.
 *  
 *  @author Anar Huseynov
 *  @version $Id$ 
 *  @since Ptolemy II 8.1
 *  @Pt.ProposedRating Red (ahuseyno)
 *  @Pt.AcceptedRating Red (ahuseyno)
 */
public class HomerMainFrame extends JFrame {

    ///////////////////////////////////////////////////////////////////
    ////                         constructor                       ////

    /** Create the UI designer frame.
     */
    public HomerMainFrame() {
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
        _contents.add(object);
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
        addVisualNamedObject(panel.getTag(), element);
    }

    public void addVisualNamedObject(String tag, HomerWidgetElement element)
            throws IllegalActionException {
        _contents.addElement(tag, element);
    }

    /** Get the set of references to on-screen remote objects.
     *  @return The set of remote object references.
     */
    public HashSet<NamedObj> getRemoteObjectSet() {
        return _contents.getRemoteElements();
    }

    /** Save the layout file.
     *  @param layoutFile The target file for the "Save As" operation.
     */
    public void saveLayoutAs(File layoutFile) {
        LayoutFileOperations.saveAs(this, layoutFile);
    }

    /** Get the tabbed layout scene.
     *  @return The reference to the tabbed area of the screen.
     */
    public TabbedLayoutScene getTabbedLayoutScene() {
        return _screenPanel;
    }

    /** Prepare the scene for creating a new layout and prompt the user for
     *  file selection.
     *  @param modelURL The url of the model file to be opened.
     */
    public void newLayout(URL modelURL) {
        _contents.clear();
        _modelURL = modelURL;

        try {
            _namedObjectTreePanel.setCompositeEntity(ServerUtility
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
        _contents.clear();
        _modelURL = modelURL;
        _layoutURL = layoutURL;

        try {
            _namedObjectTreePanel.setCompositeEntity(LayoutFileOperations.open(
                    this, modelURL, layoutURL));
        } catch (IllegalActionException e) {
            MessageHandler.error(e.getMessage(), e);
        } catch (NameDuplicationException e) {
            MessageHandler.error(e.getMessage(), e);
            e.printStackTrace();
        } catch (CloneNotSupportedException e) {
            MessageHandler.error(e.getMessage(), e);
        }
        
        // Need to remove the first tab, the default tab
        _contents.removeTab(0);
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

    public void removeVisualNamedObject(PositionableElement element) {
        _contents.removeElement(element);
    }

    public void addTab(String name) {
        _contents.addTab(name);
    }

    public void addTab(String tag, String name) {
        try {
            _contents.addTab(tag, name);
        } catch (IllegalActionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void removeTab(int index) {
        _contents.removeTab(index);
    }
    
    public void setTabTitleAt(int position, String text) {
        _contents.setNameAt(position, text);
    }

    public Scene getTabContent(String tabTag) {
        return (Scene) _contents.getContent(tabTag);
    }

    public boolean contains(NamedObj key) {
        return _contents.contains(key);
    }

    public ArrayList<TabDefinition> getAllTabs() {
        return _contents.getAllTabs();
    }

    public URL getLayoutURL() {
        try {
            if (! new File(_layoutURL.toURI()).canRead()) {
                return null;
            }
        } catch (URISyntaxException e) {
            return null;
        }
        return _layoutURL;
    }

    /** Get the model URL.
     *  @return The model URL.
     */
    public URL getModelURL() {
        try {
            if (! new File(_modelURL.toURI()).canRead()) {
                return null;
            }
        } catch (URISyntaxException e) {
            return null;
        }
        return _modelURL;
    }
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize the default look of the frame.
     */
    private void _initializeFrame() {
        _contents = new HomerMultiContent(new TabScenePanel(this), this);

        _contentPane = new JPanel();
        _contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        _contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(_contentPane);

        _namedObjectTreePanel = new NamedObjectTree();
        _namedObjectTreePanel.setBorder(new TitledBorder(new EtchedBorder(
                EtchedBorder.LOWERED, null, null), "Named Object Tree",
                TitledBorder.LEADING, TitledBorder.TOP, null,
                new Color(0, 0, 0)));
        _namedObjectTreePanel.setPreferredSize(new Dimension(250, 10));
        _contentPane.add(_namedObjectTreePanel, BorderLayout.WEST);

        JPanel pnlEast = new JPanel();
        pnlEast.setPreferredSize(new Dimension(200, 10));
        _contentPane.add(pnlEast, BorderLayout.EAST);
        pnlEast.setLayout(new BorderLayout(0, 0));

        JPanel pnlModelImage = new JPanel();
        pnlModelImage.setBorder(new TitledBorder(null, "Graph Preview",
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        pnlModelImage.setPreferredSize(new Dimension(10, 150));
        pnlEast.add(pnlModelImage, BorderLayout.NORTH);

        _remoteObjectsPanel = new RemoteObjectList(this);
        _remoteObjectsPanel.setBorder(new TitledBorder(null,
                "Remote Named Objects", TitledBorder.LEADING, TitledBorder.TOP,
                null, null));
        pnlEast.add(_remoteObjectsPanel, BorderLayout.CENTER);

        _spnScreen = new JScrollPane();
        _contentPane.add(_spnScreen, BorderLayout.CENTER);

        _screenPanel = new TabbedLayoutScene(this);
        _spnScreen.setViewportView(_screenPanel);
        // TODO is this needed?
        _screenPanel.getSceneTabs().setPreferredSize(new Dimension(600, 400));

        _contents.addListener(_remoteObjectsPanel);
        _contents.addListener(_screenPanel);

        addTab("Default");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private JPanel _contentPane;
    private JScrollPane _spnScreen;
    private NamedObjectTree _namedObjectTreePanel;
    private TabbedLayoutScene _screenPanel;
    private RemoteObjectList _remoteObjectsPanel;
    private URL _modelURL;
    private URL _layoutURL;
    private HomerMultiContent _contents;
}
