/* Menu for supporting the layout builder application.

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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import ptolemy.homer.kernel.LayoutParser.ScreenOrientation;
import ptolemy.util.MessageHandler;

///////////////////////////////////////////////////////////////////
//// HomerMenu

/** Menu for supporting the layout builder application.
 *
 *  @author Peter Foldes
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (pdf)
 *  @Pt.AcceptedRating Red (pdf)
 */
public class HomerMenu {

    ///////////////////////////////////////////////////////////////////
    ////                constructor                                ////

    /** Create the menu bar for the window.
     *  @param parent The parent frame in which the menu bar will be shown.
     */
    public HomerMenu(HomerMainFrame parent) {
        _mainFrame = parent;
        _initializeFileChooser();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

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
     *  @return The menu bar of the window.
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
            @Override
            public void actionPerformed(ActionEvent e) {
                _newMenuActionPerformed(e);
            }
        });

        JMenuItem openMenuItem = new JMenuItem("Open", KeyEvent.VK_O);
        openMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _openMenuActionPerformed(e);
            }
        });

        JMenuItem saveMenuItem = new JMenuItem("Save", KeyEvent.VK_S);
        saveMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _saveMenuActionPerformed(e);
            }
        });

        JMenuItem saveAsMenuItem = new JMenuItem("Save as", KeyEvent.VK_A);
        saveAsMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _saveAsMenuActionPerformed(e);
            }
        });

        JMenuItem exitMenuItem = new JMenuItem("Exit", KeyEvent.VK_X);
        exitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _mainFrame.dispose();
            }
        });

        // Edit menu actions.
        _portraitItem = new JCheckBoxMenuItem("Portrait", false);
        _portraitItem.setMnemonic(KeyEvent.VK_P);
        _portraitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _mainFrame.setOrientation(ScreenOrientation.PORTRAIT);
            }
        });

        _landscapeItem = new JCheckBoxMenuItem("Landscape", true);
        _landscapeItem.setMnemonic(KeyEvent.VK_L);
        _landscapeItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _mainFrame.setOrientation(ScreenOrientation.LANDSCAPE);
            }
        });

        ButtonGroup group = new ButtonGroup();
        group.add(_portraitItem);
        group.add(_landscapeItem);

        // Add all the items to the appropriate menu.
        fileMenu.add(newMenuItem);
        fileMenu.add(openMenuItem);
        fileMenu.add(saveMenuItem);
        fileMenu.add(saveAsMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);

        editMenu.add(_initializeDeviceMenu());
        editMenu.addSeparator();
        editMenu.add(_portraitItem);
        editMenu.add(_landscapeItem);

        return menuBar;
    }

    /** Get the model file filter.
     *  @return The filter for removing non-model files from the dialog.
     */
    public FileFilter getModelFilter() {
        return _modelFilter;
    }

    /** Set the orientation selection in the menu.
     *  @param orientation The proposed screen orientation.
     */
    public void setOrientation(ScreenOrientation orientation) {
        if (orientation == ScreenOrientation.LANDSCAPE) {
            _landscapeItem.setSelected(true);
        } else if (orientation == ScreenOrientation.PORTRAIT) {
            _portraitItem.setSelected(true);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize file chooser to the default model directory.
     */
    private void _initializeFileChooser() {
        _fileChooser = new JFileChooser();
        _fileChooser.setCurrentDirectory(new File(ResourceBundle.getBundle(
                "ptserver.PtolemyServerConfig").getString("MODELS_DIRECTORY")));

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

    /** Create and initialize menu items for the device screen size selection.
     * @return The new menu.
     */
    private JMenu _initializeDeviceMenu() {
        JMenu screenMenu = new JMenu("Screen Size");
        screenMenu.setMnemonic(KeyEvent.VK_S);

        try {
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().parse(_DEVICE_FILE);
            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList manufacturers = (NodeList) xpath.compile("//manufacturer")
                    .evaluate(doc, XPathConstants.NODESET);

            for (int i = 0; i < manufacturers.getLength(); i++) {
                NodeList devices = (NodeList) xpath
                        .compile("devices//device")
                        .evaluate(manufacturers.item(i), XPathConstants.NODESET);

                JMenu mfgItem = new JMenu(manufacturers.item(i).getAttributes()
                        .getNamedItem("name").getNodeValue());
                for (int x = 0; x < devices.getLength(); x++) {
                    final NamedNodeMap attributes = devices.item(x)
                            .getAttributes();
                    final int width = Integer.parseInt(attributes.getNamedItem(
                            "width").getNodeValue());
                    final int height = Integer.parseInt(attributes
                            .getNamedItem("height").getNodeValue());

                    JMenuItem deviceItem = new JMenuItem(attributes
                            .getNamedItem("name").getNodeValue()
                            + " ("
                            + width
                            + "x" + height + ")");
                    deviceItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            _mainFrame.setScreenSize(new Dimension(width,
                                    height));
                        }
                    });

                    mfgItem.add(deviceItem);
                }

                screenMenu.add(mfgItem);
            }
        } catch (Throwable ex) {
            System.out.println(ex.getMessage());
        }

        JMenuItem customSizeItem = new JMenuItem("Custom Size");
        customSizeItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TabbedLayoutScene scene = _mainFrame.getTabbedLayoutScene();
                if (scene != null) {
                    SizeDialog dialog = new SizeDialog(scene.getSceneTabs()
                            .getHeight(), scene.getSceneTabs().getWidth());
                    if (dialog.showPrompt() == JOptionPane.OK_OPTION) {
                        scene.getSceneTabs().setPreferredSize(
                                dialog.getDimensions());
                        scene.revalidate();
                    }
                }
            }
        });

        screenMenu.addSeparator();
        screenMenu.add(customSizeItem);

        return screenMenu;
    }

    /** Process action on the new menu item.
     *  @param e the action event
     */
    private void _newMenuActionPerformed(ActionEvent e) {
        _fileChooser.setDialogTitle("Choose a Ptolemy model");
        _fileChooser.setFileFilter(_modelFilter);

        int returnVal = _fileChooser.showOpenDialog(_mainFrame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = _fileChooser.getSelectedFile();

            try {
                _mainFrame.newLayout(file.toURI().toURL());
            } catch (MalformedURLException e1) {
                MessageHandler.error("Unable to parse the file", e1);
            }
        }
    }

    /** Process action on the open menu item.
     *  @param e the action event.
     */
    private void _openMenuActionPerformed(ActionEvent e) {
        OpenLayoutDialog openLayoutDialog = new OpenLayoutDialog(_mainFrame,
                this);
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
                JOptionPane.showMessageDialog(_mainFrame,
                        "The model or layout file was not selected.",
                        "Unable to open layout.", JOptionPane.PLAIN_MESSAGE);
                return;
            }

            if (!model.exists() || !layout.exists()) {
                JOptionPane.showMessageDialog(_mainFrame,
                        "The selected model or layout file does not exist.",
                        "Unable to open layout.", JOptionPane.PLAIN_MESSAGE);
                return;
            }

            try {
                _mainFrame.openLayout(model.toURI().toURL(), layout.toURI()
                        .toURL());
            } catch (MalformedURLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }

    }

    /** Process action on the save menu.
     *  @param e The action event.
     */
    private void _saveMenuActionPerformed(ActionEvent e) {
        URL layoutURL = _mainFrame.getLayoutURL();
        if (layoutURL != null) {
            _mainFrame.saveLayoutAs(new File(layoutURL.getPath()));
        } else {
            // No file was selected before, let's invoke the saveAs used in
            // the menu.
            _saveAsMenuActionPerformed(e);
        }
    }

    /** Process action on the save as menu item.
     *  @param e The action event.
     */
    private void _saveAsMenuActionPerformed(ActionEvent e) {
        _fileChooser.setDialogTitle("Select where to save the layout");
        _fileChooser.setFileFilter(_layoutFilter);

        int returnVal = _fileChooser.showSaveDialog(_mainFrame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            _mainFrame.saveLayoutAs(_fileChooser.getSelectedFile());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The file path containing device screen size information.
     */
    private static String _DEVICE_FILE = "ptolemy//homer//gui//devices.xml";

    /** The file chooser used to open/save files.
     */
    private JFileChooser _fileChooser;

    /** Landscape menu item.
     */
    private JCheckBoxMenuItem _landscapeItem;

    /** The file filter for layout files.
     */
    private FileFilter _layoutFilter;

    /** The main frame of the application.
     */
    private HomerMainFrame _mainFrame;

    /** The file filter for model files.
     */
    private FileFilter _modelFilter;

    /** Portrait menu item.
     */
    private JCheckBoxMenuItem _portraitItem;
}
