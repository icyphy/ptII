package ptolemy.homer.tree;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;

import ptolemy.vergil.tree.PTree;

public class NamedObjectTree extends JPanel implements TreeSelectionListener {

    private final PTree tree;
    private final JTextField currentSelectionField;

    public NamedObjectTree(TreeModel model) {
        this.setLayout(new BorderLayout());
        tree = new PTree(model);
        tree.addTreeSelectionListener(this);
        tree.setScrollsOnExpand(true);
        add(new JScrollPane(tree), BorderLayout.CENTER);
        currentSelectionField = new JTextField("Current Selection: NONE");

        //getContentPane().add(new JScrollPane(tree), BorderLayout.CENTER);
        add(currentSelectionField, BorderLayout.SOUTH);
        setSize(500, 200);
        setVisible(true);
    }

    public void valueChanged(TreeSelectionEvent e) {
        currentSelectionField.setText("Current Selection: "
                + tree.getLastSelectedPathComponent().toString());

    }

}
