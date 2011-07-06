/*
 TokenPublisher batches tokens, converts to them binary and then publishes the result to the MQTT topic

 Copyright (c) 2011 The Regents of the University of California.
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
package ptserver.communication;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptserver.control.Ticket;
import ptserver.data.TokenParser;

import com.ibm.mqtt.MqttException;

///////////////////////////////////////////////////////////////////
//// TokenPublisher

/** TokenPublisher batches tokens, converts to them binary and then publishes the result to the MQTT topic.
 *  <p>The batch is sent it out periodically according to the period and tokensPerPeriod parameters
 *
 *  @author Anar Huseynov
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (ahuseyno)
 *  @Pt.AcceptedRating Red (ahuseyno)
 */
public class TokenPublisher {

    ///////////////////////////////////////////////////////////////////
    ////                         constructor                       ////

    /** Create instance of the TokenPublisher with specified period and tokensPerPeriod
     *  @param period Period in millisecond between batches
     *  @param tokensPerPeriod Expected number of tokens per period
     */
    public TokenPublisher(long period, int tokensPerPeriod, RemoteModel owner) {
        _period = period;
        // _tokensPerPeriod = tokensPerPeriod;
        _owner = owner;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Cancel the publisher's timer used for sending batch of tokens.
     */
    public void cancelTimer() {
        if (_timer != null) {
            _timer.cancel();
        }
    }

    /** Return the topic where the messages are published.
     *  @return the topic where the messages are published
     *  @see #setTopic(String)
     */
    public String getTopic() {
        return _topic;
    }

    /** Send the token via MQTT protocol.
     *  <p>The token will not be sent out immediately but would be batched for the specified period.</p>
     *  @param token the token to send
     *  @exception IllegalActionException if there is a problem with MQTT broker.
     */
    public synchronized void sendToken(Token token)
            throws IllegalActionException {
        try {
            TokenParser parser = TokenParser.getInstance();
            if (parser != null) {
                parser.convertToBytes(token, _outputStream);
                _tokenCount++;
            }
        } catch (IllegalActionException e) {
            // Unable to write the token, rethrow since it can't be handled here.
            throw new IllegalActionException(null, e,
                    "Problem converting a token to a byte stream");
        } catch (IOException e) {
            throw new IllegalActionException(null, e,
                    "Can't write to the communication stream");
        }
    }

    /** Set the topic for the message to publish.
     *  @param topic the topic to publish the MQTT messages.
     *  @see #getTopic()
     */
    public void setTopic(String topic) {
        _topic = topic;
    }

    /**
     * Start the timer that sends token batches.
     */
    public void startTimer(Ticket ticket) {
        _timer = new Timer("TokenPublisher timer " + ticket);
        _timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                synchronized (TokenPublisher.this) {
                    // If tokens are queued up, attempt to publish.
                    if (_tokenCount > 0) {
                        try {
                            _owner.getMqttClient().publish(getTopic(),
                                    _outputStream.toByteArray(),
                                    RemoteModel.QOS_LEVEL, false);

                            _outputStream.reset();
                            _tokenCount = 0;
                        } catch (MqttException e) {
                            _owner._fireModelException(
                                    "The token publisher timer has failed.", e);
                        }

                        //FIXME: use a proper logger or remove
                        //System.out.println(_batchCount++);
                        //                if (_tokenCount > _tokensPerPeriod) {
                        //                    long waitTime = (long) (1.0
                        //                            * (_tokenCount - _tokensPerPeriod)
                        //                            / _tokensPerPeriod * _period);
                        //                    try {
                        //                        Thread.sleep(waitTime);
                        //                    } catch (InterruptedException e) {
                        //                    }
                        //                }
                    }
                }
            }
        }, _period, _period);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** TODO: either remove this or add proper logging mechanism
     *  The count of batches sent.
     *  private int _batchCount;
    */

    /** Maximum size of the output stream.
     */
    private static final int MAX_STREAM_SIZE = 10000;

    /** The output stream holding the batch.
     */
    private final ByteArrayOutputStream _outputStream = new ByteArrayOutputStream(
            MAX_STREAM_SIZE);

    /** Reference to the owner remote model.
     */
    private final RemoteModel _owner;

    /** The period in millisecond between batches.
     */
    private final long _period;

    /**
     * The timer used for sending batch of tokens.
     */
    private Timer _timer;

    /** The count of tokens in the batch.
     */
    private int _tokenCount;

    /** The topic where messages are published.
     */
    private String _topic;

    /** TODO: do we need to throttle the source?
     *  Expected number of tokens per period.
     *
     *  private final int _tokensPerPeriod;
     */
}
