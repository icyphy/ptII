/*
@Copyright (c) 2010-2014 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

                                                PT_COPYRIGHT_VERSION_2
                                                COPYRIGHTENDKEY


 */

package ptdb.kernel.bl.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.common.util.DBConnectorFactory;
import ptdb.common.util.Utilities;
import ptdb.kernel.database.DBConnection;

///////////////////////////////////////////////////////////////////
//// AbstractSearcher

/**
 * The abstract parent class for all the concrete searcher classes.
 *
 * <p>The entire search module is implemented in a pipe and filter chain.
 * Each concrete searcher in the chain is a concrete class of this
 * AbstractSearcher class, and they are configured to search on different set
 * of search criteria that input by the user. </p>
 *
 * <p>The actual combination of searchers chain is configured in
 * SearchManager.</p>
 *
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
public abstract class AbstractSearcher implements ResultHandler {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Handle the intermediate results that found and passed by the other
     * searcher.
     *
     * <p>The searcher normally passes the intermediate results to the next
     *  result handler.</p>
     *
     * @param intermediateResults The intermediate results to be handled.
     * @param resultHandler The result handler that found and pass these
     * results.
     */

    @Override
    public void handleIntermediateResults(List<XMLDBModel> intermediateResults,
            ResultHandler resultHandler) {

        if (_nextResultHandler != null) {
            _nextResultHandler.handleIntermediateResults(intermediateResults,
                    resultHandler);
        }

    }

    /**
     * Handle the model results passed to this class, and handle the results
     *  according to the certain search criteria. The
     * search criteria is passed to the concrete searchers when they are
     * constructed.
     *
     * @param modelResults The results to be handled in this searcher.
     * @exception DBConnectionException Thrown by the DBConnectorFactory
     *           if getting the DBConnection from it, which
     *          indicates that the DBConnection cannot be obtained.
     * @exception DBExecutionException Happens in the execution
     *          of DB tasks, and is thrown by the concrete searcher if they
     *          are performing the actual searching in the database.
     */
    @Override
    public void handleResults(ArrayList<XMLDBModel> modelResults)
            throws DBConnectionException, DBExecutionException {

        // Check whether searching is canceled, and stop the search is it is
        // canceled.
        if (isSearchCancelled()) {
            return;
        }

        // Store the passed results in the previous found results field.
        _previousResults = modelResults;

        // Sort the previous fetched results.
        if (_previousResults != null && _previousResults.size() > 0) {
            Collections.sort(_previousResults);
        }

        // Check whether there is no match in the previous searchers
        // to decide to stop here or not.
        if (_noMatch()) {
            // There is no matched result found in the previous searcher,
            // so the searching is done.

            wholeSearchDone();

        } else {
            // Check whether the search criteria has been set,
            // skip the current searcher if it is not set with the
            // search criteria.
            if (_isSearchCriteriaSet()) {

                if (this instanceof AbstractDBSearcher) {
                    try {
                        // Get the DB connection.
                        if (_dbConnection == null) {
                            _dbConnection = DBConnectorFactory
                                    .getSyncConnection(false);
                            // Pass the db connection to the next searchers.
                            setConnection(_dbConnection);
                        }

                        // Perform the actual searcher.
                        _search();

                    } catch (DBExecutionException e) {
                        // Close the connection in case of exception.

                        _dbConnection.closeConnection();
                        throw e;
                    }

                    // Only when the searcher is not the first searcher and it
                    // needs intersect the results that it gets with the passed
                    // results from the previous searchers, the results found
                    // from the current searcher needs to intersect with the
                    // results passed by the previous searchers.

                    if (!_isFirstSearcher() && _isIntersectNeeded()) {

                        // FIXME to delete later
                        //                    if (!(this instanceof HierarchyFetcher)
                        //                            && !(this instanceof AttributeSearcher)) {
                        if (_isSearchCriteriaSet()) {

                            _toPassResults = Utilities.intersectResults(
                                    _previousResults, _currentResults);

                        } else {
                            _toPassResults = _previousResults;
                        }

                    } else {
                        _toPassResults = _currentResults;
                    }

                    // Send the results to the next result handler.
                    _nextResultHandler.handleResults(_toPassResults);

                } else {
                    // For the searcher that does not require the DB
                    // connection, just execute the _search method.
                    _search();

                }

            } else {
                // Since the search criteria is not set for this searcher, just
                // pass this searcher and mark it passed.
                _pass();

                // Just pass the results to the next result handler.
                _nextResultHandler.handleResults(modelResults);

            }

            // If there is no following searcher, just finish the searching.
            // FIXME to delete later
            //            if ((_nextResultHandler instanceof SearchResultBuffer)
            //                    && _previousSearcher.isPassed()) {

            if (_isLastSeacher() && _previousSearcher.isPassed()) {

                _nextResultHandler.wholeSearchDone();
            }
        }

    }

    /**
     * Check whether the search process has been canceled by the user.
     *
     * @return true - The search has been canceled by the user.<br>
     *         false - The search hasn't been canceled.
     */
    @Override
    public boolean isSearchCancelled() {

        if (_nextResultHandler != null) {
            return _nextResultHandler.isSearchCancelled();
        } else {
            return true;
        }

    }

    /**
     * Check with this searcher has been passed the searching. Some of the
     * searcher may require multiple rounds of searching in a loop, and the
     * partial results will be passed to the next result handler during the
     * execution loop.  This method indicates whether the current searcher has
     * finished the entire searching that it wants to do.
     *
     * @return true - if the searching has passed in this searcher.<br>
     *          false - if the searching has not passed in this searcher.
     */
    public boolean isPassed() {
        return _passed;
    }

    /**
     * Mark this searcher as it does not need to intersect the results it find
     * with the results passed by other searchers.
     */
    public void noIntersect() {
        _needIntersect = false;
    }

    /**
     * In the case of getting some models that cannot be parsed or contains
     * some error, use this method to pass these error models to store for
     * error handling.
     *
     * @param errorModels The error models.
     */

    @Override
    public void passErrorModels(List<XMLDBModel> errorModels) {
        if (_nextResultHandler != null) {
            _nextResultHandler.passErrorModels(errorModels);
        }

    }

    /**
     * Set the DB connection for this searcher.
     *
     * @param connection The DBConnection instance to be set in this searcher.
     */
    @Override
    public void setConnection(DBConnection connection) {
        _dbConnection = connection;

        // Pass the DB connection to the following searchers.
        if (_nextResultHandler != null) {
            _nextResultHandler.setConnection(connection);
        }
    }

    /**
     * Mark this searcher as the first searcher in the chain.
     */
    public void setFirstSearcher() {
        _isFirstSearcher = true;
    }

    /**
     * Mark this searcher as the last searcher in the chain.
     */
    public void setLastSearcher() {
        _isLastSearcher = true;
    }

    /**
     * Set the next result handler object to this searcher.
     * This searcher will use the next handler to pass the results from this
     * searcher.
     *
     * @param nextResultHandler The next ResultHandler to set to this handler.
     */
    public void setNextResultHandler(ResultHandler nextResultHandler) {

        _nextResultHandler = nextResultHandler;
    }

    /**
     * Set the previous searcher that does the searching before this searcher.
     *
     * @param previousSearcher The previous searcher to set in this searcher.
     */
    public void setPreviousSeacher(AbstractSearcher previousSearcher) {
        _previousSearcher = previousSearcher;
    }

    /**
     * Notify the search result buffer that the searching is done. The whole
     * search means all the system's entire search process for a given complete
     * search criteria.
     *
     * @exception DBConnectionException Thrown from the database layer if the
     *  database connection cannot be obtained.
     */
    @Override
    public void wholeSearchDone() throws DBConnectionException {

        // tell the next result handler that the searching is done
        if (_nextResultHandler != null) {
            _nextResultHandler.wholeSearchDone();
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * In case of getting model result with error or cannot be parsed, use this
     * method to add the error model to the stored error models list.
     *
     * @param errorModel The error model to be added to the list.
     */
    protected void _addErrorModel(XMLDBModel errorModel) {
        if (_errorModels == null) {
            _errorModels = new ArrayList<XMLDBModel>();
        }

        _errorModels.add(errorModel);
    }

    /**
     * Check whether this searcher is the first searcher in the chain.
     *
     * @return true - this searcher is the first searcher in the chain.<br>
     *          false - this searcher is not the first searcher in the chain.
     */
    protected boolean _isFirstSearcher() {
        return _isFirstSearcher;
    }

    /**
     * Check whether this searcher is independent to exist without other
     *   searchers.
     *
     * @return true - this searcher is independent.<br>
     *          false - this searcher is not independent.
     */
    protected boolean _isIndependent() {
        return _isIndependent;
    }

    /**
     * Check with this searcher needs to intersect the results that it finds
     * with the passed results from the other searchers.
     *
     * @return true - this searcher needs to intersect the results with other
     * searchers.<br>
     *          false - this searcher does not need to intersect the results
     *          with other searchers.
     */
    protected boolean _isIntersectNeeded() {
        return _needIntersect;
    }

    /**
     * Check whether this searcher is the last searcher in the chain.
     *
     * @return true - this searcher is the last searcher in the chain.<br>
     *          false - this searcher is not the last searcher in the chain.
     */
    protected boolean _isLastSeacher() {
        return _isLastSearcher;
    }

    /**
     * Checks whether the search criteria has been set in this
     *  particular searcher.
     *
     * @return true - if the search criteria has been set.<br>
     *         false - if the search criteria has not been set.
     */
    protected abstract boolean _isSearchCriteriaSet();

    /**
     * Mark the search is done in this searcher.
     *
     * @see #isPassed()
     */
    protected void _pass() {
        _passed = true;

    }

    /**
     * Perform the actual search.
     *
     * @exception DBExecutionException Thrown from the database layer if
     * there is error occurring during the search.
     * @exception DBConnectionException Thrown by the DBConnectorFactory
     *           if getting the DBConnection from it, which
     *          indicates that the DBConnection cannot be obtained.
     */
    protected abstract void _search() throws DBExecutionException,
            DBConnectionException;

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /**
     * Contains the currently found results in this searcher.
     */
    protected ArrayList<XMLDBModel> _currentResults;

    /**
     * Connection to the xml database.
     */
    protected DBConnection _dbConnection;

    /**
     * The models contains error or cannot be parsed.
     */
    protected List<XMLDBModel> _errorModels;

    /**
     * Indicate whether the searcher existing depends on the existing of other
     * searchers. For example, the HierarchyFetcher needs to be configured with
     * other dependent searcher, otherwise it is useless.
     */
    protected boolean _isIndependent = true;

    /**
     * The next result handler to be called to pass the search results
     * from this result handler.
     */
    protected ResultHandler _nextResultHandler;

    /**
     * Results found and passed by the previous searchers.
     */
    protected ArrayList<XMLDBModel> _previousResults;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Check whether the searching is done since no result has been matched.
     *
     * @return true - the searching is done and no matched result found.<br>
     *          false - this searcher is not done.
     */
    private boolean _noMatch() {

        if (_previousSearcher == null) {
            // This is the first searcher.
            return false;
        }

        // There is no result from the previous searcher.
        if (_previousResults == null || _previousResults.size() == 0) {

            if (_previousSearcher._isSearchCriteriaSet()) {
                // In the case that the previous searcher is set and no results
                // returned, this means there is no match in the previous
                // searchers.  So this searcher should be skipped also.

                return true;

            } else {
                // If this is the last searcher and not an independent searcher,
                // the search is done.
                if (_isLastSeacher() && !_isIndependent()) {
                    return true;
                }

                // In the case that the previous searcher is not set,
                // then the searching should continue.
                return false;
            }
        }

        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * Whether searcher is the first searcher in the chain, and it default to
     * be not the first searcher.
     */
    private boolean _isFirstSearcher = false;

    /**
     * Indicate whether this searcher is the last searcher in the chain, and it
     * default is the not the last searcher in the chain.
     */
    private boolean _isLastSearcher = false;

    /**
     * Indicate whether this searcher needs to intersect its results set with
     * results found in other searchers. The default value is that every
     * searcher will need to intersect results with other searchers.
     *
     * @see #_isIntersectNeeded()
     */
    private boolean _needIntersect = true;

    /**
     * Indicate whether the search in this searcher has been passed.
     *
     * @see #isPassed()
     */
    private boolean _passed = false;

    /**
     * The previous searcher that is configured in the chain of this searcher.
     */
    private AbstractSearcher _previousSearcher;

    /**
     * The results to pass to the next result handler.
     */
    private ArrayList<XMLDBModel> _toPassResults;

}
