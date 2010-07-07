/*
@Copyright (c) 2010 The Regents of the University of California.
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
/*
 *
 */
package ptdb.kernel.bl.search;

import java.util.ArrayList;

import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.common.util.DBConnectorFactory;
import ptdb.common.util.XMLDBModelManager;
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

        // check whether searching is canceled 
        if (isSearchCancelled()) {
            return;
        }

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

                if (this instanceof AbstractDBSearcher) {
                    try {
                        //get the DB connection
                        if (_dbConnection == null) {
                            _dbConnection = DBConnectorFactory
                                    .getSyncConnection(false);
                            // pass the db connection to the next searchers 
                            setConnection(_dbConnection);
                        }

                        _search();

                    } catch (DBExecutionException e) {
                        // Close the connection in case of exception.
                        _dbConnection.closeConnection();
                        throw e;
                    }

                    // Only when the searcher is not attribute searcher or 
                    // hierarchy searcher, the results searched from the 
                    // current searcher needs to intersect with the results 
                    // passed by the previous searchers.
                    if (!(this instanceof HierarchyFetcher)
                            && !(this instanceof AttributeSearcher)) {
                        if (_isSearchCriteriaSet()) {
                            _toPassResults = XMLDBModelManager
                                    .intersectResults(_previousResults,
                                            _currentResults);
                        } else {
                            _toPassResults = _previousResults;
                        }

                    } else {
                        _toPassResults = _currentResults;
                    }

                    // send the results to the next result handler
                    _nextResultHandler.handleResults(_toPassResults);
                } else {
                    // For the searcher that does not require the DB
                    // connection just execute the _search method.
                    this._search();

                }

            } else {
                pass();

                // Just pass the results to the next result handler.
                _nextResultHandler.handleResults(modelResults);

            }

            // If there is no following searcher, just finish the searching.
            if ((_nextResultHandler instanceof SearchResultBuffer)
                    && _previousSearcher.isPassed()) {
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

        return _nextResultHandler.isSearchCancelled();
    }

    /**
     * Check with this searcher has been passed the searching. 
     * 
     * @return true - if the searching has passed in this searcher.<br>
     *          false - if the searching has not passed in this searcher.
     */
    public boolean isPassed() {
        return _passed;
    }

    /**
     * Set the DB connection for this searcher.  
     * 
     * @param connection The DBConnection instance to be set in this searcher. 
     */
    public void setConnection(DBConnection connection) {
        _dbConnection = connection;

        // Pass the DB connection to the following searchers. 
        if (_nextResultHandler != null) {
            _nextResultHandler.setConnection(connection);
        }
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
     * @exception DBConnectionException Thrown from the database layer if the 
     *  database connection cannot be obtained. 
     */
    public void wholeSearchDone() throws DBConnectionException {

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

    protected void pass() {
        _passed = true;

    }

    /**
     * Implemented by the concrete searchers to perform the actual search.
     * 
     * @exception DBExecutionException Thrown from the database layer when 
     * there is error occurring during the searching. 
     * @exception DBConnectionException Thrown by the DBConnectorFactory
     *           when getting the DBConnection from it, which
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
     * The next result handler to be called to pass the search results
     * from this result handler.
     */
    protected ResultHandler _nextResultHandler;

    /**
     * Contains the results found and passed by the previous searchers.
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

                if (this instanceof HierarchyFetcher) {
                    return true;
                }

                // in the case that the previous searcher is not set,
                // then the searching should continue.
                return false;
            }
        }

        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private boolean _passed;

    private AbstractSearcher _previousSearcher;

    /**
     * The results to pass to the next result handler.
     */
    private ArrayList<XMLDBModel> _toPassResults;

}
