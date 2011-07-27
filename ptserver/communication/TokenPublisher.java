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

import com.ibm.mqtt.IMqttClient;

///////////////////////////////////////////////////////////////////
//// TokenPublisher
/**
 * TokenPublisher batches tokens, converts to them binary and then publishes the result to the MQTT topic.
 *
 * <p>The batch is sent it out periodically according to the period parameter.
 *
 * @author Anar Huseynov
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public class TokenPublisher {

    /**
     * Create instance of the TokenPublisher with specified period and tokensPerPeriod
     * @param period The period in milliseconds between batches
     * @param proxyModelInfrastructure The infrastructure that created this listener and controls the state of the execution.
     */
    public TokenPublisher(long period,
            ProxyModelInfrastructure proxyModelInfrastructure) {
        _period = period;
        _proxyModelInfrastructure = proxyModelInfrastructure;
    }

    /**
     * Start the timer that sends token batches.
     */
    public void startTimer(Ticket ticket) {
        _timer = new Timer("TokenPublisher timer " + ticket);
        _timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                try {
                    synchronized (TokenPublisher.this) {
                        if (_tokenCount > 0) {
                            byte[] batch = _outputStream.toByteArray();
                            _mqttClient.publish(getTopic(), batch,
                                    ProxyModelInfrastructure.QOS_LEVEL, false);
                            // TODO remove this or add proper logging
                            System.out.println("publishing batch "
                                    + _batchCount++ + " batch size "
                                    + batch.length);
                            _outputStream.reset();
                            _tokenCount = 0;
                        }
                    }
                } catch (Throwable e) {
                    _proxyModelInfrastructure.fireModelException(
                            "Unhandled exception in the TokenPublisher", e);
                }
            }
        }, _period, _period);
    }

    /**
     * Cancel the publisher's timer used for sending batch of tokens.
     */
    public void cancelTimer() {
        if (_timer != null) {
            _timer.cancel();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /**
     * Return MQTT client that is used to send out MQTT messages.
     * @return the mqttClient instance
     * @see #setMqttClient(IMqttClient)
     */
    public IMqttClient getMqttClient() {
        return _mqttClient;
    }

    /**
     * Return the topic where the messages are published.
     * @return the topic where the messages are published
     * @see #setTopic(String)
     */
    public String getTopic() {
        return _topic;
    }

    /**
     * Send the token via MQTT protocol.
     *
     * <p>The token will not be sent out immediately but would be batched for the specified period.</p>
     * @param token the token to send
     * @exception IllegalActionException if there is a problem with MQTT broker.
     */
    public synchronized void sendToken(Token token)
            throws IllegalActionException {
        try {
            TokenParser.getInstance().convertToBytes(token, _outputStream);
            _tokenCount++;
        } catch (IllegalActionException e) {
            throw new IllegalActionException(null, e,
                    "Problem converting a token to a byte stream");
        } catch (IOException e) {
            throw new IllegalActionException(null, e,
                    "Can't write to the communication stream");
        }
    }

    /**
     * Set the mqttClient to be used to publish the tokens.
     * @param mqttClient the mqttClient to be used to publish the tokens.
     * @see #getMqttClient()
     */
    public void setMqttClient(IMqttClient mqttClient) {
        _mqttClient = mqttClient;
    }

    /**
     * Set the topic for the message to publish.
     * @param topic the topic to publish the MQTT messages.
     * @see #getTopic()
     */
    public void setTopic(String topic) {
        _topic = topic;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    /**
     * The mqtt client instance used to send messages.
     */
    private IMqttClient _mqttClient;

    /**
     * The topic where messages are published.
     */
    private String _topic;

    /**
     * The period in millisecond between batches.
     */
    private final long _period;

    /**
     * The count of tokens in the batch.
     */
    private int _tokenCount;
    /**
     * The output stream holding the batch.
     */
    private final ByteArrayOutputStream _outputStream = new ByteArrayOutputStream(
            10000);
    /**
     * TODO: either remove this or add proper logging mechanism
     * The count of batches sent.
     */
    private int _batchCount;

    /**
     * The timer used for sending batch of tokens.
     */
    private Timer _timer;
    /** The infrastructure that created the listener.
     */
    private final ProxyModelInfrastructure _proxyModelInfrastructure;

}
