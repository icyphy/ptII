/* Syntactic Graph for syntactic representations.

Copyright (c) 2010-2014 The Regents of the University of California.
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

import java.util.LinkedList;

///////////////////////////////////////////////////////////////////
//// SyntacticSeries

/**
 This class represents a series composition over a sequence of SyntacticTerms.
 Each term in the sequence is composed with the previous to form a chain
 of operations. Each output is connected in order with each input of the
 subsequent term. The term produced by this composition has a number of inputs
 equal to that of the first term in the sequence, and a number of outputs
 equal to that of the last term.

 The chain is initialized as empty, and terms can be added to it either
 pushing to the end, or by insertion. In these operations, checking is done
 to enforce the constraint that the number of inputs and outputs are equal
 at a composition. Removal is possible, although only when a term is has the
 same number of inputs as outputs.

@author Chris Shaver
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (shaver)
@Pt.AcceptedRating Red
 */
@SuppressWarnings("serial")
public class SyntacticSeries extends SyntacticTermList {

    /** Create an empty SyntacticSeries term. */
    public SyntacticSeries() {
        super();
    }

    /** Add a term to the end of the series.
     *  This is only allowed if the added term has as
     *  many inputs as the currently last term has outputs.
     *  Otherwise, false is returned and nothing is done.
     *
     *  @param term Term to be added to series.
     *  @return true if added, false if invalid.
     */
    @Override
    public boolean add(SyntacticTerm term) {
        if (contains(term)) {
            return false;
        }

        SyntacticRank rank = _rank == null ? term.rank()
                : SyntacticRank.compose(this, term);
        if (rank == null) {
            return false;
        }

        if (!super.add(term)) {
            return false;
        }

        if (size() == 1) {
            _inputs.clear();
            _inputs.addAll(term.getInputs());
        }

        _outputs.clear();
        _outputs.addAll(term.getOutputs());

        _rank = rank.copy();
        return true;
    }

    /** Pushes a term to the end of the series.
     *  This is only allowed if the added term has as
     *  many inputs as the currently last term has outputs.
     *  Otherwise, false is returned and nothing is done.
     *
     *  @param term Term to be added to series.
     */
    @Override
    public void push(SyntacticTerm term) {
        if (contains(term)) {
            return;
        }

        SyntacticRank rank = _rank == null ? term.rank()
                : SyntacticRank.compose(term, this);
        if (rank == null) {
            return;
        }

        // Java 1.5 does not have push(), but
        // http://download.oracle.com/javase/6/docs/api/java/util/LinkedList.html#push%28E%29
        // says "This method is equivalent to addFirst(E)."
        // super.push(term);
        addFirst(term);

        if (size() == 1) {
            _outputs.clear();
            _outputs.addAll(term.getOutputs());
        }

        _inputs.clear();
        _inputs.addAll(term.getInputs());

        _rank = rank.copy();
    }

    /** Adds a term to an arbitrary position of the series.
     *  This is allowed only if the added term is compatible
     *  with neighboring terms. If not at the beginning or end,
     *  this essentially means it must have the same number of
     *  inputs and outputs.
     *
     *  @param index Index at which to add the term.
     *  @param term Term to add to series.
     */
    @Override
    public void add(int index, SyntacticTerm term) {
        if (contains(term) || index < 0 || index > size()) {
            return;
        }

        if (index == size()) { // append case
            add(term);
            return;
        }

        else if (index == 0) { // prepend case
            push(term);
            return;
        }

        else { // middle case
            SyntacticTerm tF = get(index), tP = get(index - 1);
            if (SyntacticRank.compose(tP, term) == null
                    || SyntacticRank.compose(term, tF) == null) {
                super.add(index, term);
            }
        }
    }

    // [TODO] : add rank logic
    /** Removes a term from the series.
     *  This can only be done if the neighboring terms
     *  can be connected with each other. In the case that
     *  this is an interior term, it must have the same number
     *  of inputs and outputs to complete this operation.
     *
     *  @param term Term to remove from series.
     *  @return whether term has been removed.
     */
    public boolean remove(SyntacticTerm term) {
        if (!contains(term)) {
            return false;
        }
        int index = this.indexOf(term);
        int size = this.size();

        if (size == 1) {
            _inputs.clear();
            _outputs.clear();
        }

        else if (index == 0) {
            _inputs.clear();
            _inputs.addAll(this.get(1).getInputs());
        }

        else if (index == size - 1) {
            _outputs.clear();
            _outputs.addAll(this.get(index - 1).getOutputs());
        }

        super.remove(term);
        return true;
    }

    // TODO: Implement this method as a replacement
    // for SyntacticGraph::insertPermutations
    /** Intercolate permutations between series terms. */
    public void intercolatePermutations() {

    }

    /** Generate code for the series.
     *  The code for each term are joined by series composition
     *  operators.
     *
     *  @return code for term.
     */
    @Override
    public String generateCode() {
        LinkedList<String> termStrs = new LinkedList();
        for (SyntacticTerm node : this) {
            if (node.hasCode()) {
                termStrs.add(node.generateCode());
            }
        }

        return SyntacticGraph.stringJoin(termStrs, "\n        =>= ");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

}
