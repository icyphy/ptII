/*
 MQTTTokenListener is responsible for processing MQTT messages received,
 converting back to tokens and putting those tokens into appropriate queues.

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

import java.util.Date;

import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Settable;
import ptserver.data.AttributeChangeToken;
import ptserver.data.CommunicationToken;
import ptserver.data.PingToken;
import ptserver.data.PongToken;
import ptserver.data.Tokenizer;

import com.ibm.mqtt.MqttSimpleCallback;

///////////////////////////////////////////////////////////////////
////MQTTTokenListener
/**
*  MQTTTokenListener is responsible for processing MQTT messages received,
*  converting back to tokens and putting those tokens into appropriate queues.
*
* @author Anar Huseynov
* @version $Id$
* @since Ptolemy II 8.0
* @Pt.ProposedRating Red (ahuseyno)
* @Pt.AcceptedRating Red (ahuseyno)
*/
public class TokenListener implements MqttSimpleCallback {

    /**
     * Initialize the instance by reading needed fields from the remoteModel.
     * @param remoteModel The remoteModel that created this publisher and controls the state of the simulation.
     */
    public TokenListener(RemoteModel remoteModel) {
        _remoteModel = remoteModel;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /**
     * Callback method when the connection with the broker is lost.
     * @see com.ibm.mqtt.MqttSimpleCallback#connectionLost()
     */
    public void connectionLost() throws Exception {
        //TODO: handle connection lost case
        System.out.println("Connection was lost at " + new Date());
    }

    /**
     * Callback method when a message from the topic is received.
     * @param topicName The name of the topic from which the message was received.
     * @param payload The MQTT message.
     * @param qos The Quality of Service at which the message was delivered by the broker.
     * @param retained indicates if this message is retained by the broker.
     * @see com.ibm.mqtt.MqttSimpleCallback#publishArrived(java.lang.String, byte[], int, boolean)
     * @exception Exception if there is a problem reading next token or setting attribute value
     */
    public void publishArrived(String topicName, byte[] payload, int qos,
            boolean retained) throws Exception {
        Tokenizer tokenizer = new Tokenizer(payload);
        Token token = null;
        while ((token = tokenizer.getNextToken()) != null) {
            // The listener is only concerned about the following types.
            if (token instanceof CommunicationToken) {
                CommunicationToken communicationToken = (CommunicationToken) token;
                RemoteSourceData data = _remoteModel.getRemoteSourceMap().get(
                        communicationToken.getTargetActorName());
                data.getTokenQueue().add(communicationToken);
                //Notify remote sources to read from the queue.
                synchronized (data.getRemoteSource()) {
                    data.getRemoteSource().notifyAll();
                }
            } else if (token instanceof AttributeChangeToken) {
                AttributeChangeToken attributeChangeToken = (AttributeChangeToken) token;
                Settable remoteAttribute = _remoteModel
                        .getSettableAttributesMap().get(
                                attributeChangeToken.getTargetSettableName());
                RemoteValueListener listener = _remoteModel
                        .getSettableAttributeListenersMap().get(
                                attributeChangeToken.getTargetSettableName());
                synchronized (listener) {
                    try {
                        listener.setEnabled(false);
                        remoteAttribute.setExpression(attributeChangeToken
                                .getExpression());
                        remoteAttribute.validate();
                    } finally {
                        listener.setEnabled(true);
                    }
                }
            } else if (token instanceof PingToken) {
                _remoteModel.getExecutor().execute(
                        new PongTask(new PongToken(((PingToken) token)
                                .getTimestamp())));
            } else if (token instanceof PongToken) {
                _remoteModel.setLastPongToken((PongToken) token);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * The remote model that created the publisher.
     */
    private final RemoteModel _remoteModel;

    /**
     * The task that sends the pong back. 
     */
    private class PongTask implements Runnable {

        /**
         * Create an instance with the provided token.
         * @param token The pong token to send back.
         */
        public PongTask(PongToken token) {
            _token = token;
        }

        /** 
         * Send the token back via the model's publisher;
         * @see java.lang.Runnable#run()
         */
        public void run() {
            try {
                _remoteModel.getTokenPublisher().sendToken(_token);
            } catch (IllegalActionException e) {
                //TODO handle this
            }
        }

        /**
         * The pong token of the current object.
         */
        private final PongToken _token;

    }

}
