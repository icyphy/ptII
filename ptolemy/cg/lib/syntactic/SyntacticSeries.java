/* Syntactic Graph for syntactic representations.

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

import java.util.LinkedList;

public class SyntacticSeries extends SyntacticTermList {

    public SyntacticSeries() {
        super();
    }

    public boolean add(SyntacticTerm term) {
        if (contains(term)) return false;
        
        SyntacticRank rank = _rank == null ? term.rank() : SyntacticRank.compose(this, term);
        if (rank == null) return false;
        
        if (!super.add(term)) return false;

        if (size() == 1) {
            _inputs.clear();
            _inputs.addAll(term.getInputs());
        }

        _outputs.clear();
        _outputs.addAll(term.getOutputs());
        
        _rank = rank.copy();
        return true;
    }
    
    public void push(SyntacticTerm term) {
        if (contains(term)) return;
        
        SyntacticRank rank = _rank == null ? term.rank() : SyntacticRank.compose(term, this);
        if (rank == null) return;
        
        super.push(term);

        if (size() == 1) {
            _outputs.clear();
            _outputs.addAll(term.getOutputs());
        }
        
        _inputs.clear();
        _inputs.addAll(term.getInputs());
        
        _rank = rank.copy();
    }

    public void add(int index, SyntacticTerm term) {
        if (contains(term) || index < 0 || index > size()) return;
        
        if (index == size()) { // append case
            add(term);
            return;
        }
        
        else if (index == 0) { // prepend case
            push(term);
            return;
        }
        
        else { // middle case
            SyntacticTerm tF = get(index), tP = get(index-1);
            if (SyntacticRank.compose(tP, term) == null || SyntacticRank.compose(term, tF) == null) {
                super.add(index, term);
            }
        }
    }

    // [TODO] : add rank logic
    public boolean remove(SyntacticTerm term) {
        if (!contains(term)) return false;
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

        else if (index == size-1) {
            _outputs.clear();
            _outputs.addAll(this.get(index-1).getOutputs());
        }

        super.remove(term);
        return true;
    }

    public void intercolatePermutations() {

    }

    public String generateCode() {
        LinkedList<String> termStrs = new LinkedList(); 
        for (SyntacticTerm node : this) {
            if (node.hasCode()) termStrs.add(node.generateCode());
        }

        return SyntacticGraph.stringJoin(termStrs, "\n        =>= ");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

}