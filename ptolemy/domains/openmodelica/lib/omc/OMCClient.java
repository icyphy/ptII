/* Simple simulation program to illustrate the implementation of a client.
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

package ptolemy.domains.openmodelica.lib.omc;


import java.io.IOException;

import lbnl.lib.openmodelica.UtilSocket;
import ptolemy.kernel.util.IllegalActionException;


/**    
    A simple simulation program to illustrate how to implement a client.
    OpenModelica Compiler(OMC) which is coupled to Ptolemy II is called to initialize the
    OMC server and simulate the Modelica model. The generated simulation runtime file which
    is located in $TMPDIR/$USERNAME/OpenModelica is run by this flag "-interactive -port 10500".  
    The simulation runtime will be waiting until a UI client in Ptolemy II has been connected to its port.
    After starting the simulation the keyboard entries and the results will be displayed in the same console as you are typing
    the command. For exchanging data with the OMC server the BSD socket is utilized.   
    
    @author Mana Mirzaei 
    @version $Id$
    @since Ptolemy II 9.1
    @Pt.ProposedRating Red (cxh)
    @Pt.AcceptedRating Red (cxh)
*/
public class OMCClient {
    
    /** OMC which is coupled to Ptolemy II is called to initialize the
     *  OMC server and simulate the Modelica model, then exchange data via BSD socket.
     *  TODO it should be detailed.
     */
    public static void main() throws IllegalActionException, IOException {

        try {
            // Create a unique instance of OMCProxy.
            _omcProxy = OMCProxy.getInstance();

            // Initialize the OpenModelica compiler(OMC) server.
            _omcProxy.initServer();

        } catch (ConnectException ex) {
            throw new IllegalActionException(
                    "Unable to start the OMC server!");
        }

        // Build the Modelica model and run the executable result file in an interactive processing mode.

        try {
            _omcProxy.loadFile("BouncingBall.mo","BouncingBall");
            _omcProxy.simulateModel("BouncingBall.mo", "BouncingBall",
                    "InteractiveBouncingBall", "0.0", "0.1", 500, "0.0001",
                    "dassl", "csv", ".*", " ", " ", "interactive");
        } catch (Throwable throwable) {
            throw new IllegalActionException(
                    "Unable to simulate BouncingBall model.");
        }

        // Create a unique instance of UtilSocket.
        _utilSocket = UtilSocket.getInstance();
        
        // Establish the client socket and initiate the BSD socket connection.
        _utilSocket.establishclientsocket();
        
        // Use the BSD socket to exchange data with the OMC server.     
        _utilSocket.exchangewithsocket();
        
        // Close the server and stop the server-to-user thread.
        _utilSocket.closesocket();

        // Quit the OMC server.
        try {
            _omcProxy.quitServer();
        } catch (ConnectException ex) {
            //FIXME
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variable                  ////
    // OMCProxy object for accessing a unique source of instance.
    private static OMCProxy _omcProxy;
    
    // UtilSocket object for accessing a unique source of instance.
    private static UtilSocket _utilSocket;
}
