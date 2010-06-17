package ptdb.common.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * The attribute that need to be stored or retrieved from the database.
 * 
 * <p>
 * It is used as a data transfer object.
 * </p>
 * 
 * @author Yousef Alsaeed
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (yalsaeed)
 * @Pt.AcceptedRating Red (yalsaeed)
 *
 */
public class XMLDBAttribute {
    
    
    /**
     * Construct a XMLDBAttribute instance
     * with the given attribute name.
     *
     * @param attributeName the name for the given attribute.
     */
    public XMLDBAttribute(String attributeName, String attributeType) {
        this._attributeName = attributeName;
        this._attributeType = attributeType;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////
    
    public static final String ATTRIBUTE_TYPE_STRING = "String";
    public static final String ATTRIBUTE_TYPE_BOOLEAN = "Boolean";
    public static final String ATTRIBUTE_TYPE_LIST = "List";
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /**
     * Return the attribute name.
     * @return The attribute name.
     * 
     */
    public String getAttributeName() {
        return _attributeName;
    }
    
    
    /**
     * Return the attribute type.
     * @return A string representation of the attribute type.
     */
    public String getAttributeType() {
        return _attributeType;
    }
    
    
    /**
     * Return the attribute value.
     * @return A string representation of the attribute value.
     * 
     * @see #setAttributeValue
     */
    public List<String> getAttributeValues() {
        
        if (_attributeType.equals(XMLDBAttribute.ATTRIBUTE_TYPE_LIST)) {
            return _attributeValues;
        } else {
            return null;
        }

    }


    /**
     * Set the attribute list value only when the attribute type is list
     * @param attributeValues The list of attribute values.
     * 
     * @see #getAttributeValue
     */
    public void setAttributeValue(List<String> attributeValues) {
        
        if (_attributeType.equals(XMLDBAttribute.ATTRIBUTE_TYPE_LIST)) {
            _attributeValues = attributeValues;
        } 
    }


    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    

    /** Attribute name. */
    private String _attributeName;
    
    /** The value of the attribute in a string. */
    private List<String> _attributeValues;
    
    /** The type of the attribute in a string. */
    private String _attributeType;

}
