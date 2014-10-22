/* An actor that handles an HttpRequest by producing an output and waiting for an input.

 Copyright (c) 1997-2014 The Regents of the University of California.
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

package ptolemy.domains.de.test;

import java.util.Collection;
import java.util.LinkedList;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.BreakCausalityInterface;
import ptolemy.actor.util.CausalityInterface;
import ptolemy.data.DoubleToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/** A test actor illustrating a potential bug in DE in its
 *  handling of superdense time. In particular,
 *  the outputs of this actor have no causal dependence on the
 *  inputs, and hence no delay is needed in a feedback loop
 *  that feeds outputs back to the inputs. Inputs are read
 *  and processed in postfire(). However, if the output is
 *  fed directly back to an input, then the input used to be
 *  read <i>in the same iteration</i> in which the output
 *  was produced. If, however, there is an intervening actor
 *  in the feedback loop, then the input would be read
 *  <i>in the next iteration</i>. This leads to strange
 *  behavior where the presence of even a no-op actor (like
 *  a unit gain) changes the behavior of the model.
 *  <p>
 *  What is the solution here?
 *  <p>
 *  This actor first outputs nothing. In postfire(), it
 *  reads inputs and sums them, and if any inputs were present,
 *  it requests a new firing. In that firing, it produces the
 *  calculated sum.
 *  After 10 firings, it stops producing outputs.
 *
 *  @author Edward A. Lee
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (ltrnc)
 *  @Pt.AcceptedRating Red (ltrnc)
 *  @see org.ptolemy.ptango.lib.WebServer
 */
public class SuperdenseTimeTest extends TypedAtomicActor {

    /** Create an instance of the actor.
     *  @param container The container
     *  @param name The name.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the super
     */
    public SuperdenseTimeTest(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Ports
        in1 = new TypedIOPort(this, "in1", true, false);
        in1.setTypeEquals(BaseType.DOUBLE);
        in2 = new TypedIOPort(this, "in2", true, false);
        in2.setTypeEquals(BaseType.DOUBLE);
        in3 = new TypedIOPort(this, "in3", true, false);
        in3.setTypeEquals(BaseType.DOUBLE);
        out = new TypedIOPort(this, "out", false, true);
        out.setTypeEquals(BaseType.DOUBLE);

        _inputPorts = new LinkedList<IOPort>();
        _inputPorts.add(in1);
        _inputPorts.add(in2);
        _inputPorts.add(in3);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The inputs and output.
     */
    public TypedIOPort in1, in2, in3, out;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to return a causality interface that
     *  indicates that no output depends (immediately) on
     *  any input, and that also puts both input ports in an
     *  equivalence class.
     *  @return A representation of the dependencies between input ports
     *   and output ports.
     */
    @Override
    public CausalityInterface getCausalityInterface() {
        if (_causalityInterface == null) {
            _causalityInterface = new BreakCausalityInterface(this,
                    getDirector().defaultDependency()) {
                @Override
                public Collection<IOPort> equivalentPorts(IOPort input) {
                    return _inputPorts;
                }
            };
        }
        return _causalityInterface;
    }

    /** Respond to an HTTP request. If there is a
     *  response at the input port, then record that
     *  response and notify the servlet thread that a response
     *  is ready. Otherwise, if the servlet has received
     *  an HTTP request, then produce on the output ports
     *  the details of the request.
     *  @exception IllegalActionException If sending the
     *   outputs fails.
     */
    @Override
    public synchronized void fire() throws IllegalActionException {
        // The methods of the servlet are invoked in another
        // thread, so we synchronize on this actor for mutual exclusion.
        super.fire();
        if (_count < 10 && _value != null) {
            out.send(0, _value);
            _value = null;
        }
    }

    /** Set the output value to 0.0.
     *  @exception IllegalActionException If the superclass throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _value = null;
        _count = 0;
    }

    /** Read the inputs and sum them.
     *  @return True if a stop has not been requested.
     */
    @Override
    public synchronized boolean postfire() throws IllegalActionException {
        if (_debugging) {
            _debug("Starting postfire.");
        }
        double result = 0.0;
        boolean foundOne = false;
        if (in1.hasToken(0)) {
            result += ((DoubleToken) in1.get(0)).doubleValue();
            foundOne = true;
        }
        if (in2.hasToken(0)) {
            result += ((DoubleToken) in2.get(0)).doubleValue();
            foundOne = true;
        }
        if (in3.hasToken(0)) {
            result += ((DoubleToken) in3.get(0)).doubleValue();
            foundOne = true;
        }
        if (foundOne) {
            _value = new DoubleToken(result);
            getDirector().fireAtCurrentTime(this);
        }
        _count++;
        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The causality interface. */
    private CausalityInterface _causalityInterface;

    /** Count of firings. */
    private int _count;

    /** A collection of the two input ports, for use in the causality interface. */
    private Collection<IOPort> _inputPorts;

    /** Value to produce in fire(). */
    private DoubleToken _value;
}
