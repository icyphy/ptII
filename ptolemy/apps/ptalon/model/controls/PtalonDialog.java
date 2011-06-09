package ptolemy.apps.ptalon.model.controls;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import ptolemy.apps.ptalon.model.PtalonModel;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.util.StringUtilities;

public class PtalonDialog extends JDialog implements ActionListener,
        TextListener {

    public PtalonDialog(Frame owner, String title, PtalonModel model)
            throws HeadlessException {
        super(owner, title, true);
        _model = model;
        this.setSize(new Dimension(640, 480));
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        _tabbedPane = new JTabbedPane();
        _tabbedPane.addTab("Ptalon Code", createPtalonCodePanel());
        _tabbedPane.addTab("Model Parameters", createPtalonParametersPanel());
        panel.add(_tabbedPane, BorderLayout.CENTER);
        this.add(panel);
        this.setVisible(true);
    }

    private PtalonModel _model;

    private JPanel createPtalonCodePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        _codeArea = new JTextArea();
        //FIXME: Font.MONOSPACED does not exist under 1.5
        //_codeArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        _codeArea.setMargin(new Insets(6, 6, 6, 6));
        _codeArea.setTabSize(3);
        _codeArea.setText(_model.getCode());
        JScrollPane pane = new JScrollPane(_codeArea);
        panel.add(pane, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();

        JButton newButton = new JButton("New");
        newButton.setActionCommand("new");
        newButton.addActionListener(this);

        JButton openButton = new JButton("Open...");
        openButton.setActionCommand("open");
        openButton.addActionListener(this);

        JButton saveButton = new JButton("Save");
        saveButton.setActionCommand("save");
        saveButton.addActionListener(this);

        JButton saveAsButton = new JButton("Save As...");
        saveAsButton.setActionCommand("saveAs");
        saveAsButton.addActionListener(this);

        JButton updateButton = new JButton("Update Ptalon Model");
        updateButton.setActionCommand("update");
        updateButton.addActionListener(this);

        buttonPanel.add(newButton);
        buttonPanel.add(openButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(saveAsButton);
        buttonPanel.add(updateButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JTextArea _codeArea;

    private JTabbedPane _tabbedPane;

    private JPanel createPtalonParametersPanel() {
        List<Parameter> list = new ArrayList<Parameter>();
        for (Parameter p : PtalonModel.parameterList(_model)) {
            list.add(p);
        }
        if (list.size() == 0) {
            JPanel panel = new JPanel();
            panel.add(new JLabel("No parameters for this model."));
            return panel;
        } else {
            JPanel main = new JPanel();
            main.setLayout(new BorderLayout());
            LabelledItemPanel panel = new LabelledItemPanel();
            main.add(panel, BorderLayout.CENTER);
            _parameters.clear();
            for (Parameter p : list) {
                JTextField field = new JTextField(p.getExpression());
                _parameters.put(field, p);
                panel.addItem(p.getName(), field);
            }
            JButton updateButton = new JButton("Update Ptalon Model");
            updateButton.setActionCommand("update");
            updateButton.addActionListener(this);
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(updateButton);
            main.add(buttonPanel, BorderLayout.SOUTH);
            return main;
        }
    }

    private Hashtable<JTextField, Parameter> _parameters = new Hashtable<JTextField, Parameter>();

    public void actionPerformed(ActionEvent e) {
        if ("new".equals(e.getActionCommand())) {
            int result = JOptionPane.showConfirmDialog(this,
                    "Clear existing code and start over?", "New Code",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null);
            if (result == JOptionPane.YES_OPTION) {
                _codeArea.setText("");
            }
        } else if ("open".equals(e.getActionCommand())) {
            JFileChooser chooser = new JFileChooser();
            //FileNameExtensionFilter is not found in Java 1.5
            //			FileNameExtensionFilter filter = new FileNameExtensionFilter(
            //					"Ptalon files", "ptln");
            //			chooser.setFileFilter(filter);
            if (_model.getFile() != null) {
                chooser.setCurrentDirectory(_model.getFile().getParentFile());
            } else {
                chooser.setCurrentDirectory(_ptDir);
            }
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                FileReader reader = null;
                try {
                    reader = new FileReader(chooser.getSelectedFile());
                    StringBuffer buffer = new StringBuffer();
                    char[] c = new char[1024];
                    int i = 0;
                    while ((i = reader.read(c, 0, 1024)) > 0) {
                        buffer.append(c, 0, i);
                    }
                    reader.close();
                    _codeArea.setText(buffer.toString());
                    _codeArea.setCaretPosition(0);
                    _model.setFile(chooser.getSelectedFile());
                } catch (FileNotFoundException e1) {
                    JOptionPane.showMessageDialog(this, "Could not open file.",
                            "Ptalon", JOptionPane.ERROR_MESSAGE);
                } catch (IOException e1) {
                    JOptionPane.showMessageDialog(this, "Failed to read file.",
                            "Ptalon", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if ("save".equals(e.getActionCommand())) {
            File file = _model.getFile();
            if (file == null) {
                _saveAs();
            } else {
                _save(file);
            }
        } else if ("saveAs".equals(e.getActionCommand())) {
            _saveAs();
        } else if ("update".equals(e.getActionCommand())) {
            for (JTextField field : _parameters.keySet()) {
                _parameters.get(field).setExpression(field.getText());
            }
            _model.setCode(_codeArea.getText());
            try {
                _model.updateModel();
                _tabbedPane.setComponentAt(1, this
                        .createPtalonParametersPanel());
            } catch (IllegalActionException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Ptalon",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static File _ptDir = new File(StringUtilities
            .getProperty("ptolemy.ptII.dir"));

    private void _saveAs() {
        File file = _model.getFile();
        if (file == null) {
            file = _ptDir;
        } else {
            file = file.getParentFile();
        }

        JFileChooser chooser = new JFileChooser();
        //FileNameExtensionFilter is not found in Java 1.5
        // 		FileNameExtensionFilter filter = new FileNameExtensionFilter(
        // 				"Ptalon files", "ptln");
        // 		chooser.setFileFilter(filter);
        chooser.setCurrentDirectory(file);
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            _save(chooser.getSelectedFile());
        }
    }

    private void _save(File file) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(file);
            writer.print(_codeArea.getText());
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Could not save file.",
                    "Ptalon", JOptionPane.WARNING_MESSAGE);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public void textValueChanged(TextEvent e) {
        if (e.getSource() instanceof JTextField) {
            JTextField field = (JTextField) e.getSource();
            if (_parameters.containsKey(field)) {
                _parameters.get(field).setExpression(field.getText());
            }
        }
    }

}
