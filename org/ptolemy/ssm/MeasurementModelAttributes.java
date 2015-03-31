/* Measurement Model Attributes
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

import ptolemy.data.ArrayToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.Decorator;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;
///////////////////////////////////////////////////////////////////
////MeasurementModelAttributes

/**Attribute generator class for the MeasurementModel

@see org.ptolemy.ssm.MeasurementModel.java

@author Ilge Akkaya
@version $Id$
@since Ptolemy II 10.1
@Pt.ProposedRating Red (ilgea)
@Pt.AcceptedRating
*/
public class MeasurementModelAttributes extends MirrorDecoratorAttributes {

    /** Constructor to use when editing a model.
     *  @param target The object being decorated.
     *  @param decorator The decorator.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    public MeasurementModelAttributes(NamedObj target, Decorator decorator)
            throws IllegalActionException, NameDuplicationException {
        super(target, decorator); 
        _addedContainerParameters = new ArrayList<>();
    }

    /**
     * Constructs a MeasurementModelAttributes object.
     *
     * @param target  The object being decorated.
     * @param name    The decorator name.
     * @throws IllegalActionException If the superclass throws it.
     * @throws NameDuplicationException If the superclass throws it.
     */
    public MeasurementModelAttributes(NamedObj target, String name)
            throws IllegalActionException, NameDuplicationException {
        super(target, name); 
        _addedContainerParameters = new ArrayList<>();
    }


    /**
     * Add the state space variables defined in the scope of this decorator to the
     * container Actor.
     */
    public void addStateSpaceVariablesToContainer() {
        Parameter stateVariableNames = (Parameter) this.getAttribute("stateVariableNames");
        if (stateVariableNames != null) {
            try {
                if (stateVariableNames.getToken() != null) {
                    Token[] tokens = ((ArrayToken)stateVariableNames.getToken()).arrayValue();
                    synchronized (this.getContainer().attributeList()) {
                        for (Token t : tokens) {
                            String name = ((StringToken)t).stringValue();
                            Parameter containerParam = (Parameter) this.getContainer().getAttribute(name);
                            Parameter thisParam = (Parameter) this.getAttribute(name);

                            if (thisParam != null && containerParam == null) {
                                containerParam = new Parameter(this.getContainer(), name);
                                _addedContainerParameters.add(name);
                                containerParam.setExpression(thisParam.getExpression());
                                containerParam.setVisibility(Settable.NONE); 
                                thisParam.setVisibility(Settable.NONE);
                            } 
                        }
                    }
                }
            } catch (IllegalActionException e) {
                throw new InternalErrorException(e);
            } catch (NameDuplicationException e) {
                throw new InternalErrorException(e);
            }
        }
    }


    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {

        MeasurementModelAttributes result = (MeasurementModelAttributes) super.clone(workspace); 
        result._addedContainerParameters = null;
        return result;
    }
 
    @Override
    public void decorateContainer() {
        super.decorateContainer();
        addStateSpaceVariablesToContainer();

    }

 
    @Override
    public void removeDecorationsFromContainer() 
            throws IllegalActionException, NameDuplicationException { 
        super.removeDecorationsFromContainer();
        removeStateSpaceVariablesFromContainer();
    }

    /**
     * Remove the state space variables defined in the scope of this decorator from the
     * container Actor.
     */
    public void removeStateSpaceVariablesFromContainer() {
        Parameter stateVariableNames = (Parameter) this.getAttribute("stateVariableNames");
        if (stateVariableNames != null) {
            try {
                if (stateVariableNames.getToken() != null) {

                    Token[] tokens = ((ArrayToken)stateVariableNames.getToken()).arrayValue(); 
                    synchronized (this.getContainer().attributeList()) {
                        for (Token t : tokens) {
                            String name = ((StringToken)t).stringValue();
                            if (_addedContainerParameters.contains(name)) {
                                Parameter containerParam = (Parameter) this.getContainer().getAttribute(name); 

                                if (containerParam != null) {
                                    this.getContainer().removeAttribute(containerParam);
                                    _addedContainerParameters.remove(name);
                                } 
                            }
                        } 
                    }
                }
            } catch (IllegalActionException e) {
                throw new InternalErrorException(e);
            } 
        }
    } 
    /** Cached list of parameters added to the container by this decorator*/
    private List<String> _addedContainerParameters; 
}

