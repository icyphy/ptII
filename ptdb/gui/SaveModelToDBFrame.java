package ptdb.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneLayout;

import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
////SaveModelToDBFrame

/**
* An extended JFrame used for saving a model to the database.
* Additionally, the user can manage model attributes prior to saving.
* This associates saved attributes to those that the user selects for
* model searches.
* 
* @author Lyle Holsinger
* @since Ptolemy II 8.1
* @version $Id$
* @Pt.ProposedRating red (lholsing)
* @Pt.AcceptedRating red (lholsing)
*
*/

public class SaveModelToDBFrame extends JFrame {

    /** Construct a SaveModelToDBFrame.  Add swing Components
     * to the frame.  Add a listener for the "+" button, which
     * adds a ModelAttributePanel to the tabbed pane.  Add a 
     * listener for the Save button to call _saveModel().
     * 
     * @param model
     *          The model that is being saved to the database.
     * @param frame
     *          The frame from which the save form was opened.
     *          Passed to the object to allow repainting if 
     *          attribute modifications occur.
     */
    public SaveModelToDBFrame(NamedObj model, JFrame frame) {

        super("Save Model to Database");

        setBounds(100, 100, 500, 300);
        setResizable(false);

        JPanel outerPanel = new JPanel(new GridLayout(2, 1));
        JPanel topPanel = new JPanel(new GridLayout(1, 1));
        JPanel bottomPanel = new JPanel(new GridLayout(2, 4, 5, 5));

        _tabbedPane = new JTabbedPane();
        _attListPanel = new JPanel(false);
        _scrollPane = new JScrollPane(_attListPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        _modelAttPanelArray = new ArrayList();
        _deleteButtons = new ArrayList();
        _aList = new HashMap();
        _AttDelete = new HashMap();

        bottomPanel.setPreferredSize(getMinimumSize());
        topPanel.setBorder(BorderFactory.createEmptyBorder());
        _attListPanel.setLayout(new GridLayout(0, 2));
        _scrollPane.setLayout(new ScrollPaneLayout());

        _tabbedPane.addTab("Model Info", _scrollPane);
        _tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

        //TODO - Determine the best way to get attribute types
        //       and populate this list.
        _aList.put("Att1", "Text");
        _aList.put("Att2", "Boolean");
        _aList.put("Att3", "List");

        for (int i = 0; i < 5; i++) {

            _modelAttPanelArray.add(new ModelAttributePanel(_aList));
            _deleteButtons.add(new JButton("Delete"));

            _deleteButtons.get(i).setMnemonic(KeyEvent.VK_D);
            _deleteButtons.get(i).setActionCommand("Delete");
            _deleteButtons.get(i).setHorizontalTextPosition(
                    AbstractButton.CENTER);

            _attListPanel.add(_modelAttPanelArray.get(i));
            _attListPanel.add(_deleteButtons.get(i));

            _AttDelete.put(_deleteButtons.get(i), _modelAttPanelArray.get(i));

            _deleteButtons.get(i).addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {

                    _attListPanel.remove((JPanel) _AttDelete
                            .get((JButton) event.getSource()));
                    _attListPanel.remove((JButton) event.getSource());
                    repaint();
                }
            });
        }

        topPanel.add(_tabbedPane);

        JButton add_Button = new JButton("+");
        add_Button.setMnemonic(KeyEvent.VK_ENTER);
        add_Button.setActionCommand("Save");

        bottomPanel.add(add_Button);
        add_Button.setHorizontalTextPosition(AbstractButton.CENTER);

        add_Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {

                _modelAttPanelArray.add(new ModelAttributePanel(_aList));
                _attListPanel.add(_modelAttPanelArray.get(_modelAttPanelArray
                        .size() - 1));
                repaint();
            }
        });

        /*TODO Address layout of the frame.  
         * GridLayout is probably a bad choice. 
         * The labels below serve as spacers.
         * This is very ugly, but very temporary.
         */
        bottomPanel.add(new JLabel(""));
        bottomPanel.add(new JLabel(""));
        bottomPanel.add(new JLabel(""));
        bottomPanel.add(new JLabel(""));
        bottomPanel.add(new JLabel(""));
        bottomPanel.add(new JLabel(""));

        //Add Save button to the bottom panel
        JButton save_Button;
        save_Button = new JButton("Save");
        save_Button.setMnemonic(KeyEvent.VK_ENTER);
        save_Button.setActionCommand("Save");
        bottomPanel.add(save_Button);
        save_Button.setHorizontalTextPosition(AbstractButton.CENTER);

        save_Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {

                _saveModel();
                setVisible(false);
            }
        });

        //The following line enables to use scrolling tabs.
        _tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        outerPanel.add(topPanel);
        outerPanel.add(bottomPanel);

        add(outerPanel);

    }

    ///////////////////////////////////////////////////////////////////
    //                    private methods                          ////

    private void _saveModel() {

        //TODO

    }

    ///////////////////////////////////////////////////////////////////
    //                    private variables                        ////

    private ArrayList<ModelAttributePanel> _modelAttPanelArray;
    private ArrayList<JButton> _deleteButtons;
    private JPanel _attListPanel;
    private HashMap _aList;
    private JScrollPane _scrollPane;
    private HashMap _AttDelete;
    private JTabbedPane _tabbedPane;

}
