/*
 * 
 */
package ptdb.kernel.bl.search;

import java.util.ArrayList;

import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;

////////////////////////////////////////////////////////////////////////////
//// ResultHandler

/**
 * Indicates the implementing classes are 
 * the handlers for dealing with searched models results.
 * 
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
public interface ResultHandler {

    /////////////////////////////////////////////////////////////////////////
    ////        public methods                                       /////

    /**
     * To be implemented by the concrete classes to implement the 
     * function to handle the results of searched models.
     * 
     * @param modelResults The searched model results to be handled. 
     * @exception DBConnectionException Database connection problem occurs 
     *  during handling the results through searching in the database. 
     */
    public void handleResults(ArrayList<XMLDBModel> modelResults)
            throws DBConnectionException, DBExecutionException;

    /**
     * Check whether the searching process has been canceled by the user. 
     * 
     * @return true - The search has been canceled by the user.<br>
     *             false - The search hasn't been canceled. 
     */
    public boolean isSearchCancelled();

    /**
     * Notify the search result buffer that the searching is done. 
     */
    public void wholeSearchDone();

}
