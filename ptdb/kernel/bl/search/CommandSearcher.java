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

import ptdb.common.dto.SearchCriteria;

///////////////////////////////////////////////////////////////////
//// CommandSearcher

/**
 * The concrete searcher class which handles the search according to the
 *  XQuery command input by the user.
 *
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
public class CommandSearcher extends AbstractSearcher implements
        AbstractDBSearcher {

    /**
     * Construct the command searcher.
     *
     * @param searchCriteria The search criteria input by the user.
     */
    public CommandSearcher(SearchCriteria searchCriteria) {

        _commandSearchCriteria = searchCriteria.getSearchCommand();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Checks whether the command search criteria has been set in
     *  this command searcher instance.
     *
     * @return true - if the search criteria has been set.<br>
     *         false - if the search criteria has not been set.
     */

    @Override
    protected boolean _isSearchCriteriaSet() {
        if (_commandSearchCriteria == null || _commandSearchCriteria.equals("")) {
            return false;
        }
        return true;
    }

    /**
     * Perform the actual search for the attributes search criteria.
     *
     * <p>This method has not been implemented yet.</p>
     */

    @Override
    protected void _search() {
        // to be implemented in the next requirement

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * Stores the XQuery command string input by the user.
     */
    private String _commandSearchCriteria;

}
