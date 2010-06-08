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
////AbstractSearcher

/**
 *  This class is the abstract parent class for all the concrete searcher 
 *  classes. 
 * 
 * It contains the common functionality of the searcher. 
 * 
 * @author Alek Wang
 * @version $Id$
 *
 */
public abstract class AbstractSearcher implements ResultHandler {

    /////////////////////////////////////////////////////////////////////////
    ////        public methods                                          /////

    /**
     * This method is used to handle the model results passed to this class, 
     * and handle the results according to the certain search criteria. 
     * 
     * @param modelResults - the results to be handled in this searcher
     * @throws DBExecutionException 
     */
    public void handleResults(ArrayList<XMLDBModel> modelResults)
            throws DBConnectionException, DBExecutionException {

        while (!(_isSearchDone())) {

            // check whether the search criteria has been set
            // skip the current searcher if it is not set with the search criteria
            if (this._isSearchCriteriaSet()) {

                if (this instanceof AbstractDBSearcher) {

                    //get the DB connection
                    this._dbConnection = DBConnectorFactory
                            .getSyncConnection(false);

                    this._search();

                    // close the DB connection
                    this._dbConnection.closeConnection();

                } else {
                    // for the searcher that does not require the db connection
                    // just execute the _search method
                    this._search();
                }

            }

            // send the results to the next result handler 
            _nextResultHandler.handleResults(_currentResults);

        }

    }

    /**
     * This method is used to check whether the searching process has been 
     * cancelled by the user. 
     * @return true - The search has been canceled by the user;
     *             false - The search hasn't been canceled. 
     */
    public boolean isSearchCancelled() {

        return _isSearchCancelled;
    }

    /**
     * This method is used to set the next result handler object to this searcher. 
     * This searcher will use the next handler to pass the results from this 
     * searcher. 
     * 
     * @param nextResultHandler the nextResultHandler to set to this handler 
     */
    public void setNextResultHandler(ResultHandler nextResultHandler) {

        this._nextResultHandler = nextResultHandler;
    }

    /////////////////////////////////////////////////////////////////////////
    ////        protected methods                                       /////

    /**
     * This method checks whether the search criteria has been set in this
     *  particular searcher 
     *  
     * @return true - if the search criteria has been set
     *         false - if the search criteria has not been set
     */
    protected abstract boolean _isSearchCriteriaSet();

    /**
     * @return the _searchDone
     */
    protected boolean _isSearchDone() {
        return _searchDone;
    }

    /**
     * Implemented by the concrete searchers to perform the actual search
     */
    protected abstract void _search() throws DBExecutionException;

    protected void _setSearchDone() {
        this._searchDone = true;
    }
    
    /////////////////////////////////////////////////////////////////////////
    ////       protected variables                                     /////
    
    /**
     * The arraylist instance which contains the currently found results. 
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
