/*
 * 
 */
package ptdb.kernel.bl.search;

import ptdb.common.dto.SearchCriteria;

////////////////////////////////////////////////////////////////////////////
//// CommandSearcher


/**
 * The concrete searcher class which handles the search according to the
 *  XQuery command input by the user. 
 * 
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
public class CommandSearcher extends AbstractSearcher implements AbstractDBSearcher {

    /**
     * Construct the command searcher. 
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

    
    @Override
    protected void _search() {
        // to be implemented in the next requirement 
        
        // set the search done 
        _setSearchDone();
    }

    /////////////////////////////////////////////////////////////////////////
    ////       private variables                                        /////

    /**
     * Stores the XQuery command string input by the user. 
     */
    private String _commandSearchCriteria;

}
