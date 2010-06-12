/*
 * 
 */
package ptdb.kernel.bl.search;

import java.util.ArrayList;
import java.util.Observable;

import ptdb.common.dto.XMLDBModel;

////////////////////////////////////////////////////////////////////////////
//// SearchResultBuffer

/**
 * The buffer for caching the search results from the previous searchers.
 * 
 * <p>This class extends class Observable, to implement the Observer pattern.
 * 
 * It requires the SearchResultListener from the GUI layer to implement the 
 * Observer interface, and register in this class to get notified when there are
 * results being added into the buffer.</p>
 * 
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
public class SearchResultBuffer extends Observable implements ResultHandler {

    //////////////////////////////////////////////////////////////////////
    ////        public methods                                       /////

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
            // The results are added to the arraylist

            if (_storedResults == null) {

                _storedResults = modelResults;

            } else {
                _storedResults.addAll(modelResults);
            }

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
     * Notify the search result buffer that the searching is done. 
     */
    public void wholeSearchDone() {

        _isSearchDone = true;

        // tell all the registered observers that the searching is done
        this.notifyObservers();
    }

    /////////////////////////////////////////////////////////////////////////
    ////        private variables                                       /////

    /**
     * The field that stores the buffered results. 
     */
    private ArrayList<XMLDBModel> _storedResults;

    private boolean _isSearchCancelled = false;

    private boolean _isSearchDone = false;
}
