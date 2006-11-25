/* Code generator helper class associated with the StaticSchedulingDirector class.

 Copyright (c) 2005-2006 The Regents of the University of California.
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
package ptolemy.codegen.c.actor.sched;

import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.util.DFUtilities;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.codegen.kernel.Director;
import ptolemy.data.BooleanToken;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////
////StaticSchedulingDirector

/**
 Code generator helper associated with the StaticSchedulingDirector class. 
 This classis also associated with a code generator.

 @author Gang Zhou
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Yellow (zgang)  
 @Pt.AcceptedRating Red (eal)
 */
public class StaticSchedulingDirector extends Director {

    /** Construct the code generator helper associated with the given
     *  StaticSchedulingDirector.
     *  @param StaticSchedulingDirector The associated
     *  ptolemy.actor.sched.StaticSchedulingDirector
     */
    public StaticSchedulingDirector(ptolemy.actor.sched.StaticSchedulingDirector 
            staticSchedulingDirector) {
        super(staticSchedulingDirector);
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Generate the code for the firing of actors according to the SDF
     *  schedule.
     *  @return The generated fire code.
     *  @exception IllegalActionException If the SDF director does not have an
     *   attribute called "iterations" or a valid schedule, or the actor to be
     *   fired cannot find its associated helper.
     */
    public String generateFireCode() throws IllegalActionException {

        StringBuffer code = new StringBuffer();
        boolean inline = ((BooleanToken) _codeGenerator.inline.getToken())
                .booleanValue();

        // Generate code for one iteration.
        ptolemy.actor.sched.StaticSchedulingDirector director 
                = (ptolemy.actor.sched.StaticSchedulingDirector) getComponent();
        Schedule schedule = director.getScheduler().getSchedule();

        boolean isIDefined = false;
        Iterator actorsToFire = schedule.firingIterator();
        while (actorsToFire.hasNext()) {
            Firing firing = (Firing) actorsToFire.next();
            Actor actor = firing.getActor();

            // FIXME: Before looking for a helper class, we should check to
            // see whether the actor contains a code generator attribute.
            // If it does, we should use that as the helper.
            CodeGeneratorHelper helper 
                    = (CodeGeneratorHelper) _getHelper((NamedObj) actor);

            if (inline) {
                for (int i = 0; i < firing.getIterationCount(); i++) {

                    // generate fire code for the actor
                    code.append(helper.generateFireCode());
                    code.append(helper.generateTypeConvertFireCode());

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
            } else {

                int count = firing.getIterationCount();
                if (count > 1) {
                    if (!isIDefined) {
                        code.append("int i;" + _eol);
                        isIDefined = true;
                    }
                    code.append("for (i = 0; i < " + count + " ; i++) {" + _eol);
                }

                code.append(CodeGeneratorHelper.generateName((NamedObj) actor)
                        + "();" + _eol);

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

                if (count > 1) {
                    code.append("}" + _eol);
                }
            }
        }
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

        ptolemy.actor.sched.StaticSchedulingDirector director 
                = (ptolemy.actor.sched.StaticSchedulingDirector) getComponent();
        director.invalidateSchedule();
        director.getScheduler().getSchedule();

        return code.toString();
    }
}
