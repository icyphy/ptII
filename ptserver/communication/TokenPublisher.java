/*
 TokenPublisher batches tokens, converts to them binary and then publishes the result to the MQTT topic

 Copyright (c) 2011-2014 The Regents of the University of California.
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptserver.actor.ProxySink;
import ptserver.control.Ticket;
import ptserver.data.TokenParser;

import com.ibm.mqtt.IMqttClient;
import com.ibm.mqtt.MqttException;
import com.ibm.mqtt.MqttNotConnectedException;
import com.ibm.mqtt.MqttPersistenceException;

///////////////////////////////////////////////////////////////////
//// TokenPublisher

/** TokenPublisher batches tokens, converts to them binary and then publishes the result to the MQTT topic.
 * <p>The batch is sent it out periodically according to the period parameter.
 *
 * @author Anar Huseynov
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public class TokenPublisher {

    /** Create instance of the TokenPublisher with specified period and tokensPerPeriod.
     *  @param period The period in milliseconds between batches
     *  @param proxyModelInfrastructure The infrastructure that created this listener and controls the state of the execution.
     */
    public TokenPublisher(long period,
            ProxyModelInfrastructure proxyModelInfrastructure) {
        _period = period;
        _proxyModelInfrastructure = proxyModelInfrastructure;
    }

    /** Start the timer that sends token batches.
     *  @param ticket Ticket on which to start the publishing timer.
     */
    public void startTimer(Ticket ticket) {
        _executor = Executors.newSingleThreadScheduledExecutor();
        _publisherFuture = _executor.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                try {
                    _sendBatch();
                } catch (Throwable e) {
                    _proxyModelInfrastructure.fireModelException(
                            "Unhandled exception in the TokenPublisher", e);
                }
            }
        }, 0, _period, TimeUnit.MILLISECONDS);
    }

    /** Cancel the publisher's timer used for sending batch of tokens.
     */
    public synchronized void cancel() {
        // Send the last batch before closing
        try {
            if (_tokenCount != 0) {
                _sendBatch();
                // NOTE: for some reason, the last batch is not send out,
                // if the mqtt connection is closed right after the publish method.
                // By forcing the thread sleep, we give MQTT some time to send the last message.
                Thread.sleep(_period);
            }
        } catch (Throwable e) {
            _proxyModelInfrastructure.fireModelException(
                    "Unhandled exception in the TokenPublisher", e);
        }
        if (_executor != null) {
            _executor.shutdownNow();
            if (_publisherFuture != null) {
                _publisherFuture.cancel(true);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return MQTT client that is used to send out MQTT messages.
     *  @return the mqttClient instance
     *  @see #setMqttClient(IMqttClient)
     */
    public IMqttClient getMqttClient() {
        return _mqttClient;
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
     *  @param token The token to send
     * @param sender The sink that produced the token.  If the parameter is null,
     * then the token was not produced by the model but programmatically i.e. for monitoring purposes.
     *  @exception IllegalActionException if there is a problem with MQTT broker.
     */
    public void sendToken(Token token, ProxySink sender)
            throws IllegalActionException {
        try {
            final int currentTokenCount;
            synchronized (this) {
                TokenParser.getInstance().convertToBytes(token, _outputStream);
                _tokenCount++;
                currentTokenCount = _tokenCount;
            }
            if (sender != null) {
                if (currentTokenCount > _MAX_TOKENS_PER_PERIOD) {
                    sender.throttle(true);
                } else {
                    sender.throttle(false);
                }
            }
        } catch (Throwable e) {
            throw new IllegalActionException(null, e,
                    "Problem converting a token to a byte stream");
        }
    }

    /** Set the mqttClient to be used to publish the tokens.
     *  @param mqttClient the mqttClient to be used to publish the tokens.
     *  @see #getMqttClient()
     */
    public void setMqttClient(IMqttClient mqttClient) {
        _mqttClient = mqttClient;
    }

    /** Set the topic for the message to publish.
     *  @param topic the topic to publish the MQTT messages.
     *  @see #getTopic()
     */
    public void setTopic(String topic) {
        _topic = topic;
    }

    private synchronized void _sendBatch() throws MqttNotConnectedException,
    MqttPersistenceException, IllegalArgumentException, MqttException {
        if (_tokenCount > 0) {
            byte[] batch = _outputStream.toByteArray();
            _mqttClient.publish(getTopic(), batch,
                    ProxyModelInfrastructure.QOS_LEVEL, false);
            _LOGGER.fine("publishing batch " + _batchCount++ + " batch size "
                    + batch.length + " token count " + _tokenCount);
            _outputStream.reset();
            _tokenCount = 0;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * TODO: either remove this or add proper logging mechanism
     * The count of batches sent.
     */
    private int _batchCount;

    /** The mqtt client instance used to send messages.
     */
    private IMqttClient _mqttClient;

    /** The output stream holding the batch.
     */
    private final ByteArrayOutputStream _outputStream = new ByteArrayOutputStream(
            10000);

    /** The period in millisecond between batches.
     */
    private final long _period;

    /** The infrastructure that created the listener.
     */
    private final ProxyModelInfrastructure _proxyModelInfrastructure;

    /** The executor used for sending batch of tokens.
     */
    private ScheduledExecutorService _executor;

    /**
     * The publisher's future that sends out token batches.
     */
    private ScheduledFuture<?> _publisherFuture;

    /** The count of tokens in the batch.
     */
    private int _tokenCount;

    /** The topic where messages are published.
     */
    private String _topic;

    /**
     * Maximum tokens per period before the publisher starts forcing the throttling.
     */
    private static final int _MAX_TOKENS_PER_PERIOD = 1000;

    /**
     * The logger used by the ptserver.
     */
    private static final Logger _LOGGER = Logger.getLogger("PtolemyServer");
}
