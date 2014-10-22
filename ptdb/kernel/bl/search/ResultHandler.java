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
import java.util.List;

import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.kernel.database.DBConnection;

///////////////////////////////////////////////////////////////////
//// ResultHandler

/**
 * Handles the results from the searched models, according to different
 * implementation of the actual handling function.
 *
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
public interface ResultHandler {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Handle the intermediate results that got from a certain ResultHandler
     * instance.
     *
     * @param intermediateResults The intermediate results set to be handled
     * in this ResultHandler.
     * @param resultHandler The ResultHandler instance that gets the passed
     * intermediate results.
     */
    public void handleIntermediateResults(List<XMLDBModel> intermediateResults,
            ResultHandler resultHandler);

    /**
     * To be implemented by the concrete classes to implement the
     * function to handle the results of searched models.
     *
     * @param modelResults The searched model results to be handled.
     * @exception DBConnectionException Database connection problem occurs
     *  during handling the results through searching in the database.
     * @exception DBExecutionException Thrown from the database layer when
     * there is error occurring in the execution.
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
     * In the case of getting some models that cannot be parsed or contains
     * some error, use this method to pass these error models to store for
     * error handling.
     *
     * @param errorModels The error models.
     */
    public void passErrorModels(List<XMLDBModel> errorModels);

    /**
     * Set the DB connection for this result handler.
     *
     * @param connection The DBConnection instance to be set in this result
     *  handler.
     */
    public void setConnection(DBConnection connection);

    /**
     * Notify the search result buffer that the searching is done.
     * @exception DBConnectionException Thrown if the DB connection cannot be
     * obtained.
     */
    public void wholeSearchDone() throws DBConnectionException;

}
