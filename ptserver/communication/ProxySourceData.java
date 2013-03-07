/*
 A helper data structure holding a reference to the proxy source and its queue.

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

import java.util.concurrent.ConcurrentLinkedQueue;

import ptserver.actor.ProxySource;
import ptserver.data.CommunicationToken;

///////////////////////////////////////////////////////////////////
//// ProxySourceData

/**
 * A helper data structure holding a reference to the proxy source and its queue.
 * @author Anar Huseynov
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public class ProxySourceData {

    /**
     * Creates an instance of the data structure with the specified ProxySource and
     * a new token queue.
     * @param proxySource The remote source of the ProxySourceData
     */
    public ProxySourceData(ProxySource proxySource) {
        _proxySource = proxySource;
        _tokenQueue = new ConcurrentLinkedQueue<CommunicationToken>();
        proxySource.setProxySourceData(this);
    }

    /**
     * Return the ProxySource.
     * @return the ProxySource of the instance.
     */
    public ProxySource getProxySource() {
        return _proxySource;
    }

    /**
     * Return the token queue that stores tokens sent for the ProxySource.
     * @return the token queue
     */
    public ConcurrentLinkedQueue<CommunicationToken> getTokenQueue() {
        return _tokenQueue;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    /**
     * The remoteSource actor.
     */
    private final ProxySource _proxySource;
    /**
     * The token queue that collects tokens received for the remoteSource.
     */
    private final ConcurrentLinkedQueue<CommunicationToken> _tokenQueue;
}
