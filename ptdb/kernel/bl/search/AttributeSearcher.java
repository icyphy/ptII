/*
 * 
 */
package ptdb.kernel.bl.search;

import java.util.ArrayList;

import ptdb.common.dto.GetAttributesTask;
import ptdb.common.dto.SearchCriteria;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptolemy.kernel.util.Attribute;

////////////////////////////////////////////////////////////////////////////
////AttributeSearcher

/**
 * This class is the concrete searcher to handle the search by attributes
 *  criteria. 
 * 
 * @author Alek Wang
 * @version $Id$
 *
 */
public class AttributeSearcher extends AbstractSearcher {

    /**
     * The constructor of this Searcher. 
     * 
     * @param searchCriteria The search criteria that input by the user. 
     */
    public AttributeSearcher(SearchCriteria searchCriteria) {

        this._attributesCriteria = searchCriteria.getAttributes();
    }

    /////////////////////////////////////////////////////////////////////////
    ////        protected methods                                       /////

    /**
     * This method checks whether the attribute search criteria has been set in
     *  this attribute searcher instance. 
     *  
     * @return true - if the search criteria has been set
     *         false - if the search criteria has not been set
     */
    @Override
    protected boolean _isSearchCriteriaSet() {

        if (_attributesCriteria == null) {
            return false;
        }

        return true;

    }

    /**
     * The method to perform the actual search for the attributes. 
     * @throws DBExecutionException
     * problem happens. 
     */
    @Override
    protected void _search() throws DBExecutionException  {

        //create the AttributeSearchTask
        GetAttributesTask attributeSearchTask = new GetAttributesTask();

        // TODO set the attribute task value

        // call the executeAttributeTask() method from the DBConnection class
        // set the returned results
        this._currentResults = this._dbConnection
                .executeGetAttributesTask(attributeSearchTask);

        // set the search done 
        _setSearchDone();

    }

    /////////////////////////////////////////////////////////////////////////
    ////       private variables                                        /////

    /**
     * This field contains the search criteria of attributes.
     */
    private ArrayList<Attribute> _attributesCriteria;

}
