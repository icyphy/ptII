/*
 * Created on 01 sept. 2003
 *
 * @ProposedRating Yellow (jerome.blanc@thalesgroup.com)
 * @AcceptedRating
 */
package thales.actor.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.HTMLViewer;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;

import thales.vergil.SingleWindowApplication;
import thales.vergil.navigable.NavigableActorGraphFrame;
import thales.vergil.navigable.NavigationPTree;

/**
 * <p>Titre : SingleWindowHTMLViewer</p>
 * <p>Description : Main application Frame. Contains all the 
 * panels, menus and needed widget for the whole Design Environment</p>
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
 * <p>Société : Thales Research and technology</p>
 * @author Jérôme Blanc
 * 01 sept. 2003
 */
public class SingleWindowHTMLViewer
        extends HTMLViewer
        implements ChangeListener {

        /**
         * Main panel
         */
        protected JPanel startPanel = new JPanel();

        protected JTabbedPane _viewsTabbedPane = new JTabbedPane();

        private JMenuBar _originalMenuBar = null;

        private Configuration _configuration = null;

        public SingleWindowHTMLViewer() {
                super();

                //Keep a static reference into the SingleWindowApplication
                SingleWindowApplication._mainFrame = this;

                //Fetsh the scroller and keep reference here
                JScrollPane _scroller = null;
                Component[] liste = getContentPane().getComponents();
                for (int i = 0; i < liste.length; i++) {
                        if (liste[i] instanceof JScrollPane) {
                                _scroller = (JScrollPane) liste[i];
                        }
                }
                getContentPane().removeAll();

                //set the UI for _viewsTabbedPane
                _viewsTabbedPane.setUI(new ShortTitleTabbedPaneUI());

                getContentPane().add(_viewsTabbedPane);

                _viewsTabbedPane.addChangeListener(this);
                _viewsTabbedPane.addMouseListener(new MouseAdapter() {
                        /* (non-Javadoc)
                         * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
                         */
                        public void mouseClicked(MouseEvent e) {
                                if (e.getModifiers() == InputEvent.BUTTON3_MASK) {
                                        JTabbedPane tabbedPane = (JTabbedPane) e.getSource();
                                        int index = tabbedPane.getSelectedIndex();
                                        if (index > 0) {
                                                final Component theClickedOne =
                                                        tabbedPane.getComponentAt(index);
                                                JPopupMenu popUpMenu = new JPopupMenu();
                                                JMenuItem close = new JMenuItem("Close");
                                                close.addActionListener(new ActionListener() {
                                                        /* (non-Javadoc)
                                                         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
                                                         */
                                                        public void actionPerformed(ActionEvent e) {
                                                                closeTabbedPane(theClickedOne);
                                                        }
                                                });
                                                popUpMenu.add(close);
                                                popUpMenu.show(
                                                        (Component) e.getSource(),
                                                        e.getX(),
                                                        e.getY());
                                        }
                                }
                        }

                });

                buildStartPanel(_scroller);
        }

        /**
         * The first panel, handle the welcome window.
         * N.B: it has a null name, which is its signature
         * 
         * @param _scroller 
         */
        protected void buildStartPanel(JScrollPane _scroller) {
                startPanel.setLayout(new BorderLayout());

                if (_scroller != null) {
                        startPanel.add(_scroller, BorderLayout.CENTER);
                }

                _viewsTabbedPane.add("Start", startPanel);
        }

        /**
         * Creates a new Tab to the TabbedPane. Add a "windows closed" listener to
         * automaticly remove Tabs;
         * 
         * @param frame
         */
        public void newTabbedPanel(Tableau tableau) {
                removeEmptyTabs();
                //Create the TabbedPanel 
                JFrame frame = tableau.getFrame();

                String tableauName = tableau.getFullName();

                frame.setName(tableauName);
                frame.addWindowListener(new WindowAdapter() {
                        /* (non-Javadoc)
                         * @see java.awt.event.WindowAdapter#windowClosed(java.awt.event.WindowEvent)
                         */
                        public void windowClosed(WindowEvent e) {
                                removeEmptyTabs();
                        }

                        /* (non-Javadoc)
                         * @see java.awt.event.WindowAdapter#windowActivated(java.awt.event.WindowEvent)
                         */
                        public void windowActivated(WindowEvent e) {
                                JFrame frame = (JFrame) e.getSource();
                                frame.hide();
                                try {
                                        selectTab(frame.getName());
                                } catch (IndexOutOfBoundsException ex) {
                                }
                        }

                });

                Container container = frame.getContentPane();
                container.setSize(_viewsTabbedPane.getSize());

                Component aComp = _viewsTabbedPane.add(frame.getTitle(), container);
                aComp.setName(tableauName);
        }

        public void selectTab(String name) {
                int idx = findComponentIndex(name);
                if (idx < _viewsTabbedPane.getTabCount()) {
                        _viewsTabbedPane.setSelectedIndex(idx);
                }
        }

        /**
         * The goal here is to find any null tableau and remove them.
         * i.e: SaveAs left an orphan tableau
         *
         */
        protected void removeEmptyTabs() {
                for (int i = 0; i < _viewsTabbedPane.getComponentCount(); ++i) {
                        String name = _viewsTabbedPane.getComponentAt(i).getName();
                        if (name != null) { //Not using 
                                Tableau tableau = findComponentTableau(name);
                                if (tableau == null) {
                                        removeTab(name);
                                        --i;
                                }
                        }
                }
        }

        /**
         * Replaces corresponding Menu and Toolbar from the frame.
         * And hides the report bar
         * 
         * @param frame
         */
        public void fillMainFrame(JMenuBar menuBar) {
                if (_originalMenuBar == null) {
                        _originalMenuBar = getJMenuBar();
                }
                setJMenuBar(menuBar);
        }

        /* (non-Javadoc)
         * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
         */
        /**
         * Changes menus according to the selected Tab.
         */
        public void stateChanged(ChangeEvent e) {
                removeEmptyTabs();
                Object source = e.getSource();
                if (source instanceof JTabbedPane) {
                        JTabbedPane tabbedPane = ((JTabbedPane) source);
                        Component aComp = tabbedPane.getSelectedComponent();
                        if (aComp != null) {
                                String name = aComp.getName();
                                if (name != null && !name.equals("Start")) {
                                        Tableau tableau = findComponentTableau(name);
                                        if (tableau != null) {
                                                fillMainFrame(
                                                        ((Tableau) tableau).getFrame().getJMenuBar());
                                                _statusBar.setVisible(false);
                                        }
                                } else {
                                        //on est dans le cas du start tabbedPane
                                        fillMainFrame(_originalMenuBar);
                                        _statusBar.setVisible(true);
                                }
                        }
                }
        }

        /**
         * Finds the corresponding Tableau according to the
         * Component contained by a Tab.
         * @param aComp
         * @return
         */
        protected Tableau findComponentTableau(String tableauFullName) {
                Tableau answer = null;
                if (tableauFullName != null) {
                        tableauFullName =
                                tableauFullName.substring(15, tableauFullName.length());
                        Entity tableau = _configuration.getEntity(tableauFullName);
                        if (tableau instanceof Tableau) {
                                answer = (Tableau) tableau;
                        }
                }
                return answer;
        }

        /**
         * Closes the corresponding Tableau & Frame when close event handled
         * @param aComp
         */
        public boolean closeTabbedPane(Component aComp) {
                boolean answer = true;

                String name = aComp.getName();
                Tableau tableau = findComponentTableau(name);
                if (tableau != null) {
                        answer = tableau.close();
                        if (answer) {
                                tableau.getFrame().dispose();
                                removeTab(name);
                        }
                }

                return answer;
        }

        protected String getSelectedCompName() {
                return _viewsTabbedPane.getSelectedComponent().getName();
        }

        protected int findComponentIndex(String tableauFullName) {
                int answer = -1;

                if (tableauFullName != null) {
                        int nbTabs = _viewsTabbedPane.getComponentCount();
                        boolean found = false;
                        for (int i = 0; i < nbTabs && !found; ++i) {
                                Component aComp = _viewsTabbedPane.getComponent(i);
                                String compName = aComp.getName();
                                if (compName != null && compName.equals(tableauFullName)) {
                                        found = true;
                                        answer = i;
                                }
                        }
                }

                return answer;
        }

        /**
         * Removes the correspondig Tab according to the Component name
         * @param tableauFullName
         */
        public void removeTab(String tableauFullName) {
                int idx = findComponentIndex(tableauFullName);
                Tableau tab = findComponentTableau(tableauFullName);
                if (tab != null) {
                        Frame frame = tab.getFrame();
                        if (frame instanceof NavigableActorGraphFrame) {
                                NavigableActorGraphFrame navFrame =
                                        (NavigableActorGraphFrame) frame;
                                NavigationPTree aTree = navFrame.getTree();

                                Nameable effigy = tab.getContainer();
                                if (effigy instanceof NavigableEffigy) {
                                        NavigableEffigy navEff = (NavigableEffigy) effigy;
                                        navEff.getNavigationModel().unRegister(aTree);
                                }
                        }
                }
                if (idx < _viewsTabbedPane.getTabCount()) {
                        try {
                                _viewsTabbedPane.remove(idx);
                        } catch (IndexOutOfBoundsException e) {
                        }
                }
        }

        /* (non-Javadoc)
         * @see ptolemy.gui.Top#_close()
         */
        /**
         * Closes all the Tableau displayed into the TabbedPane.
         */
        protected boolean _close() {
                boolean close = true;
                int nbTabs = _viewsTabbedPane.getComponentCount();
                for (int i = 0; i < nbTabs - 1; ++i) {
                        if (close) {
                                close = closeTabbedPane(_viewsTabbedPane.getComponent(1));
                        }
                }
                if (close) {
                        close = super._close();
                }
                return close;
        }

        /**
         * @return
         */
        public Configuration getConfiguration() {
                return _configuration;
        }

        /**
         * @param configuration
         */
        public void setConfiguration(Configuration configuration) {
                _configuration = configuration;
        }

        /**
         * Closes the Tableau, removes its Effigy and re-opens it
         * @param tableau
         */
        public boolean reOpenGraph(Tableau tableau) {
                boolean answer = false;
                if (tableau != null) {
                        Nameable effigy = tableau.getContainer();
                        if (effigy instanceof PtolemyEffigy) {
                                NamedObj toReOpen = ((PtolemyEffigy) effigy).getModel();
                                tableau.close();
                                tableau.getFrame().dispose();
                                removeTab(tableau.getFullName());
                                try {
                                        ((PtolemyEffigy) effigy).setContainer(null);
                                        removeEmptyTabs();
                                        getConfiguration().openModel(toReOpen);
                                        answer = true;
                                } catch (IllegalActionException e) {
                                        e.printStackTrace();
                                } catch (NameDuplicationException e) {
                                        e.printStackTrace();
                                }
                        }
                }
                return answer;
        }

}
