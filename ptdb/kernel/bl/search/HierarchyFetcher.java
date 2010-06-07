/*
 * 
 */
package ptdb.kernel.bl.search;

////////////////////////////////////////////////////////////////////////////
////HierarchyFetcher

/**
 * This class is used to fetch the referencing hierarchy for the models.  
 * 
 * @author Alek Wang
 * @version $Id$
 *
 */
public class HierarchyFetcher extends AbstractSearcher implements
        ResultHandler, AbstractDBSearcher {

    /////////////////////////////////////////////////////////////////////////
    ////        protected methods                                       /////

    @Override
    protected boolean _isSearchCriteriaSet() {

        return true;
    }

    /**
     * This method is used to handle the model results passed to this class. 
     * This method will go to the database to fetch all the referencing hierarchy 
     * for the passed results
     * 
     *
     */
    @Override
    protected void _search() {
        // To be implemented in the next release 

        // create the FetchHierarchyTask

        // call the executeFetchHierarchyTask() method from the DBConnection class

        // get the results returned by the executeFetchHierarchyTask() method

        // set the returned results to the currentResults field

        // set the search done 
        _setSearchDone();

    }

}
