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

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

public class SyntacticColumn 
    extends LinkedList<SyntacticTerm> implements SyntacticTerm {

    public SyntacticColumn() {
        super();
        _inputs = new LinkedList();
        _outputs = new LinkedList();
    }

    public boolean add(SyntacticTerm node) {
        if (contains(node)) return false;
        super.add(node);
        _inputs.addAll(node.getInputs());
        _outputs.addAll(node.getOutputs());
        return true;
    }

    public boolean remove(SyntacticTerm node) {
        if (contains(node)) return false;
        _inputs.removeAll(node.getInputs());
        _outputs.removeAll(node.getOutputs());
        super.remove(node);
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
            if (rport == null) continue;

            if (outputIndex(rport) != null) {
                isAny = true;
            }
            else {
                doesFollow = false;
            }
        }

        return isAny && doesFollow;
    }

    public void sort() {
        final Comparator<SyntacticTerm> compareNodes = new Comparator<SyntacticTerm>() {
            public int compare(SyntacticTerm a, SyntacticTerm b) {
                return a.getOrder() - b.getOrder();
            }
        };

        Collections.sort(this, compareNodes);
        _inputs.clear();
        _outputs.clear();

        for (SyntacticTerm node : this) {
            _inputs.addAll(node.getInputs());
            _outputs.addAll(node.getOutputs());
        }

    }

    public int getOrder() {
        return 100;
    }

    public boolean hasCode() {
        return true;
    }

    public String generateCode() {
        LinkedList<String> termStrs = new LinkedList(); 
        for (SyntacticTerm node : this) {
            if (node.hasCode()) termStrs.add(node.generateCode());
        }

        return SyntacticGraph.stringJoin(termStrs, " | ");
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