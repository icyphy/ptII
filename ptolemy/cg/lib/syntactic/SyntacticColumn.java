/* Syntactic Column for syntactic representations.

Copyright (c) 2010-2011 The Regents of the University of California.
All rights reserved.
Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

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

package ptolemy.cg.lib.syntactic;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

/** Represent parallel composition in the context of Syntax terms.
 *  SyntacticTerm implementing objects can be put in parallel
 *  composition in this object which combines input and output
 *  ports of the constituent terms.
 *  <p>
 *  @author Chris Shaver
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating red (shaver)
 *  @Pt.AcceptedRating red
 *
 */
@SuppressWarnings("serial")
public class SyntacticColumn extends SyntacticTermList {

    /** Create new empty Syntactic Column. */
    public SyntacticColumn() {
        super();
    }

    /** Decide if given node follows completely from this column.
     *  To be true there must be at least one connection from the column to
     *  the node and all incoming connections must follow from this column.
     *  @param node The node possibly following the column.
     *  @return Whether the given node follows completely from given column.
     */
    public boolean doesFollow(SyntacticTerm node) {
        boolean doesFollow = true;
        boolean isAny = false;
        for (SyntacticPort iport : node.getInputs()) {
            SyntacticPort rport = iport.getConnectedPort();
            if (rport == null) {
                continue;
            }

            if (outputIndex(rport) != null) {
                isAny = true;
            } else {
                doesFollow = false;
            }
        }

        return isAny && doesFollow;
    }

    /** Sort constituent terms in column by type order.
     *  Sorting a column will change its neighboring permutations. */
    public void sort() {
        final Comparator<SyntacticTerm> compareNodes = new Comparator<SyntacticTerm>() {
            public int compare(SyntacticTerm a, SyntacticTerm b) {
                return a.getOrder() - b.getOrder();
            }
        };

        Collections.sort(this, compareNodes);
        _refreshPorts();
    }

    public String generateCode() {
        LinkedList<String> termStrs = new LinkedList();
        for (SyntacticTerm node : this) {
            if (node.hasCode()) {
                termStrs.add(node.generateCode());
            }
        }

        return SyntacticGraph.stringJoin(termStrs, " | ");
    }

    public boolean hasCode() {
        return !isEmpty();
    }

}
