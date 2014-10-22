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
import ptdb.common.dto.SearchCriteria;

///////////////////////////////////////////////////////////////////
//// GraphSearcher

/**
 * Inherits from the AbstractSearcher, and contains the common
 * functions for the searchers for graph pattern match search.
 *
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
public abstract class GraphSearcher extends AbstractSearcher {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * A factory which returns the concrete GraphSearcher objects
     * according to the complexity of the search criteria from the user.
     *
     * <p>Different graph searcher has different capability to perform the
     * graph pattern match search.  The original design is that this factory
     * method will return the appropriate searchers according to the given
     * graph pattern search criteria.</p>
     *
     * <p>For now, due to the limitation of searching patterns in the
     * database, this factory will anyway construct both XQueryGraphSearcher
     * and PatternMatchGraphSearcher, and return both of them in a list. The
     * first searcher in the list will be the instance of the
     * XQueryGraphSearcher, and the second will be
     * PatternMatchGraphSearcher.</p>
     *
     * @param searchCriteria The search criteria input by the user.
     * @return The list that contains the required graph searchers, created
     *  according to the search criteria.  The searchers in the returned list
     *  should be used in their order in the list.
     */
    public static ArrayList<GraphSearcher> getGraphSearcher(
            SearchCriteria searchCriteria) {

        ArrayList<GraphSearcher> graphSearchers = new ArrayList<GraphSearcher>();

        XQueryGraphSearcher xQueryGraphSearcher = new XQueryGraphSearcher(
                searchCriteria.getDBGraphSearchCriteria());

        graphSearchers.add(xQueryGraphSearcher);

        PatternMatchGraphSearcher patternMatchGraphSearcher = new PatternMatchGraphSearcher(
                searchCriteria.getDBGraphSearchCriteria());
        graphSearchers.add(patternMatchGraphSearcher);

        return graphSearchers;
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

        if (_dbGraphSearchCriteria != null
                && (_dbGraphSearchCriteria.getPortsList() != null
                        && !_dbGraphSearchCriteria.getPortsList().isEmpty()
                        || _dbGraphSearchCriteria.getComponentEntitiesList() != null
                        && !_dbGraphSearchCriteria.getComponentEntitiesList()
                                .isEmpty() || _dbGraphSearchCriteria
                        .getCompositeEntities() != null
                        && !_dbGraphSearchCriteria.getCompositeEntities()
                                .isEmpty())) {
            return true;
        }

        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /**
     * The search criteria for graph pattern matching that are input by the
     * user.
     */
    protected DBGraphSearchCriteria _dbGraphSearchCriteria;

}
