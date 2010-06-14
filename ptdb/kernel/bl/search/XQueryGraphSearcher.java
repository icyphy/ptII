/*
 *
 */
package ptdb.kernel.bl.search;

import java.util.ArrayList;

import ptdb.common.dto.DBGraphSearchCriteria;
import ptdb.common.dto.GraphSearchTask;
import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBExecutionException;

///////////////////////////////////////////////////////////////////
//// XQueryGraphSearcher

/**
 * Searcher for searching the models according to graph pattern matching
 * in the database.
 *
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
public class XQueryGraphSearcher extends GraphSearcher implements
        AbstractDBSearcher {

    /**
     * Construct the XQueryGraphSearcher from the search criteria.
     *
     * @param dbGraphSearchCriteria The DB Graph search criteria to be set in
     *  this graph searcher.
     */
    public XQueryGraphSearcher(DBGraphSearchCriteria dbGraphSearchCriteria) {
        _dbGraphSearchCriteria = dbGraphSearchCriteria;
    }

    ///////////////////////////////////////////////////////////////////
    ////        protected methods                                       /////

    @Override
    protected void _search() throws DBExecutionException {

        // create the GraphSearchTask
        GraphSearchTask graphSearchTask = new GraphSearchTask();
        graphSearchTask.setGraphSearchCriteria(_dbGraphSearchCriteria);

        // call the executeGraphSearchTask() method from the DBConnection class
        // get the results returned by the executeGraphSearchTask() method
        ArrayList<XMLDBModel> models = _dbConnection
                .executeGraphSearchTask(graphSearchTask);

        // set the returned results to the _currentResults field
        _currentResults = models;

        // set the search done
        _setSearchDone();

    }

}
