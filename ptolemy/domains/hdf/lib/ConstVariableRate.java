/* A constant source with a variable token producation rate.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Red (vogel@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.hdf.lib;

import ptolemy.actor.Director;
import ptolemy.actor.lib.Const;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// ConstVariableRate
/**
Produce a constant output. The value of the
output is that of the token contained by the <i>value</i> parameter,
which by default is an IntToken with value 1. The type of the output
is that of <i>value</i> parameter.
<p>
The <i>rate</i> parameter specifies the number of tokens to produce
on each firing. The default value is an IntToken with value 1.

@author Brian K. Vogel
@version $Id$
*/

public class ConstVariableRate extends Const {

    /** ConstVariableRateruct a constant source with the given container and name.
     *  Create the <i>value</i> parameter, initialize its value to
     *  the default value of an IntToken with value 1.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ConstVariableRate(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        Parameter tokenProductionRate =
            new Parameter(output, "tokenProductionRate",
                    new IntToken(1));
        rate = new Parameter(this, "rate", new IntToken(1));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The number of tokens to produce on each firing.
     *  By default, it contains an IntToken with value 1.  If the
     *  type of this token is changed during the execution of a model,
     *  then the director will be asked to redo type resolution.
     */
    public Parameter rate;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the argument is the <i>rate</i> parameter, then
     *  set up the consumption and production constants, and invalidate
     *  the schedule of the director.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the parameters are out of range.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == rate) {
            _productionRate =
                ((IntToken)rate.getToken()).intValue();
            if (_productionRate < 0) throw new IllegalActionException(
                    "Rate must be >= 0");
            Parameter tokenProductionRate =
                (Parameter)output.getAttribute("tokenProductionRate");
            tokenProductionRate.setToken(new IntToken(_productionRate));
            Director dir = getDirector();
            if (dir != null) {
                dir.invalidateSchedule();
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the value public variable in the new
     *  object to equal the cloned parameter in that new object.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        ConstVariableRate newObject =
            (ConstVariableRate)super.clone(workspace);
        return newObject;
    }

    /** Send the token in the <i>value</i> parameter to the output.
     *  @exception IllegalActionException If it is thrown by the
     *   send() method sending out the token.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        for (int i = 1; i < _productionRate; i++) {
            output.send(0, value.getToken());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private int _productionRate;
}
