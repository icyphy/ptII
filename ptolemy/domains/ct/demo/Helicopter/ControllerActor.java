/* The controller for the 2-D helicopter.

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
//// ControllerActor
/**
The controller for the helicopter. It has the form:
<pre><code>
    +-         -+         +-    +-  -+ -+
    | dddTm/dttt|         |     | Vx |  |
    |           | = inv(K)|-b + |    |  |
    |    dA/dt  |         |     | Vz |  |
    +-         -+         +-    +-  -+ -+
</code></pre>
where
<pre><code>
          [kInv11 kInv12]
inv(K) =  [             ]
          [kInv21 kInv22]
and
kInv11 = ((MM*TM*Sin[th])/(Iy*m) + (hM*TM^2*Cos[a]*Sin[th])/(Iy*m))/
   ((MM*TM*Cos[th]^2)/(Iy*m^2) + (hM*TM^2*Cos[a]*Cos[th]^2)/(Iy*m^2) +
     (MM*TM*Sin[th]^2)/(Iy*m^2) + (hM*TM^2*Cos[a]*Sin[th]^2)/(Iy*m^2))

kInv12 = (-((MM*TM*Cos[th])/(Iy*m)) - (hM*TM^2*Cos[a]*Cos[th])/(Iy*m))/
   ((MM*TM*Cos[th]^2)/(Iy*m^2) + (hM*TM^2*Cos[a]*Cos[th]^2)/(Iy*m^2) +
     (MM*TM*Sin[th]^2)/(Iy*m^2) + (hM*TM^2*Cos[a]*Sin[th]^2)/(Iy*m^2))

kInv21 = Cos[th]/
   (m*((MM*TM*Cos[th]^2)/(Iy*m^2) + (hM*TM^2*Cos[a]*Cos[th]^2)/(Iy*m^2) +
       (MM*TM*Sin[th]^2)/(Iy*m^2) + (hM*TM^2*Cos[a]*Sin[th]^2)/(Iy*m^2)))

kInv22 = Sin[th]/
   (m*((MM*TM*Cos[th]^2)/(Iy*m^2) + (hM*TM^2*Cos[a]*Cos[th]^2)/(Iy*m^2) +
       (MM*TM*Sin[th]^2)/(Iy*m^2) + (hM*TM^2*Cos[a]*Sin[th]^2)/(Iy*m^2)))

    [b1]
b = [  ]
    [b2]

b1 = (3*DDotTM*Dotth*Cos[th])/m - (Dotth^3*TM*Cos[th])/m +
   (DotTM*hM*TM*Cos[th]*Sin[a])/(Iy*m) +
   (3*DotTM*Cos[th]*(a*MM + hM*TM*Sin[a]))/(Iy*m) -
   (3*Dotth^2*DotTM*Sin[th])/m -
   (3*Dotth*TM*(a*MM + hM*TM*Sin[a])*Sin[th])/(Iy*m)

b2 = (3*Dotth^2*DotTM*Cos[th])/m +
   (3*Dotth*TM*Cos[th]*(a*MM + hM*TM*Sin[a]))/(Iy*m) +
   (3*DDotTM*Dotth*Sin[th])/m - (Dotth^3*TM*Sin[th])/m +
   (DotTM*hM*TM*Sin[a]*Sin[th])/(Iy*m) +
   (3*DotTM*(a*MM + hM*TM*Sin[a])*Sin[th])/(Iy*m)
</pre>
The input of the actors are Tm, DTm, DDTm, A, Th, DTh, Vx, and Vz
The outputs are DDDTm, and DA
@author  Jie Liu
@version $Id$

*/
public class ControllerActor extends TypedAtomicActor
    implements TimedActor {
    /** Construct the actor, all parameters take the default value.
     * @param container The TypedCompositeActor this star belongs to
     * @param name The name
     * @exception NameDuplicationException If another star already had
     * this name.
     * @exception IllegalActionException If there is an internal error.
     */
    public ControllerActor(TypedCompositeActor container, String name)
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

        inputVx = new TypedIOPort(this, "inputVx");
        inputVx.setInput(true);
        inputVx.setOutput(false);
        inputVx.setMultiport(false);
        inputVx.setTypeEquals(BaseType.DOUBLE);

        inputVz = new TypedIOPort(this, "inputVz");
        inputVz.setInput(true);
        inputVz.setOutput(false);
        inputVz.setMultiport(false);
        inputVz.setTypeEquals(BaseType.DOUBLE);

        outputDDDTm = new TypedIOPort(this, "outputDDDTm");
        outputDDDTm.setInput(false);
        outputDDDTm.setOutput(true);
        outputDDDTm.setMultiport(false);
        outputDDDTm.setTypeEquals(BaseType.DOUBLE);

        outputDA = new TypedIOPort(this, "outputDA");
        outputDA.setInput(false);
        outputDA.setOutput(true);
        outputDA.setMultiport(false);
        outputDA.setTypeEquals(BaseType.DOUBLE);

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
        double Vx = ((DoubleToken)inputVx.get(0)).doubleValue();
        double Vz = ((DoubleToken)inputVz.get(0)).doubleValue();

        double CosTh2 = Math.pow(Math.cos(Th), 2);
        double SinTh2 = Math.pow(Math.sin(Th), 2);
        double mass2 = _mass*_mass;
        // compute inv(K)
        double IK11 = ((_Mm*Tm*Math.sin(Th))/(_Iy*_mass) +
                (_hm*Tm*Tm*Math.cos(A)*Math.sin(Th))/(_Iy*_mass)) /
            ((_Mm*Tm*CosTh2)/(_Iy*mass2) +
                    (_hm*Tm*Tm*Math.cos(A)*CosTh2)/(_Iy*mass2) +
                    (_Mm*Tm*SinTh2)/(_Iy*mass2) +
                    (_hm*Tm*Tm*Math.cos(A)*SinTh2)/(_Iy*mass2));

        double IK12 = (-((_Mm*Tm*Math.cos(Th))/(_Iy*_mass)) -
                (_hm*Tm*Tm*Math.cos(A)*Math.cos(Th))/(_Iy*_mass))/
            ((_Mm*Tm*CosTh2)/(_Iy*mass2) +
                    (_hm*Tm*Tm*Math.cos(A)*CosTh2)/(_Iy*mass2) +
                    (_Mm*Tm*SinTh2)/(_Iy*mass2) +
                    (_hm*Tm*Tm*Math.cos(A)*SinTh2)/(_Iy*mass2));

        double IK21 = Math.cos(Th)/
            (_mass*((_Mm*Tm*CosTh2)/(_Iy*mass2) +
                    (_hm*Tm*Tm*Math.cos(A)*CosTh2)/(_Iy*mass2) +
                    (_Mm*Tm*SinTh2)/(_Iy*mass2) +
                    (_hm*Tm*Tm*Math.cos(A)*SinTh2)/(_Iy*mass2)));

        double IK22 = Math.sin(Th)/
            (_mass*((_Mm*Tm*CosTh2)/(_Iy*mass2) +
                    (_hm*Tm*Tm*Math.cos(A)*CosTh2)/(_Iy*mass2) +
                    (_Mm*Tm*SinTh2)/(_Iy*mass2) +
                    (_hm*Tm*Tm*Math.cos(A)*SinTh2)/(_Iy*mass2)));

        double B1 = (3.0*DDTm*DTh*Math.cos(Th))/_mass -
            (DTh*DTh*DTh*Tm*Math.cos(Th))/_mass +
            (DTm*_hm*Tm*Math.cos(Th)*Math.sin(A))/(_Iy*_mass) +
            (3.0*DTm*Math.cos(Th)*(A*_Mm + _hm*Tm*Math.sin(A)))/(_Iy*_mass) -
            (3.0*DTh*DTh*DTm*Math.sin(Th))/_mass -
            (3.0*DTh*Tm*(A*_Mm + _hm*Tm*Math.sin(A))*Math.sin(Th))/(_Iy*_mass);

        double B2 = (3.8*DTh*DTh*DTm*Math.cos(Th))/_mass +
            (3.0*DTh*Tm*Math.cos(Th)*(A*_Mm + _hm*Tm*Math.sin(A)))/(_Iy*_mass)+
            (3.0*DDTm*DTh*Math.sin(Th))/_mass -
            (DTh*DTh*DTh*Tm*Math.sin(Th))/_mass +
            (DTm*_hm*Tm*Math.sin(A)*Math.sin(Th))/(_Iy*_mass) +
            (3.0*DTm*(A*_Mm + _hm*Tm*Math.sin(A))*Math.sin(Th))/(_Iy*_mass);

        double DDDTm = IK11*(B1+Vx) + IK12*(B2+Vz);
        double DA = IK21*(B1+Vx) + IK22*(B2+Vz);
        outputDDDTm.broadcast(new DoubleToken(DDDTm));
        outputDA.broadcast(new DoubleToken(DA));
    }

    /** Update the parameter if they have been changed.
     *  The new parameter will be used only after this method is called.
     *  @exception IllegalActionException Not thrown in this base class
     */
    public void updateParameters() throws IllegalActionException {
        _Iy = ((DoubleToken)paramIy.getToken()).doubleValue();
        _hm = ((DoubleToken)paramHm.getToken()).doubleValue();
        _Mm = ((DoubleToken)paramMm.getToken()).doubleValue();
        _mass = ((DoubleToken)paramMass.getToken()).doubleValue();
    }

    /////////////////////////////////////////////////////////////////////
    ////                         public variables                    ////

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

    /** Input port Vx
     */
    public TypedIOPort inputVx;

    /** Input port Vz
     */
    public TypedIOPort inputVz;

    /** Output port DDDTm = dddTm/dttt
     */
    public TypedIOPort outputDDDTm;

    /** Output port DA = dA/dt
     */
    public TypedIOPort outputDA;

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
