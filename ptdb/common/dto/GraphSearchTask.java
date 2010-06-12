/*
 * 
 */
package ptdb.common.dto;


///////////////////////////////////////////////////////////////
//// GraphSearchTask

/**
 * Task to search graphical patterns on database.
 * 
 * @author Ashwini Bijwe
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (abijwe)
 * @Pt.AcceptedRating Red (abijwe)
 *
 */
public class GraphSearchTask extends Task {

    //////////////////////////////////////////////////////////////////////
    ////		public variables 				//////

    //////////////////////////////////////////////////////////////////////
    ////		public methods 					//////
    
    /**
     * Return the graphical search criteria.
     * @return - The graph search criteria.
     */
    public DBGraphSearchCriteria getGraphSearchCriteria() {
        return _graphSearchCriteria;
    }

    /**
     * Set the graph search criteria.
     * @param graphSearchCriteria - The graph search criteria. 
     */
    public void setGraphSearchCriteria(DBGraphSearchCriteria graphSearchCriteria) {
        this._graphSearchCriteria = graphSearchCriteria;
    }

    //////////////////////////////////////////////////////////////////////
    ////		protected methods 				//////

    //////////////////////////////////////////////////////////////////////
    ////		protected variables 				//////

    //////////////////////////////////////////////////////////////////////
    ////		private methods 				//////

    //////////////////////////////////////////////////////////////////////
    ////		private variables				//////
    private DBGraphSearchCriteria _graphSearchCriteria;

    }
