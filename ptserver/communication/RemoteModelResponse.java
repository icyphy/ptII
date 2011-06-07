/*
 The server's response to the request to open a model.
  
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
/**
 * 
 */
package ptserver.communication;

import java.io.Serializable;
import java.util.HashMap;

import ptserver.control.Ticket;

///////////////////////////////////////////////////////////////////
//// RemoteModelResponse
/**
 * The server's response to the request to open a model.
 * @author Anar Huseynov
 * @version $Id$ 
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public class RemoteModelResponse implements Serializable {
    /**
     * Return the map from the model's Typeable objects to its inferred type.
     * 
     * <p>This map contains only types for the sinks and sources intended to run on Android.</p>
     * @return the map from the Typeable to its inferred type.
     * @see #setModelTypes(HashMap)
     */
    public HashMap<String, String> getModelTypes() {
        return _modelTypes;
    }

    /**
     * Return the model XML string containing the model specifically created for the Android client
     * that includes only actors that need to run there.
     * @return the model XML for the Android client.
     * @see #setModelXML(String)
     */
    public String getModelXML() {
        return _modelXML;
    }

    /**
     * Return the ticket identifying the model on the server.
     * @return the ticket identifying the model on the server.
     * @see #setTicket(Ticket)
     */
    public Ticket getTicket() {
        return _ticket;
    }

    /**
     * Return the map from the model's Typeable objects to its inferred type.
     * @param modelTypes The map from the Typeable to its inferred type.
     * @see #getModelTypes()
     */
    public void setModelTypes(HashMap<String, String> modelTypes) {
        this._modelTypes = modelTypes;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /**
     * Set the model XML string containing the model specifically created for the Android client
     * that includes only actors that need to run there.
     * @param modelXML the model XML for the Android client.
     * @see #getModelXML()
     */
    public void setModelXML(String modelXML) {
        this._modelXML = modelXML;
    }

    /**
     * Set the ticket identifying the model on the server.
     * @param ticket the ticket identifying the model on the server.
     * @see #getTicket()
     */
    public void setTicket(Ticket ticket) {
        this._ticket = ticket;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    /**
     * The model XML containing only actors needed for the client
     */
    private String _modelXML;
    /**
     * The ticket opened for the remote model
     */
    private Ticket _ticket;
    /**
     * The mapping from Typeable full names to its types needed to initialize ports on the client
     */
    private HashMap<String, String> _modelTypes;
}
