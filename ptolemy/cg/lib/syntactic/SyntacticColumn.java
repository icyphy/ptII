/* Syntactic Column for syntactic representations.

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
 *  @since
 *  @Pt.ProposedRating red (shaver)
 *  @Pt.AcceptedRating red 
 *
 */
public class SyntacticColumn 
    extends LinkedList<SyntacticTerm> implements SyntacticTerm {

    /** Create new empty Syntactic Column. */
    public SyntacticColumn() {
        super();
        _inputs = new LinkedList();
        _outputs = new LinkedList();
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
    public boolean add(SyntacticTerm node) {
        if (contains(node)) return false;
        super.add(node);
        _inputs.addAll(node.getInputs());
        _outputs.addAll(node.getOutputs());
        return true;
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
    public boolean remove(SyntacticTerm node) {
        if (contains(node)) return false;
        _inputs.removeAll(node.getInputs());
        _outputs.removeAll(node.getOutputs());
        super.remove(node);
        return true;
    }

    /** Clear column */
    public void clear() {
        super.clear();
        _inputs.clear();
        _outputs.clear();
    }

    /** Get the index of the syntactic input port in the column.
     *  If the given port is not in the inputs for the 
     *  column null is returned.
     *  @param port Port to find the index of.
     *  @return index of the port or null.
     */
    public Integer inputIndex(SyntacticPort port) {
        int dex = _inputs.indexOf(port);
        return dex < 0 ? null : dex;
    }

    /** Get the index of the syntactic output port in the column.
     *  If the given port is not in the outputs for the 
     *  column null is returned.
     *  @param port Port to find the index of.
     *  @return index of the port or null.
     */
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

    /** Sort constituent terms in column by type order. 
     *  Sorting a column will change its neighboring permutations. */
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

    public int sizeInputs() {
        //return _inputs == null ? 0 : _inputs.size();
        int nins = 0;
        for (SyntacticTerm term : this) {
            nins += term.sizeInputs();
        }
        return nins;
    }
    
    public int sizeOutputs() {
        //return _outputs == null ? 0 : _outputs.size();
        int nouts = 0;
        for (SyntacticTerm term : this) {
            nouts += term.sizeOutputs();
        }
        return nouts;
    }
    
    public Rank rank() {
        return new SyntacticTerm.Rank(sizeOutputs(), 0, sizeInputs(), 0);
    }
    
    public String boundaryCode() {
        return "" + sizeInputs() + " --> " + sizeOutputs();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private LinkedList<SyntacticPort> _inputs;
    private LinkedList<SyntacticPort> _outputs;

}