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
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptdb.kernel.database.DBConnection;

///////////////////////////////////////////////////////////////////
//// SearchResultBuffer

/**
 * The buffer for caching the search results from the previous searchers.
 *
 * <p>This class extends class Observable, to implement the Observer pattern.
 * </p>
 *
 * <p>It requires the SearchResultListener from the GUI layer to implement the
 * Observer interface, and register in this class to get notified when there
 * are results being added into the buffer.</p>
 *
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 */
public class SearchResultBuffer extends Observable implements ResultHandler,
        Observer {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Get the results stored in the buffer. After the results is read once
     * from this buffer, that set of results will be removed from the buffer.
     * Do not read the same set of results twice.
     *
     * @return All the searched model results in this buffer.
     */
    public ArrayList<XMLDBModel> getResults() {

        ArrayList<XMLDBModel> returnedResults = _storedResults;

        _storedResults = null;

        return returnedResults;
    }

    /**
     * Handle the intermediate results found and passed by the other searchers
     * in the searchers chain.
     *
     * <p>The intermediate results are currently stored in a HashMap to
     * distinguish the results passed by different searchers.  This class can
     * be extended later to fetch different intermediate searched results.</p>
     *
     * @param intermediateResults The intermediate results found in the other
     * searchers.
     * @param resultHandler The result handler searcher that finds and passes
     * the intermediate results.
     */

    @Override
    public void handleIntermediateResults(List<XMLDBModel> intermediateResults,
            ResultHandler resultHandler) {

        if (_intermediateResults == null) {
            _intermediateResults = new HashMap<ResultHandler, List<XMLDBModel>>();
        }

        if (_intermediateResults.containsKey(resultHandler)) {
            _intermediateResults.get(resultHandler).addAll(intermediateResults);
        } else {
            _intermediateResults.put(resultHandler, intermediateResults);
        }

    }

    /**
     * Called by the other searcher to write the searched results
     * to this buffer. If there is some other results that are already stored
     * in the buffer, the new results will appended to the existing results.
     *
     * <p>When the results are written to the buffer in this method,
     * the buffer will notify its registered observers.
     * The SearchResultListener, which should have registered in this buffer
     *  will be called to notify.</p>
     *
     * @param modelResults The models results to be stored in this buffer.
     */
    @Override
    public void handleResults(ArrayList<XMLDBModel> modelResults) {

        // Only write and notify when the passed results contain results.
        if (modelResults != null && modelResults.size() > 0) {
            // The results are added to the ArrayList.

            if (_storedResults == null) {

                _storedResults = modelResults;

            } else {
                _storedResults.addAll(modelResults);
            }

            setChanged();

            if (!_isSearchCancelled) {
                // Notify the observers.
                notifyObservers();
            }

        }
    }

    /**
     * Check whether the searching process has been canceled by the user.
     *
     * @return true - The search has been canceled by the user.<br>
     *             false - The search hasn't been canceled.
     */
    @Override
    public boolean isSearchCancelled() {

        return _isSearchCancelled;
    }

    /**
     * Check whether the all the searchers in the search chain have finished.
     *
     * @return true - the searching is done.<br>
     *          false - the searching is not done yet.
     */
    public boolean isWholeSearchDone() {
        return _isSearchDone;
    }

    /**
     * Handle the error models passed by the other searchers.
     *
     * <p>For now, the passed error models are just stored in the buffer, but
     * this class can be modified later to add more function for handling
     * passed error models.</p>
     *
     * @param errorModels The passed error models.
     */

    @Override
    public void passErrorModels(List<XMLDBModel> errorModels) {
        if (_errorModels == null) {
            _errorModels = errorModels;
        } else {
            _errorModels.addAll(errorModels);
        }

    }

    /**
     * Set the DB connection for this result handler.
     *
     * @param connection The DBConnection instance to be set in this result
     *  handler.
     */
    @Override
    public void setConnection(DBConnection connection) {
        _dbConnection = connection;

    }

    /**
     * Implement the update method from the Observer interface. This class is
     * assumed to only observe the SearchResultFrame class, so this update()
     * method is only called by the SearchResultFrame to cancel the search.  So
     *  this method only perform the operations to cancel the search operation.
     *
     * @param observable The object that this SearchResultBuffer is observing.
     * It is assumed that this SearchResultBuffer will only observe the
     * SearchResultFrame window.
     * @param argument The argument passed to this SearcResultBuffer.
     */
    @Override
    public void update(Observable observable, Object argument) {

        _isSearchCancelled = true;

        // Close the DBConnection if it has already been created and set.
        if (_dbConnection != null) {
            try {
                _dbConnection.closeConnection();
                _dbConnection = null;
            } catch (DBConnectionException e) {
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * Notify the search result buffer that the searching is done.
     *
     * @exception DBConnectionException Thrown if the DB connection cannot be
     * obtained.
     */
    @Override
    public void wholeSearchDone() throws DBConnectionException {

        // Close the connection.
        if (_dbConnection != null) {
            _dbConnection.closeConnection();
            _dbConnection = null;
        }

        _isSearchDone = true;

        setChanged();

        if (!_isSearchCancelled) {
            // Tell all the registered observers that the searching is done.
            notifyObservers();
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private DBConnection _dbConnection;

    /**
     * The place to store the intermediate results passed by different
     * searchers.
     */
    private HashMap<ResultHandler, List<XMLDBModel>> _intermediateResults;

    /**
     * The error models passed by the other searchers.
     */
    private List<XMLDBModel> _errorModels;

    /**
     * Mark whether the search has been cancelled.
     */
    private boolean _isSearchCancelled = false;

    /**
     * Mark whether the entire search is done.
     */
    private boolean _isSearchDone = false;

    /**
     * The field that stores the buffered results.
     */
    private ArrayList<XMLDBModel> _storedResults;

}
