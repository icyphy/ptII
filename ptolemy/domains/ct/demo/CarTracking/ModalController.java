/* An actor controls the follower in a car platoon.

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
@AcceptedRating Red (yuhong@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.demo.CarTracking;

import ptolemy.kernel.util.*;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.domains.ct.kernel.CTCompositeActor;
import ptolemy.domains.ct.kernel.CTEmbeddedDirector;
import ptolemy.domains.fsm.kernel.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.DoubleToken;
import ptolemy.data.StringToken;
import ptolemy.actor.lib.AddSubtract;
import ptolemy.actor.lib.Const;
import ptolemy.actor.lib.Scale;
import ptolemy.domains.ct.lib.Integrator;
import ptolemy.domains.ct.lib.ZeroOrderHold;

//////////////////////////////////////////////////////////////////////////
//// The modal controller.
/**
An actor that controls the following car. It has three inputs, which
are the position of the first car, its correctness, and the position
of the second car. It one output, which is the driving force
to the cat. It operates in two modes, "normal" and "erroneous". 
In the normal mode, it trusts the in-coming information from the
first car, and uses a PI controller to generate the force.
In the erroneous mode, it uses a Bang-Bang control law.
The Bang-Bang control is implemented by another two-mode hybrid system.
In the "acceleration" mode, it outputs a constant maximum force.
In the "decelaration" mode, it outputs 0.

@author Jie Liu
@version $Id$
*/

public class ModalController extends CTCompositeActor {
    
     /** Construct the composite actor, the director, and all
      *  the actors contained.
      */
    public ModalController(TypedCompositeActor container, String name) 
             throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        // create ports.
        leadingPosition = new TypedIOPort(this, "leadingPosition");     
        leadingPosition.setInput(true);
        leadingPosition.setMultiport(false);
        leadingPosition.setTypeEquals(BaseType.DOUBLE); 

        correctness = new TypedIOPort(this, "correctness");
        correctness.setInput(true);
        correctness.setTypeEquals(BaseType.BOOLEAN);

        followingPosition = new TypedIOPort(this, "followingPosition");     
        followingPosition.setInput(true);
        followingPosition.setMultiport(false);
        followingPosition.setTypeEquals(BaseType.DOUBLE); 

        drivingForce = new TypedIOPort(this, "drivingForce");
        drivingForce.setOutput(true);
        drivingForce.setMultiport(false);
        drivingForce.setTypeEquals(BaseType.DOUBLE);

        // create first levelFSM
        FSMActor controller = new FSMActor(this, "Controller");

        TypedIOPort correct = new TypedIOPort(controller, "correct");
        correct.setInput(true);
        correct.setTypeEquals(BaseType.BOOLEAN);
           
        State normal = new State(controller, "normal");
        State erroneous = new State(controller, "erroneous");
        controller.initialStateName.setExpression("\"normal\"");
        
        Transition goWrong = new Transition(controller, "goWrong");
        normal.outgoingPort.link(goWrong);
        erroneous.incomingPort.link(goWrong);
        goWrong.setGuardExpression("!correct_V");
        //ResetRefinement reset1 = new ResetRefinement(goWrong, "reset1");
        // no action on this transition

        Transition goRight = new Transition(controller, "goRight");
        erroneous.outgoingPort.link(goRight);
        normal.incomingPort.link(goRight);
        goRight.setGuardExpression("correct_V");
        //ResetRefinement reset2 = new ResetRefinement(goRight, "reset2");
        //FIXME: add action, set the PI controller's internal state to 0;
        
        // create director.
        HSDirector modalDirector = new HSDirector(this, "modalDirector");
        modalDirector.controllerName.setExpression("\"Controller\"");
        StreamListener dbl = new StreamListener();
        //modalDirector.addDebugListener(dbl);

        // create normal state refinement.
        CTCompositeActor normalRefinement = new
            CTCompositeActor(this, "normalRefinement");
        normal.refinementName.setExpression("\"normalRefinement\"");

        TypedIOPort normalLeadingInput = new 
            TypedIOPort(normalRefinement, "normalLeadingInput", true, false);
        normalLeadingInput.setMultiport(false);
        normalLeadingInput.setTypeEquals(BaseType.DOUBLE);

        TypedIOPort normalFollowingInput = new 
            TypedIOPort(normalRefinement, "normalFollowingInput", true, false);
        normalFollowingInput.setMultiport(false);
        normalFollowingInput.setTypeEquals(BaseType.DOUBLE);

        TypedIOPort normalForceOutput = new 
            TypedIOPort(normalRefinement, "normalForceOutput", false, true);
        normalForceOutput.setMultiport(false);
        normalForceOutput.setTypeEquals(BaseType.DOUBLE);

        // normalRefinement Director
        CTEmbeddedDirector normalRefinementDirector = new
            CTEmbeddedDirector(normalRefinement, "normalRefinementDirector");
        normalRefinementDirector.ODESolver.setToken(new StringToken(
                "ptolemy.domains.ct.kernel.solver.ForwardEulerSolver"));
        // normalRefinementDirector.addDebugListener(dbl);
        // actors in the normal refinement.
        
        ZeroOrderHold hold1 = new ZeroOrderHold(normalRefinement, "hold1");
        ZeroOrderHold hold2 = new ZeroOrderHold(normalRefinement, "hold2");

        AddSubtract add1 = new AddSubtract(normalRefinement, "add1");
        
        Const delta = new Const(normalRefinement, "delta");
        delta.value.setToken(new DoubleToken(20.0));
        normalRefinement.connect(delta.output, add1.minus);

        Scale kp = new Scale(normalRefinement, "kp");
        kp.factor.setToken(new DoubleToken(0.01));
        
        Scale ki = new Scale(normalRefinement, "ki");
        ki.factor.setToken(new DoubleToken(0.01));
        
        Integrator si = new Integrator(normalRefinement, "si");
        
        AddSubtract add2 = new AddSubtract(normalRefinement, "add2");

        // connections
        normalRefinement.connect(normalLeadingInput, hold1.input); 
        normalRefinement.connect(hold1.output, add1.plus);
        normalRefinement.connect(normalFollowingInput, hold2.input); 
        normalRefinement.connect(hold2.output, add1.minus);
        TypedIORelation r1 = new TypedIORelation(normalRefinement, "r1");
        add1.output.link(r1);
        kp.input.link(r1);
        ki.input.link(r1);
        normalRefinement.connect(ki.output, si.input);
        normalRefinement.connect(si.output, add2.plus);
        normalRefinement.connect(kp.output, add2.plus);
        normalRefinement.connect(normalForceOutput, add2.output);

        // end of normalRefinement.
        
        // erroneous state refinement
        CTCompositeActor erroRefinement = new
            CTCompositeActor(this, "erroRefinement");
        erroneous.refinementName.setExpression("\"erroRefinement\"");

        TypedIOPort erroForceOutput = new 
            TypedIOPort(erroRefinement, "erroForceOutput", false, true);
        erroForceOutput.setMultiport(false);
        erroForceOutput.setTypeEquals(BaseType.DOUBLE);
        
        // FSM for the erroneous state control
        FSMActor bangbang = new FSMActor(erroRefinement, "BangBang");
        
        TypedIOPort closeAlarm = new TypedIOPort(bangbang, "closeAlarm");
        closeAlarm.setInput(true);
        closeAlarm.setTypeEquals(BaseType.BOOLEAN);

        TypedIOPort farAlarm = new TypedIOPort(bangbang, "farAlarm");
        farAlarm.setInput(true);
        farAlarm.setTypeEquals(BaseType.BOOLEAN);

        State accelerating = new State(bangbang, "accelerating");
        accelerating.refinementName.setExpression("\"accelerate\"");

        State decelerating = new State(bangbang, "decelerating");
        decelerating.refinementName.setExpression("\"decelerate\"");

        bangbang.initialStateName.setExpression("\"decelerating\"");
        
        Transition tooClose = new Transition(bangbang, "tooClose");
        accelerating.outgoingPort.link(tooClose);
        decelerating.incomingPort.link(tooClose);
        tooClose.setGuardExpression("closeAlarm_S");
        // no actions on this transition.

        Transition tooFar = new Transition(bangbang, "tooFar");
        decelerating.outgoingPort.link(tooFar);
        accelerating.incomingPort.link(tooFar);
        tooFar.setGuardExpression("farAlarm_S");
        // no actions on this transition.
        
        // Director for the erroneous state.
        HSDirector bangbangDirector = new 
            HSDirector(erroRefinement, "bangbangDirector");
        bangbangDirector.controllerName.setExpression("\"BangBang\"");
        //bangbangDirector.addDebugListener(dbl);
        // In side the erroneous refimenent.
        
        // accelerate state.
        CTCompositeActor accelerate = new 
            CTCompositeActor(erroRefinement, "accelerate");
        
        TypedIOPort close = new TypedIOPort(accelerate, "close");
        TypedIOPort accelForce = new TypedIOPort(accelerate, "accelForce");
        accelForce.setOutput(true);

        // accelerate director.
        CTEmbeddedDirector accelDirector = new
            CTEmbeddedDirector(accelerate, "accelDirector");

        // actors in accelerate composite actor.
        CTSubscriber closeMonitor = new
            CTSubscriber(accelerate, "closeMonitor");
        closeMonitor.entryName.setToken(new StringToken("TooClose"));
        Const accel = new Const(accelerate, "accel");
        accel.value.setToken(new DoubleToken(2.0));

        accelerate.connect(closeMonitor.output, close);
        accelerate.connect(accel.output, accelForce);
        
        // conect to the bang-bang FSM
        erroRefinement.connect(close, closeAlarm);
        
         // decelerate state.
        CTCompositeActor decelerate = new 
            CTCompositeActor(erroRefinement, "decelerate");
        
        TypedIOPort far = new TypedIOPort(decelerate, "far");
        TypedIOPort decelForce = new TypedIOPort(decelerate, "decelForce");
        decelForce.setOutput(true);
        // decelerate director.
        CTEmbeddedDirector decelDirector = new
            CTEmbeddedDirector(decelerate, "decelDirector");

        // actors in decelerate composite actor.
        CTSubscriber farMonitor = new
            CTSubscriber(decelerate, "farMonitor");
        farMonitor.entryName.setToken(new StringToken("TooFar"));
        Const decel = new Const(decelerate, "decel");
        decel.value.setToken(new DoubleToken(0.0));

        decelerate.connect(farMonitor.output, far);
        decelerate.connect(decel.output, decelForce);
        
        // conect to the bang-bang FSM
        erroRefinement.connect(far, farAlarm);
        
        // connection inside erroRefinement
        TypedIORelation innerForceRel = (TypedIORelation) 
            erroRefinement.connect(accelForce, erroForceOutput);
        decelForce.link(innerForceRel);

        // connection in the controller level.
        connect(correctness, correct);
        connect(leadingPosition, normalLeadingInput);

        connect(followingPosition, normalFollowingInput);
        TypedIORelation forceRel = (TypedIORelation)
            connect(normalForceOutput, drivingForce);
        erroForceOutput.link(forceRel);  
    }

    ////////////////////////////////////////////////////////////////////
    ////                    ports and parameters                    ////
    
    /** leading car position input.
     */
    public TypedIOPort leadingPosition;
    
    /** following car position input.
     */
    public TypedIOPort followingPosition;

    /** correctness of the leading car postion.
     */
    public TypedIOPort correctness;

    /** driving force output.
     */
    public TypedIOPort drivingForce;

}
        

