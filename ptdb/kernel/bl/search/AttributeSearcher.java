/*
 *
 */
package ptdb.kernel.bl.search;

import java.util.ArrayList;

import ptdb.common.dto.AttributeSearchTask;
import ptdb.common.dto.SearchCriteria;
import ptdb.common.exception.DBExecutionException;
import ptolemy.kernel.util.Attribute;

///////////////////////////////////////////////////////////////////
//// AttributeSearcher

/**
 * The concrete searcher to handle the search by attributes criteria.
 *
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
public class AttributeSearcher extends AbstractSearcher implements
        AbstractDBSearcher {

    /**
     * Construct the AttributeSearcher according to the input search criteria.
     *
     * @param searchCriteria The search criteria that input by the user.
     */
    public AttributeSearcher(SearchCriteria searchCriteria) {

        this._attributesCriteria = searchCriteria.getAttributes();

    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Checks whether the attribute search criteria has been set in
     *  this attribute searcher instance.
     *
     * @return true - if the search criteria has been set.<br>
     *         false - if the search criteria has not been set. 
     */
    @Override
    protected boolean _isSearchCriteriaSet() {

        if (_attributesCriteria == null || _attributesCriteria.size() == 0) {
            return false;
        }

        return true;

    }

    /**
     * Perform the actual search for the attributes.
     *
     * @exception DBExecutionException Thrown by the DBConnection if
     * unexpected problem happens during the execution of DB query tasks.
     */
    @Override
    protected void _search() throws DBExecutionException {

        AttributeSearchTask attributeSearchTask = new AttributeSearchTask();

        attributeSearchTask.setAttributesList(_attributesCriteria);

        // call the executeAttributeTask() method from the DBConnection class
        // set the returned results
        _currentResults = _dbConnection
                .executeAttributeSearchTask(attributeSearchTask);

        if (_currentResults == null) {
            // The db layer cannot perform the searching, so make the search 
            // criteria not set. 
            _attributesCriteria = null;
        }

        pass();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * This field contains the search criteria of attributes.
     */
    private ArrayList<Attribute> _attributesCriteria;

}
