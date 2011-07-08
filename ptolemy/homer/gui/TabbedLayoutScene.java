/*
 TODO
 
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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicButtonUI;

///////////////////////////////////////////////////////////////////
//// TabbedLayoutScene
/**
 * TODO
 * @author Anar Huseynov
 * @version $Id$ 
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public class TabbedLayoutScene extends JPanel {
    /**
     * TODO
     */
    public TabbedLayoutScene() {
        _tabScenes = new JTabbedPane(JTabbedPane.TOP);
        add(_tabScenes);
        _tabScenes.add("", null);
        TabButton addTabButton = new TabButton();
        addTabButton.setText("+");
        _tabScenes.setTabComponentAt(0, addTabButton);
        addTabButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                addTab("Tab " + _tabScenes.getTabCount());
                selectTab(_tabScenes.getTabCount() - 2);
            }
        });
        _tabScenes.setBorder(new LineBorder(Color.BLACK));
    }

    public JTabbedPane getSceneTabs() {
        return _tabScenes;
    }

    /**
     * TODO
     *
     */
    private class TabSceneButton extends JPanel {

        public TabSceneButton() {
            setOpaque(false);
            setLayout(new BorderLayout(0, 0));
            JLabel label = new JLabel() {
                public String getText() {
                    int i = _tabScenes.indexOfTabComponent(TabSceneButton.this);
                    if (i != -1) {
                        return _tabScenes.getTitleAt(i);
                    }
                    return null;
                };
            };
            add(label, BorderLayout.CENTER);
            JButton closeButton = new TabButton();
            closeButton.setText("x");
            add(closeButton, BorderLayout.EAST);
            closeButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    int i = _tabScenes.indexOfTabComponent(TabSceneButton.this);
                    if (i != -1) {
                        _tabScenes.remove(i);
                        if (_tabScenes.getTabCount() == 1) {
                            addTab("Default");
                        }
                        if (_tabScenes.getSelectedIndex() == _tabScenes
                                .getTabCount() - 1) {
                            _tabScenes.setSelectedIndex(_tabScenes
                                    .getTabCount() - 2);
                        }
                    }
                }
            });
            _tabScenes.setEnabledAt(_tabScenes.getTabCount() - 1, false);
        }
    }

    /**
     * TODO
     *
     */
    private class TabButton extends JButton {
        public TabButton() {
            int size = 17;
            setPreferredSize(new Dimension(size, size));
            setToolTipText("Delete");
            //Make the button looks the same for all Laf's
            setUI(new BasicButtonUI());
            //Make it transparent
            setContentAreaFilled(false);
            //No need to be focusable
            setFocusable(false);
            setBorder(BorderFactory.createEtchedBorder());
            setBorderPainted(false);
            //Making nice rollover effect
            //we use the same listener for all buttons
            addMouseListener(_MOUSE_ADAPTER);
            setRolloverEnabled(true);
        }

    }

    public void addTab(String tabName) {
        TabScenePanel tabScenePanel = new TabScenePanel(_mainFrame);
        Component view = tabScenePanel.getView();
        _tabScenes.insertTab(tabName, null, view, null,
                _tabScenes.getTabCount() - 1);
        view.setMaximumSize(view.getPreferredSize());
        int index = _tabScenes.indexOfComponent(tabScenePanel.getView());
        _tabScenes.setTabComponentAt(index, new TabSceneButton());
        _tabScenes.setSelectedIndex(index);
    }

    public void selectTab(int index) {
        _tabScenes.setSelectedIndex(index);
    }

    /**
     * @param _mainFrame the _mainFrame to set
     */
    public void setMainFrame(UIDesignerFrame mainFrame) {
        _mainFrame = mainFrame;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    /**
     * TODO
     */
    private JTabbedPane _tabScenes;
    private UIDesignerFrame _mainFrame;
    private static final MouseAdapter _MOUSE_ADAPTER = new MouseAdapter() {
        public void mouseEntered(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(true);
            }
        }

        public void mouseExited(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(false);
            }
        }
    };

}
