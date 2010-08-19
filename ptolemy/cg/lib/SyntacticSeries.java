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

package ptolemy.cg.lib;

import java.util.LinkedList;

public class SyntacticSeries 
extends LinkedList<SyntacticTerm> implements SyntacticTerm {

    public SyntacticSeries() {
        super();
        _inputs = new LinkedList();
        _outputs = new LinkedList();
    }

    public boolean add(SyntacticTerm term) {
        if (contains(term)) return false;
        if (!super.add(term)) return false;

        if (this.size() == 1) {
            _inputs.clear();
            _inputs.addAll(term.getInputs());
        }

        _outputs.clear();
        _outputs.addAll(term.getOutputs());
        return true;
    }

    public void add(int index, SyntacticTerm term) {
        if (contains(term)) return;
        super.add(index, term);

        int size = this.size();
        if (index == 0) {
            _inputs.clear();
            _inputs.addAll(term.getInputs());
        }

        else if (index == size - 1) {
            _outputs.clear();
            _outputs.addAll(term.getOutputs());
        }
    }

    public boolean remove(SyntacticTerm term) {
        if (contains(term)) return false;
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

    public void clear() {
        super.clear();
        _inputs.clear();
        _outputs.clear();
    }

    public Integer inputIndex(SyntacticPort port) {
        int dex = _inputs.indexOf(port);
        return dex < 0 ? null : dex;
    }

    public Integer outputIndex(SyntacticPort port) {
        int dex = _outputs.indexOf(port);
        return dex < 0 ? null : dex;
    }

    public int getOrder() {
        return 101;
    }

    public boolean hasCode() {
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

    /** Get all of the output ports for a column.
     *  @return A list of input ports for the column.
     */
    public LinkedList<SyntacticPort> getInputs() {
        return _inputs;
    }

    /** Get all of the output ports for a column.
     *  @return A list of input ports for the column.
     */
    public LinkedList<SyntacticPort> getOutputs() {
        return _outputs;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private LinkedList<SyntacticPort> _inputs;
    private LinkedList<SyntacticPort> _outputs;

}