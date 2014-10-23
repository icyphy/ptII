/* Interface for terms in Syntactic expressions.

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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

///////////////////////////////////////////////////////////////////
//// SyntacticTermList

/**
 This class is the base class for SyntacticTerms that are formed from
 operations on lists of other terms. Since, it extends LinkedList, it
 internally maintains a list of SyntacticTerms, although not semantics
 are given to the list other than the collective access to the ports
 of its elements. Additional information and structure to form an
 operator should override or extend the methods of this base class to
 correctly calculate rank, connectivity, and other combinator behaviors.

 @author Chris Shaver
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (shaver)
 @Pt.AcceptedRating Red
 */
@SuppressWarnings("serial")
public class SyntacticTermList extends LinkedList<SyntacticTerm> implements
SyntacticTerm {

    /** Constructs an empty term list with no rank information. */
    public SyntacticTermList() {
        super();
        _inputs = new LinkedList();
        _outputs = new LinkedList();
        _rank = null;
    }

    /** Add a Syntactic Term to the column.
     *  Inputs and outputs are added to the total
     *  inputs and outputs of the composition.
     *  <p>
     *  This method overrides the LinkedList add().
     *
     *  @param term Term to add to the composition.
     *  @return whether node was added.
     */
    @Override
    public boolean add(SyntacticTerm term) {
        SyntacticRank rank = term.rank();
        if (rank != null) {
            if (isEmpty()) {
                _rank = rank.copy();
            } else {
                _rank.product(rank);
            }
        }

        super.add(term);
        _inputs.addAll(term.getInputs());
        _outputs.addAll(term.getOutputs());
        return true;
    }

    /** Add collection of SyntacticTerms to the list by calling
     *  the add function. If add is overridden then this will
     *  polymorphically apply it repeatedly to collections.
     *
     *  @param terms Terms is a collection of SyntacticTerms.
     *  @return whether or not operation changed list.
     */
    @Override
    public boolean addAll(Collection<? extends SyntacticTerm> terms) {
        boolean changed = false;
        for (SyntacticTerm term : terms) {
            changed |= add(term);
        }

        return changed;
    }

    /** Remove collection of SyntacticTerms to the list by calling
     *  the remove function. If add is overridden then this will
     *  polymorphically apply it repeatedly to collections.
     *
     *  @param terms Terms is a collection of SyntacticTerms.
     *  @return whether or not operation changed list.
     */
    @Override
    public boolean removeAll(Collection<?> terms) {
        boolean changed = false;
        for (Object term : terms) {
            changed |= remove(term);
        }

        return changed;
    }

    /** Insert a Syntactic Term in the column.
     *  Inputs and outputs are added to the total
     *  inputs and outputs of the composition.
     *  <p>
     *  This method overrides the LinkedList add().
     *
     *  @param index Index at which to add the term.
     *  @param term Term to add to the composition.
     */
    @Override
    public void add(int index, SyntacticTerm term) {
        SyntacticRank rank = term.rank();
        if (rank != null) {
            if (isEmpty()) {
                _rank = rank.copy();
            } else {
                _rank.product(rank);
            }
        }

        super.add(index, term);
        _refreshPorts();
    }

    /** Remove a Syntactic Term from column.
     *  Inputs and outputs are removed from the
     *  total inputs and outputs.
     *  <p>
     *  This method overrides the LinkedList remove.
     *
     *  @param ot Term to be removed from the composition.
     *  @return whether term was removed.
     */
    @Override
    public boolean remove(Object ot) {
        if (!(ot instanceof SyntacticTerm) || !contains(ot)) {
            return false;
        }

        SyntacticTerm term = (SyntacticTerm) ot;
        _inputs.removeAll(term.getInputs());
        _outputs.removeAll(term.getOutputs());
        super.remove(term);

        SyntacticRank rank = term.rank();
        if (rank != null) {
            if (isEmpty()) {
                _rank = null;
            } else {
                _rank.quotent(rank);
            }
        }

        return true;
    }

    /** Clear list along with port lists. */
    @Override
    public void clear() {
        super.clear();
        _inputs.clear();
        _outputs.clear();
        _rank = null;
    }

    /** Overwrite a Syntactic Term in the column.
     *  Inputs and outputs are added to the total
     *  inputs and outputs of the list.
     *
     *  @param index Index at which to overwrite the term.
     *  @param term Term with which the term at the given index is replaced.
     *  @return term replaced.
     */
    @Override
    public SyntacticTerm set(int index, SyntacticTerm term) {
        SyntacticTerm cterm = get(index);
        if (cterm == null) {
            return null;
        }

        SyntacticRank crank = cterm.rank();
        if (crank != null) {
            _rank.quotent(crank);
        }

        SyntacticRank rank = term.rank();
        if (rank != null) {
            _rank.product(rank);
        }

        super.set(index, term);

        _refreshPorts();
        return null;
    }

    // TODO: Implement this method.
    /** Remove a term at a given index.
     *
     *  @param index Index at which to remove the term.
     *  @return term that has been removed.
     */
    @Override
    public SyntacticTerm remove(int index) {
        // TODO Auto-generated method stub
        return null;
    }

    /** Get all of the output ports for a column.
     *  @return A list of input ports for the column.
     */
    @Override
    public List<SyntacticPort> getInputs() {
        return _inputs;
    }

    /** Get all of the output ports for a column.
     *  @return A list of input ports for the column.
     */
    @Override
    public List<SyntacticPort> getOutputs() {
        return _outputs;
    }

    /** Get the number of inputs to the elements of
     *  the list exposed to the outside.
     *
     *  @return number of inputs.
     */
    @Override
    public int sizeInputs() {
        return _inputs.size();
    }

    /** Get the number of outputs to the elements of
     *  the list exposed to the outside.
     *
     *  @return number of outputs.
     */
    @Override
    public int sizeOutputs() {
        return _outputs.size();
    }

    /** Get the rank of the list.
     *
     *  @return rank of the list as a term.
     */
    @Override
    public SyntacticRank rank() {
        return _rank;
    }

    /** Get the index of the syntactic input port in the column.
     *  If the given port is not in the inputs for the
     *  column null is returned.
     *  @param port Port to find the index of.
     *  @return index of the port or null.
     */
    @Override
    public Integer inputIndex(SyntacticPort port) {
        int index = _inputs.indexOf(port);
        return index < 0 ? null : index;
    }

    /** Get the index of the syntactic output port in the column.
     *  If the given port is not in the outputs for the
     *  column null is returned.
     *  @param port Port to find the index of.
     *  @return index of the port or null.
     */
    @Override
    public Integer outputIndex(SyntacticPort port) {
        int index = _outputs.indexOf(port);
        return index < 0 ? null : index;
    }

    /** Generate code for the term.
     *  In the case of this base class it is blank.
     *
     *  @return string containing the code for the term.
     */
    @Override
    public String generateCode() {
        return "";
    }

    /** Get the sort order of the term.
     *
     *  @return sort order of the term.
     */
    @Override
    public int getOrder() {
        return 99;
    }

    /** Decide whether the term has code to generate.
     *
     *  @return whether the term has code to generate.
     */
    @Override
    public boolean hasCode() {
        return true;
    }

    /** Refresh the port lists scanning through the terms. */
    protected void _refreshPorts() {
        _inputs.clear();
        _outputs.clear();
        for (SyntacticTerm term : this) {
            _inputs.addAll(term.getInputs());
            _outputs.addAll(term.getOutputs());
        }
    }

    /** Recalculate the rank of the list as a term. */
    protected void _refreshRank() {
        // TODO: refresh rank default
    }

    /** List of exposed input ports of constituent terms. */
    protected LinkedList<SyntacticPort> _inputs;

    /** List of exposed output ports of constituent terms. */
    protected LinkedList<SyntacticPort> _outputs;

    /** Rank of list as a term. */
    protected SyntacticRank _rank;

}
