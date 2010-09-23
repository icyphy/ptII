/* Interface for terms in Syntactic expressions.

Copyright (c) 2010 The Regents of the University of California.
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
import java.util.List;
import java.util.LinkedList;

public class SyntacticTermList extends LinkedList<SyntacticTerm>
    implements SyntacticTerm {

    public SyntacticTermList() {
        super();
        _inputs     = new LinkedList();
        _outputs    = new LinkedList();
        _rank = null;
    }
    
    /** Add a Syntactic Term to the column.
     *  Inputs and outputs are added to the total 
     *  inputs and outputs of the composition.
     *  <p>
     *  This method overrides the LinkedList add.
     *  <p>
     *  @param node Term to add to the composition.
     *  @return whether node was added.
     */
    public boolean add(SyntacticTerm term) {
        SyntacticRank rank = term.rank();
        if (rank != null) {
            if (isEmpty()) _rank = rank.copy();
            else _rank.product(rank);
        }
        
        super.add(term);
        _inputs  .addAll(term.getInputs());
        _outputs .addAll(term.getOutputs());
        return true;
    }

    public boolean addAll(Collection<? extends SyntacticTerm> terms) {
        boolean changed = false;
        for (SyntacticTerm term : terms) {
            changed |= add(term);
        }
        
        return changed;
    }
    
    public boolean removeAll(Collection<?> terms) {
        boolean changed = false;
        for (Object term : terms) {
            changed |= remove(term);
        }
        
        return changed;
    }
    
    public void add(int index, SyntacticTerm term) {
        SyntacticRank rank = term.rank();
        if (rank != null) {
            if (isEmpty()) _rank = rank.copy();
            else _rank.product(rank);
        }
        
        super.add(index, term);
        _refreshPorts();
    }
    
    /** Remove a Syntactic Term from column.
     *  Inputs and outputs are removed from the 
     *  total inputs and outputs.
     *  <p>
     *  This method overrides the LinkedList remove.
     *  <p>
     *  @param node Term to be removed from the composition.
     *  @return whether term was removed.
     */
    public boolean remove(Object ot) {
        if (!(ot instanceof SyntacticTerm) || !contains(ot)) return false;
        
        SyntacticTerm term = (SyntacticTerm)ot;
        _inputs  .removeAll(term.getInputs());
        _outputs .removeAll(term.getOutputs());
        super.remove(term);
        
        SyntacticRank rank = term.rank();
        if (rank != null) {
            if (isEmpty()) _rank = null;
            else _rank.quotent(rank);
        }

        return true;
    }

    public void clear() {
        super.clear();
        _inputs.clear();
        _outputs.clear();
        _rank = null;
    }

    public SyntacticTerm set(int index, SyntacticTerm term) {
        SyntacticTerm cterm = get(index);
        if (cterm == null) return null;
        
        SyntacticRank crank = cterm.rank();
        if (crank != null) {
            _rank.quotent(crank);
        }
        
        SyntacticRank rank  = term.rank();
        if (rank != null) {
            _rank.product(rank);
        }
        
        super.set(index, term);
        
        _refreshPorts();
        return null;
    }

    public SyntacticTerm remove(int index) {
        // TODO Auto-generated method stub
        return null;
    }

    /** Get all of the output ports for a column.
     *  @return A list of input ports for the column.
     */
    public List<SyntacticPort> getInputs() {
        return _inputs;
    }

    /** Get all of the output ports for a column.
     *  @return A list of input ports for the column.
     */
    public List<SyntacticPort> getOutputs() {
        return _outputs;
    }

    public int sizeInputs() {
        return _inputs.size();
    }

    public int sizeOutputs() {
        return _outputs.size();
    }

    public SyntacticRank rank() {
        return _rank;
    }

    /** Get the index of the syntactic input port in the column.
     *  If the given port is not in the inputs for the 
     *  column null is returned.
     *  @param port Port to find the index of.
     *  @return index of the port or null.
     */
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
    public Integer outputIndex(SyntacticPort port) {
        int index = _outputs.indexOf(port);
        return index < 0 ? null : index;
    }

    public String generateCode() {
        return "";
    }

    public int getOrder() {
        return 99;
    }

    public boolean hasCode() {
        return true;
    }
    
    protected void _refreshPorts() {
        _inputs.clear();
        _outputs.clear();
        for (SyntacticTerm term : this) {
            _inputs  .addAll(term.getInputs());
            _outputs .addAll(term.getOutputs());
        }
    }
    
    protected void _refreshRank() {
        // TODO: refresh rank default
    }
    
    protected LinkedList<SyntacticPort> _inputs;
    protected LinkedList<SyntacticPort> _outputs;
    protected SyntacticRank _rank;

}
