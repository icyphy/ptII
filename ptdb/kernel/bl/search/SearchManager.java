/**
 * 
 */
package ptdb.kernel.bl.search;

import java.util.ArrayList;
import ptdb.common.dto.SearchCriteria;
import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;


////////////////////////////////////////////////////////////////////////////
////SearchManager

/**
 * This class is the Business layer interface class which mainly handles the
 * search models function. It constructs the actual searcher classes according
 * to the search criteria.
 * 
 * @author Alek Wang
 * @version $Id$
 * 
 */
public class SearchManager {

    /////////////////////////////////////////////////////////////////////////
    ////        public methods                                       /////

    /**
     * This method is used to be called by the GUI layer class to pass the
     * search criteria. It is invoked by the Search GUI frame.
     * 
     * Algorithm: The search manager first creates all the searchers needed in
     * the search, and configure them in the order of attribute searcher ->
     * command searcher -> graph searcher.
     * 
     * Once all the results are searched and found, the results will be passed
     * to the hierarchy fetcher to fetch the referencing information.
     * 
     * After the hierarchy of the results are fetched, the results with
     * hierarchy will be writtent to the result buffer.
     * 
     * 
     * @param searchCriteria
     *            The SearchCriteria object which contains the search criterias
     *            input by the user.
     * @param searchResultBuffer
     *            The buffer which is used to store the search results. The
     *            search result buffer extends from the class Observable. The
     *            GUI layer has registered the listener for search result
     *            listener before passing the buffer to this method.
     * @throws DBExecutionException 
     */
    public void search(SearchCriteria searchCriteria,
            ResultHandler searchResultBuffer) throws DBConnectionException, DBExecutionException {

        // The search Manager will create all the searcher, and pass the search
        // criteria to them
        AttributeSearcher attributeSearcher = new AttributeSearcher(
                searchCriteria);

        CommandSearcher commandSearcher = new CommandSearcher(searchCriteria);

        ArrayList<GraphSearcher> graphSearchers = GraphSearcher
                .getGraphSearcher(searchCriteria);

        // create a the Hierarchy fetcher to fetch the hierarchy of the models
        HierarchyFetcher hierarchyFetcher = new HierarchyFetcher();

        // configure the searchers to set the next result handler
        attributeSearcher.setNextResultHandler(commandSearcher);
        commandSearcher.setNextResultHandler(graphSearchers.get(0));

        // if the pattern match graph searcher is also returned
        // set the pattern match searcher after DB graph searcher 
        if (graphSearchers.size() > 1) {
            graphSearchers.get(0).setNextResultHandler(graphSearchers.get(1));

            // set the hierarchyfetcher to the last searcher
            graphSearchers.get(1).setNextResultHandler(hierarchyFetcher);
        } else {
            graphSearchers.get(0).setNextResultHandler(hierarchyFetcher);
        }

        // assign the result handler to the fetcher
        hierarchyFetcher.setNextResultHandler(searchResultBuffer);

        // start the search
        attributeSearcher.handleResults(new ArrayList<XMLDBModel>());

    }

}
