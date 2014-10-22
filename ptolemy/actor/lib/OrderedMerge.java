/* An actor that merges two monotonically increasing streams into one.

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
package ptolemy.actor.lib;

import java.util.HashSet;
import java.util.Set;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.graph.Inequality;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// OrderedMerge

/**
 This actor merges two monotonically nondecreasing streams of tokens into
 one monotonically nondecreasing stream. On each firing, it reads data from
 one of the inputs.  On the first firing, it simply records that token.
 On the second firing, it reads data from the other input and outputs
 the smaller of the recorded token and the one it just read.  If they
 are equal, then it outputs the recorded token. It then
 records the larger token.  On each subsequent firing, it reads a token
 from the input port that did not provide the recorded token, and produces
 at the output the smaller of the recorded token and the one just read.
 Each time it produces an output token, it also produces
 <i>true</i> on the <i>selectedA</i> output
 if the output token came from <i>inputA</i>, and <i>false</i>
 if it came from <i>inputB</i>.
 <p>
 If both input sequences are nondecreasing, then the output sequence
 will be nondecreasing.
 Note that if the inputs are not nondecreasing, then the output is
 rather complex.  The key is that in each firing, it produces the smaller
 of the recorded token and the token it is currently reading.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.0.1
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class OrderedMerge extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public OrderedMerge(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        eliminateDuplicates = new Parameter(this, "eliminateDuplicates");
        eliminateDuplicates.setTypeEquals(BaseType.BOOLEAN);
        eliminateDuplicates.setExpression("false");

        inputA = new TypedIOPort(this, "inputA", true, false);
        inputB = new TypedIOPort(this, "inputB", true, false);
        inputB.setTypeSameAs(inputA);
        inputA.setTypeAtMost(BaseType.SCALAR);

        // For the benefit of the DDF director, this actor sets
        // consumption rate values.
        inputA_tokenConsumptionRate = new Parameter(inputA,
                "tokenConsumptionRate");
        inputA_tokenConsumptionRate.setVisibility(Settable.NOT_EDITABLE);
        inputA_tokenConsumptionRate.setTypeEquals(BaseType.INT);

        inputB_tokenConsumptionRate = new Parameter(inputB,
                "tokenConsumptionRate");
        inputB_tokenConsumptionRate.setVisibility(Settable.NOT_EDITABLE);
        inputB_tokenConsumptionRate.setTypeEquals(BaseType.INT);

        output = new TypedIOPort(this, "output", false, true);

        selectedA = new TypedIOPort(this, "selectedA", false, true);
        selectedA.setTypeEquals(BaseType.BOOLEAN);

        // Add an attribute to get the port placed on the bottom.
        StringAttribute channelCardinal = new StringAttribute(selectedA,
                "_cardinal");
        channelCardinal.setExpression("SOUTH");

        _attachText("_iconDescription", "<svg>\n"
                + "<polygon points=\"-10,20 10,10 10,-10, -10,-20\" "
                + "style=\"fill:blue\"/>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** If true, eliminate duplicate tokens in the output stream.
     *  This is a boolean that defaults to false.
     */
    public Parameter eliminateDuplicates;

    /** The first input port, which accepts any scalar token. */
    public TypedIOPort inputA;

    /** The token consumption rate for <i>inputA</i>. */
    public Parameter inputA_tokenConsumptionRate;

    /** The second input port, which accepts any scalar token with
     *  the same type as the first input port.
     */
    public TypedIOPort inputB;

    /** The token consumption rate for <i>inputB</i>. */
    public Parameter inputB_tokenConsumptionRate;

    /** The output port, which has the same type as the input ports. */
    public TypedIOPort output;

    /** Output port indicating whether the output token came from
     *  <i>inputA</i>.
     */
    public TypedIOPort selectedA;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the type constraints.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        OrderedMerge newObject = (OrderedMerge) super.clone(workspace);
        newObject.inputA.setTypeAtMost(BaseType.SCALAR);
        newObject.inputB.setTypeSameAs(newObject.inputA);
        return newObject;
    }

    /** Read one token from the port that did not provide the recorded
     *  token (or <i>inputA</i>, on the first firing), and output the
     *  smaller of the recorded token or the newly read token.
     *  If there is no token on the port to be read, then do nothing
     *  and return. If an output token is produced, then also produce
     *  <i>true</i> on the <i>selectedA</i> output
     *  if the output token came from <i>inputA</i>, and <i>false</i>
     *  if it came from <i>inputB</i>.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (_nextPort.hasToken(0)) {
            ScalarToken readToken = (ScalarToken) _nextPort.get(0);

            if (_debugging) {
                _debug("Read input token from " + _nextPort.getName()
                        + " with value " + readToken);
            }
            // The strategy here is to keep reading from the same
            // port until its value is greater than or equal to the
            // recorded token. When that occurs, output the recorded
            // token, record the just-read input token, and start
            // reading from the other input port.

            if (_recordedToken == null) {
                // First firing or after a duplicate.  Just record the token.
                _tentativeRecordedToken = readToken;
                _tentativeReadFromA = true;
                // Swap ports.
                if (_nextPort == inputA) {
                    _tentativeNextPort = inputB;
                } else {
                    _tentativeNextPort = inputA;
                }
            } else {
                // Logic is different if we have to eliminate duplicates.
                if (((BooleanToken) eliminateDuplicates.getToken())
                        .booleanValue()) {
                    // We are set to eliminate duplicates.
                    if (readToken.equals(_recordedToken)) {
                        // Input is a duplicate of the recorded token.
                        // Produce the recorded token as output,
                        // discard the input token and continue reading from the
                        // same input port with no recorded token.
                        output.send(0, _recordedToken);
                        _tentativeLastProduced = _recordedToken;
                        if (_debugging) {
                            _debug("Sent output token with value "
                                    + _recordedToken
                                    + "\nDiscarded duplicate input.");
                        }
                        _tentativeRecordedToken = null;
                        if (_readFromA) {
                            selectedA.send(0, BooleanToken.TRUE);
                        } else {
                            selectedA.send(0, BooleanToken.FALSE);
                        }
                        // Read from the same port again, so leave
                        // _tentativeNextPort alone.
                    } else if (readToken.equals(_lastProduced)) {
                        // Token is the same as last produced.
                        // Do not send an output and leave everything the same
                        // Except there is no longer a recorded token.
                        if (_debugging) {
                            _debug("Discarded duplicate input " + readToken);
                        }
                    } else {
                        // Not a duplicate.
                        if (readToken.isLessThan(_recordedToken).booleanValue()) {
                            // Produce the smaller output.
                            output.send(0, readToken);
                            _tentativeLastProduced = readToken;

                            if (_debugging) {
                                _debug("Sent output token with value "
                                        + readToken);
                            }

                            // Token was just read from _nextPort.
                            if (_nextPort == inputA) {
                                selectedA.send(0, BooleanToken.TRUE);
                            } else {
                                selectedA.send(0, BooleanToken.FALSE);
                            }
                            // Read from the same port again next time.
                        } else {
                            // Produce the smaller output.
                            output.send(0, _recordedToken);
                            _tentativeLastProduced = _recordedToken;
                            if (_debugging) {
                                _debug("Sent output token with value "
                                        + _recordedToken);
                            }
                            if (_readFromA) {
                                // Recorded token was read from A.
                                selectedA.send(0, BooleanToken.TRUE);
                            } else {
                                selectedA.send(0, BooleanToken.FALSE);
                            }

                            _tentativeRecordedToken = readToken;
                            _tentativeReadFromA = _nextPort == inputA;

                            // Swap ports.
                            if (_nextPort == inputA) {
                                _tentativeNextPort = inputB;
                            } else {
                                _tentativeNextPort = inputA;
                            }
                        }
                    }
                } else {
                    // Not eliminating duplicates.
                    if (readToken.isLessThan(_recordedToken).booleanValue()) {
                        // Produce the smaller output.
                        output.send(0, readToken);

                        if (_debugging) {
                            _debug("Sent output token with value " + readToken);
                        }

                        // Token was just read from _nextPort.
                        if (_nextPort == inputA) {
                            selectedA.send(0, BooleanToken.TRUE);
                        } else {
                            selectedA.send(0, BooleanToken.FALSE);
                        }
                    } else {
                        // Produce the smaller output.
                        output.send(0, _recordedToken);
                        if (_debugging) {
                            _debug("Sent output token with value "
                                    + _recordedToken);
                        }

                        if (_readFromA) {
                            selectedA.send(0, BooleanToken.TRUE);
                        } else {
                            selectedA.send(0, BooleanToken.FALSE);
                        }

                        _tentativeRecordedToken = readToken;
                        _tentativeReadFromA = _nextPort == inputA;

                        // Swap ports.
                        if (_nextPort == inputA) {
                            _tentativeNextPort = inputB;
                        } else {
                            _tentativeNextPort = inputA;
                        }
                    }
                }
            }
        }
    }

    /** Initialize this actor to indicate that no token is recorded.
     *  @exception IllegalActionException If a derived class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _nextPort = inputA;
        _recordedToken = null;
        _lastProduced = null;
        _tentativeLastProduced = null;
        inputA_tokenConsumptionRate.setToken(_one);
        inputB_tokenConsumptionRate.setToken(_zero);
    }

    /** Commit the recorded token.
     *  @return True.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        _recordedToken = _tentativeRecordedToken;
        _readFromA = _tentativeReadFromA;
        _nextPort = _tentativeNextPort;
        _lastProduced = _tentativeLastProduced;

        if (_nextPort == inputA) {
            inputA_tokenConsumptionRate.setToken(_one);
            inputB_tokenConsumptionRate.setToken(_zero);
        } else {
            inputA_tokenConsumptionRate.setToken(_zero);
            inputB_tokenConsumptionRate.setToken(_one);
        }

        if (_debugging) {
            _debug("Next port to read input from is " + _nextPort.getName());
        }

        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the port that this actor will read from on the next
     *  invocation of the fire() method. This will be null before the
     *  first invocation of initialize().
     *  @return The next input port.
     */
    protected TypedIOPort _getNextPort() {
        // This method is Added by Gang Zhou so that DDFOrderedMerge
        // can extend this class.
        return _nextPort;
    }

    /**
     * The output must be greater than or equal to each of both inputs. Since
     * inputA is set to be the same as inputB, the output is simply set to be
     * greater than or equal to inputA.
     * @return A set of type constraints
     */
    @Override
    protected Set<Inequality> _defaultTypeConstraints() {
        Set<Inequality> result = new HashSet<Inequality>();
        result.add(new Inequality(inputA.getTypeTerm(), output.getTypeTerm()));
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The last produced token. Used to eliminate duplicates. */
    private ScalarToken _lastProduced;

    /** The port from which to read next. */
    private TypedIOPort _nextPort = null;

    /** A final static IntToken with value 1. */
    private final static IntToken _one = new IntToken(1);

    /** Indicator of whether the _recordedToken was read from A. */
    private boolean _readFromA;

    /** The recorded token. */
    private ScalarToken _recordedToken = null;

    /** The tentative last produced token. Used to eliminate duplicates. */
    private ScalarToken _tentativeLastProduced;

    /** Tentative indicator of having read from A. */
    private boolean _tentativeReadFromA;

    /** The tentative recorded token. */
    private ScalarToken _tentativeRecordedToken = null;

    /** The tentative port from which to read next. */
    private TypedIOPort _tentativeNextPort = null;

    /** A final static IntToken with value 0. */
    private final static IntToken _zero = new IntToken(0);
}
