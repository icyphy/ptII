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

import ptdb.common.dto.DBGraphSearchCriteria;
import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.kernel.bl.load.DBModelFetcher;
import ptolemy.actor.gt.GraphMatcher;
import ptolemy.actor.gt.Pattern;
import ptolemy.actor.gt.data.MatchResult;
import ptolemy.kernel.CompositeEntity;
import ptolemy.moml.MoMLParser;
import ptolemy.vergil.gt.MatchResultRecorder;

///////////////////////////////////////////////////////////////////
//// PatternMatchGraphSearcher

/**
 * Search for models using the Ptolemy Graph matching functionality.
 *
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
public class PatternMatchGraphSearcher extends GraphSearcher {

    /**
     * Construct the PatternMatchGraphSearcher with the db graph search
     * criteria.
     *
     * <p>This searcher is not an independent searcher, so it needs to exist
     * with other independent searchers. </p>
     *
     * @param dbGraphSearchCriteria The search criteria for the graph matching.
     */
    public PatternMatchGraphSearcher(DBGraphSearchCriteria dbGraphSearchCriteria) {
        _dbGraphSearchCriteria = dbGraphSearchCriteria;

        _isIndependent = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Perform the actual pattern match for the passed models.
     *
     * @exception DBConnectionException Thrown from the database layer if the
     * connection to database cannot be obtained.
     * @exception DBExecutionException Thrown from the database layer if error
     * occurs during some execution.
     */

    @Override
    protected void _search() throws DBConnectionException, DBExecutionException {

        Pattern pattern = _dbGraphSearchCriteria.getPattern();

        _parser = new MoMLParser();
        GraphMatcher matcher = new GraphMatcher();

        int count = 0;
        ArrayList<XMLDBModel> modelsBatch = new ArrayList<XMLDBModel>();

        int fetchedCount = 0;

        for (XMLDBModel model : _previousResults) {

            // Get models from the LoadManager, as 5 models in a batch.
            // Fetching 5 models will not hit the database with searching for
            // too many models that will hurt the database performance.  Also,
            // fetching 5 models in a batch can help to reduce the times that
            // this searcher needs to hit the database to fetch the full models.

            modelsBatch.add(model);
            count++;
            fetchedCount++;

            if (count == 5 || fetchedCount == _previousResults.size()) {

                count = 0;

                List<XMLDBModel> fullModels = DBModelFetcher.load(modelsBatch);

                for (XMLDBModel fullModel : fullModels) {

                    if (isSearchCancelled()) {
                        return;
                    }

                    CompositeEntity modelNamedObj;
                    try {
                        _parser.resetAll();
                        modelNamedObj = (CompositeEntity) _parser
                                .parse(fullModel.getModel());

                    } catch (Exception e) {
                        // Add this model to the error models list.
                        _addErrorModel(fullModel);

                        continue;
                    }

                    MatchResultRecorder recorder = new MatchResultRecorder();
                    matcher.setMatchCallback(recorder);

                    matcher.match(pattern, modelNamedObj);

                    List<MatchResult> matchResults = recorder.getResults();

                    if (!matchResults.isEmpty()) {
                        ArrayList<XMLDBModel> tempResultsList = new ArrayList<XMLDBModel>();
                        tempResultsList.add(fullModel);

                        _nextResultHandler.handleResults(tempResultsList);
                    }
                }
                modelsBatch.clear();

            }

        }
        // Pass the error models.
        passErrorModels(_errorModels);

        // The pattern match searcher normally should be the last searcher
        // performing the actual search.  So once the search in this searcher
        // is done, the whole search process is done.
        wholeSearchDone();

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private MoMLParser _parser;

}
