/* A helper class for ptolemy.actor.lib.Limiter.

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
package ptolemy.data.properties.lattice.logicalAND.actor.lib;

import java.util.List;

import ptolemy.data.ScalarToken;
import ptolemy.data.properties.lattice.MonotonicFunction;
import ptolemy.data.properties.lattice.PropertyConstraintSolver;
import ptolemy.data.properties.lattice.PropertyTerm;
import ptolemy.data.properties.lattice.logicalAND.Lattice;
import ptolemy.data.properties.lattice.logicalAND.actor.AtomicActor;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// Limiter

/**
 A helper class for ptolemy.actor.lib.Limiter.

 @author Thomas Mandl, Man-Kit Leung
 @version $Id: Limiter.java,v 1.2 2007/06/13 22:41:54 mankit Exp $
 @since Ptolemy II 6.2
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
*/
public class Limiter extends AtomicActor {
    /**
     * Construct an Limiter helper.
     * @param actor the associated actor
     * @throws IllegalActionException 
     */
    public Limiter(PropertyConstraintSolver solver, 
            ptolemy.actor.lib.Limiter actor) throws IllegalActionException {

        super(solver, actor, false);        
        _lattice = (Lattice) getSolver().getLattice();
        _actor = actor;        
   }
       
    public List<Inequality> constraintList() throws IllegalActionException {
        setAtLeast(_actor.output, new FunctionTerm());

        return super.constraintList();
    }
        
    protected List<Attribute> _getPropertyableAttributes() {
        List<Attribute> result = super._getPropertyableAttributes();
        result.add(_actor.top);
        result.add(_actor.bottom);
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private ptolemy.actor.lib.Limiter _actor;
    private Lattice _lattice;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    // This class implements a monotonic function of the input port
    // type. The result of the function is the same as the input type
    // if is not Complex; otherwise, the result is Double.
    private class FunctionTerm 
    extends MonotonicFunction implements PropertyTerm {
    
        ///////////////////////////////////////////////////////////////
        ////                       public inner methods            ////
    
        /** Return the function result.
         *  @return A Type.
         */
        public Object getValue() {
          
            try {
                if ((getSolver().getProperty(_actor.bottom) == _lattice.TRUE) &&
                    (getSolver().getProperty(_actor.top) == _lattice.TRUE) &&
                    (!((ScalarToken)_actor.bottom.getToken()).isLessThan(
                      ((ScalarToken)_actor.top.getToken())).booleanValue())) {
                        
                    return(_lattice.TRUE);
                }
            } catch (IllegalActionException e) {
                assert false;
                return(_lattice.FALSE);
            } 

            return(getSolver().getProperty(_actor.input));
        }
    
        public boolean isEffective() {
            return true;
        }

        public void setEffective(boolean isEffective) {
        }

        protected InequalityTerm[] _getDependentTerms() {
            return new InequalityTerm[] {            
                getPropertyTerm(_actor.input)                
            };
        }

        ///////////////////////////////////////////////////////////////
        ////                       private inner variable          ////
    }
}

