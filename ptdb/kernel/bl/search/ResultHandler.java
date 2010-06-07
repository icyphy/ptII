/*
 * 
 */
package ptdb.kernel.bl.search;

import java.util.ArrayList;

import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;


////////////////////////////////////////////////////////////////////////////
////ResultHandler

/**
 * This class is the interface that indicates the implementing classes are 
 * the handlers for dealing with searched models results. 
 * 
 * @author Alek Wang
 * @version $Id$
 *
 */
public interface ResultHandler {

    /////////////////////////////////////////////////////////////////////////
    ////        public methods                                       /////

    /**
     * This method is implemented by the concrete classes to implement the 
     * function to handle the results of searched models.
     * 
     * @param modelResults The results to be handled
     * @throws DBConnectionException Database connection problem occurs
     */
    public void handleResults(ArrayList<XMLDBModel> modelResults)
            throws DBConnectionException, DBExecutionException;

    /**
     * This method is used to check whether the searching process has been 
     * cancelled by the user. 
     * @return true - The search has been canceled by the user;
     *             false - The search hasn't been canceled. 
     */
    public boolean isSearchCancelled();

}
