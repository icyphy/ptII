/*
 The server's response to the request to open a model.

 Copyright (c) 2011-2013 The Regents of the University of California.
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

import java.io.Serializable;
import java.util.HashMap;

import ptserver.control.Ticket;

///////////////////////////////////////////////////////////////////
//// RemoteModelResponse

/** The server's response to the request to open a model.
 *  @author Anar Huseynov
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (ahuseyno)
 *  @Pt.AcceptedRating Red (ahuseyno)
 */
@SuppressWarnings("serial")
public class ProxyModelResponse implements Serializable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the URL of the message broker.
     *  @return The URL of the message broker to which the client should publish tokens.
     *  @see #setBrokerUrl(String)
     */
    public String getBrokerUrl() {
        return _brokerUrl;
    }

    /** Get the model image (PNG) byte array.
     *  @return The image of the model.
     *  @see #setModelImage(byte[])
     */
    public byte[] getModelImage() {
        return _modelImage;
    }

    /** Get the map from the model's Typeable objects to its inferred type.
     *  <p>This map contains only types for the sinks and sources intended to run on Android.</p>
     *  @return the map from the Typeable to its inferred type.
     *  @see #setModelTypes(HashMap)
     */
    public HashMap<String, String> getModelTypes() {
        return _modelTypes;
    }

    /** Get the model XML string containing the model specifically created for
     *  the Android client
     *  that includes only actors that need to run there.
     *  @return The model XML for the Android client.
     *  @see #setModelXML(String)
     */
    public String getModelXML() {
        return _modelXML;
    }

    /** Get the ticket identifying the model on the server.
     *  @return The ticket identifying the model on the server.
     *  @see #setTicket(Ticket)
     */
    public Ticket getTicket() {
        return _ticket;
    }

    /** Set the URL of the message broker.
     *  @param brokerUrl The URL of the message broker to which the client should publish tokens.
     *  @see #getBrokerUrl()
     */
    public void setBrokerUrl(String brokerUrl) {
        _brokerUrl = brokerUrl;
    }

    /** Set the model image (PNG) byte array.
     *  @param modelImage The byte array of the model image.
     *  @see #getModelImage()
     */
    public void setModelImage(byte[] modelImage) {
        _modelImage = modelImage;
    }

    /** Set the map from the model's Typeable objects to its inferred type.
     *  @param modelTypes The map from the Typeable to its inferred type.
     *  @see #getModelTypes()
     */
    public void setModelTypes(HashMap<String, String> modelTypes) {
        //new Exception("ProxyModelTypes.setModelTypes(): " + modelTypes).printStackTrace();
        _modelTypes = modelTypes;
    }

    /** Set the model XML string containing the model specifically created for the Android client
     *  that includes only actors that need to run there.
     *  @param modelXML The model XML for the Android client.
     *  @see #getModelXML()
     */
    public void setModelXML(String modelXML) {
        _modelXML = modelXML;
    }

    /** Set the ticket identifying the model on the server.
     *  @param ticket The ticket identifying the model on the server.
     *  @see #getTicket()
     */
    public void setTicket(Ticket ticket) {
        _ticket = ticket;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The URL of the server's message broker.
     */
    private String _brokerUrl;

    /** The image of the model in byte form.
     */
    private byte[] _modelImage;

    /** The mapping from Typeable full names to its types needed to initialize ports on the client.
     */
    private HashMap<String, String> _modelTypes;

    /** The model XML containing only actors needed for the client.
     */
    private String _modelXML;

    /** The ticket opened for the remote model.
     */
    private Ticket _ticket;
}
