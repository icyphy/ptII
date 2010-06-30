package ptdb.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ptdb.common.dto.XMLDBAttribute;

///////////////////////////////////////////////////////////////////
//// ModelAttributePanel

/**
 * An extended JPanel displaying an attribute.  Multiple ModelAttributePanel objects
 * may be displayed on the SaveModelToDBFrame.  The _attributeMap indicates the type of
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

        _attributeMap = attMap;
        
        setBorder(BorderFactory.createEtchedBorder());
        
        setMaximumSize(new Dimension(650, 30));
        setMinimumSize(getMaximumSize());
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        
        setAlignmentX(LEFT_ALIGNMENT);
        setAlignmentY(TOP_ALIGNMENT);
        
        JLabel nameLabel = new JLabel(" Attribute ");
        nameLabel.setPreferredSize(new Dimension(50, 20));
        nameLabel.setAlignmentX(LEFT_ALIGNMENT);
        
        _attributeName = new JComboBox(getAttributeList(_attributeMap));
        _attributeName.setPreferredSize(new Dimension(180, 20));
        _attributeName.setAlignmentX(LEFT_ALIGNMENT);
        
        JLabel valueLabel = new JLabel(" Value ");
        valueLabel.setPreferredSize(new Dimension(40, 20));
        valueLabel.setAlignmentX(LEFT_ALIGNMENT);

        _textValue = new JTextField();
        _textValue.setPreferredSize(new Dimension(180, 20));
        _textValue.setAlignmentX(LEFT_ALIGNMENT);
        
        _booleanValue = new JCheckBox();
        _booleanValue.setAlignmentX(LEFT_ALIGNMENT);
        
        _listValue = new JComboBox();
        _listValue.setPreferredSize(new Dimension(180, 20));
        _listValue.setAlignmentX(LEFT_ALIGNMENT);

        _attributeName.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                updateDisplay();
            }

        });

        add(nameLabel);
        add(_attributeName);
        add(valueLabel);
        add(_textValue);
        add(_booleanValue);
        add(_listValue);

        updateDisplay();

    }

    /** Get the value of this attribute.
     *
     * @return
     *          The string value of the attribute,
     *          It is obtained from the applicable form Component based on
     *          the type mapped to the selected attribute.
     * 
     * @see #setValue
     * 
     */
    public String getValue() {

        try {
            
            if (((XMLDBAttribute)_attributeMap
                    .get(_attributeName.getSelectedItem()
                    .toString())).getAttributeType()
                    .equals(XMLDBAttribute.ATTRIBUTE_TYPE_STRING)) {
    
                return _textValue.getText();
    
            } else if (((XMLDBAttribute)_attributeMap
                    .get(_attributeName.getSelectedItem()
                    .toString())).getAttributeType()
                    .equals(XMLDBAttribute.ATTRIBUTE_TYPE_LIST)) {
    
                if (_listValue.getSelectedItem() != null) {
    
                    return _listValue.getSelectedItem().toString();
    
                } else {
    
                    return "";
    
                }
    
            } else if (((XMLDBAttribute)_attributeMap
                    .get(_attributeName.getSelectedItem()
                    .toString())).getAttributeType()
                    .equals(XMLDBAttribute.ATTRIBUTE_TYPE_BOOLEAN)) {
    
                if (_booleanValue.isSelected()) {
    
                    return "TRUE";
    
                } else {
    
                    return "FALSE";
    
                }
    
            } else {
    
                return "";
    
            }
            
        } catch (NullPointerException e){
            
            return "";
        
        }

    }

    ///////////////////////////////////////////////////////////////////
    //                    public methods                          ////

    /** Get the selected attribute's name.
     *
     * @return
     *          This attribute's name.
     * 
     * @see #setAttributeName
     * 
     */
    public String getAttributeName() {

        return _attributeName.getSelectedItem().toString();

    }
    
    /** Set the attribute name.
     * 
     * @param name 
     *          The attribute name.
     * 
     * @see #getAttributeName
     * 
     */
   public void setAttributeName(String name) {

       _attributeName.setSelectedItem(name);
       updateDisplay();

   }
    
    /** Set the value of this attribute.
     * 
     * @param value 
     *          The value of the attribute.
     * 
     * @see #getValue
     * 
     */
   public void setValue(String value) {

       if (((XMLDBAttribute)_attributeMap.get(_attributeName.getSelectedItem()
               .toString())) == null){
           
           return;
       
       }
       else if (((XMLDBAttribute)_attributeMap
               .get(_attributeName.getSelectedItem()
               .toString())).getAttributeType()
               .equals(XMLDBAttribute.ATTRIBUTE_TYPE_STRING)) {

           _textValue.setText(value);

       } else if (((XMLDBAttribute)_attributeMap
               .get(_attributeName.getSelectedItem()
               .toString())).getAttributeType()
               .equals(XMLDBAttribute.ATTRIBUTE_TYPE_LIST)) {

           _listValue.setSelectedItem(value);

       } else if (((XMLDBAttribute)_attributeMap
               .get(_attributeName.getSelectedItem()
               .toString())).getAttributeType()
               .equals(XMLDBAttribute.ATTRIBUTE_TYPE_BOOLEAN)) {

           if (value.equals("TRUE")) {

               _booleanValue.setSelected(true);

           } else if (value.equals("FALSE")){

               _booleanValue.setSelected(false);
               
           }
           
       }

   }

    ///////////////////////////////////////////////////////////////////
    //                    private methods                          ////

    private void updateDisplay() {

        if (((XMLDBAttribute)_attributeMap.get(_attributeName.getSelectedItem()
                .toString())) == null){   
            
            _listValue.setEnabled(false);
            _textValue.setEnabled(false);
            _booleanValue.setEnabled(false);
            
            _textValue.setText("");
            _booleanValue.setSelected(false);
            _listValue.setSelectedItem(null);
        
        } else if (((XMLDBAttribute)_attributeMap
                .get(_attributeName.getSelectedItem()
                .toString())).getAttributeType()
                .equals(XMLDBAttribute.ATTRIBUTE_TYPE_BOOLEAN)) {

            _booleanValue.setEnabled(true);
            _listValue.setEnabled(false);
            _listValue.setSelectedItem(null);
            _textValue.setEnabled(false);
            _textValue.setText("");
        } else if (((XMLDBAttribute)_attributeMap
                .get(_attributeName.getSelectedItem()
                .toString())).getAttributeType()
                .equals(XMLDBAttribute.ATTRIBUTE_TYPE_STRING)) {

            _textValue.setEnabled(true);
            _booleanValue.setEnabled(false);
            _booleanValue.setSelected(false);
            _listValue.setEnabled(false);
            _listValue.setSelectedItem(null);

        } else if (((XMLDBAttribute)_attributeMap
                .get(_attributeName.getSelectedItem()
                .toString())).getAttributeType()
                .equals(XMLDBAttribute.ATTRIBUTE_TYPE_LIST)) {

            _listValue.setEnabled(true);
            _textValue.setEnabled(false);
            _textValue.setText("");
            _booleanValue.setEnabled(false);
            _booleanValue.setSelected(false);
            
            DefaultComboBoxModel theModel = 
                (DefaultComboBoxModel)_listValue.getModel();
            theModel.removeAllElements();
            
            ArrayList<String> choiceList = (ArrayList)
                ((XMLDBAttribute)_attributeMap
                    .get(_attributeName.getSelectedItem()
                    .toString())).getAttributeValues();

            _listValue.addItem("");
            for(String choice : choiceList){
                
                _listValue.addItem(choice);
                
            }
            
        
        } else {
            
            _listValue.setEnabled(false);
            _textValue.setEnabled(false);
            _booleanValue.setEnabled(false);
            
            _textValue.setText("");
            _booleanValue.setSelected(false);
            _listValue.setSelectedItem(null);
                
        }

    }

    private String[] getAttributeList(HashMap attMap) {

        String[] returnList = new String[attMap.size() + 1];

        Iterator iterator = (attMap.keySet()).iterator();

        int i = 0;
        returnList[i++] = "";
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
    private HashMap _attributeMap;

}
