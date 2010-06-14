package ptdb.gui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

///////////////////////////////////////////////////////////////////
//// ModelAttributePanel

/**
 * An extended JPanel displaying an attribute.  Multiple ModelAttributePanel objects
 * may be displayed on the SaveModelToDBFrame.  The _attMap indicates the type of
 * attribute.  For a selected attribute name, a different Component may be displayed
 * on the panel.
 *
 * @author Lyle Holsinger
 * @since Ptolemy II 8.1
 * @version $Id$
 * @Pt.ProposedRating red (lholsing)
 * @Pt.AcceptedRating red (lholsing)
 *
 */

public class ModelAttributePanel extends JPanel {

    /** Construct a ModelAttributePanel.  All components are
     * created and added to the panel.  A listener is added for
     * the _attributeName ComboBox.  When the value changes,
     * updateDisplay() is called to display the proper form
     * Component.
     *
     * @param attMap
     *          Associates attributes with their type to
     *          allow display of proper form Components.
     */
    public ModelAttributePanel(HashMap attMap) {

        super();

        _attMap = attMap;

        setMaximumSize(new Dimension(0, 30));
        setLayout(new GridLayout(1, 4));
        setBorder(BorderFactory.createEtchedBorder());

        JLabel nameLabel = new JLabel("Attribute");
        _attributeName = new JComboBox(getAttributeList(_attMap));

        JLabel valueLabel = new JLabel("Value");

        _textValue = new JTextField();
        _booleanValue = new JCheckBox();
        _listValue = new JComboBox();

        _attributeName.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                JComboBox cb = (JComboBox) e.getSource();
                String att = (String) cb.getSelectedItem();
                updateDisplay(att);
            }

        });

        add(nameLabel);
        add(_attributeName);
        add(valueLabel);
        add(_textValue);
        add(_booleanValue);
        add(_listValue);

        updateDisplay(_attributeName.getSelectedItem().toString());

    }

    /** Get the value of this attribute.
     *
     * @return
     *          The string value of the attribute,
     *          It is obtained from the applicable form Component based on
     *          the type mapped to the selected attribute.
     */
    public String getValue() {

        if (_attMap.get(_attributeName.getSelectedItem().toString()) == "Text") {

            return _textValue.getText();

        } else if (_attMap.get(_attributeName.getSelectedItem().toString()) == "List") {

            if (_listValue.getSelectedItem() != null) {

                return _listValue.getSelectedItem().toString();

            } else {

                return "";

            }

        } else if (_attMap.get(_attributeName.getSelectedItem().toString()) == "Boolean") {

            if (_booleanValue.isSelected()) {

                return "TRUE";

            } else {

                return "FALSE";

            }

        } else {

            return null;

        }

    }

    ///////////////////////////////////////////////////////////////////
    //                    public methods                          ////

    /** Get the selected attribute's name.
     *
     * @return
     *          This attribute's name.
     */
    public String getAttributeName() {

        return _attributeName.getSelectedItem().toString();

    }

    ///////////////////////////////////////////////////////////////////
    //                    private methods                          ////

    private void updateDisplay(String att) {

        if (_attMap.get(att) == "Boolean") {

            _booleanValue.setVisible(true);
            _listValue.setVisible(false);
            _listValue.setSelectedItem(null);
            _textValue.setVisible(false);
            _textValue.setText("");
        } else if (_attMap.get(att) == "Text") {

            _textValue.setVisible(true);
            _booleanValue.setVisible(false);
            _booleanValue.setSelected(false);
            _listValue.setVisible(false);
            _listValue.setSelectedItem(null);

        } else if (_attMap.get(att) == "List") {

            _listValue.setVisible(true);
            _textValue.setVisible(false);
            _textValue.setText("");
            _booleanValue.setVisible(false);
            _booleanValue.setSelected(false);

        }

    }

    private String[] getAttributeList(HashMap attMap) {

        String[] returnList = new String[attMap.size()];

        Iterator iterator = (attMap.keySet()).iterator();

        int i = 0;
        while (iterator.hasNext()) {

            returnList[i] = iterator.next().toString();
            i++;
        }

        return returnList;
    }

    ///////////////////////////////////////////////////////////////////
    //                    private variables                        ////

    private JComboBox _attributeName;
    private JTextField _textValue;
    private JCheckBox _booleanValue;
    private JComboBox _listValue;
    private HashMap _attMap;

}
