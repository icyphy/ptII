/* TCPPacketReceiver simulates a device that resolves variable-size
 TCP packets to release their token contents

@Copyright (c) 2011-2014 The Regents of the University of California.
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

package ptolemy.domains.ptides.lib.qm;

import java.util.Iterator;
import java.util.Set;

import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.type.BaseType;
import ptolemy.domains.ptides.kernel.PtidesDirector;
import ptolemy.domains.ptides.lib.InputDevice;
import ptolemy.graph.Inequality;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 *  This actor receives RecordTokens from a network input port and decomposes the
 *  packet into several tokens that are contained within the packet.
 *  This actor should used with a PTIDES director and in pairswith the {@link TCPPacketReceiver},
 *  typically with a network fabric model in between.
 *
 *  <p>{@link TCPPacketReceiver} decomposes TCP packets containing PTIDES events.</p>
 *
 *  <p>
 *  This actor consumes one input token and creates a stream of output tokens, each with
 *  the timestamp specified as a part of the token.>/p?
 *
 *  @author Ilge Akkaya
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating
 *  @Pt.AcceptedRating
 */
public class TCPPacketReceiver extends InputDevice {

    /** Construct a TCPPacketReceiver.
     *  @param container the container of the TCPPacketReceiver.
     *  @param name the name of the TCPPacketReceiver.
     *  @exception IllegalActionException If the TCPPacketReceiver cannot be constructed.
     *  @exception NameDuplicationException If there is a name collision.
     */
    public TCPPacketReceiver(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new TypedIOPort(this, "input", true, false);
        output = new TypedIOPort(this, "output", false, true);
        input.setTypeEquals(BaseType.RECORD);

        //output.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The input port, which is a record type. */
    public TypedIOPort input;

    /** The output port. */
    public TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Decompose RecordToken into its components.
     *  @exception IllegalActionException If thrown by the super cass or
     *  if there are problems decomposing the record.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        Director director = getDirector();

        if (director == null || !(director instanceof PtidesDirector)) {
            throw new IllegalActionException(this, "Director expected to"
                    + "be a Ptides director, but it's not.");
        }

        PtidesDirector ptidesDirector = (PtidesDirector) director;

        // consume input
        if (input.hasToken(0)) {

            RecordToken fullTCPFrame = (RecordToken) input.get(0);

            //RecordToken TCPHeader = (RecordToken) fullTCPFrame.get(TCPlabel);

            RecordToken dataContent = (RecordToken) fullTCPFrame.get(tokens);

            if (fullTCPFrame.labelSet().size() != 2) {
                throw new IllegalActionException(
                        "the input record token has a size not equal to 2: "
                                + "Here we assume the Record is of types: TCPHeader + tokens");
            }

            //if (record.get(payload).length == BaseType.RECORD)

            // produce different events for each token within the record
            //RecordToken payloadToken = (RecordToken)record.get(payload);
            // make sure the TCPFrameTransmitter wraps the event labels with name "payloadLabels"

            Iterator pLabels = dataContent.labelSet().iterator();
            // send all the received tokens to the outputs according to their timestamps
            while (pLabels.hasNext()) {

                ptidesDirector.getModelTime();
                ptidesDirector.getMicrostep();

                String singleEventLabel = (String) pLabels.next();
                //String singleEventLabel = Integer.toString(i);
                RecordToken singleEventRecord = (RecordToken) dataContent
                        .get(singleEventLabel);
                new Time(getDirector(),
                        ((DoubleToken) singleEventRecord.get(timestamp))
                        .doubleValue());

                ((IntToken) singleEventRecord.get(microstep)).intValue();

                if (output != null) {
                    // FIXME setTag not supported in Ptides director
                    //ptidesDirector.setTag(tokenTimestamp, tokenMicrostep);
                    output.send(0, singleEventRecord.get(payload));
                }
                //ptidesDirector.setTag(lastModelTime, lastMicrostep);
            }
        }
        //ptidesDirector.setTag(lastModelTime, lastMicrostep);
    }

    /** Perform a check to see if this device is connected to a network
     *  port on the outside. If not, throw an exception. Also call
     *  preinitialize of the super class.
     *  @exception IllegalActionException If there are no outside source
     *  ports, or if any of the outside source ports is not a network
     *  port.
     */
    @Override
    public void preinitialize() throws IllegalActionException {

        super.preinitialize();

        boolean flag = false;
        for (TypedIOPort input : inputPortList()) {
            for (IOPort sourcePort : input.sourcePortList()) {
                if (sourcePort.getContainer() == getContainer()) {
                    flag = true;
                }
            }
        }
        if (!flag) {
            throw new IllegalActionException(
                    this,
                    "A NetworkReceiver must be connected to a port "
                            + "on the outside, and that port should be a network port "
                            + "(a port with the parameter networkPort).");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Do not establish the usual default type constraints.
     *  @return null
     */
    @Override
    protected Set<Inequality> _defaultTypeConstraints() {
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private variable                      ////

    //*************** TCP  FIELDS ***************//
    /** label of the source port Field -- 16 bits.
     */
    //private static final String sourcePort = "sourcePort";

    /** label of the destination port Field -- 16 bits.
     */
    //private static final String destinationPort = "destinationPort";

    /** label of the sequenceNumber  Field -- 32 bits.
     */
    //private static final String sequenceNumber = "sequenceNumber";

    /** label of the acknowledgementNumber  Field -- 32 bits.
     */
    //private static final String acknowledgementNumber = "ackNumber";

    /** label of the OFFSET/Control Bits - 16 bits in total. Decomposed as;
     * Data Offset  (4 bits)
     * Reserved     (3 bits)
     * ECN          (3 bits) ( Explicit Congestion Notification)
     * Control Bits ( 6 bits)
     */

    //private static final String offsetControlBits = "offsetControlBits";

    /** label of the window size  Field -- 16 bits.
     */
    //private static final String windowSize = "windowSize";

    /** label of the checksum   Field -- 16 bits.
     */
    //private static final String checksum = "checksum";

    /** label of the Urgent Pointer   Field -- 16 bits.
     */
    //private static final String urgentPointer = "urgentPointer";

    /** defining the options field but not including to the RecordToken as of now
     *  8 bytes --
     */
    //private static final String options = "options";

    /** label of the timestamp that is transmitted within the RecordToken.
     */
    private static final String timestamp = "timestamp";

    /** label of the microstep that is transmitted within the RecordToken.
     */
    private static final String microstep = "microstep";

    /** label of the payload that is transmitted within the RecordToken.
     */
    private static final String payload = "payload";

    //private static final int max_packet_length = 5;

    // data tokens
    private static final String tokens = "tokens";

}
