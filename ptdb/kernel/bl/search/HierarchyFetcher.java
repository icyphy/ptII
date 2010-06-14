/*
 *
 */
package ptdb.kernel.bl.search;

import ptdb.common.dto.FetchHierarchyTask;
import ptdb.common.exception.DBExecutionException;

///////////////////////////////////////////////////////////////////
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

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    @Override
    protected boolean _isSearchCriteriaSet() {

        // There is no criteria need to be set in this searcher, so always
        // returns true.
        return true;
    }

    /**
     * Handle the model results passed to this class.
     * Go to the database to fetch all the referencing hierarchy
     * for the passed results.
     *
     * @exception DBExecutionException Thrown by the DBConnection when
     * unexpected problem happens during the execution of DB query tasks.
     */
    @Override
    protected void _search() throws DBExecutionException {

        // create the FetchHierarchyTask
        FetchHierarchyTask fetchHierarchyTask = new FetchHierarchyTask();

        fetchHierarchyTask.setModelsList(_previousResults);

        // call the executeFetchHierarchyTask() method from the DBConnection class
        // get the results returned by the executeFetchHierarchyTask() method
        _currentResults = _dbConnection
                .executeFetchHierarchyTask(fetchHierarchyTask);

        // set the search done
        _setSearchDone();

    }

}
