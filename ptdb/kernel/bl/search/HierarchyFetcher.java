/*
 * 
 */
package ptdb.kernel.bl.search;

////////////////////////////////////////////////////////////////////////////
//// HierarchyFetcher

/**
 * Fetch the referencing hierarchy for the models.
 * 
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
public class HierarchyFetcher extends AbstractSearcher implements
        ResultHandler, AbstractDBSearcher {

    /////////////////////////////////////////////////////////////////////////
    ////        protected methods                                       /////

    @Override
    protected boolean _isSearchCriteriaSet() {

        return true;
    }

    /**
     * Handle the model results passed to this class. 
     * Go to the database to fetch all the referencing hierarchy 
     * for the passed results. 
     */
    @Override
    protected void _search() {
        // To be implemented in the next release 

        // create the FetchHierarchyTask

        // call the executeFetchHierarchyTask() method from the DBConnection class

        // get the results returned by the executeFetchHierarchyTask() method

        // set the returned results to the currentResults field

        // set the search done 
        _setSearchDone();

    }

}
