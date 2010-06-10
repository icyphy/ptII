/*
 * 
 */
package ptdb.kernel.bl.search;

import java.util.ArrayList;
import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.common.util.DBConnectorFactory;
import ptdb.kernel.database.DBConnection;

////////////////////////////////////////////////////////////////////////////
//// AbstractSearcher

/**
 * The abstract parent class for all the concrete searcher classes. 
 * 
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
public abstract class AbstractSearcher implements ResultHandler {

    /////////////////////////////////////////////////////////////////////////
    ////        public methods                                          /////

    /**
     * <p>Handle the model results passed to this class, 
     * and handle the results according to the certain search criteria. </p>
     * 
     * @param modelResults The results to be handled in this searcher. 
     * @exception DBConnectionException Thrown by the DBConnectorFactory
     *           when getting the DBConnection from it, which
     *          indicates that the DBConnection cannot be obtained. 
     * @exception DBExecutionException Happens in the execution 
     *          of DB tasks, and is thrown by the concrete searcher when they
     *          are performing the actual searching in the database. 
     */
    public void handleResults(ArrayList<XMLDBModel> modelResults)
            throws DBConnectionException, DBExecutionException {

        // check whether the search criteria has been set
        // skip the current searcher if it is not set with the search criteria
        if (this._isSearchCriteriaSet()) {

            while (!_isSearchDone()) {
                if (this instanceof AbstractDBSearcher) {

                    //get the DB connection
                    this._dbConnection = DBConnectorFactory
                            .getSyncConnection(false);

                    _search();

                    // close the DB connection
                    this._dbConnection.closeConnection();

                } else {
                    // for the searcher that does not require the db connection
                    // just execute the _search method
                    this._search();
                }

                // send the results to the next result handler 
                _nextResultHandler.handleResults(_currentResults);

            }
        }
    }

    /**
     * Check whether the searching process has been canceled by the user. 
     * 
     * @return true - The search has been canceled by the user<br>
     *         false - The search hasn't been canceled. 
     */
    public boolean isSearchCancelled() {

        return _isSearchCancelled;
    }

    /**
     * <p>Set the next result handler object to this searcher. 
     * This searcher will use the next handler to pass the results from this 
     * searcher. </p>
     * 
     * @param nextResultHandler The next ResultHandler to set to this handler.
     */
    public void setNextResultHandler(ResultHandler nextResultHandler) {

        this._nextResultHandler = nextResultHandler;
    }

    /////////////////////////////////////////////////////////////////////////
    ////        protected methods                                       /////

    /**
     * <p>Checks whether the search criteria has been set in this
     *  particular searcher. </p>
     *  
     * @return true - if the search criteria has been set. <br>
     *         false - if the search criteria has not been set.
     */
    protected abstract boolean _isSearchCriteriaSet();

    protected boolean _isSearchDone() {
        return _searchDone;
    }

    /**
     * Implemented by the concrete searchers to perform the actual search. 
     */
    protected abstract void _search() throws DBExecutionException;

    protected void _setSearchDone() {

        this._searchDone = true;
    }

    /////////////////////////////////////////////////////////////////////////
    ////       protected variables                                     /////

    /**
     * Contains the currently found results. 
     */
    protected ArrayList<XMLDBModel> _currentResults;

    protected DBConnection _dbConnection;

    /////////////////////////////////////////////////////////////////////////
    ////       private variables                                        /////

    private boolean _isSearchCancelled = false;

    /**
     * The next result handler to be called to pass the search results 
     * from this result handler. 
     */
    private ResultHandler _nextResultHandler;

    private boolean _searchDone = false;

}
