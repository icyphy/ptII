package org.mlc.swing.example;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.mlc.swing.layout.LayoutFrame;

/**
 * @author Michael Connor
 */
public class ExamplePanel extends javax.swing.JPanel {
    JLabel nameLabel = new JLabel("Name");

    JTextField nameText = new JTextField();

    JTabbedPane tabbedPane = new JTabbedPane();

    JPanel firstTab = new JPanel();

    JPanel secondTab = new JPanel();

    JPanel thirdTab = new JPanel();

    public ExamplePanel() {
        super();
        org.mlc.swing.layout.LayoutConstraintsManager layoutConstraintsManager = new org.mlc.swing.layout.LayoutConstraintsManager();
        setBorder(com.jgoodies.forms.factories.Borders.DIALOG_BORDER);

        layoutConstraintsManager.setLayout("panel", this);
        layoutConstraintsManager.setLayout("firstTab", firstTab);
        layoutConstraintsManager.setLayout("secondTab", secondTab);
        layoutConstraintsManager.setLayout("thirdTab", thirdTab);

        LayoutFrame layoutFrame = new LayoutFrame(layoutConstraintsManager);
        layoutFrame.setVisible(true);

        this.add(tabbedPane, "tabbedPane");
        this.add(nameLabel, "nameLabel");
        this.add(nameText, "nameText");
        tabbedPane.add("First", firstTab);
        tabbedPane.add("Second", secondTab);
        tabbedPane.add("Third", thirdTab);
    }

    public static void main(String[] args) {
        ExamplePanel examplePanel = new ExamplePanel();

        JFrame frame = new JFrame("Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(examplePanel, BorderLayout.CENTER);
        frame.setSize(400, 500);
        frame.setVisible(true);
    }

}
