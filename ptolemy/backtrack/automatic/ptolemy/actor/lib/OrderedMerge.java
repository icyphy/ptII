/* An actor that merges two monotonically increasing streams into one.

 Copyright (c) 1998-2005 The Regents of the University of California.
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
package ptolemy.backtrack.automatic.ptolemy.actor.lib;

import java.lang.Object;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.CheckpointRecord;
import ptolemy.backtrack.util.FieldRecord;
import ptolemy.data.BooleanToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;
//////////////////////////////////////////////////////////////////////////
//// OrderedMerge

/** 
 * This actor merges two monotonically nondecreasing streams of tokens into
 * one monotonically nondecreasing stream. On each firing, it reads data from
 * one of the inputs.  On the first firing, it simply records that token.
 * On the second firing, it reads data from the other input and outputs
 * the smaller of the recorded token and the one it just read.  If they
 * are equal, then it outputs the recorded token. It then
 * records the larger token.  On each subsequent firing, it reads a token
 * from the input port that did not provide the recorded token, and produces
 * at the output the smaller of the recorded token and the one just read.
 * Each time it produces an output token, it also produces
 * <i>true</i> on the <i>selectedA</i> output
 * if the output token came from <i>inputA</i>, and <i>false</i>
 * if it came from <i>inputB</i>.
 * <p>
 * If both input sequences are nondecreasing, then the output sequence
 * will be nondecreasing.
 * Note that if the inputs are not nondecreasing, then the output is
 * rather complex.  The key is that in each firing, it produces the smaller
 * of the recorded token and the token it is currently reading.
 * @author Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 2.0.1
 * @Pt.ProposedRating Red (eal)
 * @Pt.AcceptedRating Red (eal)
 */
public class OrderedMerge extends TypedAtomicActor implements Rollbackable {

    protected Checkpoint $CHECKPOINT = new Checkpoint(this);

    // Add an attribute to get the port placed on the bottom.
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    /**     
     * The first input port, which accepts any scalar token. 
     */
    public TypedIOPort inputA;

    /**     
     * The second input port, which accepts any scalar token with
     * the same type as the first input port.
     */
    public TypedIOPort inputB;

    /**     
     * The output port, which has the same type as the input ports. 
     */
    public TypedIOPort output;

    /**     
     * Output port indicating whether the output token came from
     * <i>inputA</i>.
     */
    public TypedIOPort selectedA;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    // First firing.  Just record the token.
    // Produce the smaller output.
    /**         // Token was just read from _nextPort.

     *     // Produce the smaller output.
The recorded token.     // Swap ports.
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

     */
    // This method is Added by Gang Zhou so that DDFOrderedMerge
    // can extend this class.
    private     ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
ScalarToken _recordedToken = null;

    /**     
     * The port from which to read next. 
     */
    private TypedIOPort _nextPort = null;

    /**     
     * Indicator of whether the _recordedToken was read from A. 
     */
    private boolean _readFromA;

    /**     
     * Tentative indicator of having read from A. 
     */
    private boolean _tentativeReadFromA;

    /**     
     * The tentative recorded token. 
     */
    private ScalarToken _tentativeRecordedToken = null;

    /**     
     * The tentative port from which to read next. 
     */
    private TypedIOPort _tentativeNextPort = null;

    /**     
     * Construct an actor with the given container and name.
     * @param container The container.
     * @param name The name of this actor.
     * @exception IllegalActionException If the actor cannot be contained
     * by the proposed container.
     * @exception NameDuplicationException If the container already has an
     * actor with this name.
     */
    public OrderedMerge(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        inputA = new TypedIOPort(this, "inputA", true, false);
        inputB = new TypedIOPort(this, "inputB", true, false);
        inputB.setTypeSameAs(inputA);
        inputA.setTypeAtMost(BaseType.SCALAR);
        output = new TypedIOPort(this, "output", false, true);
        output.setTypeSameAs(inputA);
        selectedA = new TypedIOPort(this, "selectedA", false, true);
        selectedA.setTypeEquals(BaseType.BOOLEAN);
        StringAttribute channelCardinal = new StringAttribute(selectedA, "_cardinal");
        channelCardinal.setExpression("SOUTH");
        _attachText("_iconDescription", "<svg>\n" + "<polygon points=\"-10,20 10,10 10,-10, -10,-20\" "+"style=\"fill:blue\"/>\n"+"</svg>\n");
    }

    /**     
     * Clone the actor into the specified workspace. This calls the
     * base class and then sets the type constraints.
     * @param workspace The workspace for the new object.
     * @return A new actor.
     * @exception CloneNotSupportedException If a derived class has
     * an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException  {
        OrderedMerge newObject = (OrderedMerge)super.clone(workspace);
        newObject.inputA.setTypeAtMost(BaseType.SCALAR);
        newObject.inputB.setTypeSameAs(newObject.inputA);
        newObject.output.setTypeSameAs(newObject.inputA);
        return newObject;
    }

    /**     
     * Read one token from the port that did not provide the recorded
     * token (or <i>inputA</i>, on the first firing), and output the
     * smaller of the recorded token or the newly read token.
     * If there is no token on the port to be read, then do nothing
     * and return. If an output token is produced, then also produce
     * <i>true</i> on the <i>selectedA</i> output
     * if the output token came from <i>inputA</i>, and <i>false</i>
     * if it came from <i>inputB</i>.
     * @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException  {
        super.fire();
        if (_nextPort.hasToken(0)) {
            ScalarToken readToken = (ScalarToken)_nextPort.get(0);
            if (_debugging) {
                _debug("Read input token from " + _nextPort.getName()+" with value "+readToken);
            }
            if (_recordedToken == null) {
                $ASSIGN$_tentativeRecordedToken(readToken);
                $ASSIGN$_tentativeReadFromA(true);
                $ASSIGN$_tentativeNextPort(inputB);
            } else {
                if ((readToken.isLessThan(_recordedToken)).booleanValue()) {
                    output.send(0, readToken);
                    if (_debugging) {
                        _debug("Sent output token with value " + readToken);
                    }
                    if (_nextPort == inputA) {
                        selectedA.send(0, BooleanToken.TRUE);
                    } else {
                        selectedA.send(0, BooleanToken.FALSE);
                    }
                } else {
                    output.send(0, _recordedToken);
                    if (_debugging) {
                        _debug("Sent output token with value " + _recordedToken);
                    }
                    if (_readFromA) {
                        selectedA.send(0, BooleanToken.TRUE);
                    } else {
                        selectedA.send(0, BooleanToken.FALSE);
                    }
                    $ASSIGN$_tentativeRecordedToken(readToken);
                    $ASSIGN$_tentativeReadFromA((_nextPort == inputA));
                    if (_nextPort == inputA) {
                        $ASSIGN$_tentativeNextPort(inputB);
                    } else {
                        $ASSIGN$_tentativeNextPort(inputA);
                    }
                }
            }
        }
    }

    /**     
     * Initialize this actor to indicate that no token is recorded.
     * @exception IllegalActionException If a derived class throws it.
     */
    public void initialize() throws IllegalActionException  {
        super.initialize();
        $ASSIGN$_nextPort(inputA);
        $ASSIGN$_recordedToken(null);
    }

    /**     
     * Commit the recorded token.
     * @return True.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public boolean postfire() throws IllegalActionException  {
        $ASSIGN$_recordedToken(_tentativeRecordedToken);
        $ASSIGN$_readFromA(_tentativeReadFromA);
        $ASSIGN$_nextPort(_tentativeNextPort);
        if (_debugging) {
            _debug("Next port to read input from is " + _nextPort.getName());
        }
        return super.postfire();
    }

    /**     
     * Return the port that this actor will read from on the next
     * invocation of the fire() method. This will be null before the
     * first invocation of initialize().
     * @return The next input port.
     */
    protected TypedIOPort _getNextPort() {
        return _nextPort;
    }

    private final ScalarToken $ASSIGN$_recordedToken(ScalarToken newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_recordedToken.add(null, _recordedToken, $CHECKPOINT.getTimestamp());
        }
        return _recordedToken = newValue;
    }

    private final TypedIOPort $ASSIGN$_nextPort(TypedIOPort newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_nextPort.add(null, _nextPort, $CHECKPOINT.getTimestamp());
        }
        return _nextPort = newValue;
    }

    private final boolean $ASSIGN$_readFromA(boolean newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_readFromA.add(null, _readFromA, $CHECKPOINT.getTimestamp());
        }
        return _readFromA = newValue;
    }

    private final boolean $ASSIGN$_tentativeReadFromA(boolean newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_tentativeReadFromA.add(null, _tentativeReadFromA, $CHECKPOINT.getTimestamp());
        }
        return _tentativeReadFromA = newValue;
    }

    private final ScalarToken $ASSIGN$_tentativeRecordedToken(ScalarToken newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_tentativeRecordedToken.add(null, _tentativeRecordedToken, $CHECKPOINT.getTimestamp());
        }
        return _tentativeRecordedToken = newValue;
    }

    private final TypedIOPort $ASSIGN$_tentativeNextPort(TypedIOPort newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_tentativeNextPort.add(null, _tentativeNextPort, $CHECKPOINT.getTimestamp());
        }
        return _tentativeNextPort = newValue;
    }

    public void $COMMIT(long timestamp) {
        FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
        $RECORD$$CHECKPOINT.commit(timestamp);
    }

    public void $RESTORE(long timestamp, boolean trim) {
        _recordedToken = (ScalarToken)$RECORD$_recordedToken.restore(_recordedToken, timestamp, trim);
        _nextPort = (TypedIOPort)$RECORD$_nextPort.restore(_nextPort, timestamp, trim);
        _readFromA = $RECORD$_readFromA.restore(_readFromA, timestamp, trim);
        _tentativeReadFromA = $RECORD$_tentativeReadFromA.restore(_tentativeReadFromA, timestamp, trim);
        _tentativeRecordedToken = (ScalarToken)$RECORD$_tentativeRecordedToken.restore(_tentativeRecordedToken, timestamp, trim);
        _tentativeNextPort = (TypedIOPort)$RECORD$_tentativeNextPort.restore(_tentativeNextPort, timestamp, trim);
        if (timestamp <= $RECORD$$CHECKPOINT.getTopTimestamp()) {
            $CHECKPOINT = $RECORD$$CHECKPOINT.restore($CHECKPOINT, this, timestamp, trim);
            FieldRecord.popState($RECORDS);
            $RESTORE(timestamp, trim);
        }
    }

    public final Checkpoint $GET$CHECKPOINT() {
        return $CHECKPOINT;
    }

    public final Object $SET$CHECKPOINT(Checkpoint checkpoint) {
        if ($CHECKPOINT != checkpoint) {
            Checkpoint oldCheckpoint = $CHECKPOINT;
            if (checkpoint != null) {
                $RECORD$$CHECKPOINT.add($CHECKPOINT, checkpoint.getTimestamp());
                FieldRecord.pushState($RECORDS);
            }
            $CHECKPOINT = checkpoint;
            oldCheckpoint.setCheckpoint(checkpoint);
            checkpoint.addObject(this);
        }
        return this;
    }

    protected CheckpointRecord $RECORD$$CHECKPOINT = new CheckpointRecord();

    private FieldRecord $RECORD$_recordedToken = new FieldRecord(0);

    private FieldRecord $RECORD$_nextPort = new FieldRecord(0);

    private FieldRecord $RECORD$_readFromA = new FieldRecord(0);

    private FieldRecord $RECORD$_tentativeReadFromA = new FieldRecord(0);

    private FieldRecord $RECORD$_tentativeRecordedToken = new FieldRecord(0);

    private FieldRecord $RECORD$_tentativeNextPort = new FieldRecord(0);

    private FieldRecord[] $RECORDS = new FieldRecord[] {
            $RECORD$_recordedToken,
            $RECORD$_nextPort,
            $RECORD$_readFromA,
            $RECORD$_tentativeReadFromA,
            $RECORD$_tentativeRecordedToken,
            $RECORD$_tentativeNextPort
        };

}

