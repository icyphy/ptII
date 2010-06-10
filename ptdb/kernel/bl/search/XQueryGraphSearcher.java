/*
 * 
 */
package ptdb.kernel.bl.search;

////////////////////////////////////////////////////////////////////////////
//// XQueryGraphSearcher

/**
 * Searcher for searching the models according to graph 
 * pattern matching in the database. 
 * 
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
public class XQueryGraphSearcher extends GraphSearcher implements
        AbstractDBSearcher {

    /////////////////////////////////////////////////////////////////////////
    ////        protected methods                                       /////

    @Override
    protected void _search() {
        // To be implemented in the next release 

        // set the search done 
        _setSearchDone();

    }

}
