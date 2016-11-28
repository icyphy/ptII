/*
  Below is the copyright agreement for the Ptolemy II system.

  Copyright (c) 2014-2016 The Regents of the University of California.
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

  Ptolemy II includes the work of others, to see those copyrights, follow
  the copyright link on the splash page or see copyright.htm.
*/
package org.ptolemy.ssm;

import java.util.ArrayList;
import java.util.List;

import org.ptolemy.ssm.MirrorDecoratorListener.DecoratorEvent;

import ptolemy.actor.gui.ColorAttribute;
import ptolemy.data.ArrayToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// StateSpaceModel

/**
 * A decorator that implements a State Space Model.
 * @author Ilge Akkaya
 * @version $Id$
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class StateSpaceModel extends MirrorDecorator {

    /** Construct a StateSpaceModel with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public StateSpaceModel(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////


    /** An expression for the prior distribution from which the
     * samples are drawn.
     */
    public Parameter prior;

    /** The process noise. If the system contains multiple state
     * variables, the process noise should be an expression that
     * returns an ArrayToken. See multivariateGaussian for one such
     * function.
     */
    public Parameter processNoise;

    /** An expression for a prior distribution from which the initial
     * particles are sampled
     */
    public Parameter priorDistribution;

    /** The names of the state variables, in an array of strings.
     *  The default is an ArrayToken of an empty String.
     */
    public Parameter stateVariableNames;

    /** The value of current time. This parameter is not visible in
     *  the expression screen except in expert mode. Its value
     *  initially is just 0.0, a double, but upon each firing, it is
     *  given a value equal to the current time as reported by the
     *  director.
     */
    public Parameter t;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the argument is the <i>stateVariableNames</i> parameter,
     *  then create hidden parameters that correspond to the parameter.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If
     *   <i>stateVariableNamest</i> cannot be evaluated or cannot be
     *   converted to the output type, or if the superclass throws it.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {

        if (attribute == stateVariableNames) {

            sendParameterEvent(DecoratorEvent.CHANGED_PARAMETER, stateVariableNames);
            // create a hidden parameter that corresponds to the specified state variable, if not already present
            ArrayToken names = (ArrayToken) stateVariableNames.getToken();
            String stateName = ((StringToken) names.getElement(0))
                .stringValue();
            List<String> temp = new ArrayList<>();

            if (stateName.length() > 0) {
                // Set the output type according to the state variables
                try {
                    // create missing parameters for the newly added state variables.
                    for (int i = 0; i < names.length(); i++) {
                        stateName = ((StringToken) names.getElement(i))
                            .stringValue();
                        temp.add(stateName);
                        // check if this state name already existed before
                        if (!_cachedStateVariableNames.contains(stateName)) {
                            Parameter y = (Parameter) this.getAttribute(stateName);
                            if ( y == null
                                    && stateName.length() != 0) {
                                y = new Parameter(this, stateName);
                                y.setExpression("0.0");
                                sendParameterEvent(DecoratorEvent.ADDED_PARAMETER, y);
                            }
                            // FindBugs: Possible null pointer dereference of y.
                            if (y != null) {
                                y.setVisibility(Settable.NONE);
                            }
                            if (this.getAttribute(stateName+"_update") == null) {
                                Parameter yUpdate = new Parameter(this, stateName+"_update");
                                yUpdate.setExpression(stateName);
                                sendParameterEvent(DecoratorEvent.ADDED_PARAMETER, yUpdate);
                            }
                            _cachedStateVariableNames.add(stateName);
                        }
                    }
                    // remove parameters corresponding to obsolete state variables.
                    for (String old : _cachedStateVariableNames) {
                        if (! temp.contains(old)) {
                            Parameter yUpdate = (Parameter) this.getAttribute(old+"_update");
                            sendParameterEvent(DecoratorEvent.REMOVED_PARAMETER,yUpdate);
                            if (yUpdate != null) {
                                yUpdate.setContainer(null);
                            }
                            Parameter y = (Parameter) this.getAttribute(old);
                            sendParameterEvent(DecoratorEvent.REMOVED_PARAMETER,y);
                            if (y != null) {
                                y.setContainer(null);
                            }
                            _cachedStateVariableNames.remove(old);
                        }
                    }
                } catch (NameDuplicationException e) {
                    // should not happen
                    throw new InternalErrorException("Duplicate field in " + this.getName());
                }
            }
        } else {
            // FIXME: If the attribute is changed in the SSM, this needs to be propagated to the
            // container StateSpaceActor b/c we likely would like to change the expressions accordingly
            super.attributeChanged(attribute);
        }
    }


    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        StateSpaceModel newObject = (StateSpaceModel) super
            .clone(workspace);
        newObject._cachedStateVariableNames = new ArrayList<>();
        return newObject;
    }


    /** Initialize the class. */
    private void _init() throws IllegalActionException,
            NameDuplicationException {

        //create parameters for the initial state variable names here.
        String[] names = {"x", "y"};
        for (int i=0; i < names.length; i++) {
            String stateName = names[i];
            Parameter y = new Parameter(this, stateName);
            y.setExpression("0.0");
            Parameter yUpdate = new Parameter(this, stateName.concat("_update"));
            yUpdate.setExpression(stateName);
        }

        stateVariableNames = new Parameter(this, "stateVariableNames");
        stateVariableNames.setExpression("{\"x\",\"y\"}");
        stateVariableNames.setTypeEquals(new ArrayType(BaseType.STRING));

        prior = new Parameter(this, "prior");
        prior.setExpression("{random()*200-100,random()*200-100}");
        prior.setTypeEquals(new ArrayType(BaseType.DOUBLE));

        processNoise = new Parameter(this, "processNoise");
        processNoise.setExpression("multivariateGaussian({0.0,0.0},[1.0,0.4;0.4,1.2])");


        t = new Parameter(this, "t");
        t.setTypeEquals(BaseType.DOUBLE);
        t.setVisibility(Settable.EXPERT);
        t.setExpression("0.0");

        ColorAttribute color = new ColorAttribute(this,
                "decoratorHighlightColor");
        color.setExpression("{1.0,0.4,0.0,1.0}");


        _cachedStateVariableNames = new ArrayList<>();
    }

    private List<String> _cachedStateVariableNames;
}
