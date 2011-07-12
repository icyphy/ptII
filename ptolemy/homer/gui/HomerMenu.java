/* Menu for supporting the layout builder application.

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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.MalformedURLException;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import ptolemy.util.MessageHandler;

///////////////////////////////////////////////////////////////////
//// HomerMenu

/** Menu for supporting the layout builder application.
 * 
 *  @author Peter Foldes
 *  @version $Id$
 *  @since Ptolemy II 8.1
 *  @Pt.ProposedRating Red (pdf)
 *  @Pt.AcceptedRating Red (pdf)
 */
public class HomerMenu {

    ///////////////////////////////////////////////////////////////////
    ////                constructor                                ////

    /** Create the menu bar for the window.
     *  @param parent The parent frame in which the menu bar will be shown.
     */
    public HomerMenu(UIDesignerFrame parent) {
        _parent = parent;

        _initializeFileChooser();
    }

    ///////////////////////////////////////////////////////////////////
    ////                public methods                             ////

    /** Get the file chooser.
     *  @return The file chooser
     */
    public JFileChooser getFileChooser() {
        return _fileChooser;
    }

    /** Get the layout file filter.
     *  @return The filter for removing non-layout files from the dialog.
     */
    public FileFilter getLayoutFilter() {
        return _layoutFilter;
    }

    /** Configure the window menu bar.
     */
    public JMenuBar getMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // Add top menus
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(fileMenu);

        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic(KeyEvent.VK_E);
        menuBar.add(editMenu);

        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        menuBar.add(helpMenu);

        // File menu items
        JMenuItem newMenuItem = new JMenuItem("New", KeyEvent.VK_N);
        newMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _newMenuActionPerformed(e);
            }
        });

        JMenuItem openMenuItem = new JMenuItem("Open", KeyEvent.VK_O);
        openMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _openMenuActionPerformed(e);
            }
        });

        JMenuItem saveMenuItem = new JMenuItem("Save", KeyEvent.VK_S);
        saveMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _saveMenuActionPerformed(e);
            }
        });

        JMenuItem saveAsMenuItem = new JMenuItem("Save as", KeyEvent.VK_A);
        saveAsMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _saveAsMenuActionPerformed(e);
            }
        });

        JMenuItem exitMenuItem = new JMenuItem("Exit", KeyEvent.VK_X);
        exitMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _parent.dispose();
            }
        });

        // Edit menu actions.
        JMenuItem screenSizeItem = new JMenuItem("Screen Size", KeyEvent.VK_S);
        screenSizeItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TabbedLayoutScene scene = _parent.getTabbedLayoutScene();
                if (scene != null) {
                    SizeDialog dialog = new SizeDialog(scene.getSceneTabs()
                            .getHeight(), scene.getSceneTabs().getWidth());
                    if (dialog.showPrompt() == JOptionPane.OK_OPTION) {
                        try {
                            scene.getSceneTabs().setPreferredSize(
                                    dialog.getDimensions());
                            scene.revalidate();
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(_parent, new JLabel(
                                    ex.getClass().getName()),
                                    "Invalid Size Specified",
                                    JOptionPane.WARNING_MESSAGE);
                        }
                    }
                }
            }
        });

        JCheckBoxMenuItem portraitItem = new JCheckBoxMenuItem("Portrait",
                false);
        portraitItem.setMnemonic(KeyEvent.VK_P);
        portraitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TabbedLayoutScene scene = _parent.getTabbedLayoutScene();
                if (scene != null) {
                    double height = scene.getSceneTabs().getPreferredSize()
                            .getHeight();
                    double width = scene.getSceneTabs().getPreferredSize()
                            .getWidth();

                    if (height < width) {
                        scene.getSceneTabs().setPreferredSize(
                                new Dimension((int) height, (int) width));
                        scene.revalidate();
                    }
                }
            }
        });

        JCheckBoxMenuItem landscapeItem = new JCheckBoxMenuItem("Landscape",
                true);
        landscapeItem.setMnemonic(KeyEvent.VK_L);
        landscapeItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TabbedLayoutScene scene = _parent.getTabbedLayoutScene();
                if (scene != null) {
                    double height = scene.getSceneTabs().getPreferredSize()
                            .getHeight();
                    double width = scene.getSceneTabs().getPreferredSize()
                            .getWidth();

                    if (height > width) {
                        scene.getSceneTabs().setPreferredSize(
                                new Dimension((int) height, (int) width));
                        scene.revalidate();
                    }
                }
            }
        });

        ButtonGroup group = new ButtonGroup();
        group.add(portraitItem);
        group.add(landscapeItem);

        // Add all the items to the appropriate menu.
        fileMenu.add(newMenuItem);
        fileMenu.add(openMenuItem);
        fileMenu.add(saveMenuItem);
        fileMenu.add(saveAsMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);

        editMenu.add(screenSizeItem);
        editMenu.addSeparator();
        editMenu.add(portraitItem);
        editMenu.add(landscapeItem);

        return menuBar;
    }

    /** Get the model file filter.
     *  @return The filter for removing non-model files from the dialog.
     */
    public FileFilter getModelFilter() {
        return _modelFilter;
    }

    ///////////////////////////////////////////////////////////////////
    ////                private methods                            ////

    private void _newMenuActionPerformed(ActionEvent e) {
        _fileChooser.setDialogTitle("Choose a Ptolemy model");
        _fileChooser.setFileFilter(_modelFilter);

        int returnVal = _fileChooser.showOpenDialog(_parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = _fileChooser.getSelectedFile();

            try {
                _parent.newLayout(file.toURI().toURL());
            } catch (MalformedURLException e1) {
                MessageHandler.error("Unable to parse the file", e1);
            }
        }
    }

    private void _openMenuActionPerformed(ActionEvent e) {
        OpenLayoutDialog openLayoutDialog = new OpenLayoutDialog(_parent, this);
        Object result = openLayoutDialog.showDialog();

        if ((result == null) || (result == JOptionPane.UNINITIALIZED_VALUE)
                || ((Integer) result == JOptionPane.CANCEL_OPTION)) {
            return;
        }

        if ((Integer) result == JOptionPane.OK_OPTION) {

            // Check if files have been selected and they exist
            File model = openLayoutDialog.getModelFile();
            File layout = openLayoutDialog.getLayoutFile();

            if (model == null || layout == null) {
                JOptionPane.showMessageDialog(_parent,
                        "The model or layout file was not selected.",
                        "Unable to open layout.", JOptionPane.PLAIN_MESSAGE);
                return;
            }

            if (!model.exists() || !layout.exists()) {
                JOptionPane.showMessageDialog(_parent,
                        "The selected model or layout file does not exist.",
                        "Unable to open layout.", JOptionPane.PLAIN_MESSAGE);
                return;
            }
        }

        JOptionPane.showConfirmDialog(_parent, openLayoutDialog.getLayoutFile()
                .toURI().toString());
    }

    private void _saveMenuActionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
    }

    private void _saveAsMenuActionPerformed(ActionEvent e) {
        _fileChooser.setDialogTitle("Select where to save the layout");
        _fileChooser.setFileFilter(_layoutFilter);

        int returnVal = _fileChooser.showOpenDialog(_parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            _parent.saveLayoutAs(_fileChooser.getSelectedFile());
        }
    }

    private void _initializeFileChooser() {
        _fileChooser = new JFileChooser();

        _modelFilter = new FileFilter() {
            @Override
            public String getDescription() {
                return "Ptolemy model files";
            }

            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                if (f.getName().endsWith(".xml")
                        && !f.getName().endsWith(".layout.xml")) {
                    return true;
                }
                return false;
            }
        };

        _layoutFilter = new FileFilter() {
            @Override
            public String getDescription() {
                return "Ptolemy layout files";
            }

            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                if (f.getName().endsWith(".layout.xml")) {
                    return true;
                }
                return false;
            }
        };

        _fileChooser.addChoosableFileFilter(_modelFilter);
        _fileChooser.addChoosableFileFilter(_layoutFilter);
    }

    ///////////////////////////////////////////////////////////////////
    ////                private variables                          ////

    private UIDesignerFrame _parent;
    private JFileChooser _fileChooser;
    private FileFilter _modelFilter;
    private FileFilter _layoutFilter;
}
