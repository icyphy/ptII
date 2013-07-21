/* The interface to the UtilSocket.
 *
 * Copyright (c) 2012-2013,
 * Programming Environment Laboratory (PELAB),
 * Department of Computer and getInformation Science (IDA),
 * Linkoping University (LiU).
 *
 * All rights reserved.
 *
 * (The new BSD license, see also
 *  http://www.opensource.org/licenses/bsd-license.php)
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * * Neither the name of Authors nor the name of Linkopings University nor
 *   the names of its contributors may be used to endorse or promote products
 *   derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 * 
 */

package lbnl.lib.openmodelica;

import java.io.IOException;

//FIXME: what is a UtilSocket and how would I use it?
/**
    The interface to the UtilSocket.

    @author Mana Mirzaei 
    @version $Id$
    @since Ptolemy II 9.1
    @Pt.ProposedRating Red (cxh)
    @Pt.AcceptedRating Red (cxh)
 */
public interface IUtilSocket {

    /** Close the input and output stream prior to closing the socket.
     *  @throws IOException
     */
    // FIXME: Use camelCase here, so change closesocket to closeSocket
    public void closesocket() throws IOException;

    /** Establish the client socket.  Creating an output stream to
     *  send information to the server socket besides creating an
     *  input stream to receive response from the server socket.
     *  @return true if the client socket was established.
     *  @throws IOException If there is a problem establishing the
     *  client socket.
     */
    // FIXME: Use camelCase here.
    public boolean establishclientsocket() throws IOException;

    /** Exchange data through the BSD socket.
     * @throws IOException If there is a problem exchanging data with
     * the socket.
     * @throws Exception If there is a problem exchanging data with
     * the socket.
     */
    // FIXME: Use camelCase here.
    public void exchangewithsocket() throws IOException, Exception;

}
