/* A State space particle filter with Map constraints

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
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Decorator;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
A particle filter implementation that expects a state-space model
and several measurements to be tied with itself via decorators.

@author Ilge Akkaya
@version $Id$
@since Ptolemy II 10.1
@Pt.ProposedRating Red (ilgea)
@Pt.AcceptedRating
 */
public class ConstrainedParticleFilter extends ParticleFilter {
    public ConstrainedParticleFilter(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name); 
    }
    /**
     * Check if the Actor is associated with a unique enabled StateSpaceModel. Ideally,
     * here, we would also be checking whether the enabled decorator provides the parameters
     * expected by the actor.
     * @exception IllegalActionException
     */
    @Override
    public boolean validDecoratorAssociationExists()
            throws IllegalActionException {
        if (!super.validDecoratorAssociationExists()) {
            return false;
        }
        boolean found = false;
        Set<Decorator> cachedDecorators =decorators();
        for (Decorator d : cachedDecorators) {
            if (d instanceof Map) {
                Parameter isEnabled = (Parameter) getDecoratorAttribute(d,
                        "enable");
                if (((BooleanToken) isEnabled.getToken()).booleanValue()) {
                    if (!found) {
                        found = true;
                        _mapDecorator = (Map) d;
                    } else {
                        throw new IllegalActionException(
                                this,
                                "A StateSpaceActor "
                                        + "must be associated with exactly one Map decorator"
                                        + "at a time.");
                    }
                }
            }  
        }
        return found;
    }

    
    @Override
    public boolean satisfiesMapConstraints(double[] coordinates) {
        return _mapDecorator.withinValidMapArea(coordinates[0], coordinates[1]);
    }
    private Map _mapDecorator; 
}
