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

import ptdb.common.dto.SearchCriteria;
import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;

///////////////////////////////////////////////////////////////////
//// SearchManager

/**
 * Business layer interface class that mainly handles the search models
 * function. It constructs and configures the actual searcher classes according
 * to the search criteria, and triggers the searching.
 *
 * <p>For different requirement for search configuration, this class can be
 * modified to add more searchers chain configuration.</p>
 *
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
public class SearchManager {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * This method is used to be called by the GUI layer class to pass the
     * search criteria. It is invoked by the Search GUI frame.
     *
     * <p>Algorithm: The search manager first creates all the searchers needed
     * in the search, and configures them in the order of attribute searcher -&gt;
     * command searcher -&gt; graph searcher.<br>
     *
     * Once all the results are searched and found, the results will be passed
     * to the hierarchy fetcher to fetch the referencing information.<br>
     *
     * After the hierarchy of the results are fetched, the results will be
     * written to the result buffer together with the hierarchy. </p>
     *
     *
     * @param searchCriteria The search criteria input by the user.
     * @param searchResultBuffer The buffer that is used to store the search
     *  results. The search result buffer extends  the class Observable. The
     *            GUI layer has registered some listener for checking search
     *            result before passing the buffer to this method.
     * @exception DBConnectionException Thrown from the database layer when the
     *  database layer fails to create a connection to the database.
     * @exception DBExecutionException Thrown from the database layer when the
     *  database layer fails to execute the searching in the database.
     */
    public void search(SearchCriteria searchCriteria,
            ResultHandler searchResultBuffer) throws DBConnectionException,
            DBExecutionException {

        // The search Manager will create all the searcher, and pass the search
        // criteria to them.
        AttributeSearcher attributeSearcher = new AttributeSearcher(
                searchCriteria);

        NameSearcher nameSearcher = new NameSearcher(searchCriteria);

        CommandSearcher commandSearcher = new CommandSearcher(searchCriteria);

        ArrayList<GraphSearcher> graphSearchers = GraphSearcher
                .getGraphSearcher(searchCriteria);

        // create a the Hierarchy fetcher to fetch the hierarchy of the models
        HierarchyFetcher hierarchyFetcher = new HierarchyFetcher();

        // configure the searchers to set the next result handlers
        // also configure the searchers to set the previous searcher
        attributeSearcher.setFirstSearcher();

        attributeSearcher.setNextResultHandler(nameSearcher);
        nameSearcher.setPreviousSeacher(attributeSearcher);

        nameSearcher.setNextResultHandler(commandSearcher);
        commandSearcher.setPreviousSeacher(nameSearcher);

        commandSearcher.setNextResultHandler(graphSearchers.get(0));
        graphSearchers.get(0).setPreviousSeacher(commandSearcher);

        // set the pattern match searcher after DB graph searcher
        graphSearchers.get(0).setNextResultHandler(graphSearchers.get(1));
        graphSearchers.get(1).setPreviousSeacher(graphSearchers.get(0));

        // set the hierarchy fetcher to the last searcher
        graphSearchers.get(1).setNextResultHandler(hierarchyFetcher);
        hierarchyFetcher.setPreviousSeacher(graphSearchers.get(1));

        // assign the result handler to the fetcher
        hierarchyFetcher.setNextResultHandler(searchResultBuffer);
        hierarchyFetcher.setLastSearcher();

        // start the search
        attributeSearcher.handleResults(new ArrayList<XMLDBModel>());

    }

}
