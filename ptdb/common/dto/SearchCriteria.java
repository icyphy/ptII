/*
 *
 */
package ptdb.common.dto;

import java.util.ArrayList;

import ptolemy.kernel.util.Attribute;

///////////////////////////////////////////////////////////////////
//// SearchCriteria

/**
 * <p>DTO (Data Transfer Object) which contains all the search criteria input by
 * the user.  It is constructed by the GUI layer class to pass the search
 * criteria. <br>
 *
 * There are three categories of criteria in this class:<br>
 * 1. Some attributes information of the Ptolemy model that the user wants to
 * search on.<br>
 * 2. The XQuery command to be executed directly in the XML database for
 * searching. <br>
 * 3. The pattern to be matched to search the model. </p>
 *
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
public class SearchCriteria {

    ///////////////////////////////////////////////////////////////////
    ////        public methods                                     ////

    /**
     * Get the attributes criteria from the search criteria.
     * 
     * @return All the attributes input by the user as the search criteria.
     * @see #setAttributes
     */
    public ArrayList<Attribute> getAttributes() {
        return _attributes;
    }

    /**
     * Get the graph search criteria from the search criteria.
     * 
     * @return The graph search criteria input by the user.
     * @see #setDBGraphSearchCriteria
     */
    public DBGraphSearchCriteria getDBGraphSearchCriteria() {
        return _dbGraphSearchCriteria;
    }

    
    /**
     * Get the name of the model from the search criteria. 
     * 
     * @return The model name search criteria. 
     */
    public String getModelName() {
        return _modelName;
    }
    
    
    /**
     * Get the search command criteria from the search criteria.
     *
     * @return The XQuery search command input by the user.
     * @see #setSearchCommand
     */
    public String getSearchCommand() {
        return _searchCommand;
    }

    /**
     * Set the attribute criteria for the search criteria.
     * 
     * @param attributes The attributes input by the user to set in the
     * search criteria.
     * @see #getAttributes
     */
    public void setAttributes(ArrayList<Attribute> attributes) {
        this._attributes = attributes;
    }

    /**
     * Set the graph search criteria for the search criteria.
     * 
     * @param dbGraphSearchCriteria The graph search criteria input by the user
     * to set in the search criteria.
     * @see #getDBGraphSearchCriteria
     */
    public void setDBGraphSearchCriteria(
            DBGraphSearchCriteria dbGraphSearchCriteria) {
        _dbGraphSearchCriteria = dbGraphSearchCriteria;
    }
    
    
    /**
     * Set the name of the model for the search criteria. 
     * 
     * @param modelName The name of the model to be set in the search criteria.
     */
    public void setModelName(String modelName) {
        _modelName = modelName;
    }

    /**
     * Set the XQuery search command for the search criteria.
     * 
     * @param searchCommand The XQuery search command to set in the search
     * criteria.
     * @see #getSearchCommand
     */
    public void setSearchCommand(String searchCommand) {
        this._searchCommand = searchCommand;
    }

    ///////////////////////////////////////////////////////////////////
    ////    private variables                                      ////

    /**
     * Contains all the specified searching attributes.
     */
    private ArrayList<Attribute> _attributes;

    private DBGraphSearchCriteria _dbGraphSearchCriteria;
    
    /**
     * The name of the model as the search criteria. 
     */
    private String _modelName;

    /**
     * Represents the XQuery search command input by the user.
     */
    private String _searchCommand;

}
