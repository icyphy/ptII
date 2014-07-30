/*
 * Copyright (c) 2004-2007 by Michael Connor. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  o Neither the name of FormLayoutBuilder or Michael Connor nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.mlc.swing.layout;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;

import com.jgoodies.forms.factories.Borders;

/**
 * This is the frame that enables you to build a layout. The principle component
 * is the FormEditor panel.
 *
 * @author Michael Connor mlconnor&#064;yahoo.com
@version $Id$
@since Ptolemy II 8.0
 */
@SuppressWarnings("serial")
public class LayoutFrame extends JFrame implements MultiContainerFrame {
    LayoutConstraintsManager constraintsManager;

    JMenuBar menuBar = new JMenuBar();

    JMenu actionMenu = new JMenu("File");
    JMenuItem saveXML = new JMenuItem("Save As");
    JMenuItem viewCode = new JMenuItem("View Code");
    JMenuItem exit = new JMenuItem("Exit");

    JMenu viewMenu = new JMenu("View");
    JCheckBoxMenuItem viewDebugMenu = new JCheckBoxMenuItem("Debug Frame");

    final JFileChooser fileChooser = new JFileChooser();

    Map<ContainerLayout, FormEditor> editors = new HashMap<ContainerLayout, FormEditor>();

    JTabbedPane tabs = new JTabbedPane();

    Map<ContainerLayout, Component> layoutToTab = new HashMap<ContainerLayout, Component>();

    List<ContainerLayout> newLayouts = new ArrayList<ContainerLayout>();

    // Palette palette = new Palette();
    public JFrame dframe = null;

    /** Creates a new instance of Class */
    public LayoutFrame(LayoutConstraintsManager constraintsManager) {
        super("FormLayoutMaker - Constraints Editor");

        if (constraintsManager.getLayouts().size() == 0) {
            throw new RuntimeException(
                    "You must register at least one container by calling LayoutConstraintsManager.setLayout(String name, Container container) before instantiating a LayoutFrame");
        }

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.constraintsManager = constraintsManager;

        actionMenu.setMnemonic('F');
        saveXML.setMnemonic('A');
        saveXML.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
                InputEvent.CTRL_MASK));
        viewCode.setMnemonic('V');
        viewCode.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
                InputEvent.CTRL_MASK));
        exit.setMnemonic('X');
        actionMenu.add(saveXML);
        actionMenu.add(viewCode);
        actionMenu.add(exit);

        viewDebugMenu.setMnemonic('D');
        viewDebugMenu.setSelected(UserPrefs.getPrefs().showDebugPanel());
        viewMenu.add(viewDebugMenu);
        // KBR 03/26/06 Disable by default for invocation from user's
        // program. Enabled when the debug preview window is established.
        viewDebugMenu.setEnabled(false);

        menuBar.add(actionMenu);
        menuBar.add(viewMenu);
        this.setJMenuBar(menuBar);

        fileChooser.setFileFilter(new XmlFileFilter());

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });

        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exitApplication();
            }
        });

        viewDebugMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LayoutFrame.this.enableDebugPreview(viewDebugMenu.isSelected());
            }
        });

        List<ContainerLayout> layouts = constraintsManager.getLayouts();

        for (int index = 0; index < layouts.size(); index++) {
            ContainerLayout containerLayout = layouts.get(index);
            Container container = constraintsManager
                    .getContainer(containerLayout);
            if (container == null) {
                throw new RuntimeException(
                        "A container with name "
                                + containerLayout.getName()
                                + " was found in the contstraints file but was not found in the container");
            }
            addContainerLayout(containerLayout, container);
        }

        getContentPane().setLayout(new BorderLayout(3, 3));
        getContentPane().add(tabs, BorderLayout.CENTER);
        // getContentPane().add (palette, BorderLayout.SOUTH);

        viewCode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<ContainerLayout> layouts = LayoutFrame.this.constraintsManager
                        .getLayouts();
                StringBuffer declarationBuffer = new StringBuffer(
                        "// here are declarations for the controls you created\n");
                StringBuffer setLayoutBuffer = new StringBuffer(
                        "// here is where we load the layout constraints.  "
                                + "change the xml filename!!!\norg.mlc.swing.layout.LayoutConstraintsManager layoutConstraintsManager "
                                + "= \n    org.mlc.swing.layout.LayoutConstraintsManager.getLayoutConstraintsManager(\n        "
                                + "this.getClass().getResourceAsStream(\"yourConstraintFile.xml\"));\n");
                /** @todo KBR generate compilable code */
                // LayoutFrame layoutFrame = new LayoutFrame(layoutConstraintsManager);
                // layoutFrame.setVisible(true);
                StringBuffer addBuffer = new StringBuffer(
                        "// here we add the controls to the container.  you may\n// "
                                + "need to change the name of panel\n");

                StringBuffer importBuffer = new StringBuffer();
                importBuffer.append("import org.mlc.swing.layout.*;\n");
                HashSet<String> importSet = new HashSet<String>();

                StringBuffer declBuffer = new StringBuffer();
                declBuffer
                .append("// here are declarations for the controls you created\n");

                StringBuffer addBuffer2 = new StringBuffer();
                addBuffer2
                .append("// here we add the controls to the container.\n");

                StringBuffer confBuffer = new StringBuffer();
                confBuffer.append("// control configuration\n");

                for (int index = 0; index < layouts.size(); index++) {
                    ContainerLayout containerLayout = layouts.get(index);
                    FormEditor editor = editors.get(containerLayout);
                    Map<Component, String> componentsToNames = containerLayout
                            .getComponentsToNames();

                    for (Object element : componentsToNames.keySet()) {
                        Component component = (Component) element;
                        String componentName = componentsToNames.get(component);
                        if (editor.isNewComponent(component)) {
                            String _decl = "";
                            String _import = "";
                            String _add = "";
                            String _config = "";

                            ComponentDef cDef = containerLayout
                                    .getComponentDef(componentName);
                            if (cDef == null) {
                                // "old style"
                                String constructorArg = "";
                                if (LayoutConstraintsManager
                                        .isTextComponent(component)) {
                                    Map<String, Object> customProperties = containerLayout
                                            .getCustomProperties(componentName);
                                    Object textValue = customProperties
                                            .get("text");
                                    if (textValue != null
                                            && textValue instanceof String) {
                                        constructorArg = "\""
                                                + (String) textValue + "\"";
                                    }
                                }
                                _decl = component.getClass().getName()
                                        + " "
                                        + containerLayout
                                        .getComponentName(component)
                                        + " = new "
                                        + component.getClass().getName() + "("
                                        + constructorArg + ");\n";
                                _add = containerLayout.getName() + ".add ("
                                        + componentName + ", \""
                                        + componentName + "\");\n";
                            } else {
                                // "new style"
                                _import = cDef.getImports(componentName);
                                _decl = cDef.getDeclarations(componentName);
                                _add = cDef.getAdd(componentName);
                                _add = _add.replaceAll("\\$\\{container\\}",
                                        containerLayout.getName());
                                _config = cDef.getConfigure(componentName);
                            }

                            // put imports into a set to prevent multiple instances
                            // KBR 09/05/05 Need to put each line of the import into
                            // the set [using JButton and ButtonBar was generating
                            // two JButton import statements]
                            String[] outstrs = _import.split("\n");
                            for (String outstr : outstrs) {
                                importSet.add(outstr);
                            }

                            declBuffer.append(_decl + "\n");
                            addBuffer2.append(_add + "\n");
                            if (_config.trim().length() != 0) {
                                confBuffer.append(_config + "\n");
                            }

                            String constructorArg = "";
                            if (LayoutConstraintsManager
                                    .isTextComponent(component)) {
                                Map<String, Object> customProperties = containerLayout
                                        .getCustomProperties(componentName);
                                Object textValue = customProperties.get("text");
                                if (textValue != null
                                        && textValue instanceof String) {
                                    constructorArg = "\"" + (String) textValue
                                            + "\"";
                                }
                            }

                            String newDeclaration = component.getClass()
                                    .getName()
                                    + " "
                                    + containerLayout
                                    .getComponentName(component)
                                    + " = new "
                                    + component.getClass().getName()
                                    + "("
                                    + constructorArg + ");\n";
                            declarationBuffer.append(newDeclaration);
                            addBuffer.append(containerLayout.getName()
                                    + ".add (" + componentName + ", \""
                                    + componentName + "\");\n");
                        }
                    }

                    if (newLayouts.contains(containerLayout)) {
                        setLayoutBuffer.append(containerLayout.getName()
                                + ".setBorder(com.jgoodies.forms.factories.Borders.DIALOG_BORDER);\n");
                        setLayoutBuffer.append(containerLayout.getName()
                                + ".setLayout(layoutConstraintsManager.createLayout (\""
                                + containerLayout.getName() + "\", "
                                + containerLayout.getName() + ");\n");
                    }
                }

                // build up the imports string using all unique imports
                Iterator<String> itor = importSet.iterator();
                while (itor.hasNext()) {
                    importBuffer.append(itor.next() + "\n");
                }

                // String finalText = declarationBuffer.toString() + "\n" +
                // setLayoutBuffer.toString() + "\n" + addBuffer.toString();
                String finalText = importBuffer.toString() + "\n"
                        + declBuffer.toString() + "\n"
                        + setLayoutBuffer.toString() + "\n"
                        + addBuffer2.toString() + "\n" + confBuffer.toString()
                        + "\n";
                CodeDialog codeDialog = new CodeDialog(LayoutFrame.this,
                        finalText);
                codeDialog.setVisible(true);
            }
        });

        saveXML.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                java.util.prefs.Preferences prefs = java.util.prefs.Preferences
                        .userNodeForPackage(getClass());
                String pathString = prefs.get("lastpath", null);
                if (pathString != null) {
                    File path = new File(pathString);
                    if (path.exists()) {
                        fileChooser.setCurrentDirectory(path);
                    }
                }

                int returnVal = fileChooser.showSaveDialog(LayoutFrame.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();

                    // KBR fix logged bug. If the user does not specify an XML extension,
                    // add one, UNLESS they specify the trailing period.
                    String filename = file.getAbsolutePath();
                    if (!filename.endsWith(".xml")
                            && !filename.endsWith(".XML")
                            && !filename.endsWith(".")) {
                        file = new File(file.getAbsolutePath() + ".XML");
                    }

                    if (file.exists()) {
                        File path = file.getParentFile();
                        if (path != null) {
                            pathString = path.getAbsolutePath();
                            prefs.put("lastpath", pathString);
                        }

                        int result = JOptionPane
                                .showConfirmDialog(
                                        LayoutFrame.this,
                                        "The file you selected exists, ok to overwrite?",
                                        "File Exists",
                                        JOptionPane.YES_NO_OPTION);
                        if (result != JOptionPane.YES_OPTION) {
                            return;
                        }
                    }

                    FileOutputStream outStream = null;
                    try {
                        outStream = new FileOutputStream(file);
                        String xml = LayoutFrame.this.constraintsManager
                                .getXML();
                        outStream.write(xml.getBytes());
                    } catch (Exception exception) {
                        JOptionPane.showMessageDialog(
                                LayoutFrame.this,
                                "Error writing to file. "
                                        + exception.getMessage());
                        exception.printStackTrace();
                    } finally {
                        try {
                            if (outStream != null) {
                                outStream.close();
                            }
                        } catch (Exception ignore) {
                        }
                    }
                }
            }
        });

        pack();
    }

    @Override
    public boolean hasContainer(String name) {
        return constraintsManager.getContainerLayout(name) != null;
    }

    private class XmlFileFilter extends FileFilter {

        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }

            String ext = null;
            String s = f.getName();
            int i = s.lastIndexOf('.');
            boolean isXml = false;

            if (i > 0 && i < s.length() - 1) {
                ext = s.substring(i + 1).toLowerCase(Locale.getDefault());
                isXml = ext.equals("xml");
            }

            return isXml;

        }

        @Override
        public String getDescription() {
            return "xml files";
        }

    }

    public void exitApplication() {
        int result = JOptionPane.showConfirmDialog(LayoutFrame.this,
                "Are you sure you want to exit?", "Exit Confirmation",
                JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            //      UserPrefs.getPrefs().saveWinLoc("main", getLocationOnScreen(), getSize());
            //      UserPrefs.getPrefs().saveWinLoc("debug", dframe.getLocationOnScreen(),
            //          dframe.getSize());
            UserPrefs.getPrefs().saveWinLoc("main", this);
            UserPrefs.getPrefs().saveWinLoc("debug", dframe);
            UserPrefs.getPrefs().saveDebugState(viewDebugMenu.isSelected());
            setVisible(false);
            System.exit(0);
        }
    }

    @Override
    public void removeContainer(String name) {
        ContainerLayout layout = constraintsManager.getContainerLayout(name);
        if (layout == null) {
            throw new RuntimeException("Container " + name + " does not exist");
        }
        // Also have to remove any contained containers!
        // EAL, 3/3/06.
        Container container = constraintsManager.getContainer(layout);
        Component[] components = container.getComponents();
        for (Component component2 : components) {
            if (component2 instanceof Container) {
                String componentName = layout.getComponentName(component2);
                if (hasContainer(componentName)) {
                    removeContainer(componentName);
                }
            }
        }
        constraintsManager.removeLayout(layout);
        FormEditor editor = editors.get(layout);
        tabs.remove(editor);
        newLayouts.remove(layout);
    }

    /**
     * This is for adding containers on the fly. The idea is that when someone
     * creates a new panel in one of the existing FormEditors, it can be added
     * here and then they can lay it out.
     */
    @Override
    public void addContainer(String name, Container container)
            throws IllegalArgumentException {
        // check to see if another panel with this name already exists
        ContainerLayout layout = constraintsManager.getContainerLayout(name);
        if (layout != null) {
            throw new IllegalArgumentException("A container with name " + name
                    + " already exists");
        }

        layout = new ContainerLayout(name, "pref", "pref");
        constraintsManager.addLayout(layout);
        container.setLayout(layout);
        newLayouts.add(layout);

        addContainerLayout(layout, container);
    }

    private void addContainerLayout(ContainerLayout containerLayout,
            Container container) {
        FormEditor formEditor = new FormEditor(this, containerLayout, container);
        editors.put(containerLayout, formEditor);
        tabs.addTab(containerLayout.getName(), formEditor);
    }

    private class CodeDialog extends JDialog {
        public CodeDialog(Frame owner, String text) {
            super(owner, "FormLayoutMaker - Code View", true);

            UserPrefs.getPrefs().useSavedBounds("codeview", this);

            JPanel content = new JPanel();
            getContentPane().setLayout(new BorderLayout());
            getContentPane().add(content, BorderLayout.CENTER);

            JTextArea textArea = new JTextArea();
            textArea.setEditable(false);
            textArea.setText(text);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            content.setLayout(new BorderLayout());

            JScrollPane areaScrollPane = new JScrollPane(textArea);
            areaScrollPane.setPreferredSize(new Dimension(600, 400));

            content.add(areaScrollPane, BorderLayout.CENTER);
            pack();

            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    UserPrefs.getPrefs()
                    .saveWinLoc("codeview", CodeDialog.this);
                }
            });
        }
    }

    /**
     * Establish the current preview window. Used to switch between the "normal"
     * and "debug" preview windows.
     *
     * KBR 03/26/06 Use this as the mechanism to enable the 'debug preview'
     * menu, which is disabled by default (to have it disabled when FLM is
     * invoked via the user's app).
     *
     * @param dframe the Jframe for the window.
     */
    void setPreviewFrame(LayoutConstraintsManager lcm, JFrame dframe) {
        //    if ( dframe == null )
        //      dframe = makeNormalPreview(lcm);
        if (this.dframe != null) {
            this.dframe.setVisible(false);
        }
        this.dframe = dframe;

        //    ContainerLayout layout = constraintsManager.getContainerLayout("panel");
        //    FormEditor fe = editors.get(layout);
        //    if ( fe != null )
        //      fe.setContainer(lcm.getContainer(layout));

        UserPrefs.getPrefs().useSavedBounds("debug", dframe);
        //    Rectangle r = UserPrefs.getPrefs().getWinLoc("debug");
        //    dframe.setLocation(r.x, r.y);
        //    dframe.setSize(r.width, r.height);
        dframe.setVisible(true);

        viewDebugMenu.setEnabled(true); // we have a debug frame, enable the menu
    }

    /**
     * Activate "debug" version of preview frame. The title is set accordingly.
     * @param b true to activate debug version
     */
    protected void enableDebugPreview(boolean b) {
        if (dframe == null) {
            return;
        }
        dframe.setTitle("FormLayoutMaker - Preview" + (b ? " (Debug)" : ""));
        FormDebugPanel fdp = (FormDebugPanel) dframe.getContentPane()
                .getComponent(0);
        fdp.deactivate(!b);
    }

    /**
     * Makes a preview frame using FormDebugPanel. The panel can be switched
     * between debug and non-debug modes via @see enableDebugPreview.
     * @param lcm the constraints to be used by the preview panel
     * @return JFrame the preview window
     */
    private static JFrame makeDebugPreview(LayoutConstraintsManager lcm) {
        FormDebugPanel fdp = new FormDebugPanel(true, false);
        fdp.setBorder(Borders.DIALOG_BORDER);
        JFrame debugFrame = new JFrame();
        lcm.setLayout("panel", fdp);
        debugFrame.getContentPane().add(fdp, BorderLayout.CENTER);
        debugFrame
        .setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        return debugFrame;
    }

    public static void main(String[] args) {
        LayoutConstraintsManager constraintsManager = new LayoutConstraintsManager();

        // Always use a FormDebugPanel as the preview panel, but switch
        // it depending on user preference.
        JFrame frame = LayoutFrame.makeDebugPreview(constraintsManager);

        LayoutFrame layoutFrame = new LayoutFrame(constraintsManager);
        JFrame.setDefaultLookAndFeelDecorated(true);
        UserPrefs.getPrefs().useSavedBounds("main", layoutFrame);
        //    Rectangle r = UserPrefs.getPrefs().getWinLoc("main");
        //    layoutFrame.setLocation(r.x, r.y);
        //    layoutFrame.setSize(r.width, r.height);
        layoutFrame.setVisible(true);
        layoutFrame
        .setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        layoutFrame.setPreviewFrame(constraintsManager, frame);
        layoutFrame.enableDebugPreview(UserPrefs.getPrefs().showDebugPanel());
    }
}
