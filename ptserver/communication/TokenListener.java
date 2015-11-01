/*
 TokenListener is responsible for processing MQTT messages received,
 converting back to tokens and putting those tokens into appropriate queues.

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

import java.util.logging.Logger;

import ptolemy.data.Token;
import ptolemy.kernel.util.Settable;
import ptserver.data.AttributeChangeToken;
import ptserver.data.CommunicationToken;
import ptserver.data.PingToken;
import ptserver.data.PongToken;
import ptserver.data.RemoteEventToken;
import ptserver.data.Tokenizer;

import com.ibm.mqtt.MqttSimpleCallback;

///////////////////////////////////////////////////////////////////
//// TokenListener

/** TokenListener is responsible for processing MQTT messages received,
 *  converting back to tokens and putting those tokens into appropriate queues.
 *
 *  @author Anar Huseynov
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (ahuseyno)
 *  @Pt.AcceptedRating Red (ahuseyno)
 */
public class TokenListener implements MqttSimpleCallback {

    ///////////////////////////////////////////////////////////////////
    ////                         constructor                       ////

    /** Initialize the instance by reading needed fields from the remoteModel.
     *  @param proxyModelInfrastructure The infrastructure that created this listener and controls the state of the execution.
     */
    public TokenListener(ProxyModelInfrastructure proxyModelInfrastructure) {
        _proxyModelInfrastructure = proxyModelInfrastructure;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Callback method when the connection with the broker is lost.
     *  @exception Exception If the connection was lost.
     *  @see com.ibm.mqtt.MqttSimpleCallback#connectionLost()
     */
    @Override
    public void connectionLost() throws Exception {
        //TODO: handle connection lost case
        _LOGGER.info("Connection was lost");
    }

    /** Callback method when a message from the topic is received.
     *  @param topicName The name of the topic from which the message was received.
     *  @param payload The MQTT message.
     *  @param qos The Quality of Service at which the message was delivered by the broker.
     *  @param retained indicates if this message is retained by the broker.
     *  @see com.ibm.mqtt.MqttSimpleCallback#publishArrived(java.lang.String, byte[], int, boolean)
     *  @exception Exception if there is a problem reading next token or setting attribute value
     */
    @Override
    public void publishArrived(String topicName, byte[] payload, int qos,
            boolean retained) throws Exception {
        Tokenizer tokenizer = new Tokenizer(payload);
        Token token = null;
        // TODO remove this or change to proper logging
        _LOGGER.fine("received batch " + _batchCount++);
        while ((token = tokenizer.getNextToken()) != null) {

            // The listener is only concerned about the following types.
            if (token instanceof CommunicationToken) {
                CommunicationToken communicationToken = (CommunicationToken) token;
                ProxySourceData data = _proxyModelInfrastructure
                        .getProxySourceMap().get(
                                communicationToken.getTargetActorName());
                data.getTokenQueue().add(communicationToken);

                //Notify remote sources to read from the queue.
                synchronized (data.getProxySource()) {
                    data.getProxySource().notifyAll();
                }
            } else if (token instanceof AttributeChangeToken) {
                AttributeChangeToken attributeChangeToken = (AttributeChangeToken) token;
                Settable remoteAttribute = _proxyModelInfrastructure
                        .getRemoteAttributesMap().get(
                                attributeChangeToken.getTargetSettableName());

                ProxyValueListener listener = _proxyModelInfrastructure
                        .getRemoteAttributeListenersMap().get(
                                attributeChangeToken.getTargetSettableName());
                synchronized (listener) {
                    try {
                        listener.setEnabled(false);
                        // Note: obtaining the write access breaks this functionality.
                        // Disabled for now since documentation does not say that
                        // write access needs to be obtained for setting the expression.
                        //                        ((Attribute) remoteAttribute).workspace()
                        //                                .getWriteAccess();
                        remoteAttribute.setExpression(attributeChangeToken
                                .getExpression());
                        remoteAttribute.validate();
                    } finally {
                        //   ((Attribute) remoteAttribute).workspace().doneWriting();
                        listener.setEnabled(true);
                    }
                }
                _LOGGER.info("Received attribute change token");
            } else if (token instanceof PingToken) {
                _proxyModelInfrastructure.getExecutor().execute(
                        new PongTask(new PongToken(((PingToken) token)
                                .getTimestamp())));
                _LOGGER.info("Received ping token");
            } else if (token instanceof PongToken) {
                _proxyModelInfrastructure.setLastPongToken((PongToken) token);
                _LOGGER.info("Received pong token");
            } else if (token instanceof RemoteEventToken) {
                _proxyModelInfrastructure
                .fireServerEvent((RemoteEventToken) token);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The infrastructure that created the listener.
     */
    private final ProxyModelInfrastructure _proxyModelInfrastructure;

    /**
     * The batch counter used for logging purposes.
     */
    private int _batchCount;
    /**
     * The logger used by the ptserver.
     */
    private static final Logger _LOGGER = Logger.getLogger("PtolemyServer");

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** The task that sends the pong back.
     */
    private class PongTask implements Runnable {

        ///////////////////////////////////////////////////////////////////
        ////                         constructor                       ////

        /** Create an instance with the provided token.
         *  @param token The pong token to send back.
         */
        public PongTask(PongToken token) {
            _token = token;
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** Send the token back via the model's publisher.
         *  @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            try {
                _proxyModelInfrastructure.getTokenPublisher().sendToken(_token,
                        null);
                _LOGGER.info("Sent pong token");
            } catch (Throwable e) {
                _proxyModelInfrastructure.fireModelException(
                        "Unhandled exception in the PongTask", e);
            }
        }

        ///////////////////////////////////////////////////////////////////
        ////                         private variables                 ////

        /** The pong token of the current object.
         */
        private final PongToken _token;
    }

}
