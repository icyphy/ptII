/* A Ptolemy thread that provide a facade for a composite actor.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.domains.tm.lib;

import java.util.Iterator;

import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.tm.kernel.TMActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.PtolemyThread;

///////////////////////////////////////////////////////////////////
//// TMCompositeFacade

/**
 A facade for a composite actor that creates and executes its internal
 model a background process
 <P>
 FIXME: EXPERIMENTAL.
 <P>
 @author Jie Liu
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (liuj)
 @Pt.AcceptedRating Red (liuj)
 */
public class TMCompositeFacade extends TypedCompositeActor implements TMActor {
    /** Construct an actor with the specified container and name.
     *  There is one parameter which is the full class name of
     *  a Ptolemy actor. This actor has no ports.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public TMCompositeFacade(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        priority = new Parameter(this, "priority", new IntToken(10));
        priority.setTypeEquals(BaseType.INT);
        executionTime = new Parameter(this, "executionTime", new DoubleToken(
                0.0));
        executionTime.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The default execution time.
     */
    public Parameter executionTime;

    /** Priority of the background process.
     */
    public Parameter priority;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** update local cache of executionTime.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == executionTime) {
            double time = ((DoubleToken) executionTime.getToken())
                    .doubleValue();

            if (time < 0.0) {
                throw new IllegalActionException(this,
                        " execution time cannot be less than 0.");
            }

            _executionTime = time;
        } else if (attribute == priority) {
            _priority = ((IntToken) priority.getToken()).intValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Create the execution thread.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        _directorThread = new PtolemyThread(new RunnableDirector());
    }

    @Override
    public boolean prefire() throws IllegalActionException {
        if (!_idle) {
            return false;
        }

        // Notice that prefire returns false will prevent this
        // actor to be further executed. But the event has already
        // been delivered to the input receiver.
        if (!super.prefire()) {
            return false;
        } else {
            // Start the background process.
            _transferInputs();
            _directorThread.setPriority(_priority);
            _directorThread.start();
            return true;
        }
    }

    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (_idle) {
            // Produces the last outputs.
            _transferOutputs();
        } else {
            System.out.println("fire: Missed deadline.");

            // FIXME: Kill the thread?
        }
    }

    @Override
    public boolean postfire() throws IllegalActionException {
        if (_idle) {
            return super.postfire();
        } else {
            System.out.println("postfire: Missed deadline.");

            // FIXME: Kill the thread?
            return true;
        }
    }

    /** Transfer inputs from the ports.
     *  @exception IllegalActionException If the director throws it
     *  while transferring inputs.
     */
    protected synchronized void _transferInputs() throws IllegalActionException {
        Iterator inputPorts = inputPortList().iterator();

        while (inputPorts.hasNext()) {
            IOPort port = (IOPort) inputPorts.next();
            getDirector().transferInputs(port);
        }
    }

    /** Transfer outputs from the ports.
     *  @exception IllegalActionException If the director throws it
     *  while transferring outputs
     */
    protected synchronized void _transferOutputs()
            throws IllegalActionException {
        // Use the executive director to transfer outputs.
        Director executiveDirector = getExecutiveDirector();

        if (executiveDirector != null) {
            Iterator outports = outputPortList().iterator();

            while (outports.hasNext()) {
                IOPort p = (IOPort) outports.next();
                executiveDirector.transferOutputs(p);
            }
        }
    }

    @Override
    public double getExecutionTime() {
        return _executionTime;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Indicating whether the execution of the internal model is idle.
    private boolean _idle = true;

    private double _executionTime;

    private PtolemyThread _directorThread;

    private int _priority;

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////
    private class RunnableDirector implements Runnable {
        @Override
        public void run() {
            _idle = false;

            try {
                getDirector().fire();
            } catch (IllegalActionException ex) {
                throw new InvalidStateException(ex.getMessage());
            }

            _idle = true;
        }
    }
}
