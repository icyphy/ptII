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
/*
 * NewComponentDialog.java
 *
 * Created on March 24, 2005, 6:49 PM
 */

package org.mlc.swing.layout;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import bsh.Interpreter;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;

/**
 * When performing drag-and-drop from the component palette to the table,
 * this class is used to define or edit a component's settings. These
 * settings include the import, declaration, configuration lines which
 * will be used to generate the code for the component. The same settings
 * are used to create a 'preview' component to be shown in the preview
 * window.
 *
 * @author Michael Connor
@version $Id$
@since Ptolemy II 8.0
 */
@SuppressWarnings("serial")
public class NewComponentDialog extends JPanel {
    JLabel componentNameLabel = new JLabel("Name");
    JTextField componentNameTextField = new JTextField();

    JLabel importsLabel = new JLabel("Imports");
    JTextArea importsComponent = createTextArea(3, 40);

    JLabel declarationsLabel = new JLabel("Declarations");
    JTextArea declarationsComponent = createTextArea(3, 40);

    JLabel configureLabel = new JLabel("Configure");
    JTextArea configureComponent = createTextArea(4, 40);

    JLabel addToContainerLabel = new JLabel("Add");
    JTextArea addToContainerComponent = createTextArea(3, 40);

    JLabel removeFromContainerLabel = new JLabel("Remove");
    JTextArea removeFromContainerComponent = createTextArea(2, 40);

    JLabel previewLabel = new JLabel("Preview");
    JScrollPane previewComponent = new JScrollPane();

    JButton prevButton = new JButton("Preview");
    JButton okButton = new JButton("OK");
    JButton cancelButton = new JButton("Cancel");
    Component buttonBar = ButtonBarFactory.buildRightAlignedBar(new JButton[] {
            prevButton, okButton, cancelButton });

    ComponentDef componentDef;
    private String preview;
    Window myOwner;
    private boolean success = false;

    public boolean succeeded() {
        return success;
    }

    /** Creates a new instance of NewComponentDialog */
    public NewComponentDialog(Window owner) {
        myOwner = owner;
        LayoutConstraintsManager layoutConstraintsManager = LayoutConstraintsManager
                .getLayoutConstraintsManager(this.getClass()
                        .getResourceAsStream("editableLayoutConstraints.xml"));

        this.setBorder(Borders.DIALOG_BORDER);

        layoutConstraintsManager.setLayout("newComponentPanel", this);

        // here we add the controls to the container. you may
        // need to change the name of panel
        add(new JScrollPane(removeFromContainerComponent),
                "removeFromContainerComponent");

        add(configureLabel, "configureLabel");
        add(new JScrollPane(importsComponent), "importsComponent");
        add(new JScrollPane(declarationsComponent), "declarationsComponent");
        add(new JScrollPane(configureComponent), "configureComponent");
        add(new JScrollPane(addToContainerComponent), "addToContainerComponent");
        add(buttonBar, "buttonBar");
        add(declarationsLabel, "declarationsLabel");
        add(componentNameLabel, "componentNameLabel");
        add(importsLabel, "importsLabel");
        add(addToContainerLabel, "addToContainerLabel");
        add(componentNameTextField, "componentNameTextField");
        add(removeFromContainerLabel, "removeFromContainerLabel");

        add(previewLabel, "previewLabel");
        add(previewComponent, "previewComponent");

        prevButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doPreview();
            }
        });
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                success = true;
                componentDef.add = getAdd();
                componentDef.configure = getConfiguration();
                componentDef.declarations = getDeclarations();
                componentDef.name = componentNameTextField.getText().trim();
                componentDef.imports = getImports();
                UserPrefs.getPrefs().saveWinLoc("newcomp", myOwner);
                myOwner.setVisible(false);
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                success = false;
                UserPrefs.getPrefs().saveWinLoc("newcomp", myOwner);
                myOwner.setVisible(false);
            }
        });
    }

    // class PreviewThread implements Runnable {
    //
    // boolean running = true;
    //
    // public void run() {
    // while (running) {
    // }
    // }
    // }

    /**
    Get an instance of the specified component. In FormLayoutMaker,
    this instance is placed in the preview panel.
    <p>
    For example, if the component is a JButton, this method is the
    equivalent of <code>new JButton(<i>text</i>)</code>.

    @return Component An instance of the component, null if there
    is a problem in the specification. [It is recommended that the
    'Preview' button should be clicked before exiting the dialog.]
     */
    public Component getInstance() {
        Component component = null;
        String script;
        if (preview == null || preview.length() == 0) {
            script = getImports() + "\n" + getDeclarations() + "\n"
                    + getConfiguration();
        } else {
            script = preview.trim();
        }
        String componentName = componentNameTextField.getText();
        script = script.replaceAll("\\$\\{name\\}", componentName);

        Interpreter interpreter = new Interpreter();
        interpreter.setStrictJava(true);

        JPanel temporaryContainer = null;
        try {

            interpreter.set("container", temporaryContainer);
            interpreter.eval(script);
            component = (Component) interpreter.get(componentName);

        } catch (bsh.EvalError error) {
            System.out.println(error);
        }
        return component;
    }

    private void doPreview() {
        Component component = getInstance();
        if (component == null) {
            return;
        }
        JPanel temporaryContainer = new JPanel();
        temporaryContainer.add(component);
        if (temporaryContainer != null) {
            previewComponent.setViewportView(temporaryContainer);
        }
    }

    /** Get the component's name */
    public String getComponentName() {
        return componentNameTextField.getText();
    }

    public void setComponentName(String componentName) {
        componentNameTextField.setText(componentName);
    }

    /** Get the component's &lt;imports&gt; section. */
    public String getImports() {
        return importsComponent.getText().trim();
    }

    /** Get the component's &lt;declarations&gt; section. */
    public String getDeclarations() {
        return declarationsComponent.getText().trim();
    }

    /** Get the component's &lt;configuration&gt; section. */
    public String getConfiguration() {
        return configureComponent.getText().trim();
    }

    public String getAdd() {
        String res = addToContainerComponent.getText();
        return cleanString(res);
    }

    public void setRemove(String remove) {
        removeFromContainerComponent.setText(remove);
    }

    public void setComponentDef(ComponentDef componentDef) {
        editComponentDef(componentDef.clone());
    }

    public void editComponentDef(ComponentDef componentDef) {
        this.componentDef = componentDef;

        importsComponent.setText(cleanString(componentDef.imports));
        declarationsComponent.setText(cleanString(componentDef.declarations));
        configureComponent.setText(cleanString(componentDef.configure));
        addToContainerComponent.setText(cleanString(componentDef.add));
        removeFromContainerComponent.setText(cleanString(componentDef.remove));
        preview = cleanString(componentDef.preview);
    }

    /** Cleans a string. Removes extra newlines.
     *
     * @param instr
     * @return A clean string.
     */
    private String cleanString(String instr) {
        if (instr == null) {
            return instr;
        }

        // Java 1.5 library method
        //    while ( res.contains("\n\n") )
        // KBR 09/05/05 Reworked to deal with leading space for multiline
        // sections.
        String[] outstrs = instr.split("\n");
        String outstr = "";
        for (String outstr2 : outstrs) {
            String tmp = outstr2.trim();
            outstr += tmp + (tmp.length() > 0 ? "\n" : "");
        }
        return outstr;
    }

    private static JTextArea createTextArea(int rows, int cols) {
        JTextArea textArea = new JTextArea(rows, cols);
        //KBR linewrap makes longer sections hard to work with
        //    textArea.setWrapStyleWord(true);
        //    textArea.setLineWrap(true);
        return textArea;
    }

    /**
     * Creates and displays a dialog for editing a component's settings. See
     * {@link #doDialog(JFrame,ComponentDef)} for an example.
     */
    public static NewComponentDialog editDialog(JFrame owner,
            ComponentDef componentDef) {
        JDialog dlg = new JDialog(owner, "Edit Component", true);
        UserPrefs.getPrefs().useSavedBounds("newcomp", dlg);
        dlg.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        NewComponentDialog panel = new NewComponentDialog(dlg);
        panel.editComponentDef(componentDef);
        panel.setComponentName(componentDef.name);
        dlg.getContentPane().add(panel);
        dlg.pack();
        dlg.setVisible(true);
        return panel;
    }

    /**
     * Creates and displays a dialog for defining a new component's settings. The
     * dialog should be used as follows:
     * <code>
     * NewComponentDialog dlg = NewComponentDialog.doDialog(frame, componentDef);
     * if (dlg.succeeded()) {
     *    [do something with dlg.componentDef]
     * }
     * </code>
     */
    public static NewComponentDialog doDialog(JFrame owner,
            ComponentDef componentDef) {
        JDialog dlg = new JDialog(owner, "New Component", true);
        UserPrefs.getPrefs().useSavedBounds("newcomp", dlg);
        dlg.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        NewComponentDialog panel = new NewComponentDialog(dlg);
        panel.setComponentDef(componentDef);
        panel.setComponentName("untitled");
        dlg.getContentPane().add(panel);
        dlg.pack();
        dlg.setVisible(true);
        return panel;
    }

    /** Unit testing.
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        NewComponentDialog dialog = new NewComponentDialog(frame);

        ComponentDef componentDef = new ComponentDef();
        componentDef.imports = "import javax.swing.JLabel;";
        componentDef.declarations = "JLabel ${name} = new JLabel(\"Hello World\");";
        componentDef.configure = "";
        componentDef.add = "${container}.add(${name}, \"${name}\");";
        componentDef.remove = "${container}.remove($name);";
        dialog.setComponentName("untitled");
        dialog.setComponentDef(componentDef);

        frame.getContentPane().add(dialog);
        frame.pack();
        frame.setVisible(true);
    }

}
