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

import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptserver.data.TokenParser;

import com.ibm.mqtt.IMqttClient;
import com.ibm.mqtt.MqttException;

///////////////////////////////////////////////////////////////////
//// TokenPublisher
/**
 * TokenPublisher batches tokens, converts to them binary and then publishes the result to the MQTT topic.
 *
 * <p>The batch is sent it out periodically according to the period and tokensPerPeriod parameters
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
     * @param period Period in millisecond between batches
     * @param tokensPerPeriod Expected number of tokens per period
     */
    public TokenPublisher(long period, int tokensPerPeriod) {
        this._period = period;
        this._tokensPerPeriod = tokensPerPeriod;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /**
     * Return mqtt client that is used to send out MQTT messages.
     * @return the mqttClient instance
     * @see #setMqttClient(IMqttClient)
     */
    public IMqttClient getMqttClient() {
        return _mqttClient;
    }

    /**
     * Return the topic where the messages are published
     * @return the topic where the messages are published
     * @see #setTopic(String)
     */
    public String getTopic() {
        return _topic;
    }

    /**
     * Send the token via MQTT protocol
     *
     * The token will not be sent out immediately but would be batched for the specified period
     * @param token the token to send
     * @exception IllegalActionException if there is a problem with MQTT broker.
     */
    public void sendToken(Token token) throws IllegalActionException {
        if (_lastSent == -1) {
            _lastSent = System.currentTimeMillis();
        }
        try {
            TokenParser.getInstance().convertToBytes(token, _outputStream);

            _tokenCount++;
            long now = System.currentTimeMillis();
            if (now - _lastSent >= _period) {
                _mqttClient.publish(getTopic(), _outputStream.toByteArray(),
                        RemoteModel.QOS_LEVEL, false);
                //FIXME: use a proper logger or remove
                //System.out.println(_batchCount++);
                if (_tokenCount > _tokensPerPeriod) {
                    long waitTime = (long) (1.0
                            * (_tokenCount - _tokensPerPeriod)
                            / _tokensPerPeriod * _period);
                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException e) {
                    }
                }
                _outputStream.reset();
                _tokenCount = 0;
                _lastSent = System.currentTimeMillis();
            }
        } catch (IllegalActionException e) {
            throw new IllegalActionException(null, e,
                    "Problem convernting a token to a byte stream");
        } catch (IOException e) {
            throw new IllegalActionException(null, e,
                    "Problem convernting a token to a byte stream");
        } catch (MqttException e1) {
            throw new IllegalActionException(null, e1,
                    "MQTT problem publishing a token stream");
        }
    }

    /**
     * @param mqttClient the mqttClient to set
     * @see #getMqttClient()
     */
    public void setMqttClient(IMqttClient mqttClient) {
        this._mqttClient = mqttClient;
    }

    /**
     * @param topic the topic to set
     * @see #getTopic()
     */
    public void setTopic(String topic) {
        this._topic = topic;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    /**
     * The mqtt client instance used to send messages
     */
    private IMqttClient _mqttClient;

    /**
     * The topic where messages are published
     */
    private String _topic;

    /**
     * The timestamp of the last sent batch
     */
    private long _lastSent = -1;

    /**
     * The period in millisecond between batches
     */
    private final long _period;

    /**
     * The count of tokens in the batch
     */
    private int _tokenCount;

    /**
     * Expected number of tokens per period
     */
    private final int _tokensPerPeriod;

    /**
     * The output stream holding the batch
     */
    private final ByteArrayOutputStream _outputStream = new ByteArrayOutputStream(
            10000);
    /**
     * The count of batches sent
     */
    private int _batchCount;

}
