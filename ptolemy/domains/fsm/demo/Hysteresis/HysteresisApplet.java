/* An demo applet that uses Ptolemy II HDF and FSM domains to
   demonstrate hysteresis.

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

@ProposedRating Red (vogel@eecs.berkeley.edu)
@AcceptedRating Red (vogel@eecs.berkeley.edu)
*/

package ptolemy.domains.fsm.demo.Hysteresis;

import java.awt.event.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JPanel;
import java.util.Enumeration;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.data.type.BaseType;
import ptolemy.actor.*;
import ptolemy.actor.lib.*;
import ptolemy.actor.gui.*;
import ptolemy.domains.sdf.gui.SDFApplet;
import ptolemy.domains.fsm.demo.Hysteresis.*;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.domains.sdf.lib.*;
import ptolemy.plot.*;
import ptolemy.domains.fsm.kernel.*;
import ptolemy.domains.fsm.lib.*;
import ptolemy.domains.fsm.kernel.util.VariableList;

//////////////////////////////////////////////////////////////////////////
//// HysteresisApplet
/** This applet provides a demo of hysteresis using SDF combined with
    FSM. Since SDF is a special case of HDF, the HDF and FSM domains
    are used.


@author Brian K. Vogel
@version $Id$
*/
public class HysteresisApplet extends SDFApplet {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** After invoking super.init(), create and connect the actors.
     */
    public void init() {
        super.init();
        // The 1 argument requests a go and a stop button.
        getContentPane().add(_createRunControls(2), BorderLayout.SOUTH);
        try {
	    

	    // Create and configure ramp source
	    Ramp rampSig = new Ramp(_toplevel, "rampSig");

	    // Create and configure Sine transformer
	    Sine sineSig = new Sine(_toplevel, "sineSig");
	    sineSig.omega.setToken(new DoubleToken(0.1));
          
	    // Create and configure noise source
            Gaussian noise = new Gaussian(_toplevel, "noise");
            noise.standardDeviation.setToken(new DoubleToken(0.2));

            // Create the adder.
            AddSubtract add = new AddSubtract(_toplevel, "add");

            // Create and configure hystplotter
            SequencePlotter hystplotter = new SequencePlotter(_toplevel, "plot1");

	   

            // Place the hystplotter in the applet in such a way that it fills
            // the available space.
            hystplotter.place(getContentPane());
	    
            hystplotter.plot.setBackground(getBackground());
            hystplotter.plot.setGrid(false);
            hystplotter.plot.setTitle("T = 0.3, Noise std dev = 0.2");
            hystplotter.plot.setXRange(0.0, 200.0);
            hystplotter.plot.setWrap(true);
            hystplotter.plot.setYRange(-1.3, 1.3);
            hystplotter.plot.setMarksStyle("none");
            hystplotter.plot.setPointsPersistence(200);

	    // Create and configure the SDF test acotor which refines
	    // to a FSM.	    
	    TypedCompositeActor tempAct =
		new TypedCompositeActor(_toplevel, "tempAct");
	    
	    // ports
	    TypedIOPort tempActInPort = 
		(TypedIOPort)tempAct.newPort("dataIn");
            tempActInPort.setInput(true);
            tempActInPort.setTypeEquals(BaseType.DOUBLE);
	    

            TypedIOPort tempActOutPort = 
		(TypedIOPort)tempAct.newPort("dataOut");
            tempActOutPort.setOutput(true);
            tempActOutPort.setTypeEquals(BaseType.DOUBLE);

	    // tempAct's top level controller
            HDFFSMController ctrl =
		new HDFFSMController(tempAct, "Controller");

	    StreamListener sa = new StreamListener();
	    ctrl.addDebugListener(sa);
	    // states and transitions
            HDFFSMState ctrls0 = new HDFFSMState(ctrl, "ctrls0");
	    HDFFSMState ctrls1 = new HDFFSMState(ctrl, "ctrls1");
	    ctrl.setInitialState(ctrls0);
	    HDFFSMTransition ctrlTrs0Tos1 =
		(HDFFSMTransition)ctrl.createTransition(ctrls0, ctrls1);
	    // Note: TriggerEvent occurs when a token is available on
	    // the specified input port.
            ctrlTrs0Tos1.setTriggerEvent("TheGuard");
	    // Note: setTriggerCondition sets the guard.
            ctrlTrs0Tos1.setTriggerCondition("TheGuard < -0.3");
	    

	    HDFFSMTransition ctrlTrs0Tos0 =
	    (HDFFSMTransition)ctrl.createTransition(ctrls0, ctrls0);
	    // Note: TriggerEvent occurs when a token is available on
	    // the specified input port.
	    ctrlTrs0Tos0.setTriggerEvent("TheGuard");
	    // Note: setTriggerCondition sets the guard.
	    ctrlTrs0Tos0.setTriggerCondition("TheGuard > -0.3");
	    
	    

	    HDFFSMTransition ctrlTrs1Tos0 =
		(HDFFSMTransition)ctrl.createTransition(ctrls1, ctrls0);
	    // Note: TriggerEvent occurs when a token is available on
	    // the specified input port.
	    ctrlTrs1Tos0.setTriggerEvent("TheGuard");
	    // Note: setTriggerCondition sets the guard.
            ctrlTrs1Tos0.setTriggerCondition("TheGuard > 0.3");


	    HDFFSMTransition ctrlTrs1Tos1 =
		(HDFFSMTransition)ctrl.createTransition(ctrls1, ctrls1);
	    // Note: TriggerEvent occurs when a token is available on
	    // the specified input port.
	    ctrlTrs1Tos1.setTriggerEvent("TheGuard");
	    // Note: setTriggerCondition sets the guard.
            ctrlTrs1Tos1.setTriggerCondition("TheGuard < 0.3");


	    // Controller's director
	    HDFFSMDirector ctrlDir =
		new HDFFSMDirector(tempAct, "ControllerDirector");
            ctrlDir.setController(ctrl);

	    StreamListener sa2 = new StreamListener();
	    ctrlDir.addDebugListener(sa2);

	    // submachine refining tempAct's s0 state
            TypedCompositeActor tempActState0 =
		new TypedCompositeActor(tempAct, "state0");
	    ctrls0.setRefinement(tempActState0);
            // ports
	    //TypedIOPort tempActState0InPort = 
	    //(TypedIOPort)tempActState0.newPort("state0Input");
	    // FIXME: uncoment above two lines when following problem
	    // is fixed: Currently, The names of all ports linked
	    // to a common relation must have the same name.
	    TypedIOPort tempActState0InPort = 
		(TypedIOPort)tempActState0.newPort("dataIn");
            tempActState0InPort.setInput(true);
            tempActState0InPort.setTypeEquals(BaseType.DOUBLE);

	     TypedIOPort tempActState0OutPort = 
		 (TypedIOPort)tempActState0.newPort("dataOut");
            tempActState0OutPort.setOutput(true);
            tempActState0OutPort.setTypeEquals(BaseType.DOUBLE);

	    

	    // Set up tempAct to contain an SDFDirector and an SDF diagram.
	    try {
		// Initialization
		SDFDirector _director0 =
		    new SDFDirector(tempActState0, "SDFDirector0");
		Parameter iterparam0 = _director0.iterations;
		
		iterparam0.setToken(new IntToken(iterations));
		SDFScheduler scheduler0 = new SDFScheduler(_workspace);
		
		_director0.setScheduler(scheduler0);
		_director0.setScheduleValid(false);
	    } catch (Exception ex) {
		report("Failed to setup SDF director 0 and scheduler:\n", ex);
		
	    }

	    // Add an SDF Actor and connect up ports.
	    Const const0 = new Const(tempActState0, "Const0");
	    const0.value.setToken(new DoubleToken(-1));
	    
	    // For Const actor, no input port is required.
	    //tempActState0.connect(tempActState0InPort, const0.input);

	    
	    tempActState0.connect(const0.output, tempActState0OutPort);
	    /////////////////////////////////////////////////////////////////////
	    //////////////////////////////////////////////////////////////////////

	    // submachine refining tempAct's s1 state
            TypedCompositeActor tempActState1 =
		new TypedCompositeActor(tempAct, "state1");
	    ctrls1.setRefinement(tempActState1);
            // ports
	    //TypedIOPort tempActState1InPort = 
	    //(TypedIOPort)tempActState1.newPort("tempActState1InPort");
	    TypedIOPort tempActState1InPort = 
		(TypedIOPort)tempActState1.newPort("dataIn");
            tempActState1InPort.setInput(true);
            tempActState1InPort.setTypeEquals(BaseType.DOUBLE);

	    TypedIOPort tempActState1OutPort = 
	    (TypedIOPort)tempActState1.newPort("dataOut");
            tempActState1OutPort.setOutput(true);
            tempActState1OutPort.setTypeEquals(BaseType.DOUBLE);

	    //Conect tempAct's components
	    TypedIORelation tempActInRel =
		(TypedIORelation)tempAct.newRelation("tempActInRel");
            tempActInPort.link(tempActInRel);
            tempActState0InPort.link(tempActInRel);
            tempActState1InPort.link(tempActInRel);
	   
	    // Dosn't work????
	    //tempAct.connect(tempActInPort, tempActState0InPort);
	    //tempAct.connect(tempActInPort, tempActState1InPort);
	    
	     TypedIORelation tempActOutRel =
		 (TypedIORelation)tempAct.newRelation("tempActOutRel");
            tempActOutPort.link(tempActInRel);
            tempActState0OutPort.link(tempActInRel);
            tempActState1OutPort.link(tempActInRel);

	    

	    // Set up tempAct to contain an SDFDirector and an SDF diagram.
	    try {
		// Initialization
		SDFDirector _director1 =
		    new SDFDirector(tempActState1, "SDFDirector1");
		Parameter iterparam1 = _director1.iterations;
		
		iterparam1.setToken(new IntToken(iterations));
		SDFScheduler scheduler1 = new SDFScheduler(_workspace);
		
		_director1.setScheduler(scheduler1);
		_director1.setScheduleValid(false);
	    } catch (Exception ex) {
		report("Failed to setup SDF director 1 and scheduler:\n", ex);
		
	    }

	    // Add an SDF Actor and connect up ports.
	    Const const1 = new Const(tempActState1, "Const1");
	    const1.value.setToken(new DoubleToken(1));
	    
	    // For Const actor, no input port is required.
	    //tempActState1.connect(tempActState1InPort, const1.input);

	    
	    tempActState1.connect(const1.output, tempActState1OutPort);

	    //////////
            _toplevel.connect(rampSig.output, sineSig.input);
	    _toplevel.connect(sineSig.output, add.plus);
	    _toplevel.connect(noise.output, add.plus);
	    
	    _toplevel.connect(tempActOutPort, hystplotter.input);

	    TypedIORelation noisyRel =
		(TypedIORelation)_toplevel.newRelation("noisyRel");
	    (add.output).link(noisyRel);
	    (tempActInPort).link(noisyRel);
	    (hystplotter.input).link(noisyRel);
           
        } catch (Exception ex) {
            report("Setup failed:", ex);
        }
    }

    // Number of iterations to run refining SDF graphs.
    int iterations = 200;
}
