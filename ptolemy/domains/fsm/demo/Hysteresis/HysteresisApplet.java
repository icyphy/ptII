/* An demo applet that uses Ptolemy II SDF and FSM domains to
   demonstrate hysteresis.

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

@ProposedRating Red (vogel@eecs.berkeley.edu)
@AcceptedRating Red
*/

package ptolemy.domains.fsm.demo.Hysteresis;

import ptolemy.actor.*;
import ptolemy.actor.gui.*;
import ptolemy.actor.lib.*;
import ptolemy.domains.fsm.kernel.*;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.plot.*;
import ptolemy.gui.Query;
import ptolemy.gui.QueryListener;
import java.util.Enumeration;
import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import javax.swing.BoxLayout;
import ptolemy.domains.sdf.gui.SDFApplet;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.domains.sdf.lib.*;

//////////////////////////////////////////////////////////////////////////
//// HysteresisApplet
/** This applet provides a demo of hysteresis using SDF combined with
    FSM. Since SDF is a special case of HDF, the HDF-specific finite
    state machine director is used.

@author Brian K. Vogel
@version $Id$
*/
public class HysteresisApplet extends SDFApplet {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Initialize the applet.
     */
    public void init() {
        super.init();
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
            hystplotter.plot.setTitle("Threshold = 0.3, Noise std dev = 0.2");
            hystplotter.plot.setXRange(0.0, 200.0);
            hystplotter.plot.setWrap(true);
            hystplotter.plot.setYRange(-1.3, 1.3);
            hystplotter.plot.setMarksStyle("none");
            hystplotter.plot.setPointsPersistence(200);

	    // Create and configure the SDF test acotor which refines
	    // to a FSM.	    
	    TypedCompositeActor hdfActor =
		new TypedCompositeActor(_toplevel, "hdfActor");
        
	    // create ports
	    TypedIOPort hdfActorInPort = 
		(TypedIOPort)hdfActor.newPort("dataIn");
            hdfActorInPort.setInput(true);
            hdfActorInPort.setTypeEquals(BaseType.DOUBLE);
	    

            TypedIOPort hdfActorOutPort = 
		(TypedIOPort)hdfActor.newPort("dataOut");
            hdfActorOutPort.setOutput(true);
            hdfActorOutPort.setTypeEquals(BaseType.DOUBLE);

	    // hdfActor's top level controller
	    HDFFSMActor ctrl = new HDFFSMActor(hdfActor, "Controller");

	    // For debugging.
	    //StreamListener sa = new StreamListener();
	    //ctrl.addDebugListener(sa);
	    // states and transitions

	    // Create useless redundant ports for the sake of akwardness.
 	    TypedIOPort ctrlActInPort = 
		(TypedIOPort)ctrl.newPort("dataIn");
            ctrlActInPort.setInput(true);
            ctrlActInPort.setTypeEquals(BaseType.DOUBLE);

            TypedIOPort ctrlOutPort = 
		(TypedIOPort)ctrl.newPort("dataOut");
            ctrlOutPort.setOutput(true);
            ctrlOutPort.setTypeEquals(BaseType.DOUBLE);

	    State ctrls0 = new State(ctrl, "ctrls0");
	    State ctrls1 = new State(ctrl, "ctrls1");

	    ctrl.initialStateName.setToken(new StringToken("ctrls0"));

	    Transition ctrlTrs0Tos1 = new Transition(ctrl, "ctrlTrs0Tos1");
            ctrls0.outgoingPort.link(ctrlTrs0Tos1);
            ctrls1.incomingPort.link(ctrlTrs0Tos1);
            ctrlTrs0Tos1.setGuardExpression("dataIn_V < -0.3");
            
	    Transition ctrlTrs1Tos0 = new Transition(ctrl, "ctrlTrs1Tos0");
            ctrls0.incomingPort.link(ctrlTrs1Tos0);
            ctrls1.outgoingPort.link(ctrlTrs1Tos0);
            ctrlTrs1Tos0.setGuardExpression("dataIn_V > 0.3");

	    // The HDF director
            HDFFSMDirector sdrDir = new HDFFSMDirector(hdfActor, "hdfActorDirector");
            sdrDir.controllerName.setToken(new StringToken("Controller"));
	    
	    // Add a opaque composite actor. This actor will contain an
	    // SDF model.
            TypedCompositeActor hdfActorState0 =
		new TypedCompositeActor(hdfActor, "state0");
	    // Set "state0" to be the submachine refining hdfActor's "ctrls0" state
	    ctrls0.refinementName.setToken(new StringToken("state0"));
	    
	    // Note: Currently, the names of all ports linked
	    // to a common relation must have the same name.

	    // Add ports to hdfActorState0.

	    // Add an input port.
	    TypedIOPort hdfActorState0InPort = 
		(TypedIOPort)hdfActorState0.newPort("dataIn");
            hdfActorState0InPort.setInput(true);
            hdfActorState0InPort.setTypeEquals(BaseType.DOUBLE);

	    // Add an output port.
	    TypedIOPort hdfActorState0OutPort = 
		 (TypedIOPort)hdfActorState0.newPort("dataOut");
            hdfActorState0OutPort.setOutput(true);
            hdfActorState0OutPort.setTypeEquals(BaseType.DOUBLE);

	    // Set up hdfActorState0 to contain an SDFDirector and an SDF diagram.
	    try {
		// Initialization
		SDFDirector _director0 =
		    new SDFDirector(hdfActorState0, "SDFDirector0");
		SDFScheduler scheduler0 = new SDFScheduler(_workspace);
		_director0.setScheduler(scheduler0);
		_director0.setScheduleValid(false);
	    } catch (Exception ex) {
		report("Failed to setup SDF director 0 and scheduler:\n", ex);
	    }

	    // Add an SDF Actor to state0 and connect up ports.
	    Const const0 = new Const(hdfActorState0, "Const0");
	    const0.value.setToken(new DoubleToken(-1));

	    // For Const actor, no input port is required.
	    hdfActorState0.connect(hdfActorState0InPort, const0.trigger);
	    
	    hdfActorState0.connect(const0.output, hdfActorState0OutPort);
	    ///////////// End of hdfActorState0 config.

	    // Now config state1.
	    // submachine refining hdfActor's s1 state
            TypedCompositeActor hdfActorState1 =
		new TypedCompositeActor(hdfActor, "state1");
	    // Set "state1" to be the submachine refining hdfActor's "ctrls1" state
	    ctrls1.refinementName.setToken(new StringToken("state1"));

	    // Add ports to state1.
	    TypedIOPort hdfActorState1InPort = 
		(TypedIOPort)hdfActorState1.newPort("dataIn");
            hdfActorState1InPort.setInput(true);
            hdfActorState1InPort.setTypeEquals(BaseType.DOUBLE);

	    TypedIOPort hdfActorState1OutPort = 
	    (TypedIOPort)hdfActorState1.newPort("dataOut");
            hdfActorState1OutPort.setOutput(true);
            hdfActorState1OutPort.setTypeEquals(BaseType.DOUBLE);
	    
	    // Set up hdfActorState1 to contain an SDFDirector and an SDF diagram.
	    try {
		// Initialization
		SDFDirector _director1 =
		    new SDFDirector(hdfActorState1, "SDFDirector1");
		SDFScheduler scheduler1 = new SDFScheduler(_workspace);
		_director1.setScheduler(scheduler1);
		_director1.setScheduleValid(false);
	    } catch (Exception ex) {
		report("Failed to setup SDF director 1 and scheduler:\n", ex);
	    }
	    
	    // Add an SDF Actor to state1 and connect up ports.
	    Const const1 = new Const(hdfActorState1, "Const1");
	    const1.value.setToken(new DoubleToken(1));
	    
	    // For Const actor, no input port is required.
	    hdfActorState1.connect(hdfActorState1InPort, const1.trigger);

	    hdfActorState1.connect(const1.output, hdfActorState1OutPort);

	    TypedIORelation rel3 =
		(TypedIORelation)hdfActor.newRelation("rel3");
	    (hdfActorState0OutPort).link(rel3);
	    (hdfActorState1OutPort).link(rel3);
	    (hdfActorOutPort).link(rel3);

	    TypedIORelation rel2 =
		(TypedIORelation)hdfActor.newRelation("rel2");
	    (hdfActorState0InPort).link(rel2);
	    (hdfActorState1InPort).link(rel2);
	    (hdfActorInPort).link(rel2);
	    (ctrlActInPort).link(rel2);

	    // Conect the actors.
	    _toplevel.connect(rampSig.output, sineSig.input);
	    _toplevel.connect(sineSig.output, add.plus);
	    _toplevel.connect(noise.output, add.plus);
	    
	    _toplevel.connect(hdfActorOutPort, hystplotter.input);

	    TypedIORelation noisyRel =
		(TypedIORelation)_toplevel.newRelation("noisyRel");
	    (add.output).link(noisyRel);
	    (hdfActorInPort).link(noisyRel);
	    (hystplotter.input).link(noisyRel);

            // We are now allowed to run the model.
            _initCompleted = true;

            // The 2 argument requests a go and stop button.
            //getContentPane().add(_createRunControls(2));

        } catch (Exception ex) {
            System.err.println("Setup failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    public static final boolean DEBUG = true;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Execute the model.  This overrides the base class to read the
     *  values in the query box first and set parameters.
     *  @exception IllegalActionException If topology changes on the
     *   model or parameter changes on the actors throw it.
     */     
    protected void _go() throws IllegalActionException {
        // If an exception occurred during initialization, then we don't
        // want to run here.  The model is probably not complete.
        if (!_initCompleted) return;

        // If the manager is not idle then either a run is in progress
        // or the model has been corrupted.  In either case, we do not
        // want to run.
        if (_manager.getState() != _manager.IDLE) return;

        // The superclass sets the stop time of the director based on
        // the value in the entry box on the screen.  Then it starts
        // execution of the model in its own thread, leaving the user
        // interface of this applet live.
        super._go();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Flag to prevent spurious exception being thrown by _go() method.
    // If this flag is not true, the _go() method will not execute the model.
    private boolean _initCompleted = false;
}
