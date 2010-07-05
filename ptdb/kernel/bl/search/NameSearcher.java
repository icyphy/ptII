/*
 * 
 */
package ptdb.kernel.bl.search;

import ptdb.common.dto.ModelNameSearchTask;
import ptdb.common.dto.SearchCriteria;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;

///////////////////////////////////////////////////////////////
//// NameSearcher

/**
 * The concrete searcher to handle the search by model name.  
 * 
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 8.1
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

    //////////////////////////////////////////////////////////////////////
    ////                    protected methods                         ////

    @Override
    protected boolean _isSearchCriteriaSet() {
        if (_modelNameCriteria == null || _modelNameCriteria.isEmpty()) {
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
    protected void _search() throws DBExecutionException, DBConnectionException {
        // Create the ModelNameSearchTask. 
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
        } 

        pass();
    }


    //////////////////////////////////////////////////////////////////////
    ////                    private variables                         ////

    /**
     * The search criteria of model name.
     */
    private String _modelNameCriteria;

}
