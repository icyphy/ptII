/* Dataflow representation of a Soot Block

 Copyright (c) 2001-2002 The Regents of the University of California.
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
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.copernicus.jhdl;

import soot.jimple.InvokeExpr;
import soot.toolkits.graph.Block;

import ptolemy.graph.Node;
import ptolemy.copernicus.jhdl.util.JHDLUnsupportedException;

import java.util.HashSet;

public class RequiredBlockDataFlowGraph extends BlockDataFlowGraph {

  public RequiredBlockDataFlowGraph(Block block) throws JHDLUnsupportedException {
    super(block);

  }

  public HashSet getRequiredNodeSet() {

    if (_requiredNodeSet == null) {
      _requiredNodeSet = new HashSet();
    }

    return _requiredNodeSet;
  }

  protected Node _processInvokeExpr(InvokeExpr ie)
    throws JHDLUnsupportedException {

    if (_requiredNodeSet == null) {
      _requiredNodeSet = new HashSet();
    }

    //The data flattening will start from output ports, so
    //they are the required nodes
    if (ie.getMethod().getName().equals("send")){
      _requiredNodeSet.add(ie);
    }

    return super._processInvokeExpr(ie);
  }


  protected HashSet _requiredNodeSet;

}
