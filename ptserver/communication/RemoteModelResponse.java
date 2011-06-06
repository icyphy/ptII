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

import ptolemy.data.type.Type;
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

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /**
     * Set the model XML string containing the model specifically created for the Android client
     * that includes only actors that need to run there.
     * @param the model XML for the Android client.
     */
    public void setModelXML(String _modelXML) {
        this._modelXML = _modelXML;
    }

    /**
     * Return the model XML string containing the model specifically created for the Android client
     * that includes only actors that need to run there.
     * @return the model XML for the Android client.
     */
    public String getModelXML() {
        return _modelXML;
    }

    /**
     * Set the ticket identifying the model on the server.
     * @param the ticket identifying the model on the server.
     */
    public void setTicket(Ticket _ticket) {
        this._ticket = _ticket;
    }

    /**
     * Return the ticket identifying the model on the server.
     * @return the ticket identifying the model on the server.
     */
    public Ticket getTicket() {
        return _ticket;
    }

    /**
     * <p>Return the map from the model's Typeable objects to its inferred type.</p>
     * @param The map from the Typeable to its inferred type.
     */
    public void setModelTypes(HashMap<String, Type> _modelTypes) {
        this._modelTypes = _modelTypes;
    }

    /**
     * <p>Return the map from the model's Typeable objects to its inferred type.</p>
     * 
     * This map contains only types for the sinks and sources intended to run on Android.
     * @return the map from the Typeable to its inferred type.
     */
    public HashMap<String, Type> getModelTypes() {
        return _modelTypes;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private String _modelXML;
    private Ticket _ticket;
    private HashMap<String, Type> _modelTypes;
}
