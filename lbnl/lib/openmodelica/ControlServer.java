/* Thread that runs Control module of OMI.
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
       <p>ControlServer runs Control module, one of the modules of the OpenModelica Interactive (OMI) which are designated for communication over TCP/IP.
        Control module is the interface between OMI and Ptolemy II that is implemented as a single thread to support parallel 
        tasks and independent reactivity. Control module is considered the major controlling and communication 
        instance at simulation initialization phase besides managing simulation properties during simulation runtime. 
        A client permanently sends operations as messages to Control unit. It also reacts to the feedback from other 
        internal OMI components and sends messages back to the client, such as errors or status messages.</p>

        Request from Ptolemy II to Control:

        UI Request                        Description                            OMI::Control Reply
       ---------------------------------------------------------------------------------------------------------
        start#SEQ#end                     Start or continues the simulation      done#SEQ#end
       ---------------------------------------------------------------------------------------------------------
        pause#SEQ#end                     Pause the running simulation           done#SEQ#end
       ---------------------------------------------------------------------------------------------------------
        changevalue#SEQ#par=newvalue#end  Change the value of the appended       done#SEQ#end
                                         parameters and sets the simulation  
                                         time back to the point where the user 
                                         clicked in the UI.
       ---------------------------------------------------------------------------------------------------------
        setcontrolclienturl#SEQ#ip#port#  Change the IP and port of the          done#SEQ#end
                                          Control Server. Otherwise the 
                                          default configuration will be used.                             
       ---------------------------------------------------------------------------------------------------------
        settransferclienturl#SEQ#ip#port# Change the IP and port of the          done#SEQ#end
                                          Transfer Server. Otherwise the 
                                          default configuration will be used.  


        Message from Control to Ptolemy II:
       --------------------------------------------------------------------------------------------------------- 
        UI Request                   Description                               OMI::Control Reply
       ---------------------------------------------------------------------------------------------------------
        Error: MESSAGE               If an error occurs the OMI::Control       Up to the UI developers
                                     generates an error messages and sends
                                     the messages with the prefix “Error:” 
                                     to the UI.    
      @author Mana Mirzaei
      @version $Id$
      @since Ptolemy II 9.1
      @Pt.ProposedRating Red (cxh)
      @Pt.AcceptedRating Red (cxh)

 */
public class ControlServer extends Thread {
    /** Construct Control Server thread by creating Control Server and setting IP and port of the Server.
     *  @param toServer An output stream to send the request from Control Client to Control Server.
     */
    public ControlServer(BufferedWriter toServer) {
        try {
            _controlServer = new ServerSocket(10500);

            // Change IP and port of  Control Server.
            String _controlRequest = "setcontrolclienturl#1#127.0.0.1#10500#end";
            if (_controlRequest != null) {
                toServer.write(_controlRequest);
                toServer.flush();
                System.out.println("IP and port of Control Server is changed!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            Socket _controlConnection = _controlServer.accept();
            _inFromControlServer = new BufferedReader(new InputStreamReader(
                    _controlConnection.getInputStream()));

            char[] serverBuffer = new char[100];
            _inFromControlServer.read(serverBuffer);

            System.out.println("Message from Control Server : ");

            // Read simulation result from the server char by char until 
            // there is no char to read. Then write them to the console.
            for (char readChar : serverBuffer)
                // done#SEQ#end
                System.out.print(readChar);

            System.out.println();
            _inFromControlServer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (_controlServer.isBound()) {
            try {
                _controlServer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Control Server is closed!");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private variables                    ////
    // Control Server.
    private ServerSocket _controlServer = null;

    // An input stream to receive the response/simulation result back from Control Server.
    private BufferedReader _inFromControlServer = null;

}
