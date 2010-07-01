/*
 *
 */
package ptdb.kernel.bl.search;

import java.util.ArrayList;
import java.util.Iterator;
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
 * Searcher for using the pattern match function in the Ptolemy.
 *
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
public class PatternMatchGraphSearcher extends GraphSearcher {

    /**
     * Construct the PatternMatchGraphSearcher with the db graph search 
     * criteria.
     * 
     * @param dbGraphSearchCriteria The search criteria for the graph matching.
     */
    public PatternMatchGraphSearcher(DBGraphSearchCriteria dbGraphSearchCriteria) {
        _dbGraphSearchCriteria = dbGraphSearchCriteria;
    }

    ///////////////////////////////////////////////////////////////////
    ////        protected methods                                  ////

    @Override
    protected void _search() throws DBConnectionException, DBExecutionException {

        Pattern pattern = _dbGraphSearchCriteria.getPattern();
        GraphMatcher matcher = new GraphMatcher();
        _parser = new MoMLParser();

        int count = 0;
        ArrayList<XMLDBModel> modelsBatch = new ArrayList<XMLDBModel>();

        for (Iterator iterator = _previousResults.iterator(); iterator
                .hasNext();) {
            XMLDBModel model = (XMLDBModel) iterator.next();
            // get models from the LoadManager, as 5 models in a batch

            modelsBatch.add(model);
            count++;

            if (count == 5 || !iterator.hasNext()) {

                count = 0;

                List<XMLDBModel> fullModels = DBModelFetcher.load(modelsBatch);

                for (Iterator iterator2 = fullModels.iterator(); iterator2
                        .hasNext();) {
                    XMLDBModel fullModel = (XMLDBModel) iterator2.next();

                    CompositeEntity modelNamedObj;
                    try {
                        modelNamedObj = (CompositeEntity) _parser
                                .parse(fullModel.getModel());
                    } catch (Exception e) {
                        // Just skip this model. 
                        e.printStackTrace();
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
                modelsBatch = new ArrayList<XMLDBModel>();

            }

        }

        wholeSearchDone();

    }

    ///////////////////////////////////////////////////////////////////
    ////        private variables                                  ////
    private MoMLParser _parser;

}
