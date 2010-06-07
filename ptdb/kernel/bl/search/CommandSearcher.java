/*
 * 
 */
package ptdb.kernel.bl.search;

import ptdb.common.dto.SearchCriteria;

////////////////////////////////////////////////////////////////////////////
////CommandSearcher

/**
 * This class is the concrete searcher class which handles the search according
 * to the XQuery command. 
 * 
 * @author Alek Wang
 * @version $Id$
 *
 */
public class CommandSearcher extends AbstractSearcher {

    /**
     * The constructor for this command searcher. 
     * 
     * @param searchCriteria The search criteria input by the user. 
     */
    public CommandSearcher(SearchCriteria searchCriteria) {

        this._commandSearchCriteria = searchCriteria.getSearchCommand();
    }

    /////////////////////////////////////////////////////////////////////////
    ////        protected methods                                       /////

    @Override
    protected boolean _isSearchCriteriaSet() {
        if (_commandSearchCriteria != null) {
            return true;
        }
        return false;
    }

    /**
     * This method performs the actual search for command search. 
     */
    @Override
    protected void _search() {
        // to be implemented in the next requirement 
        
        // set the search done 
        _setSearchDone();
    }

    /////////////////////////////////////////////////////////////////////////
    ////       private variables                                        /////

    /**
     * This field stores the XQuery command string input by the user. 
     */
    private String _commandSearchCriteria;

}
