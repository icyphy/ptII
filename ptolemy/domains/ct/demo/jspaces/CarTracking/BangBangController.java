/* An actor that does Bang-Bang control

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (liuj@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.demo.jspaces.CarTracking;

import ptolemy.domains.ct.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.lib.TimedActor;

//////////////////////////////////////////////////////////////////////////
//// BangBangController
/**
Wait for the event for the position of the follower and send
alarm events. The actor has zero delay.

@author Jie Liu
@version $Id$
*/
public class BangBangController extends TypedAtomicActor
    implements TimedActor {

    /** Construct an actor with the specified container and name.
     *  @param container The composite actor to contain this one.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public BangBangController(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        _highValue = 1.0;
        highValue = new Parameter(this, "highValue",
                new DoubleToken(10.0));
        highValue.setTypeEquals(BaseType.DOUBLE);

        _lowValue = 0.0;
        lowValue = new Parameter(this, "lowValue",
                new DoubleToken(10.0));
        lowValue.setTypeEquals(BaseType.DOUBLE);
        
        tooSmall = new TypedIOPort(this, "tooSmall", true, false);
        tooSmall.setTypeEquals(BaseType.BOOLEAN);

        tooLarge = 
            new TypedIOPort(this, "tooLarge", true, false);
        tooLarge.setTypeEquals(BaseType.BOOLEAN);
        
        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       ports and parameters                ////

    /** Threshold for too close. Default value is 10.0;
     */
    public Parameter highValue;

    /** Threshold for too far away. Default value is 50.0;
     */
    public Parameter lowValue;

    /** Input port for the leader position.
     */
    public TypedIOPort tooSmall;

    /** Input port for the follower position.
     */
    public TypedIOPort tooLarge;

    /** Output port for too close.
     */
    public TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute is <i>delay</i>, then check that the value
     *  is non-negative.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the delay is negative.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == highValue ) {
            _highValue =
                ((DoubleToken)((Parameter)attribute).getToken()).doubleValue();
        } else if (attribute == lowValue ) {
            _lowValue=
                ((DoubleToken)((Parameter)attribute).getToken()).doubleValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the parameter.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   has an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        BangBangController newobj = 
            (BangBangController)super.clone(ws);
        newobj.highValue = 
            (Parameter)newobj.getAttribute("highValue");
        newobj.lowValue = 
            (Parameter)newobj.getAttribute("lowValue");
        newobj.tooSmall = 
            (TypedIOPort)newobj.getPort("tooSmall");
        newobj.tooLarge = 
            (TypedIOPort)newobj.getPort("tooLarge");
        newobj.output = 
            (TypedIOPort)newobj.getPort("output");
        return newobj;
    }

    /** set the default output token to the _lowValue.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _lastToken = new DoubleToken(_lowValue);
        
    }

    /** Read one token from the input and save it so that the
     *  postfire method can produce it to the output.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        if (tooSmall.hasToken(0)) {
            if(((BooleanToken)tooSmall.get(0)).booleanValue()) {
                _lastToken = new DoubleToken(_highValue);
            }
        }
        if (tooLarge.hasToken(0)) {
            if(((BooleanToken)tooLarge.get(0)).booleanValue()) {
                _lastToken = new DoubleToken(_lowValue);
            }
        }
        output.send(0, _lastToken);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // latest inputs for leader.
    private double _tooSmall;

    // Last output token, if nothing changed in this iteration, 
    // just output it.
    private DoubleToken _lastToken;

    //
    private double _highValue;

    // away threshold
    private double _lowValue;

}
