/* A CSP actor that contends for a shared resource.

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
package ptolemy.domains.csp.demo.BusContention;

// Ptolemy imports.
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.process.TerminateProcessException;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
import ptolemy.domains.csp.kernel.CSPActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// Processor

/**
 A CSP actor that contends for a shared resource. A Processor actor
 is granted access to the shared resource via a Controller actor.
 The processor must connect to a Controller via its requestInput
 and requestOutput ports. The shared resource that the Processor
 attempts to gain access to is a Memory actor. The memory actor
 is connected to via the Processor's memoryInput and memoryOutput
 ports.

 The Processor actor has four informal states. In state one it
 determines at what time it will next attempt to access the
 shared resource. It will then place a request at the determined
 time. In state two, it will wait to see if it has been granted
 the request. The Processor enters state three if it is granted
 the request. It remains in state three for 300 milliseconds,
 accesses the shared resource and then returns to state two. If
 the Processor is denied its request, then it enters four and
 remains in that state for 300 milliseconds after which it returns
 to state four.

 In addition to the resource contention features of Processor,
 it can also notify an ExecEventListener as this actor jumps between
 its three states. Such notification is enabled by adding an
 ExecEventListener to this actor's listener list via the addListeners()
 method. Listeners can be removed via the removeListeners() method.
 ExecEventListeners are currently implemented to serve as conduits
 between Ptolemy II and the Diva graphical user interface.

 @author John S. Davis II
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating Red (davisj)
 @Pt.AcceptedRating Red (cxh)
 */
public class Processor extends CSPActor {
    /** Construct a Processor actor with the specified container,
     *  name and priority code of this actor.
     * @param cont The container of this actor.
     * @param name The name of this actor.
     * @param code The priority code of this actor.
     * @exception IllegalActionException If the actor cannot be
     *  contained by the proposed container.
     * @exception NameDuplicationException If the container
     *  already has an actor with this name.
     */
    public Processor(CompositeEntity cont, String name, int code)
            throws IllegalActionException, NameDuplicationException {
        super(cont, name);

        requestOutput = new TypedIOPort(this, "requestOutput", false, true);
        requestInput = new TypedIOPort(this, "requestInput", true, false);
        memoryOutput = new TypedIOPort(this, "memoryOutput", false, true);
        memoryInput = new TypedIOPort(this, "memoryInput", true, false);

        requestOutput.setTypeEquals(BaseType.INT);
        requestInput.setTypeEquals(BaseType.BOOLEAN);
        memoryOutput.setTypeEquals(BaseType.STRING);
        memoryInput.setTypeEquals(BaseType.GENERAL);

        _code = code;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The resource request input port. Resources are granted through
     *  this port. The type of this port is BaseType.BOOLEAN.
     */
    public TypedIOPort requestInput;

    /** The resource request output port. Resource requests are made
     *  through this port with a token that includes the requestor's
     *  priority level. The type of this port is BaseType.INT.
     */
    public TypedIOPort requestOutput;

    /** The input port through which this actor receives data from
     *  the shared resource. The type of this port is BaseType.GENERAL.
     */
    public TypedIOPort memoryInput;

    /** The output port through which this actor sends data to the
     *  shared resource. The type of this port is BaseType.STRING.
     */
    public TypedIOPort memoryOutput;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Attempt to access the shared resource.
     * @exception IllegalActionException If there is an error
     *  during communication through any of this actor's ports.
     */
    public void accessMemory(boolean read) throws IllegalActionException {
        // State 1
        _debug(new ExecEvent(this, ExecEvent.WAITING));

        double delayTime = java.lang.Math.random();

        if (delayTime < 0.25) {
            delayTime = 2.5;
        } else if (delayTime >= 0.25 && delayTime < 0.5) {
            delayTime = 5.0;
        } else if (delayTime >= 0.5 && delayTime < 0.75) {
            delayTime = 7.5;
        } else {
            delayTime = 10.0;
        }

        delay(delayTime);

        IntToken iToken = new IntToken(_code);
        requestOutput.broadcast(iToken);

        // State 2
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            throw new TerminateProcessException(this, "Terminated");
        }

        BooleanToken bToken = (BooleanToken) requestInput.get(0);

        if (bToken.booleanValue()) {
            // State 3
            _debug(new ExecEvent(this, ExecEvent.ACCESSING));

            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                throw new TerminateProcessException(this, "Terminated");
            }

            if (read) {
                memoryInput.get(0);
            } else {
                StringToken strToken = new StringToken(getName());
                memoryOutput.broadcast(strToken);
            }

            return;
        } else {
            // State 4
            _debug(new ExecEvent(this, ExecEvent.BLOCKED));

            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                throw new TerminateProcessException(this, "Terminated");
            }
        }

        accessMemory(read);
    }

    /** Execute this actor by requesting and accepting access
     *  to a shared resource until b() returns true.
     * @exception IllegalActionException If an error occurs
     *  during communication through one of the ports.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        while (true) {
            if (performReadNext()) {
                accessMemory(true);
            } else {
                accessMemory(false);
            }
        }
    }

    /** Return true if this actor has randomly determined to attempt
     *  to read data from the shared resource on its next memory
     *  access; otherwise return false.
     * @return True if this actor will attempt to read from the
     *  shared resource; return false otherwise.
     */
    public boolean performReadNext() {
        if (java.lang.Math.random() < 0.5) {
            return true;
        }

        return false;
    }

    /** Wrapup this actor by first calling the super class
     *  wrapup method and then generating an event that
     *  will set the final state of this actor for the
     *  graphical user interface.
     * @exception IllegalActionException If the super class
     *  throws it.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _debug(new ExecEvent(this, ExecEvent.WAITING));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    private int _code;
}
