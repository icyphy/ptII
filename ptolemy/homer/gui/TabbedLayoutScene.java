/*
 The tabbed panel that contains the scenes onto which widgets can be dropped by the user
 in order to construct a layout for a particular model file.

 Copyright (c) 2011-2013 The Regents of the University of California.
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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicButtonUI;

import ptolemy.homer.events.TabEvent;

///////////////////////////////////////////////////////////////////
//// TabbedLayoutScene

/** The tabbed panel that contains the scenes onto which widgets can be dropped by the user
 *  in order to construct a layout for a particular model file.
 *
 *  @author Anar Huseynov
 *  @version $Id$
 *  @since Ptolemy II 8.1
 *  @Pt.ProposedRating Red (ahuseyno)
 *  @Pt.AcceptedRating Red (ahuseyno)
 */
public class TabbedLayoutScene extends JPanel implements ActionListener {

    /** The tab button that is added to the tab.
     */
    private static class TabButton extends JButton {
        // FindBugs indicates that this should be a static class.

        /** Create the button that will sit within the tab.
         */
        public TabButton() {
            setPreferredSize(new Dimension(17, 17));
            setToolTipText("Delete");

            // Set the look and feel.
            setUI(new BasicButtonUI());

            // Make it transparent.
            setContentAreaFilled(false);

            // Make it non-focusable.
            setFocusable(false);
            setBorder(BorderFactory.createEtchedBorder());
            setBorderPainted(false);

            // Add the nice rollover effect.
            addMouseListener(MOUSE_ADAPTER);
            setRolloverEnabled(true);
        }
    }

    /** The panel that contains the label and + button.
     */
    private class TabSceneButton extends JPanel {

        /** Create the panel that will sit within the tab.
         */
        public TabSceneButton() {
            setOpaque(false);
            setLayout(new BorderLayout(0, 0));

            _label = new JLabel() {
                @Override
                public String getText() {
                    int i = _tabScenes.indexOfTabComponent(TabSceneButton.this);
                    if (i != -1) {
                        return _tabScenes.getTitleAt(i);
                    }

                    return null;
                };

                @Override
                public void setText(String text) {
                    int i = _tabScenes.indexOfTabComponent(TabSceneButton.this);
                    if (i != -1) {
                        _mainFrame.setTabTitleAt(i, text);
                    }
                };
            };
            _label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);

                    // For simple click, just select the tab
                    if (e.getClickCount() == 1) {
                        int selectedIndex = -1;
                        if (_tabScenes.indexOfTabComponent(TabSceneButton.this) == _tabScenes
                                .getTabCount() - 1) {
                            selectedIndex = _tabScenes.getTabCount() - 2;
                        } else {
                            selectedIndex = _tabScenes
                                    .indexOfTabComponent(TabSceneButton.this);
                        }
                        _tabScenes.setSelectedIndex(selectedIndex);
                    } else {
                        // On double click let user rename the tab.
                        TabSceneButton.this.remove(_label);
                        TabSceneButton.this.add(_editableLabel,
                                BorderLayout.CENTER);
                        _editableLabel.setText(_label.getText());
                        _editableLabel.setFocusable(true);
                        _editableLabel.requestFocusInWindow();
                        _editableLabel.selectAll();
                        TabbedLayoutScene.this.repaint();
                    }
                }
            });

            _editableLabel = new JTextField();
            _editableLabel.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER
                            || e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        _setInEditedTitle();
                    }
                }
            });
            _editableLabel.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    _setInEditedTitle();
                }
            });
            _editableLabel.setColumns(6);

            add(_label, BorderLayout.CENTER);
            JButton closeButton = new TabButton();
            closeButton.setText("x");

            add(closeButton, BorderLayout.EAST);
            closeButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    _mainFrame.removeTab(_tabScenes
                            .indexOfTabComponent(TabSceneButton.this));

                    if (_mainFrame.getAllTabs().size() == 0) {
                        _mainFrame.addTab("Default");
                    }
                }
            });

            _tabScenes.setEnabledAt(_tabScenes.getTabCount() - 2, false);
        }

        /** Removes editable text box with a label from the tab.
         */
        private void _setInEditedTitle() {
            TabSceneButton.this.remove(_editableLabel);
            TabSceneButton.this.add(_label, BorderLayout.CENTER);
            _label.setText(_editableLabel.getText());
            TabbedLayoutScene.this.repaint();
        }

        /** The label displaying the tab name.
         */
        private JLabel _label;

        /** The text box used for changing the tab name.
         */
        private JTextField _editableLabel;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         constructor                       ////

    /** Create the default scene with initial tabs.
     *  @param mainFrame A reference to the parent frame.
     */
    public TabbedLayoutScene(HomerMainFrame mainFrame) {
        _mainFrame = mainFrame;
        _tabScenes = new JTabbedPane(SwingConstants.TOP);

        add(_tabScenes);

        // Create the "add tab" tab.
        _tabScenes.add("", null);
        TabButton addTabButton = new TabButton();
        addTabButton.setText("+");
        addTabButton.setToolTipText("Add tab");
        _tabScenes.setTabComponentAt(0, addTabButton);

        addTabButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _mainFrame.addTab("Tab " + _tabScenes.getTabCount());
                selectTab(_tabScenes.getTabCount() - 2);
            }
        });

        _tabScenes.setEnabledAt(_tabScenes.indexOfTabComponent(addTabButton),
                false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Process action performed event.
     *  @param event The event object.
     *  @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
        if (event instanceof TabEvent) {
            TabEvent tabEvent = (TabEvent) event;
            if (tabEvent.getActionCommand().equals("addTab")) {
                _addTab(tabEvent.getTag(), tabEvent.getName(),
                        (TabScenePanel) tabEvent.getContent());
            } else if (tabEvent.getActionCommand().equals("removeTab")) {
                _removeTab(tabEvent.getPosition());
            } else if (tabEvent.getActionCommand().equals("renameTab")) {
                _renameTab(tabEvent.getPosition(), tabEvent.getName());
            }
        }

        if (event.getActionCommand().equals("clear")) {
            _clear();
        }
    }

    /** Get the tabs within the container.
     *  @return The reference to the tabbed pane contained within.
     */
    public JTabbedPane getSceneTabs() {
        return _tabScenes;
    }

    /** Set the selected tab.
     *  @param index Index of the tab that should be selected.
     */
    public void selectTab(int index) {
        _tabScenes.setSelectedIndex(index);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Add a tab with the specified name.
     *  @param tag The tag of the content panel.
     *  @param name The name of the content panel.
     *  @param contentPanel The content panel.
     */
    private void _addTab(String tag, String name, TabScenePanel contentPanel) {
        contentPanel.setTag(tag);
        contentPanel.setName(name);

        Component view = _mainFrame.getTabContent(tag).getView();
        _tabScenes.insertTab(name, null, view, null,
                _tabScenes.getTabCount() - 1);
        view.setMaximumSize(view.getPreferredSize());

        int index = _tabScenes.indexOfComponent(view);
        _tabScenes.setTabComponentAt(index, new TabSceneButton());
        _tabScenes.setSelectedIndex(index);

        //FIXME: somehow never gets key events.  even tried adding to the main frame, but no luck there either.
        //        view.addKeyListener(new KeyAdapter() {
        //            public void keyReleased(KeyEvent e) {
        //                //// Method #1
        //                for (Map.Entry<NamedObj, NamedObjectWidgetInterface> namedObj : _mainFrame
        //                        .getWidgetMap().entrySet()) {
        //                    if (((Widget) namedObj.getValue()).getState().isSelected()) {
        //                        _mainFrame.removeNamedObject(namedObj.getKey());
        //                    }
        //                }
        //                //// Method #2
        //                //                for (TabScenePanel scenePanel : _viewSceneMap.values()) {
        //                //                    for (Object selected : scenePanel.getScene()
        //                //                            .getSelectedObjects()) {
        //                //                        removeNamedObject(((NamedObjectWidgetInterface) selected)
        //                //                                .getNamedObject());
        //                //                    }
        //                //                }
        //            }
        //        });

    }

    /** Remove all but the default tab.
     */
    private void _clear() {
        // The last one should be the "add new tab" tab.
        for (int i = _tabScenes.getTabCount() - 2; i >= 0; --i) {
            _removeTab(i);
        }
    }

    /** Remove the selected tab and its associated component.
     *  @param index The tab index to be removed.
     */
    private void _removeTab(int index) {
        _tabScenes.removeTabAt(index);
        if (_tabScenes.getSelectedIndex() == _tabScenes.getTabCount() - 1) {
            _tabScenes.setSelectedIndex(_tabScenes.getTabCount() - 2);
        }
    }

    /** Rename the tab at the specified position.
     *  @param position The tab position.
     *  @param name The new name.
     */
    private void _renameTab(int position, String name) {
        if (position < 0 || position >= _tabScenes.getTabCount()) {
            return;
        }
        _tabScenes.setTitleAt(position, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The JFrame container of the this panel.
     */
    private HomerMainFrame _mainFrame;

    /** The JTabbedPane that is being wrapped.
     */
    private JTabbedPane _tabScenes;

    /** The standard mouse adapter to be used on on all buttons.
     */
    private static final MouseAdapter MOUSE_ADAPTER = new MouseAdapter() {
        public void mouseEntered(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                ((AbstractButton) component).setBorderPainted(true);
            }
        }

        public void mouseExited(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                ((AbstractButton) component).setBorderPainted(false);
            }
        }
    };
}
