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

import ptdb.common.dto.DBGraphSearchCriteria;
import ptdb.common.dto.GraphSearchTask;
import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBExecutionException;

///////////////////////////////////////////////////////////////////
//// XQueryGraphSearcher

/**
 * Searcher for searching the models in the database according to graph pattern
 *  matching in the database.
 *
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
public class XQueryGraphSearcher extends GraphSearcher implements
AbstractDBSearcher {

    /**
     * Construct the XQueryGraphSearcher from the search criteria.
     *
     * @param dbGraphSearchCriteria The DB Graph search criteria to be set in
     *  this graph searcher.
     */
    public XQueryGraphSearcher(DBGraphSearchCriteria dbGraphSearchCriteria) {
        _dbGraphSearchCriteria = dbGraphSearchCriteria;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Perform the actual search in the database according the graph search
     * criteria.
     *
     * @exception DBExecutionException Thrown from the database if the error
     * occurs in the database execution.
     */

    @Override
    protected void _search() throws DBExecutionException {

        GraphSearchTask graphSearchTask = new GraphSearchTask();
        graphSearchTask.setGraphSearchCriteria(_dbGraphSearchCriteria);

        // Call the executeGraphSearchTask() method from the DBConnection class
        // and get the results returned by the executeGraphSearchTask() method.
        ArrayList<XMLDBModel> models = _dbConnection
                .executeGraphSearchTask(graphSearchTask);

        // Set the returned results to the _currentResults field.
        _currentResults = models;

        if (_currentResults == null) {
            // The db layer cannot perform the searching, so make the search
            // criteria not set.

            _dbGraphSearchCriteria = null;
        } else {
            // Pass the intermediate results found in this searcher.
            handleIntermediateResults(_currentResults, this);
        }

        // The search is done in this searcher, so mark this searcher passed.
        _pass();

    }

}
