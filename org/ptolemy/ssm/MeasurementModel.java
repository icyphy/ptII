/* An abstract measurement model.

 Copyright (c) 2014 The Regents of the University of California.
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

import java.util.ArrayList;
import java.util.List;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.ColorAttribute;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Decorator;
import ptolemy.kernel.util.DecoratorAttributes;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
/**
An abstract decorator that defines a measurement model.

@author Ilge Akkaya 
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (ilgea)
@Pt.AcceptedRating
 */
public abstract class MeasurementModel extends MirrorDecorator 
implements StateSpaceActor {

    public MeasurementModel(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name); 
        _init();
    }

    /**
     * The measurement equation that will refer to the state space model.
     */
    public TypedIOPort z;

    /**
     * The measurement port
     */
    public Parameter zParameter;

    


    @Override
    public DecoratorAttributes createDecoratorAttributes(NamedObj target) {
        if (target instanceof InferenceActor) {
            try {
                MeasurementModelAttributes ssa = new MeasurementModelAttributes(target, this);
                registerListener(ssa);
                return ssa;
            } catch (KernelException ex) {
                // This should not occur.
                throw new InternalErrorException(ex);
            }
        } else {
            return null;
        }
    }

    @Override
    public List<NamedObj> decoratedObjects() throws IllegalActionException {
        if (workspace().getVersion() == _decoratedObjectsVersion) {
            return _decoratedObjects;
        }
        _decoratedObjectsVersion = workspace().getVersion();
        List<NamedObj> objectList = new ArrayList<>();
        CompositeEntity container = (CompositeEntity) getContainer(); 
        if (container != null) {
            for (Object object : container.deepEntityList()) {
                if (object instanceof InferenceActor) {
                    objectList.add((NamedObj)object); 
                } 
            }
            _decoratedObjects = objectList;
        }
        return objectList;

    }
    @Override
    public boolean validUniqueDecoratorAssociationExists()
            throws IllegalActionException {
        boolean found = false;
        for (Decorator d : this.decorators()) {
            if (d instanceof StateSpaceModel) {
                Parameter isEnabled = (Parameter) this.getDecoratorAttribute(d, "enable");
                if ( ((BooleanToken)isEnabled.getToken()).booleanValue()) {
                    if (!found) {
                        found = true; 
                    } else {
                        throw new IllegalActionException(this, "A StateSpaceActor "
                                + "can be associated with exactly one StateSpaceModel "
                                + "at a time.");
                    }
                }
            }
        }
        return found;
    } 

    public String getMeasurementParameterPostfix() {
        return MEASUREMENT_PARAMETER_POSTFIX;
    }

    private void _init() throws IllegalActionException, NameDuplicationException {

        z = new TypedIOPort(this, "z", false, true);

        zParameter = new Parameter(this,"zParameter");
        zParameter.setDisplayName("z");
        zParameter.setExpression(""); 

        ColorAttribute color = new ColorAttribute(this,
                "decoratorHighlightColor");
        color.setExpression("{0.4,0.2,1.0,1.0}");
    }

    private static final String MEASUREMENT_PARAMETER_POSTFIX = "Parameter";
}
