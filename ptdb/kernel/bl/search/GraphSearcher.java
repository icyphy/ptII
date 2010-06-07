/*
 * 
 */
package ptdb.kernel.bl.search;

import java.util.ArrayList;
import ptdb.common.dto.SearchCriteria;

////////////////////////////////////////////////////////////////////////////
////GraphSearcher

/**
 * This class inherits from the AbstractSearch, and it contains the common
 * functions used in the graph search. 
 * 
 * @author Alek Wang
 * @version $Id$
 *
 */
public abstract class GraphSearcher extends AbstractSearcher {

    /////////////////////////////////////////////////////////////////////////
    ////        public methods                                       /////

    /**
     * This is a factory which returns the concrete GraphSearcher objects 
     * according to the search criteria from the user. 
     * 
     * @param searchCriteria The search criteria input by the user
     * @return The list that contains the required graph searchers, created 
     *  according to the search criteria.  The searcher in the returned list 
     *  should be used in their returned order. 
     */
    public static ArrayList<GraphSearcher> getGraphSearcher(
            SearchCriteria searchCriteria) {

        ArrayList<GraphSearcher> graphSearchers = new ArrayList<GraphSearcher>();

        // this method will be implemented to add more logic in the 
        // later release 
        XQueryGraphSearcher xQueryGraphSearcher = new XQueryGraphSearcher();

        graphSearchers.add(xQueryGraphSearcher);

        return graphSearchers;
    }

    /////////////////////////////////////////////////////////////////////////
    ////        protected methods                                       /////

    @Override
    protected boolean _isSearchCriteriaSet() {
        // To be implemented in the next release 

        return false;
    }

}
