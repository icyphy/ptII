/*
 * 
 */
package ptdb.kernel.bl.search;

import java.util.ArrayList;
import java.util.Observable;

import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;


////////////////////////////////////////////////////////////////////////////
////SearchResultBuffer

/**
 * This class is the buffer for caching the search results from the previous 
 *  searchers. 
 * 
 * This class extends class Observable, to implement the Observer pattern.  
 * 
 * It requires the SearchResultListener from the GUI layer to implement the 
 * Observer interface, and register in this class to observe when there are
 * results be added into the buffer. 
 * 
 * @author Alek Wang
 * @version $Id$
 *
 */
public class SearchResultBuffer extends Observable implements ResultHandler {

    /////////////////////////////////////////////////////////////////////////
    ////        public methods                                       /////

    /**
     * This method is used by the GUI to get the results stored in the buffer.
     * 
     * @return The ArrayList that contains all the results in this buffer. 
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
     * This method called by the other searcher to write the searched results 
     * to this buffer.  
     * 
     * When the results are written to the buffer in this method,
     * the buffer will call its registered observer¡¯s update() method. 
     * The SearchResultListener, which should have registered in this buffer
     *  will be called to notify.  
     * 
     */
    public void handleResults(ArrayList<XMLDBModel> modelResults)
            throws DBConnectionException {

        // The results are added to the arraylist

        if (_storedResults == null) {

            _storedResults = modelResults;

        } else {
            _storedResults.addAll(modelResults);
        }

        // notify the observers
        this.notifyObservers();

    }

    /**
     * This method is used to check whether the searching process has been 
     * canceled by the user. 
     * 
     * @return true - The search has been canceled by the user;
     *             false - The search hasn't been canceled. 
     */
    public boolean isSearchCancelled() {

        return _isSearchCancelled;
    }

    /////////////////////////////////////////////////////////////////////////
    ////        private variables                                       /////

    /**
     * the field that stores the buffered results
     */
    private ArrayList<XMLDBModel> _storedResults;

    private boolean _isSearchCancelled = false;

}
