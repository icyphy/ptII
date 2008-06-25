/* A helper class for ptolemy.actor.lib.Recorder.

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
package ptolemy.data.properties.lattice.logicalOrBackward.actor.lib;

import java.util.List;

import ptolemy.data.IntToken;
import ptolemy.data.properties.lattice.PropertyConstraintSolver;
import ptolemy.data.properties.lattice.logicalOrBackward.Lattice;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// Recorder

/**
 A helper class for ptolemy.actor.lib.Recorder.

 @author Thomas Mandl, Man-Kit Leung
 @version $Id: Recorder.java,v 1.2 2007/06/13 22:41:54 mankit Exp $
 @since Ptolemy II 6.2
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
*/
public class Recorder extends Sink {
    /**
     * Construct an Recorder helper.
     * @param actor the associated actor
     * @throws IllegalActionException 
     */
    public Recorder(PropertyConstraintSolver solver, 
            ptolemy.actor.lib.Recorder actor) throws IllegalActionException {
        super(solver, actor, false);
    }

    public List<Inequality> constraintList() throws IllegalActionException {
        ptolemy.actor.lib.Recorder actor =
            (ptolemy.actor.lib.Recorder) getComponent();
        
        Lattice lattice = (Lattice) getSolver().getLattice();        

        if (((IntToken)actor.capacity.getToken()).intValue() > 0) {
            setEquals(actor.input, lattice.TRUE);            
        } else {
            setEquals(actor.input, lattice.FALSE);
        }

        return super.constraintList();
    } 
}
