/* Thread that runs Transfer module of OMI.
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**   
      <p>TransferServer runs Transfer module, one of the modules of the OpenModelica Interactive (OMI) which are designated for communication over TCP/IP.
      Transfer module gets simulation results from a result manager and sends them to the Ptolemy II upon starting a simulation. 
      This module employs filter mask property containing names of all properties whose result values are significant to the Ptolemy II.</p>

        Message format from Transfer to UI:   
        <pre>  result#ID#Tn#                   
               var1=Val:var2=Val# 
               par1=Val:par2=Val# 
               end
        </pre>
       If no filter is set, the result by default is : result#time####end
       If filter such as setfilter#4#load#w#end is set, the result is according to the filter : result#time#load#w#end/result#0.02#1#3#end

      @author Mana Mirzaei
      @version $Id$
      @since Ptolemy II 9.1
      @Pt.ProposedRating Red (cxh)
      @Pt.AcceptedRating Red (cxh)
 */
public class TransferServer extends Thread {
    /** Construct Transfer Server thread by creating Transfer Server and setting IP and port of the Server.
     *  @param toServer An output stream to send the request from Control Client to Control Server.
     *  @throws IOException If I/O error occurs while creating the socket.
     */
    public TransferServer(BufferedWriter toServer) throws IOException {
        try {
            _transferServer = new ServerSocket(10502);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException(
                    "An I/O error occurs when creating the server socket!"
                            + e.getMessage());
        }
    }

    public void run() {
        try {

            Socket _transferConnection = _transferServer.accept();
            _inFromTransferServer = new BufferedReader(new InputStreamReader(
                    _transferConnection.getInputStream()));

            // Read simulation result from Transfer Server, then write them to the console.

            System.out.println("Message from Transfer Server : ");

            char[] serverBuffer = new char[1024];

            while (true) {
                _inFromTransferServer.read(serverBuffer);
                System.out.println(new String(serverBuffer).trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (_transferServer.isBound()) {
            try {
                _transferServer.close();
                _inFromTransferServer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Transfer Server is closed!");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private variables                    ////
    // An input stream to receive the simulation result back from Transfer Server.
    private BufferedReader _inFromTransferServer = null;

    // Transfer Server.
    private ServerSocket _transferServer = null;

}
