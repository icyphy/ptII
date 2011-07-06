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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import ptolemy.homer.gui.tree.NamedObjectTree;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptserver.util.PtolemyModuleJavaSEInitializer;

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

    static {
        PtolemyModuleJavaSEInitializer.initializeInjector();
    }

    /**
     * Create the frame.
     */
    public UIDesignerFrame() {
        setTitle("UI Designer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 800, 600);

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu mnFile = new JMenu("File");
        menuBar.add(mnFile);

        JMenuItem mntmNew = new JMenuItem("New");
        mntmNew.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                newMenuActionPerformed(e);
            }
        });
        mnFile.add(mntmNew);
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
        pnlEast.setPreferredSize(new Dimension(150, 10));
        _contentPane.add(pnlEast, BorderLayout.EAST);
        pnlEast.setLayout(new BorderLayout(0, 0));

        JPanel pnlModelImage = new JPanel();
        pnlModelImage.setBorder(new TitledBorder(null, "Graph Preview",
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        pnlModelImage.setPreferredSize(new Dimension(10, 150));
        pnlEast.add(pnlModelImage, BorderLayout.NORTH);

        JPanel pnlRemoteObjects = new JPanel();
        pnlRemoteObjects.setBorder(new TitledBorder(null,
                "Remote Named Objects", TitledBorder.LEADING, TitledBorder.TOP,
                null, null));
        pnlEast.add(pnlRemoteObjects, BorderLayout.CENTER);

        _spnScreen = new JScrollPane();
        _contentPane.add(_spnScreen, BorderLayout.CENTER);

        _pnlScreen = new TabbedLayoutScene();
        _pnlScreen.addTab("Default");
        _pnlScreen.selectTab(0);
        _spnScreen.setViewportView(_pnlScreen);
        _fileChooser = new JFileChooser();
        _fileChooser.addChoosableFileFilter(new FileFilter() {

            @Override
            public String getDescription() {
                return "Graph Model files";
            }

            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                if (f.getName().endsWith(".xml")) {
                    return true;
                }
                return false;
            }
        });

    }

    private void newMenuActionPerformed(ActionEvent e) {
        int returnVal = _fileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = _fileChooser.getSelectedFile();
            MoMLParser parser = new MoMLParser(new Workspace());
            MoMLParser.setMoMLFilters(BackwardCompatibility.allFilters());
            try {
                CompositeEntity topLevel = (CompositeEntity) parser.parse(null,
                        file.toURI().toURL());
                _pnlNamedObjectTree.setCompositeEntity(topLevel);
            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }

    private JPanel _contentPane;
    private JScrollPane _spnScreen;
    private NamedObjectTree _pnlNamedObjectTree;
    private TabbedLayoutScene _pnlScreen;
    private final JFileChooser _fileChooser;
}
