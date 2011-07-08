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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import ptolemy.homer.kernel.LayoutFileOperations;
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

    public HomerMenu(UIDesignerFrame parent) {
        _parent = parent;

        _initializeFileChooser();
    }

    ///////////////////////////////////////////////////////////////////
    ////                public methods                             ////

    public JMenuBar getMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // Add top menus
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        JMenu helpMenu = new JMenu("Help");
        menuBar.add(helpMenu);

        // File menu items
        JMenuItem newMenuItem = new JMenuItem("New");
        newMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _newMenuActionPerformed(e);
            }
        });
        fileMenu.add(newMenuItem);

        JMenuItem openMenuItem = new JMenuItem("Open");
        openMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _openMenuActionPerformed(e);
            }
        });
        fileMenu.add(openMenuItem);

        JMenuItem saveMenuItem = new JMenuItem("Save");
        saveMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _saveMenuActionPerformed(e);
            }
        });
        fileMenu.add(saveMenuItem);

        JMenuItem saveAsMenuItem = new JMenuItem("Save as");
        saveAsMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _saveAsMenuActionPerformed(e);
            }
        });
        fileMenu.add(saveAsMenuItem);

        fileMenu.addSeparator();

        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _parent.dispose();
            }
        });
        fileMenu.add(exitMenuItem);

        return menuBar;
    }

    public JFileChooser getFileChooser() {
        return _fileChooser;
    }

    public FileFilter getModelFilter() {
        return _modelFilter;
    }

    public FileFilter getLayoutFilter() {
        return _layoutFilter;
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

        if (result == null || result == JOptionPane.UNINITIALIZED_VALUE
                || (Integer) result == JOptionPane.CANCEL_OPTION) {
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
        LayoutFileOperations.save(_parent);
    }

    private void _saveAsMenuActionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub

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
