/*
 *
 */
package ptdb.kernel.bl.search;

import java.util.ArrayList;
import java.util.Iterator;

import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.common.util.DBConnectorFactory;
import ptdb.kernel.database.DBConnection;

///////////////////////////////////////////////////////////////////
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

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Handle the model results passed to this class,
     * and handle the results according to the certain search criteria.
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

        // store the passed results in the previous found results field
        _previousResults = modelResults;

        // check whether there is no match in the previous searchers
        // to decide to stop here or not
        if (_noMatch()) {
            // there is no matched result found in the previous searcher
            // so the searching is done

            wholeSearchDone();

        } else {
            // check whether the search criteria has been set
            // skip the current searcher if it is not set with the
            // search criteria
            if (this._isSearchCriteriaSet()) {

                while (!_isSearchDone()) {
                    if (this instanceof AbstractDBSearcher) {
                        try {
                            //get the DB connection
                            this._dbConnection = DBConnectorFactory
                                    .getSyncConnection(false);

                            _search();

                            // commit the DB connection
                            this._dbConnection.commitConnection();
                        } finally {
                            // close the connection anyway
                            if (this._dbConnection != null) {
                                this._dbConnection.closeConnection();
                            }

                        }

                        if (!(this instanceof HierarchyFetcher)
                                && !(this instanceof AttributeSearcher)) {
                            _toPassResults = _intersectResults(
                                    _previousResults, _currentResults);
                        } else {
                            _toPassResults = _currentResults;
                        }

                    } else {
                        // for the searcher that does not require the db
                        // connection just execute the _search method
                        this._search();

                        _toPassResults = _currentResults;
                    }

                    // send the results to the next result handler
                    _nextResultHandler.handleResults(_toPassResults);

                }

            } else {

                // Just pass the results to the next result handler
                _nextResultHandler.handleResults(modelResults);

            }

            // if there is no following searcher, just finish the searching
            if (_nextResultHandler instanceof SearchResultBuffer) {
                ((SearchResultBuffer) _nextResultHandler).wholeSearchDone();
            }
        }

    }

    /**
     * Check whether the searching process has been canceled by the user.
     *
     * @return true - The search has been canceled by the user.<br>
     *         false - The search hasn't been canceled.
     */
    public boolean isSearchCancelled() {

        return _isSearchCancelled;
    }

    /**
     * Set the next result handler object to this searcher.
     * This searcher will use the next handler to pass the results from this
     * searcher.
     *
     * @param nextResultHandler The next ResultHandler to set to this handler.
     */
    public void setNextResultHandler(ResultHandler nextResultHandler) {

        this._nextResultHandler = nextResultHandler;
    }

    /**
     * Set the previous searcher that does the searching before this searcher.
     *
     * @param searcher The previous searcher to set in this searcher.
     */
    public void setPreviousSeacher(AbstractSearcher searcher) {
        _previousSearcher = searcher;
    }

    /**
     * Notify the search result buffer that the searching is done.
     */
    public void wholeSearchDone() {

        // tell the next result handler that the searching is done
        if (_nextResultHandler != null) {
            _nextResultHandler.wholeSearchDone();
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Checks whether the search criteria has been set in this
     *  particular searcher.
     *
     * @return true - if the search criteria has been set.<br>
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

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /**
     * Contains the currently found results in this searcher.
     */
    protected ArrayList<XMLDBModel> _currentResults;

    protected DBConnection _dbConnection;

    /**
     * Contains the results found and passed by the previous searchers.
     */
    protected ArrayList<XMLDBModel> _previousResults;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private ArrayList<XMLDBModel> _intersectResults(
            ArrayList<XMLDBModel> previousResults,
            ArrayList<XMLDBModel> currentResults) {

        // If the previous result is empty, just return the results found
        // in this searcher.
        if (previousResults == null || previousResults.size() == 0) {
            return currentResults;
        }

        // If the current result is empty, just return the empty set.
        if (currentResults == null || currentResults.size() == 0) {
            return currentResults;
        }

        java.util.Hashtable<String, XMLDBModel> existingModels = new java.util.Hashtable<String, XMLDBModel>();
        ArrayList<XMLDBModel> returnedResults = new ArrayList<XMLDBModel>();

        for (Iterator iterator = previousResults.iterator(); iterator.hasNext();) {
            XMLDBModel xmldbModel = (XMLDBModel) iterator.next();
            existingModels.put(xmldbModel.getModelName(), xmldbModel);
        }

        for (Iterator iterator = currentResults.iterator(); iterator.hasNext();) {
            XMLDBModel xmldbModel = (XMLDBModel) iterator.next();
            if (existingModels.get(xmldbModel.getModelName()) != null) {
                returnedResults.add(xmldbModel);
            }
        }

        return returnedResults;
    }

    /**
     * Check whether the searching is done since no result has been matched.
     *
     * @return true - the searching is done and no matched result found.<br>
     *          false - this searcher is not done.
     */
    private boolean _noMatch() {

        if (_previousSearcher == null) {
            // this is the first searcher
            return false;
        }

        // there is no result from the previous searcher
        if (_previousResults == null || _previousResults.size() == 0) {

            if (_previousSearcher._isSearchCriteriaSet()) {
                // in the case that the previous searcher is set and no results
                // returned, this means there is no match in the previous
                // searchers.  So this searcher should be skipped also.

                return true;

            } else {
                // in the case that the previous searcher is not set,
                // then the searching should continue.
                return false;
            }
        }

        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private boolean _isSearchCancelled = false;

    /**
     * The next result handler to be called to pass the search results
     * from this result handler.
     */
    private ResultHandler _nextResultHandler;

    private AbstractSearcher _previousSearcher;

    private boolean _searchDone = false;

    /**
     * The results to pass to the next result handler.
     */
    private ArrayList<XMLDBModel> _toPassResults;

}
