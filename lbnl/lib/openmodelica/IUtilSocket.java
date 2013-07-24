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
import java.net.UnknownHostException;

/**
    <p> The interface to the UtilSocket. </p>

    @author Mana Mirzaei 
    @version $Id$
    @since Ptolemy II 9.1
    @Pt.ProposedRating Red (cxh)
    @Pt.AcceptedRating Red (cxh)
 */
public interface IUtilSocket {

    /** Close the socket.
     *  @throws IOException If an I/O error occurs at the time of closing the socket.
     */
    public void closesocket() throws IOException;

    /** Establish the client socket and set up streams for exchanging data between the client and the server. 
     * @return 
     *  @throws IOException If an I/O error occurs when creating the socket.
     *  @throws UnknownHostException If the IP address of the host could not be determined.
     */
    public void establishclientsocket() throws IOException,
    UnknownHostException;

    /**  There are two modules, control and transfer, in the OpenModelica Interactive (OMI) which are designated for a communication over TCP/IP.
     *   This network communication technology is used to send and receive messages between the client, Ptolemy II and the server, OMC. 
     *
     *   Control module is the interface between OMI and a UI that is implemented as a single thread to support parallel 
     *   tasks and independent reactivity. Control module is considered the major controlling and communication 
     *   instance at simulation initialization phase besides managing simulation properties during simulation runtime. 
     *   A client permanently sends operations as messages to the Control unit. It also reacts to the feedback from other 
     *   internal OMI components and sends messages back to the client, such as error or status messages. 
     *  
     *   Transfer module gets simulation results from a result manager and sends them to the UI upon starting a simulation. 
     *   This module employs filter mask property containing names of all properties whose result values are significant to the UI.
     *
     *   The following commands are available message from the UI to OMI(Request-Reply):
     *   
     *   ---------------------------------------------------------------------------------------------------------
     *   UI Request                     Description                            OMI::Control Reply
     *   ---------------------------------------------------------------------------------------------------------
     *   start#SEQ#end                 Starts or continues the simulation      done#SEQ#end
     *   ---------------------------------------------------------------------------------------------------------
     *   pause#SEQ#end                 Pauses the running simulation           done#SEQ#end
     *   ---------------------------------------------------------------------------------------------------------
     *   changevalue#1#load.w=2.3#end  Change the value of the appended 
     *                                 parameters and sets the simulation 
     *                                 time back to the point where the user 
     *                                 clicked in the UI.
     *   ---------------------------------------------------------------------------------------------------------
     *                                 
     *  The following commands are available messages from OMI::Transfer to UI. TODO COMPLETE AFTER FIXING THE CODE - GETTING BACK THE RESULT BACK FROM 
     *  SERVER TO THE UI.
     *                                 
     *   ---------------------------------------------------------------------------------------------------------                              
     *   OMI::Transfer                   Description                                       UI
     *   ---------------------------------------------------------------------------------------------------------                            
     *   result#ID#Tn#                   
     *   var1=Val:var2=Val# 
     *   par1=Val:par2=Val# 
     *   end
     *  ---------------------------------------------------------------------------------------------------------
     *   result#ID#Tn# 
     *   1=Val:2=Val# 
     *   1=Val:2=Val# 
     *   end
     *   ---------------------------------------------------------------------------------------------------------
     *          
     *   For the time being, the interactive simulation hangs after 1.958, OpenModelica Developer team are trying to fix this issue.
     *   @throws IOException If an I/O error occurs when client and server are exchanging the message.
     */
    public void exchangewithsocket() throws IOException;

}
