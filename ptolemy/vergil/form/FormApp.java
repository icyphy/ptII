package ptolemy.vergil.form;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class FormApp extends JFrame
{
    private JPopupMenu popup = null;
//
    public FormApp(String aString)
    {
        super(aString);
        getContentPane().add(createComponents(), BorderLayout.CENTER);
    }
    
    public static void main(String[] args)
    {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (Exception e) {}    
        FormApp app = new FormApp("FormApp");
        app.addWindowListener(new WindowAdapter()
        {
            public void windowClosed(WindowEvent e) { System.exit(0); }
            public void windowClosing(WindowEvent e) { System.exit(0); }
        });
        app.pack();
        app.setVisible(true);
    }

    private void doVergilDialog()
    {
        FormKeyModel keyModel = new FormKeyModel();
        keyModel.add(new PortKey("in", true, false, false));
        keyModel.add(new PortKey("out", false, true, false));
//        keyModel.add(new PortKey("inout", true, true, false));
        keyModel.add(new PortKey("in[]", true, false, true));
        keyModel.add(new PortKey("out[]", false, true, true));
//        keyModel.add(new PortKey("inout[]", true, true, true));
        keyModel.add(new ParameterKey("parameter"));
        FormColumnModel columnModel = new FormColumnModel();
        columnModel.addColumn(new ControlCell());
        columnModel.addColumn(new KeyCell(keyModel, "in"));
        FormCell nameCell = new NameCell("");
        nameCell.setEnabled(false);
        columnModel.addColumn(nameCell);
        columnModel.addColumn(new StringCell("value", ""));
        columnModel.addColumn(new StringCell("type", ""));
        FormCell classCell = new StringCell("class", "");
        classCell.setEnabled(false);
        columnModel.addColumn(classCell);
        columnModel.addColumn(new BooleanCell("visible", Boolean.FALSE));
        FormFrame app = new FormFrame("Vergil Form Editor");
        app.setPreferredScrollableViewportSize(new Dimension(600, 180));
        app.setColumnModel(columnModel);
        Object[][] parameterData = { { null, "in", "i", "0", "double", "ptolemy.actor.TypedIOPort", Boolean.TRUE },
                                     { null, "out", "q", "0", "double", "ptolemy.actor.TypedIOPort", Boolean.TRUE },
                                     { null, "parameter", "p", "0", "double", "", Boolean.FALSE } };
        app.addRows(parameterData);
        app.pack();
        app.setVisible(true);
    }

    private void doWdlDialog()
    {
        FormKeyModel keyModel = new FormKeyModel();
        keyModel.add(new PortKey("in", true, false, false));
        keyModel.add(new PortKey("out", false, true, false));
        keyModel.add(new PortKey("inout", true, true, false));
        keyModel.add(new PortKey("in[]", true, false, true));
        keyModel.add(new PortKey("out[]", false, true, true));
        keyModel.add(new PortKey("inout[]", true, true, true));
        keyModel.add(new ParameterKey("parameter"));
        FormColumnModel columnModel = new FormColumnModel();
        columnModel.addColumn(new ControlCell());
        columnModel.addColumn(new KeyCell(keyModel, "in"));
        columnModel.addColumn(new StringCell("flow", ""));
        columnModel.addColumn(new NameCell(""));
        columnModel.addColumn(new StringCell("value", ""));
        columnModel.addColumn(new StringCell("type", ""));
        columnModel.addColumn(new StringCell("default-type", ""));
        FormFrame app = new FormFrame("Wdl Form Editor");
        app.setColumnModel(columnModel);
        Object[][] parameterData = { { null, "in", "event", "i", "0", "double", "v" },
                                     { null, "out", "token", "q", "0", "double", "v" },
                                     { null, "parameter", "", "p", "0", "double", "v" } };
        app.addRows(parameterData);
        app.pack();
        app.setVisible(true);
    }

    private void popUp(MouseEvent e)
    {
        if (popup == null)
            popup = createPopUpMenu();
        popup.show(e.getComponent(), e.getX(), e.getY());
    }

// GUI creation.    
    private Component createOkButton()
    {
        JButton button = new JButton("Ok");
        button.setMnemonic('O');
        button.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { dispose(); } });
        return button;
    }      
    private Component createVergilButton()
    {
        JButton button = new JButton("Vergil");
        button.setMnemonic('F');
        button.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { doVergilDialog(); } });
        return button;
    }      
    private Component createWdlButton()
    {
        JButton button = new JButton("Wdl");
        button.setMnemonic('F');
        button.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { doWdlDialog(); } });
        return button;
    }      
    
    private Component createButtons()
    {
        JPanel buttonRow = new JPanel();
        buttonRow.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        buttonRow.setLayout(new BoxLayout(buttonRow, BoxLayout.X_AXIS));
        buttonRow.add(Box.createHorizontalGlue());
        buttonRow.add(createVergilButton());
        buttonRow.add(Box.createHorizontalGlue());
        buttonRow.add(createWdlButton());
        buttonRow.add(Box.createHorizontalGlue());
        buttonRow.add(createOkButton());
        buttonRow.add(Box.createHorizontalGlue());
        return buttonRow;
    }     
    private Component createComponents()
    {
        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        pane.add(createButtons());
        pane.addMouseListener(new PopupListener() { public void showPopup(MouseEvent e) { popUp(e); } });
        return pane;
    }    
    private JPopupMenu createPopUpMenu()
    {
        JPopupMenu popUp = new JPopupMenu();
        JMenuItem vergilDialog = new JMenuItem("Create Vergil Dialog");
        vergilDialog.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { doVergilDialog(); } });
        popUp.add(vergilDialog);
        JMenuItem wdlDialog = new JMenuItem("Create Wdl Dialog");
        wdlDialog.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { doWdlDialog(); } });
        popUp.add(wdlDialog);
        return popUp;
    }    
}