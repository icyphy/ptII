/* Linearization of the Hover mode.

 Copyright (c) 1999 The Regents of the University of California.
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
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.*;
import ptolemy.domains.ct.kernel.*;
import ptolemy.domains.ct.lib.*;
import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// HoverLinearizer
/**
Linearization of the Hover mode
Vx = -a0(Px-CPx)-a1*DPx-a2*DDPx-a3*D3Px-a4*D4Px
Vz = -a0(Pz-CPz)-a1*DPz-a2*DDPz-a3*D3Pz-a4*D4Pz
@author  Jie Liu
@version $Id$
*/
public class HoverLinearizer extends CTActor{
    /** Constructor
     */
    public HoverLinearizer(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        inputPx = new TypedIOPort(this, "inputPx");
        inputPx.setInput(true);
        inputPx.setOutput(false);
        inputPx.setMultiport(false);
        inputPx.setTypeEquals(BaseType.DOUBLE);

        inputDPx = new TypedIOPort(this, "inputDPx");
        inputDPx.setInput(true);
        inputDPx.setOutput(false);
        inputDPx.setMultiport(false);
        inputDPx.setTypeEquals(BaseType.DOUBLE);

        inputDDPx = new TypedIOPort(this, "inputDDPx");
        inputDDPx.setInput(true);
        inputDDPx.setOutput(false);
        inputDDPx.setMultiport(false);
        inputDDPx.setTypeEquals(BaseType.DOUBLE);

        inputD3Px = new TypedIOPort(this, "inputD3Px");
        inputD3Px.setInput(true);
        inputD3Px.setOutput(false);
        inputD3Px.setMultiport(false);
        inputD3Px.setTypeEquals(BaseType.DOUBLE);

        inputD4Px = new TypedIOPort(this, "inputD4Px");
        inputD4Px.setInput(true);
        inputD4Px.setOutput(false);
        inputD4Px.setMultiport(false);
        inputD4Px.setTypeEquals(BaseType.DOUBLE);

        inputPz = new TypedIOPort(this, "inputPz");
        inputPz.setInput(true);
        inputPz.setOutput(false);
        inputPz.setMultiport(false);
        inputPz.setTypeEquals(BaseType.DOUBLE);

        inputDPz = new TypedIOPort(this, "inputDPz");
        inputDPz.setInput(true);
        inputDPz.setOutput(false);
        inputDPz.setMultiport(false);
        inputDPz.setTypeEquals(BaseType.DOUBLE);

        inputDDPz = new TypedIOPort(this, "inputDDPz");
        inputDDPz.setInput(true);
        inputDDPz.setOutput(false);
        inputDDPz.setMultiport(false);
        inputDDPz.setTypeEquals(BaseType.DOUBLE);

        inputD3Pz = new TypedIOPort(this, "inputD3Pz");
        inputD3Pz.setInput(true);
        inputD3Pz.setOutput(false);
        inputD3Pz.setMultiport(false);
        inputD3Pz.setTypeEquals(BaseType.DOUBLE);

        inputD4Pz = new TypedIOPort(this, "inputD4Pz");
        inputD4Pz.setInput(true);
        inputD4Pz.setOutput(false);
        inputD4Pz.setMultiport(false);
        inputD4Pz.setTypeEquals(BaseType.DOUBLE);


        outputVx = new TypedIOPort(this, "outputVx");
        outputVx.setInput(false);
        outputVx.setOutput(true);
        outputVx.setMultiport(false);
        outputVx.setTypeEquals(BaseType.DOUBLE);

        outputVz = new TypedIOPort(this, "outputVz");
        outputVz.setInput(false);
        outputVz.setOutput(true);
        outputVz.setMultiport(false);
        outputVz.setTypeEquals(BaseType.DOUBLE);

        outputV = new TypedIOPort(this, "outputV");
        outputV.setInput(false);
        outputV.setOutput(true);
        outputV.setMultiport(false);
        outputV.setTypeEquals(BaseType.DOUBLE);

        outputR = new TypedIOPort(this, "outputR");
        outputR.setInput(false);
        outputR.setOutput(true);
        outputR.setMultiport(false);
        outputR.setTypeEquals(BaseType.DOUBLE);

        String s = new String();
        for(int i = 0; i< 5; i++) {
            s = s + _alphaP[i] + " ";
        }
        paramAlphaP = new Parameter(this, "AlphaP", new StringToken(s));

        _cPx = 0.0;
        paramCPx = new Parameter(this, "CPx", new DoubleToken(_cPx));

        _cPz = -2.0;
        paramCPz = new Parameter(this, "CPz", new DoubleToken(_cPz));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Compute the output.
     *
     *  @exception IllegalActionException If there's no input token
     *        when needed.
     */
    public void fire() throws IllegalActionException {
        double Px = ((DoubleToken)inputPx.get(0)).doubleValue();
        double DPx = ((DoubleToken)inputDPx.get(0)).doubleValue();
        double DDPx = ((DoubleToken)inputDDPx.get(0)).doubleValue();
        double D3Px = ((DoubleToken)inputD3Px.get(0)).doubleValue();
        double D4Px = ((DoubleToken)inputD4Px.get(0)).doubleValue();

        double Pz = ((DoubleToken)inputPz.get(0)).doubleValue();
        double DPz = ((DoubleToken)inputDPz.get(0)).doubleValue();
        double DDPz = ((DoubleToken)inputDDPz.get(0)).doubleValue();
        double D3Pz = ((DoubleToken)inputD3Pz.get(0)).doubleValue();
        double D4Pz = ((DoubleToken)inputD4Pz.get(0)).doubleValue();

        double Vx = -1.0*(_alphaP[0]*(Px-_cPx) + _alphaP[1]* DPx +
                _alphaP[2]*DDPx + _alphaP[3]*D3Px + _alphaP[4]*D4Px);
        double Vz = -1.0*(_alphaP[0]*(Pz-_cPz) + _alphaP[1]* DPz +
                _alphaP[2]*DDPz + _alphaP[3]*D3Pz + _alphaP[4]*D4Pz);

        double V = Math.sqrt(DPx*DPx + DPz*DPz);
        double R = Math.PI/2.0;
        if (DPx != 0.0) {
            R = Math.atan(DPz/DPx);
        }

        outputV.broadcast(new DoubleToken(V));
        outputR.broadcast(new DoubleToken(R));

        outputVx.broadcast(new DoubleToken(Vx));
        outputVz.broadcast(new DoubleToken(Vz));
    }

    /** Update the parameter if they have been changed.
     *  The new parameter will be used only after this method is called.
     *  @exception IllegalActionException  Not thrown in this base class.
     */
    public void updateParameters() throws IllegalActionException {
        String taps = ((StringToken)paramAlphaP.getToken()).toString();
        StringTokenizer stokens = new StringTokenizer(taps);
        int index = 0;
        int tokencount = stokens.countTokens();
        while(stokens.hasMoreTokens() && index < 5) {
            String valueToken = stokens.nextToken();
            _alphaP[index++] = (new Double(valueToken)).doubleValue();
        }
        _cPx = ((DoubleToken)paramCPx.getToken()).doubleValue();
        _cPz = ((DoubleToken)paramCPz.getToken()).doubleValue();
    }

    /** Input port Px, single port with type double.
     */
    public TypedIOPort inputPx;

    /** Input port DPx, single port with type double.
     */
    public TypedIOPort inputDPx;

    /** Input port DDPx, single port with type double.
     */
    public TypedIOPort inputDDPx;

    /** Input port D3Px, single port with type double.
     */
    public TypedIOPort inputD3Px;

    /** Input port D4Px, single port with type double.
     */
    public TypedIOPort inputD4Px;

    /** Input port Pz, single port with type double.
     */
    public TypedIOPort inputPz;

    /** Input port DPz, single port with type double.
     */
    public TypedIOPort inputDPz;

    /** Input port DDPz, single port with type double.
     */
    public TypedIOPort inputDDPz;

    /** Input port D3Pz, single port with type double.
     */
    public TypedIOPort inputD3Pz;

    /** Input port D4Pz, single port with type double.
     */
    public TypedIOPort inputD4Pz;

    /** Output port Vx, single port with type double.
     */
    public TypedIOPort outputVx;

    /** output port Vz, single port with type double.
     */
    public TypedIOPort outputVz;

    /** Output port V, single port with type double.
     */
    public TypedIOPort outputV;

    /** output port R, single port with type double.
     */
    public TypedIOPort outputR;

    /** Parameter for alphaP, string,
     *  default "500.0, 650.0, 395.0,  121.0, 17.8"
     */
    public Parameter paramAlphaP;

    /** Parameter for CPx, the set point of Px, double,
     *  default 0.0.
     */
    public Parameter paramCPx;

    /** Parameter for CPz, the set point of Pz, double,
     *  default -2.0.
     */
    public Parameter paramCPz;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private double[] _alphaP = {500.0, 650.0, 395.0,  121.0, 17.8};

    private double _cPx;

    private double _cPz;

}
