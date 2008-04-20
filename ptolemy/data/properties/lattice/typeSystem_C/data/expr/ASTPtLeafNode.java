/* A helper class for ptolemy.data.expr.ASTPtRootNode.

 Copyright (c) 2006 The Regents of the University of California.
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

package ptolemy.data.properties.lattice.typeSystem_C.data.expr;

import java.util.Arrays;
import java.util.List;

import ptolemy.data.expr.Constants;
import ptolemy.data.properties.lattice.PropertyConstraintASTNodeHelper;
import ptolemy.data.properties.lattice.PropertyConstraintSolver;
import ptolemy.data.properties.lattice.typeSystem_C.Lattice;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// AddSubtract

/**
 A helper class for ptolemy.data.expr.ASTPtRootNode.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 6.2
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class ASTPtLeafNode extends PropertyConstraintASTNodeHelper {

    public ASTPtLeafNode(PropertyConstraintSolver solver, 
            ptolemy.data.expr.ASTPtLeafNode node) throws IllegalActionException {
        super(solver, node, false);
    }
    
   public List<Inequality> constraintList() throws IllegalActionException {
       ptolemy.data.expr.ASTPtLeafNode node = 
           (ptolemy.data.expr.ASTPtLeafNode) getComponent();

       Lattice lattice = (Lattice) getSolver().getLattice();

       String name = node.getName();
       
       if (name != null) {
           if (_doubleleafes.contains(name)) {
               setEquals(node, lattice.DOUBLE);
           } else if (Constants.get(name) != null) {
               setEquals(node, lattice.convertJavaToCtype(Constants.get(name).getType(), Constants.get(name)));
//FIXME: Do we need to set up default constraints for inputs here?
//           } else {
//               _useDefaultConstraints = true;                                
           }
       } else if (node.isConstant()) {
           setEquals(node, lattice.convertJavaToCtype(node.getToken().getType(), node.getToken()));
//           setEquals(node, lattice.UNKNOWN);        
       }

       return super.constraintList();
   }

   private static List _doubleleafes = Arrays.asList( 
           new String[]{ "PI"
   });        
   
   public boolean isEffective() {
       return true;
   }

   public void setEffective(boolean isEffective) {
       
   }
}
