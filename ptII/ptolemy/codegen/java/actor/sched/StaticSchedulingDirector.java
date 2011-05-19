/* Code generator helper class associated with the StaticSchedulingDirector class.

 Copyright (c) 2005-2010 The Regents of the University of California.
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

 */
package ptolemy.codegen.java.actor.sched;

import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.util.DFUtilities;
import ptolemy.codegen.actor.Director;
import ptolemy.codegen.kernel.ActorCodeGenerator;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.codegen.kernel.CodeStream;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
////StaticSchedulingDirector

/**
 Code generator helper associated with the StaticSchedulingDirector class.
 This class is also associated with a code generator.

 @author Gang Zhou
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (zgang)
 @Pt.AcceptedRating Red (eal)
 */
public class StaticSchedulingDirector extends Director {

    /** Construct the code generator helper associated with the given
     *  StaticSchedulingDirector.
     *  @param staticSchedulingDirector The associated
     *  ptolemy.actor.sched.StaticSchedulingDirector
     */
    public StaticSchedulingDirector(
            ptolemy.actor.sched.StaticSchedulingDirector staticSchedulingDirector) {
        super(staticSchedulingDirector);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate the code for the firing of actors according to the SDF
     *  schedule.
     *  @return The generated fire code.
     *  @exception IllegalActionException If the SDF director does not have an
     *   attribute called "iterations" or a valid schedule, or the actor to be
     *   fired cannot find its associated helper.
     */
    public String generateFireCode() throws IllegalActionException {

        StringBuffer code = new StringBuffer();
        code.append(CodeStream.indent(_codeGenerator
                .comment("The firing of the StaticSchedulingDirector")));
        boolean inline = ((BooleanToken) _codeGenerator.inline.getToken())
                .booleanValue();

        // Generate code for one iteration.
        ptolemy.actor.sched.StaticSchedulingDirector director = (ptolemy.actor.sched.StaticSchedulingDirector) getComponent();
        Schedule schedule = director.getScheduler().getSchedule();

        boolean isIDefined = false;
        Iterator actorsToFire = schedule.firingIterator();
        while (actorsToFire.hasNext()) {
            Firing firing = (Firing) actorsToFire.next();
            Actor actor = firing.getActor();

            // FIXME: Before looking for a helper class, we should check to
            // see whether the actor contains a code generator attribute.
            // If it does, we should use that as the helper.
            CodeGeneratorHelper helper = (CodeGeneratorHelper) _getHelper((NamedObj) actor);

            if (inline) {
                for (int i = 0; i < firing.getIterationCount(); i++) {

                    // generate fire code for the actor
                    code.append(helper.generateFireCode());

                    _generateUpdatePortOffsetCode(code, actor);
                }
            } else {

                int count = firing.getIterationCount();
                if (count > 1) {
                    if (!isIDefined) {
                        code.append("int i;" + _eol);
                        isIDefined = true;
                    }
                    code
                            .append("for (i = 0; i < " + count + " ; i++) {"
                                    + _eol);
                }

                code.append(CodeGeneratorHelper.generateName((NamedObj) actor)
                        + "();" + _eol);

                _generateUpdatePortOffsetCode(code, actor);

                if (count > 1) {
                    code.append("}" + _eol);
                }
            }
        }
        return code.toString();
    }

    /**
     * Generate the code that updates the input/output port offset.
     * @param code The given code buffer.
     * @param actor The given actor.
     * @exception IllegalActionException Thrown if
     *  _updatePortOffset(IOPort, StringBuffer, int) or getRate(IOPort)
     *  throw it.
     */
    private void _generateUpdatePortOffsetCode(StringBuffer code, Actor actor)
            throws IllegalActionException {
        // update buffer offset after firing each actor once
        Iterator inputPorts = actor.inputPortList().iterator();
        while (inputPorts.hasNext()) {
            IOPort port = (IOPort) inputPorts.next();
            int rate = DFUtilities.getRate(port);
            _updatePortOffset(port, code, rate);
        }

        Iterator outputPorts = actor.outputPortList().iterator();
        while (outputPorts.hasNext()) {
            IOPort port = (IOPort) outputPorts.next();
            int rate = DFUtilities.getRate(port);
            _updateConnectedPortsOffset(port, code, rate);
        }
    }

    /** Generate a main loop for an execution under the control of
     *  this director. If the associated director has a parameter
     *  named <i>iterations</i> with a value greater than zero,
     *  then wrap code generated by generateFireCode() in a
     *  loop that executes the specified number of iterations.
     *  Otherwise, wrap it in a loop that executes forever.
     *  In the loop, first get the code returned by generateFireCode(),
     *  and follow that with the code produced by the container
     *  help for generateModeTransitionCode(). That code will
     *  make state transitions in modal models at the conclusion
     *  of each iteration. Next, this code calls postfire(), and
     *  that returns false, breaks out of the main loop.
     *  Finally, _iteration is incremented. This value can
     *  be used with the constant PERIOD to calculate the
     *  current time after each pass through the loop.
     *  @return Code for the main loop of an execution.
     *  @exception IllegalActionException If something goes wrong.
     */
    public String generateMainLoop() throws IllegalActionException {

        // Need a leading _eol here or else the execute decl. gets stripped out.
        StringBuffer code = new StringBuffer(_eol
                + "public void execute() throws Exception {" + _eol);

        Attribute iterations = _director.getAttribute("iterations");
        if (iterations == null) {
            code.append(_eol + "while (true) {" + _eol);
        } else {
            int iterationCount = ((IntToken) ((Variable) iterations).getToken())
                    .intValue();
            if (iterationCount <= 0) {
                code.append(_eol + "while (true) {" + _eol);
            } else {
                // Declare iteration outside of the loop to avoid
                // mode" with gcc-3.3.3
                code.append(_eol + "int iteration;" + _eol);
                code.append("for (iteration = 0; iteration < " + iterationCount
                        + "; iteration ++) {" + _eol);
            }
        }
        code.append("run();" + _eol + "}" + _eol + "}" + _eol + _eol
                + "public void run() throws Exception {" + _eol);

        code.append(generateFireCode());

        // The code generated in generateModeTransitionCode() is executed
        // after one global iteration, e.g., in HDF model.
        ActorCodeGenerator modelHelper = (ActorCodeGenerator) _getHelper(_director
                .getContainer());
        modelHelper.generateModeTransitionCode(code);

        /*if (callPostfire) {
            code.append(_INDENT2 + "if (!postfire()) {" + _eol + _INDENT3
                    + "break;" + _eol + _INDENT2 + "}" + _eol);
        }
         */
        _generateUpdatePortOffsetCode(code, (Actor) _director.getContainer());

        code.append(generatePostfireCode());

        code.append("++_iteration;" + _eol);

        code.append("}" + _eol);
        return code.toString();
    }

    /** Generate the preinitialize code for this director.
     *  The preinitialize code for the director is generated by appending
     *  the preinitialize code for each actor.
     *  @return The generated preinitialize code.
     *  @exception IllegalActionException If getting the helper fails,
     *   or if generating the preinitialize code for a helper fails,
     *   or if there is a problem getting the buffer size of a port.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generatePreinitializeCode());

        ptolemy.actor.sched.StaticSchedulingDirector director = (ptolemy.actor.sched.StaticSchedulingDirector) getComponent();

        // Force schedule (re)calculation before generating code
        // because we need to know buffer capacity. (otherwise
        // sometimes new receivers are created but the schedule
        // is not re-calculated.)
        director.invalidateSchedule();
        director.getScheduler().getSchedule();

        return code.toString();
    }

    /** Generate a variable declaration for the <i>period</i> parameter,
     *  if there is one.
     *  @return code The generated code.
     *  @exception IllegalActionException If the helper class for the model
     *   director cannot be found.
     */
    public String generateVariableDeclaration() throws IllegalActionException {
        StringBuffer variableDeclarations = new StringBuffer(super
                .generateVariableDeclaration());
        Attribute period = _director.getAttribute("period");
        ptolemy.actor.sched.StaticSchedulingDirector director = (ptolemy.actor.sched.StaticSchedulingDirector) getComponent();
        // Print period and iteration counter only if it is the containing actor
        // is the top level.
        // FIXME: should this test also be applied to the other code?
        if (director.getContainer().getContainer() == null) {
            Double periodValue = 0.0;
            if (period != null) {
                periodValue = ((DoubleToken) ((Variable) period).getToken())
                        .doubleValue();
            }
            variableDeclarations
                    .append(_eol
                            + _codeGenerator
                                    .comment("Provide the period attribute as constant."));
            variableDeclarations.append("public final static double PERIOD = "
                    + periodValue + ";" + _eol);
            variableDeclarations.append(_eol
                    + _codeGenerator.comment("Provide the iteration count."));
            variableDeclarations.append("public static int _iteration = 0;"
                    + _eol);
        }

        return variableDeclarations.toString();
    }
}
