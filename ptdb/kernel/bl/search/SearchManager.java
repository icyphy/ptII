/*
 *
 */
package ptdb.kernel.bl.search;

import java.util.ArrayList;

import ptdb.common.dto.SearchCriteria;
import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;

///////////////////////////////////////////////////////////////////
//// SearchManager

/**
 * Business layer interface class that mainly handles the search models
 * function. It constructs and configures the actual searcher classes according
 * to the search criteria, and triggers the searching.
 *
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
public class SearchManager {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * This method is used to be called by the GUI layer class to pass the
     * search criteria. It is invoked by the Search GUI frame.
     *
     * <p>Algorithm: The search manager first creates all the searchers needed
     * in the search, and configures them in the order of attribute searcher ->
     * command searcher -> graph searcher.<br>
     *
     * Once all the results are searched and found, the results will be passed
     * to the hierarchy fetcher to fetch the referencing information.<br>
     *
     * After the hierarchy of the results are fetched, the results will be
     * written to the result buffer together with the hierarchy. </p>
     *
     *
     * @param searchCriteria The search criteria input by the user.
     * @param searchResultBuffer The buffer that is used to store the search
     *  results. The search result buffer extends  the class Observable. The
     *            GUI layer has registered some listener for checking search
     *            result before passing the buffer to this method.
     * @exception DBConnectionException Thrown from the database layer when the
     *  database layer fails to create a connection to the database.
     * @exception DBExecutionException Thrown from the database layer when the
     *  database layer fails to execute the searching in the database.
     */
    public void search(SearchCriteria searchCriteria,
            ResultHandler searchResultBuffer) throws DBConnectionException,
            DBExecutionException {

        // The search Manager will create all the searcher, and pass the search
        // criteria to them.
        AttributeSearcher attributeSearcher = new AttributeSearcher(
                searchCriteria);

        CommandSearcher commandSearcher = new CommandSearcher(searchCriteria);

        ArrayList<GraphSearcher> graphSearchers = GraphSearcher
                .getGraphSearcher(searchCriteria);

        // create a the Hierarchy fetcher to fetch the hierarchy of the models
        HierarchyFetcher hierarchyFetcher = new HierarchyFetcher();

        // configure the searchers to set the next result handlers
        // also configure the searchers to set the previous searcher
        attributeSearcher.setNextResultHandler(commandSearcher);
        commandSearcher.setPreviousSeacher(attributeSearcher);

        commandSearcher.setNextResultHandler(graphSearchers.get(0));
        graphSearchers.get(0).setPreviousSeacher(commandSearcher);

        // set the pattern match searcher after DB graph searcher
        graphSearchers.get(0).setNextResultHandler(graphSearchers.get(1));
        graphSearchers.get(1).setPreviousSeacher(graphSearchers.get(0));

        // set the hierarchy fetcher to the last searcher
        graphSearchers.get(1).setNextResultHandler(hierarchyFetcher);
        hierarchyFetcher.setPreviousSeacher(graphSearchers.get(1));

        // assign the result handler to the fetcher
        hierarchyFetcher.setNextResultHandler(searchResultBuffer);

        // start the search
        attributeSearcher.handleResults(new ArrayList<XMLDBModel>());

    }

}
