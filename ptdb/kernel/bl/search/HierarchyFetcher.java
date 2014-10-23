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

import ptdb.common.dto.FetchHierarchyTask;
import ptdb.common.exception.DBExecutionException;

///////////////////////////////////////////////////////////////////
//// HierarchyFetcher

/**
 * Fetch the referencing hierarchy for the models.
 *
 * <p>This searcher does not perform any search in the database according
 * to the search criteria, but it fetches all the parents models of the results
 * found in the other searchers.</p>
 *
 * <p>This searcher does not actually perform the search according to any
 *  search criteria, but just fetch the reference hierarchy for the passed
 *  models.  So this searcher does not exist without other searchers.  Also,
 *  the results get from this searcher does not need to intersect with the
 *  results from the other searchers.</p>
 *
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
public class HierarchyFetcher extends AbstractSearcher implements
AbstractDBSearcher {

    /**
     * Construct the HierarchyFetcher object.
     */
    public HierarchyFetcher() {
        super();

        noIntersect();

        _isIndependent = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Checks whether the search criteria has been set in
     *  this searcher instance.  This searcher does not need any search
     *  criteria, since it just fetch the hierarchy, so the search criteria
     *  is always marked set for this searcher.
     *
     * @return true - if the search criteria has been set.<br>
     *         false - if the search criteria has not been set.
     */

    @Override
    protected boolean _isSearchCriteriaSet() {

        // There is no criteria need to be set in this searcher, so always
        // returns true.
        return true;
    }

    /**
     * Handle the model results passed to this class.
     * Go to the database to fetch all the referencing hierarchy
     * for the passed results.
     *
     * @exception DBExecutionException Thrown by the DBConnection when
     * unexpected problem happens during the execution of DB query tasks.
     */

    @Override
    protected void _search() throws DBExecutionException {

        FetchHierarchyTask fetchHierarchyTask = new FetchHierarchyTask();

        fetchHierarchyTask.setModelsList(_previousResults);

        // Call the executeFetchHierarchyTask() method from the
        // DBConnection class, and get the results returned by the
        // executeFetchHierarchyTask() method.
        _currentResults = _dbConnection
                .executeFetchHierarchyTask(fetchHierarchyTask);

    }

}
