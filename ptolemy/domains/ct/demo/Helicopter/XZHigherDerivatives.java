/* Compute the third and fourth derivatives of X and Z.

 Copyright (c) 1998-2000 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARIMath.sinG OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
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
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)

*/

package ptolemy.domains.ct.demo.Helicopter;

import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.actor.lib.TimedActor;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.*;
import ptolemy.domains.ct.kernel.*;
import ptolemy.domains.ct.lib.*;

//////////////////////////////////////////////////////////////////////////
//// XZHigherDerivatives
/**
Compute the third and fourth derivatives of Px and Pz
DDDotPx = (Dotth*TM*Cos[th))/m + (DotTM*Math.sin[th))/m

DDDDotPx = (2*Dotth*DotTM*Cos[th))/m +
   (TM*Cos[th)*(a*MM + hM*TM*Math.sin[a)))/(Iy*m) + (DDotTM*Math.sin[th))/m -
   (Dotth^2*TM*Math.sin[th))/m

DDDotPz = -((DotTM*Cos[th))/m) + (Dotth*TM*Math.sin[th))/m

DDDDotPz = -((DDotTM*Cos[th))/m) + (Dotth^2*TM*Cos[th))/m +
   (2*Dotth*DotTM*Math.sin[th))/m +
   (TM*(a*MM + hM*TM*Math.sin[a))*Math.sin[th))/(Iy*m)

@author  liuj
@version $Id$

*/
public class XZHigherDerivatives extends TypedAtomicActor
    implements TimedActor{
    /** Construct the actor, all parameters take the default value.
     * @param container The TypedCompositeActor this star belongs to
     * @param name The name
     * @exception NameDuplicationException If another star already had
     * this name.
     * @exception IllegalActionException If there is an internal error.
     */
    public XZHigherDerivatives(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        inputTm = new TypedIOPort(this, "inputTm");
        inputTm.setInput(true);
        inputTm.setOutput(false);
        inputTm.setMultiport(false);
        inputTm.setTypeEquals(BaseType.DOUBLE);

        inputDTm = new TypedIOPort(this, "inputDTm");
        inputDTm.setInput(true);
        inputDTm.setOutput(false);
        inputDTm.setMultiport(false);
        inputDTm.setTypeEquals(BaseType.DOUBLE);

        inputDDTm = new TypedIOPort(this, "inputDDTm");
        inputDDTm.setInput(true);
        inputDDTm.setOutput(false);
        inputDDTm.setMultiport(false);
        inputDDTm.setTypeEquals(BaseType.DOUBLE);

        inputA = new TypedIOPort(this, "inputA");
        inputA.setInput(true);
        inputA.setOutput(false);
        inputA.setMultiport(false);
        inputA.setTypeEquals(BaseType.DOUBLE);

        inputTh = new TypedIOPort(this, "inputTh");
        inputTh.setInput(true);
        inputTh.setOutput(false);
        inputTh.setMultiport(false);
        inputTh.setTypeEquals(BaseType.DOUBLE);

        inputDTh = new TypedIOPort(this, "inputDTh");
        inputDTh.setInput(true);
        inputDTh.setOutput(false);
        inputDTh.setMultiport(false);
        inputDTh.setTypeEquals(BaseType.DOUBLE);

        outputD3Px = new TypedIOPort(this, "outputD3Px");
        outputD3Px.setInput(false);
        outputD3Px.setOutput(true);
        outputD3Px.setMultiport(false);
        outputD3Px.setTypeEquals(BaseType.DOUBLE);

        outputD4Px = new TypedIOPort(this, "outputD4Px");
        outputD4Px.setInput(false);
        outputD4Px.setOutput(true);
        outputD4Px.setMultiport(false);
        outputD4Px.setTypeEquals(BaseType.DOUBLE);

        outputD3Pz = new TypedIOPort(this, "outputD3Pz");
        outputD3Pz.setInput(false);
        outputD3Pz.setOutput(true);
        outputD3Pz.setMultiport(false);
        outputD3Pz.setTypeEquals(BaseType.DOUBLE);

        outputD4Pz = new TypedIOPort(this, "outputD4Pz");
        outputD4Pz.setInput(false);
        outputD4Pz.setOutput(true);
        outputD4Pz.setMultiport(false);
        outputD4Pz.setTypeEquals(BaseType.DOUBLE);

        _Iy = (double)0.271256;
        paramIy = new Parameter(this, "Iy", new DoubleToken(_Iy));

        _hm = (double)0.2943;
        paramHm = new Parameter(this, "hm", new DoubleToken(_hm));

        _Mm = (double)25.23;
        paramMm = new Parameter(this, "Mm", new DoubleToken(_Mm));

        _mass = (double)4.9;
        paramMass = new Parameter(this, "Mass", new DoubleToken(_mass));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Compute the output.
     *
     *  @exception IllegalActionException If there's no input token
     *        when needed.
     */
    public void fire() throws IllegalActionException{
        double Tm = ((DoubleToken)inputTm.get(0)).doubleValue();
        double DTm = ((DoubleToken)inputDTm.get(0)).doubleValue();
        double DDTm = ((DoubleToken)inputDDTm.get(0)).doubleValue();
        double Th = ((DoubleToken)inputTh.get(0)).doubleValue();
        double DTh = ((DoubleToken)inputDTh.get(0)).doubleValue();
        double A = ((DoubleToken)inputA.get(0)).doubleValue();

        double CosTh2 = Math.pow(Math.cos(Th), 2);
        double SinTh2 = Math.pow(Math.sin(Th), 2);
        double mass2 = _mass*_mass;

        double D3Px = (DTh*Tm*Math.cos(Th))/_mass + (DTm*Math.sin(Th))/_mass;

        double D4Px = (2.0*DTh*DTm*Math.cos(Th))/_mass +
            (Tm*Math.cos(Th)*(A*_Mm + _hm*Tm*Math.sin(A)))/(_Iy*_mass) +
            (DDTm*Math.sin(Th))/_mass - (DTh*DTh*Tm*Math.sin(Th))/_mass;

        double D3Pz = -((DTm*Math.cos(Th))/_mass) +(DTh*Tm*Math.sin(Th))/_mass;

        double D4Pz = -((DDTm*Math.cos(Th))/_mass) +
            (DTh*DTh*Tm*Math.cos(Th))/_mass + (2.0*DTh*DTm*Math.sin(Th))/_mass
            + (Tm*(A*_Mm + _hm*Tm*Math.sin(A))*Math.sin(Th))/(_Iy*_mass);

        outputD3Px.broadcast(new DoubleToken(D3Px));
        outputD4Px.broadcast(new DoubleToken(D4Px));
        outputD3Pz.broadcast(new DoubleToken(D3Pz));
        outputD4Pz.broadcast(new DoubleToken(D4Pz));
    }


    /** Update the parameter if they have been changed.
     *  The new parameter will be used only after this method is called.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void updateParameters() throws IllegalActionException {
        _Iy = ((DoubleToken)paramIy.getToken()).doubleValue();
        _hm = ((DoubleToken)paramHm.getToken()).doubleValue();
        _Mm = ((DoubleToken)paramMm.getToken()).doubleValue();
        _mass = ((DoubleToken)paramMass.getToken()).doubleValue();
    }

    /** Input port Tm
     */
    public TypedIOPort inputTm;

    /** Input port DTm = dTm/dt
     */
    public TypedIOPort inputDTm;

    /** Input port DDTm = ddTm/dtt
     */
    public TypedIOPort inputDDTm;

    /** Input port a
     */
    public TypedIOPort inputA;

    /** Input port Th
     */
    public TypedIOPort inputTh;

    /** Input port DTh
     */
    public TypedIOPort inputDTh;


    /** Output port D3Px = dddPx/dttt
     */
    public TypedIOPort outputD3Px;

    /** Output port D4Px = ddddPx/dtttt
     */
    public TypedIOPort outputD4Px;

    /** Output port D3Pz = dddPz/dttt
     */
    public TypedIOPort outputD3Pz;

    /** Output port D4Pz = ddddPz/dtttt
     */
    public TypedIOPort outputD4Pz;

    /** Parameter for Iy, double, default 0.271256.
     */
    public Parameter paramIy;

    /** Parameter for hm, double, default 0.2943.
     */
    public Parameter paramHm;

    /** Parameter for Mm, double, default 25.23.
     */
    public Parameter paramMm;

    /** Parameter for the mass, double, default 4.9.
     */
    public Parameter paramMass;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private double _Iy;

    private double _hm;

    private double _Mm;

    private double _mass;
}
