/* NetworkInputDevice simulates a hardware device that sends data to the network.

@Copyright (c) 2008-2010 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

package ptolemy.domains.ptides.lib;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.domains.ptides.kernel.PtidesBasicDirector;
import ptolemy.graph.Inequality;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
////NetworkOutputDevice

/** This actor abstracts the network component that sends network packages
 *  to other platforms.
 *  This actor (and subclasses of this actor) should
 *  be directly connected to a network output port in a Ptides director.
 *  <p>
 *  A network output port is an output port of a composite
 *  actor with a <i>networkPort</i> parameter set to true. The composite actor
 *  should be governed by a Ptides director, and
 *  abstracts a computation platform in the Ptides domain.
 *  </p><p>
 *  This actor is expected to be used in pairs with the {@link NetworkInputDevice}.
 *  This actor produces network packages from the source
 *  platform, and the {@link NetworkInputDevice} consumes those packages 
 *  in the sink platform.
 *  Unlike SensorOutputDevice for example, this actor is necessarily needed for
 *  both simulation and code generation purposes.
 *  </p><p>
 *  This actor consumes the input token, and creates a RecordToken, with three
 *  labels: timestamp, microstep and payload. It then sends the RecordToken to
 *  its output port.
 *
 *  @author Jia Zou, Slobodan Matic
 *  @version $ld$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Yellow (jiazou)
 *  @Pt.AcceptedRating
*/
public class NetworkOutputDevice extends OutputDevice {

    /**
     * Constructs a NetworkOutputDevice object.
     * @param container The container.
     * @param name The name of this actor within the container.
     * @exception IllegalActionException if the super constructor throws it.
     * @exception NameDuplicationException if the super constructor throws it.
     */
    public NetworkOutputDevice(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new TypedIOPort(this, "input", true, false);
        output = new TypedIOPort(this, "output", false, true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The input port.  This base class imposes no type constraints except
     *  that the type of the input cannot be greater than the type of the
     *  output.
     */
    public TypedIOPort input;

    /** The output port. By default, the type of this port is constrained
     *  to be at least that of the input.
     */
    public TypedIOPort output;

    /** label of the timestamp that's transmitterd within the RecordToken.
     */
    private static final String timestamp = "timestamp";

    /** label of the microstep that's transmitterd within the RecordToken.
     */
    private static final String microstep = "microstep";

    /** label of the payload that's transmitterd within the RecordToken.
     */
    private static final String payload = "payload";

    ///////////////////////////////////////////////////////////////////
    ////                         public  variables                 ////
    /** Creates a RecordToken with two lables: timestamp, and payload.
     *  The payload is the token consumed from the input.
     *  It then sends the output token to its output port.
     *  @exception IllegalActionException If there is no director, or the
     *  input can not be read, or the output can not be sent.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        Director director = getDirector();

        if (director == null || !(director instanceof PtidesBasicDirector)) {
            throw new IllegalActionException(this, "Director expected to" +
            		"be a Ptides director, but it's not.");
        }

        PtidesBasicDirector ptidesDirector = (PtidesBasicDirector) director;

        if (input.hasToken(0)) {

            String[] labels = new String[] { timestamp, microstep, payload };
            Token[] values = new Token[] {
                    new DoubleToken(ptidesDirector.getModelTime()
                            .getDoubleValue()),
                    new IntToken(ptidesDirector.getMicrostep()), input.get(0) };
            RecordToken record = new RecordToken(labels, values);

            output.send(0, record);
        }
    }

    /** Perform a check to see if this device is connected to a network
     *  port on the outside. If not, throw an exception. Also call
     *  preinitialize of the super class.
     *  @exception IllegalActionException If there are no outside sink
     *  ports, or if any of the outside sink ports is not a network
     *  port.
     */
    public void preinitialize() throws IllegalActionException {

        super.preinitialize();

        boolean flag = false;
        for (IOPort output : (List<IOPort>)outputPortList()) {
            for (IOPort sinkPort : (List<IOPort>)output.sinkPortList()) {
                if (sinkPort.getContainer() == getContainer()) {
                    flag = true;
                    if (!PtidesBasicDirector.isNetworkPort(sinkPort)){
                        throw new IllegalActionException(
                                this, sinkPort,
                                "A NetworkOutputDevice must be directly connected " +
                                "to a network port (a port with the parameter " +
                                "networkPort).");
                    }
                }
            }
        }
        if (!flag) {
            throw new IllegalActionException(
                    this,
                    "A NetworkOutputDevice must be connected to a port " +
                    "on the outside, and that port should be a network port " +
                    "(a port with the parameter networkPort).");
        }
    }

    /** Return the type constraints of this actor. The type constraint is
     *  that the output RecordToken has two fields, a "timestamp" of type
     *  double and a "payload" of type same as the input type.
     *  @return a list of Inequality.
     */
    public Set<Inequality> typeConstraints() {
        String[] labels = { timestamp, microstep, payload };
        Type[] types = { BaseType.DOUBLE, BaseType.INT, BaseType.UNKNOWN };
        RecordType type = new RecordType(labels, types);
        output.setTypeEquals(type);

        // since the output port has a clone of the above RecordType, need to
        // get the type from the output port.
        RecordType outputType = (RecordType) output.getType();

        HashSet typeConstraints = new HashSet<Inequality>();
        Inequality inequality = new Inequality(input.getTypeTerm(), outputType
                .getTypeTerm(payload));
        typeConstraints.add(inequality);
        return typeConstraints;
    }
}
