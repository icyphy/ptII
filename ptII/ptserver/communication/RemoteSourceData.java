/*
 A helper data structure holding a reference to the remote source and its queue.
 
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

import java.util.concurrent.ArrayBlockingQueue;

import ptserver.actor.RemoteSource;
import ptserver.data.CommunicationToken;

//////////////////////////////////////////////////////////////////////////
//// RemoteSourceData

/**
 * A helper data structure holding a reference to the remote source and its queue.
 * @author Anar Huseynov
 * @version $Id$ 
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public class RemoteSourceData {

    /**
     * Creates an instance of the data structure with the specified RemoteSource and 
     * a new token queue of max 100 elements.
     * @param remoteSource The remote source of the RemoteSourceData
     */
    public RemoteSourceData(RemoteSource remoteSource) {
        _remoteSource = remoteSource;
        _tokenQueue = new ArrayBlockingQueue<CommunicationToken>(100);
        remoteSource.setTokenQueue(_tokenQueue);
    }

    /**
     * Return the RemoteSource.
     * @return the RemoteSource
     */
    public RemoteSource getRemoteSource() {
        return _remoteSource;
    }

    /**
     * Return the token queue that stores tokens sent for the RemoteSource.
     * @return the token queue
     */
    public ArrayBlockingQueue<CommunicationToken> getTokenQueue() {
        return _tokenQueue;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    /**
     * The remoteSource actor
     */
    private final RemoteSource _remoteSource;
    /**
     * The token queue that collects tokens received for the remoteSource
     */
    private final ArrayBlockingQueue<CommunicationToken> _tokenQueue;

}
