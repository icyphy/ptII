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

import ptdb.common.dto.ModelNameSearchTask;
import ptdb.common.dto.SearchCriteria;
import ptdb.common.exception.DBExecutionException;

///////////////////////////////////////////////////////////////////
//// NameSearcher

/**
 * The concrete searcher to handle the search by model name search criteria.
 *
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
public class NameSearcher extends AbstractSearcher implements
        AbstractDBSearcher {

    /**
     * Construct the NameSearcher object according to the passed search
     * criteria.
     *
     * @param searchCriteria The search criteria passed by the user.
     */
    public NameSearcher(SearchCriteria searchCriteria) {
        _modelNameCriteria = searchCriteria.getModelName();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Checks whether the graph search criteria has been set in
     *  this graph searcher instance.
     *
     * @return true - if the search criteria has been set.<br>
     *         false - if the search criteria has not been set.
     */

    @Override
    protected boolean _isSearchCriteriaSet() {
        if (_modelNameCriteria == null || _modelNameCriteria.length() == 0) {
            return false;
        }

        return true;

    }

    /**
     * Perform the actual search according to the model name.
     *
     * @exception DBExecutionException Thrown by the DBConnection if
     * unexpected problem happens during the execution of DB query tasks.
     */

    @Override
    protected void _search() throws DBExecutionException {

        ModelNameSearchTask modelNameSearchTask = new ModelNameSearchTask(
                _modelNameCriteria);

        // Call the executeModelNameSearchTask() method from the DBConnection
        // class.
        // Get the results returned by the executeModelNameSearchTask() method.
        // Set the returned results to the currentResults field.
        _currentResults = _dbConnection
                .executeModelNameSearchTask(modelNameSearchTask);

        if (_currentResults == null) {
            // The db layer cannot perform the searching, so make the search
            // criteria not set.
            _modelNameCriteria = null;
        } else {
            // Pass the intermediate results.
            handleIntermediateResults(_currentResults, this);
        }

        // The search is done in this searcher, mark this searcher as passed.
        _pass();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * The search criteria of model name.
     */
    private String _modelNameCriteria;

}
