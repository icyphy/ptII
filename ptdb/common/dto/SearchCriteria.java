/*
 * 
 */
package ptdb.common.dto;

import java.util.ArrayList;
import ptolemy.kernel.util.Attribute;

///////////////////////////////////////////////////////////////////////////////
//// SearchCriteria

/**
 * This class is the DTO which contains all the search criteria input by the 
 * user.
 * 
 * It is constructed by the GUI layer class to pass the search criteria.
 * 
 * @author Alek Wang
 * @version $Id$
 *
 */
public class SearchCriteria {

    ///////////////////////////////////////////////////////////////////////////
    ////////    public methods                                        ////////

    /**
     * @return the _attributes
     */
    public ArrayList<Attribute> getAttributes() {
        return _attributes;
    }

    /**
     * @return the _searchCommand
     */
    public String getSearchCommand() {
        return _searchCommand;
    }

    /**
     * @param _attributes the _attributes to set
     */
    public void setAttributes(ArrayList<Attribute> attributes) {
        this._attributes = attributes;
    }

    /**
     * @param _searchCommand the _searchCommand to set
     */
    public void setSearchCommand(String searchCommand) {
        this._searchCommand = searchCommand;
    }

    ///////////////////////////////////////////////////////////////////////////
    ////////    private variables                                     ////////

    /**
     * This list contains all the specified searching attributes.  
     */
    private ArrayList<Attribute> _attributes;

    /**
     * The string that represents the XQuery search command input by the user. 
     */
    private String _searchCommand;

}
