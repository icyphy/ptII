/*
 *
 */
package ptdb.kernel.bl.search;

import java.util.ArrayList;
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
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 */
public class SearchResultBuffer extends Observable implements ResultHandler,
        Observer {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Get the results stored in the buffer.
     *
     * @return All the searched model results in this buffer.
     */
    public ArrayList<XMLDBModel> getResults() {

        // fetch the stored results
        ArrayList<XMLDBModel> returnedResults = _storedResults;

        // reset the stored results to null
        _storedResults = null;

        // return the stored results
        return returnedResults;
    }

    /**
     * Called by the other searcher to write the searched results
     * to this buffer.
     *
     * <p>When the results are written to the buffer in this method,
     * the buffer will notify its registered observers.
     * The SearchResultListener, which should have registered in this buffer
     *  will be called to notify.</p>
     *
     * @param modelResults The models results to be stored in this buffer.
     */
    public void handleResults(ArrayList<XMLDBModel> modelResults) {

        // only write and notify when the passed results contain results
        if (modelResults != null && modelResults.size() > 0) {
            // The results are added to the arraylist.

            if (_storedResults == null) {

                _storedResults = modelResults;

            } else {
                _storedResults.addAll(modelResults);
            }

            setChanged();

            // notify the observers
            this.notifyObservers();
        }
    }

    /**
     * Check whether the searching process has been canceled by the user.
     *
     * @return true - The search has been canceled by the user.<br>
     *             false - The search hasn't been canceled.
     */
    public boolean isSearchCancelled() {

        return _isSearchCancelled;
    }

    /**
     * Check whether the whole searching is done.
     *
     * @return true - the searching is done.<br>
     *          false - the searching is not done yet.
     */
    public boolean isWholeSearchDone() {
        return _isSearchDone;
    }

    /**
     * Set the DB connection for this result handler.  
     * 
     * @param connection The DBConnection instance to be set in this result 
     *  handler. 
     */
    public void setConnection(DBConnection connection) {
        _dbConnection = connection;

    }

    /**
     * Implement the update method from the Observer interface. 
     * 
     * @param o The object that this SearchResultBuffer is observing. 
     * @param arg The argument passed to this SearcResultBuffer. 
     */
    public void update(Observable o, Object arg) {
        _isSearchCancelled = true;
    }

    /**
     * Notify the search result buffer that the searching is done.
     * @exception DBConnectionException Thrown if the DB connection cannot be
     * obtained. 
     */
    public void wholeSearchDone() throws DBConnectionException {

        // close the connection
        if (_dbConnection != null) {
            _dbConnection.closeConnection();
        }

        _isSearchDone = true;

        setChanged();

        // tell all the registered observers that the searching is done
        this.notifyObservers();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private DBConnection _dbConnection;

    private boolean _isSearchCancelled = false;

    private boolean _isSearchDone = false;

    /**
     * The field that stores the buffered results.
     */
    private ArrayList<XMLDBModel> _storedResults;

}
