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
@AcceptedRating Red (liuxj@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.demo.jspaces.CarTracking;

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
import ptolemy.actor.lib.Expression;
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

public class SimpleModalController extends CTCompositeActor {
    
     /** Construct the composite actor, the director, and all
      *  the actors contained.
      */
    public SimpleModalController(TypedCompositeActor container, String name) 
             throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        // create ports.
        
        correctness = new TypedIOPort(this, "correctness");
        correctness.setInput(true);
        correctness.setTypeEquals(BaseType.BOOLEAN);

        leadingPosition = new TypedIOPort(this, "leadingPosition");     
        leadingPosition.setInput(true);
        leadingPosition.setMultiport(false);
        leadingPosition.setTypeEquals(BaseType.DOUBLE); 

        followingPosition = new TypedIOPort(this, "followingPosition");     
        followingPosition.setInput(true);
        followingPosition.setMultiport(false);
        followingPosition.setTypeEquals(BaseType.DOUBLE); 

        incomingForce = new TypedIOPort(this, "incomingForce");
        incomingForce.setInput(true);
        incomingForce.setMultiport(false);
        incomingForce.setTypeEquals(BaseType.DOUBLE);

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

        TypedIOPort normalForceInput = new 
            TypedIOPort(normalRefinement, "normalForceInput", true, false);
        normalForceInput.setMultiport(false);
        normalForceInput.setTypeEquals(BaseType.DOUBLE);
        
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
        // an expression actors in the normal refinement.
        Expression expr = new Expression(normalRefinement, "controlLaw");
        TypedIOPort exprP1 = new TypedIOPort(expr, "exprP1", true, false);
        exprP1.setTypeEquals(BaseType.DOUBLE);
        TypedIOPort exprP2 = new TypedIOPort(expr, "exprP2", true, false);
        exprP2.setTypeEquals(BaseType.DOUBLE);
        TypedIOPort exprF = new TypedIOPort(expr, "exprF", true, false);
        exprF.setTypeEquals(BaseType.DOUBLE);

        expr.output.setTypeEquals(BaseType.DOUBLE);
        
        expr.expression.setExpression("exprF");

        normalRefinement.connect(normalLeadingInput, exprP1);
        normalRefinement.connect(normalFollowingInput, exprP2);
        normalRefinement.connect(normalForceInput, exprF);
        normalRefinement.connect(expr.output, normalForceOutput);
        
        // end of normalRefinement.
        
        // erroneous state refinement
        CTCompositeActor erroRefinement = new
            CTCompositeActor(this, "erroRefinement");
        erroneous.refinementName.setExpression("\"erroRefinement\"");

        TypedIOPort erroForceOutput = new 
            TypedIOPort(erroRefinement, "erroForceOutput", false, true);
        erroForceOutput.setMultiport(false);
        erroForceOutput.setTypeEquals(BaseType.DOUBLE);
        
        // erroneous refinement director.
        CTEmbeddedDirector erroRefinementDirector = new
            CTEmbeddedDirector(erroRefinement, "erroRefinementDirector");

        // actors in the erroneous refinement

        CTSubscriber closeMonitor = new
            CTSubscriber(erroRefinement, "closeMonitor");
        closeMonitor.entryName.setToken(new StringToken("TooClose"));

        CTSubscriber farMonitor = new
            CTSubscriber(erroRefinement, "farMonitor");
        farMonitor.entryName.setToken(new StringToken("TooFar"));

        BangBangController bangbang = 
            new BangBangController(erroRefinement, "BangBang");
        bangbang.highValue.setToken(new DoubleToken(2.0));

        erroRefinement.connect(closeMonitor.output, bangbang.tooLarge);
        erroRefinement.connect(farMonitor.output, bangbang.tooSmall);
        erroRefinement.connect(bangbang.output, erroForceOutput);
     
        // connection in the controller level.
        connect(correctness, correct);
        connect(leadingPosition, normalLeadingInput);
        connect(incomingForce, normalForceInput);

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

    /** incoming force.
     */
    public TypedIOPort incomingForce;

    /** correctness of the leading car postion.
     */
    public TypedIOPort correctness;

    /** driving force output.
     */
    public TypedIOPort drivingForce;

}
        

