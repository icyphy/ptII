/* A State space model simulator.

 Copyright (c) 2009-2015 The Regents of the University of California.
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
package org.ptolemy.ssm;

import java.util.Set;

import ptolemy.data.BooleanToken; 
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Decorator;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
///////////////////////////////////////////////////////////////////
////ParticleFilterSSM

/**
An actor that simulates a state space model that decorates itself.
Defines an initial value for the state and simulates the model from there.

@author Ilge Akkaya
@version $Id$
@since Ptolemy II 10.1
@Pt.ProposedRating Red (ilgea)
@Pt.AcceptedRating
 */
public class ConstrainedStateSpaceSimulator extends StateSpaceSimulator {

    public ConstrainedStateSpaceSimulator(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        maxTrialsToSatisfyConstraints = new Parameter(this,"maxTrialsToSatisfyConstraints");
        maxTrialsToSatisfyConstraints.setExpression("1000");
        _mapDecorator = null;
    }

    public ConstrainedStateSpaceSimulator(Workspace workspace)
            throws NameDuplicationException, IllegalActionException {
        super(workspace);
        _mapDecorator = null;
    }

    public Parameter maxTrialsToSatisfyConstraints;

    public void attributeChanged(Attribute attribute) throws IllegalActionException {
        if (attribute == maxTrialsToSatisfyConstraints) {
            MAX_TRIALS = ((IntToken)maxTrialsToSatisfyConstraints.getToken()).intValue();
        } else {
            super.attributeChanged(attribute);
        }
    }


    /** Clone the object into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new NamedObj.
     *  @exception CloneNotSupportedException If any of the attributes
     *   cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ConstrainedStateSpaceSimulator newObject = (ConstrainedStateSpaceSimulator) super
                .clone(workspace);
        newObject._mapDecorator = null; 
        return newObject;
    }
 

    /**
     * Check if the Actor is associated with a unique enabled StateSpaceModel. Ideally,
     * here, we would also be checking whether the enabled decorator provides the parameters
     * expected by the actor.
     * @exception IllegalActionException
     */
    @Override
    public boolean validDecoratorAssociationExists() throws IllegalActionException {
        if ( !super.validDecoratorAssociationExists()) {
            return false;
        }
        boolean found = false;
        Set<Decorator> cachedDecorators = this.decorators();
        for (Decorator d : cachedDecorators) {
            if (d instanceof Map) {
                Parameter isEnabled = (Parameter) this.getDecoratorAttribute(d, "enable");
                if ( ((BooleanToken)isEnabled.getToken()).booleanValue()) {
                    if (!found) {
                        found = true;
                        _mapDecorator = (Map) d;
                    } else {
                        throw new IllegalActionException(this, "A StateSpaceActor "
                                + "can be associated with exactly one Map "
                                + "at a time.");
                    }
                }
            }
        }
        return found;
    }


    @Override
    public boolean satisfiesMapConstraints(double[] coordinates) {
        double xCoord = coordinates[0];
        double yCoord = coordinates[1];

        return _mapDecorator.withinValidMapArea(xCoord, yCoord);
    }

    private Map _mapDecorator;   
}
