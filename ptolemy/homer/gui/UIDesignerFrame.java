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
import java.awt.Dimension;
import java.awt.Point;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.netbeans.api.visual.widget.Widget;

import ptolemy.homer.gui.tree.NamedObjectTree;
import ptolemy.homer.kernel.LayoutFileOperations;
import ptolemy.homer.kernel.WidgetLoader;
import ptolemy.homer.widgets.NamedObjectWidgetInterface;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// UIDesignerFrame
/**
 * TODO
 * @author Anar Huseynov
 * @version $Id$ 
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public class UIDesignerFrame extends JFrame {

    /**
     * Create the frame.
     */
    public UIDesignerFrame() {
        setTitle("UI Designer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 800, 600);

        _initializeFrame();
        setJMenuBar(new HomerMenu(this).getMenuBar());

        _pnlNamedObjectTree.setCompositeEntity(LayoutFileOperations
                .openModelFile(this.getClass().getResource(
                        "/ptserver/test/junit/SoundSpectrum.xml")));
    }

    public void addNonVisualNamedObject(NamedObj object) {
        _remoteObjectSet.add(object);
        _pnlRemoteObjects.addItem(object);
    }

    public void addVisualNamedObject(TabScenePanel panel, NamedObj object,
            Point location) throws IllegalActionException,
            NameDuplicationException {
        // TODO handle attribute styles
        NamedObjectWidgetInterface widget = (NamedObjectWidgetInterface) WidgetLoader
                .loadWidget(panel.getScene(), object, object.getClass());
        _widgetMap.put(object, widget);
        _remoteObjectSet.add(object);
        _widgetTabMap.put(widget, panel);
        panel.addWidget((Widget) widget, location);
        _pnlRemoteObjects.addItem(object);
    }

    public void removeNamedObject(NamedObj object) {
        NamedObjectWidgetInterface widget = _widgetMap.get(object);
        if (widget != null) {
            _widgetTabMap.remove(widget);
        }
        _widgetMap.remove(object);
        _remoteObjectSet.remove(object);
    }

    public void newLayout(URL model) {
        _widgetMap.clear();
        _widgetTabMap.clear();
        _remoteObjectSet.clear();
        _pnlScreen.clear();
        
        _pnlNamedObjectTree.setCompositeEntity(LayoutFileOperations
                .openModelFile(model));
    }

    /**
     * @return the _widgetMap
     */
    public HashMap<NamedObj, NamedObjectWidgetInterface> getWidgetMap() {
        return _widgetMap;
    }

    /**
     * @return the _widgetTabMap
     */
    public HashMap<NamedObjectWidgetInterface, TabScenePanel> getWidgetTabMap() {
        return _widgetTabMap;
    }

    /**
     * @return the _remoteObjectSet
     */
    public HashSet<NamedObj> getRemoteObjectSet() {
        return _remoteObjectSet;
    }

    private void _initializeFrame() {
        _contentPane = new JPanel();
        _contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        _contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(_contentPane);

        _pnlNamedObjectTree = new NamedObjectTree();
        _pnlNamedObjectTree.setBorder(new TitledBorder(null, "NamedObjectTree",
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
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

    private JPanel _contentPane;
    private JScrollPane _spnScreen;
    private NamedObjectTree _pnlNamedObjectTree;
    private TabbedLayoutScene _pnlScreen;
    private HashMap<NamedObj, NamedObjectWidgetInterface> _widgetMap = new HashMap<NamedObj, NamedObjectWidgetInterface>();
    private HashMap<NamedObjectWidgetInterface, TabScenePanel> _widgetTabMap = new HashMap<NamedObjectWidgetInterface, TabScenePanel>();
    private HashSet<NamedObj> _remoteObjectSet = new HashSet<NamedObj>();
    private RemoteObjectList _pnlRemoteObjects;
}
