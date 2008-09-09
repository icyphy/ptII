/* A property constraint helper for composite actor.

 Copyright (c) 2005-2006 The Regents of the University of California.
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
package ptolemy.data.properties.lattice.namingSetLattice.kernel;

import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.data.StringToken;
import ptolemy.data.properties.Property;
import ptolemy.data.properties.lattice.PropertyConstraintCompositeHelper;
import ptolemy.data.properties.lattice.PropertyConstraintSolver;
import ptolemy.data.properties.lattice.PropertySet;
import ptolemy.data.properties.lattice.PropertyConstraintHelper.Inequality;
import ptolemy.data.properties.token.PropertyToken;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// CompositeActor

/**
 A property constraint helper for composite actor.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 6.2
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class CompositeEntity extends PropertyConstraintCompositeHelper {

    /** 
     * Construct a property constraint helper for the given
     * CompositeActor. This is the helper class for any 
     * CompositeActor that does not have a specific defined
     * helper class.
     * @param solver The given solver.
     * @param entity The given CompositeEntity.
     * @throws IllegalActionException 
     */
    public CompositeEntity(PropertyConstraintSolver solver, 
            ptolemy.kernel.CompositeEntity CompositeEntity)
        throws IllegalActionException {

        super(solver, CompositeEntity);
    }
    

    public List<Inequality> constraintList() throws IllegalActionException {
        ptolemy.kernel.CompositeEntity actor = 
            (ptolemy.kernel.CompositeEntity) getComponent();
        
        Property name = new PropertyToken(
                new StringToken(actor.getName()));
        
        PropertySet nameSet = new PropertySet(
                getSolver().getLattice(), new Property[] {name});
        
        for (IOPort port : (List<IOPort>) actor.portList()) {
            setAtLeast(port, nameSet);
        }
        return super.constraintList();
    }
    
    protected List<Attribute> _getPropertyableAttributes() {
        return new LinkedList<Attribute>();
    }
}
