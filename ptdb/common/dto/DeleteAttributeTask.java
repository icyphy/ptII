package ptdb.common.dto;


///////////////////////////////////////////////////////////////
//// DeleteAttributeTask

/**
 * A task request to delete an attribute from the database.
 * 
 * <p>It is used as a data transfer object and hold the attribute 
 * with its getter and setter method.</p>
 * 
 * @author Yousef Alsaeed
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (yalsaeed)
 * @Pt.AcceptedRating Red (yalsaeed)
 *
 */


public class DeleteAttributeTask {


    /**
     * Construct an instance of the object and set the attribute to be deleted
     * from the database.
     * 
     * @param attribute the attribute to be deleted from the database.
     */
    public DeleteAttributeTask(XMLDBAttribute attribute) {
        
        _xmlDBAttribute = attribute;
    }

    //////////////////////////////////////////////////////////////////////
    ////		public methods 					  ////
    
   /**
    * Return the attribute to be deleted from the database.
    * 
    * @return The attribute to be deleted from the database.
    * @see #setXMLDBAttribute
    */
    public XMLDBAttribute getXMLDBAttribute() {
        return _xmlDBAttribute;
    }
    
    /**
     * Set the attribute to be deleted from the database.
     * 
     * @param attribute the attribute to be deleted from the database.
     * @see #getXMLDBAttribute
     */
    public void setXMLDBAttribute(XMLDBAttribute attribute) {
        _xmlDBAttribute = attribute;
    }

    
    //////////////////////////////////////////////////////////////////////
    ////		private variables				  ////
    
    /** The attribute to be deleted from the database. */
    private XMLDBAttribute _xmlDBAttribute;

}
