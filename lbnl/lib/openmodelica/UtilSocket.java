/* Methods for interfacing clients using BSD sockets.
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
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.swing.JOptionPane;

import ptolemy.util.StringUtilities;

/**    
     This file provides methods that allow clients to
     establish a socket connection. Clients typically call
     the method establishclientsocket() once, 
     and then call the method exchangewithsocket().
     At the end, a client should call
     closesocket() to close the socket connection.
     TODO COMPLETE THE DESCRIPTION.
          
    @author Mana Mirzaei
    @version $Id$
    @since Ptolemy II 9.1
    @Pt.ProposedRating Red (cxh)
    @Pt.AcceptedRating Red (cxh)
*/
public class UtilSocket implements IUtilSocket {

    ///////////////////////////////////////////////////////////////////
    ////                        public methods                    ////

    /** Close the input and output stream prior to closing the socket.
     *  @throws IOException
     */
    public void closesocket() throws IOException {

        try {
            _input.close();
            _output.close();
            _clientSocket.close();

        } catch (UnknownHostException e) {
            System.err.println("Trying to connect to unknown host: " + e);
        } catch (IOException e) {
            System.err.println("IOException:  " + e);
        }

        System.out.println("Socket is closed.");
        StringUtilities.exit(1);
    }

    /** Establish the client socket.
     *  Creating an output stream to send information to the server socket besides
     *  creating an input stream to receive response from the server socket.
     *  @throws IOException 
     */
    public void establishclientsocket() throws IOException {

        try {
            // Create a Socket connected to the specified host and port.
            _clientSocket = new Socket(_hostName, _portNo);

            System.out.println("Socket is opened.");

            // Create an output stream to send information to the server socket.
            _output = new DataOutputStream(_clientSocket.getOutputStream());

            // Create an input stream to receive response from the server socket.
            _input = new BufferedReader(new InputStreamReader(
                    _clientSocket.getInputStream()));

        } catch (IOException e) {
            System.out
                    .println("Failed to open the socket / create the inputstream or output stream: "
                            + e);
            StringUtilities.exit(1);
        }
    }

    /** Exchange data through the BSD socket.
     *  TODO COMPLETE THE DETAILS
     *  @throws IOException 
     */
    public void exchangewithsocket() throws IOException {

        System.out.println("Enter the operations to be sent to the server: ");

        if (_clientSocket != null && _output != null && _input != null) {
            try {

                // The basic functions such as start,pause and changevalue of parameter only work now.
                // The interactive simulation hangs after 1.958. 

                // Scanner userInput = new Scanner(System.in);
                // String operations = userInput.nextLine();

                // Get input from user using GUI.
                // Start the simulation operation by "start#1#end".
                String operations = JOptionPane
                        .showInputDialog("Enter your operation: ");
                
                // Write to the socket.
                _output.writeBytes(operations);
                
                System.out.println(operations
                        + " is sent successfully to the server.");

                String serverResponse = null;
                
                // FIXME IT'S NOT WORKING YET.
                // Read response from the server. 
                while ((serverResponse = _input.readLine()) != null) {
                    System.out.println("Server: " + serverResponse);
                }

                // Pause the simulation.
                //operations = "pause#3#end";
                /*_output.writeBytes(operations);
                System.out.println(operations
                        + " is sent successfully to the server.");

                // Change the value of the appended parameters and 
                // sets the simulation time back to the point where the user clicked in the UI
                /* operations = "changevalue#1#load.w=2.3#end";
                _output.writeBytes(operations);
                System.out.println(operations + " is sent successfully to the server.");*/

            } catch (IOException e) {
                System.out.println("Failed to exchange data via sockets: " + e);
                StringUtilities.exit(1);
            }
        }

    }

    /** Create an instance of UtilSocket object in order to provide a global point of access to the instance.
     *  It provides a unique source of the UtilSocket instance.
     *  @return _utilSocket The UtilSocket object representing the instance value.
     */
    public static UtilSocket getInstance() {
        if (_utilSocket == null)
            _utilSocket = new UtilSocket();
        return _utilSocket;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private variables                    ////
    // The name of the socket for establishing the connection.
    private Socket _clientSocket = null;

    // The name of the host.
    private String _hostName = "127.0.0.1";

    // Input stream for receiving response from the server.
    private BufferedReader _input = null;

    // Output stream for sending information to the server.
    private DataOutputStream _output = null;

    // Port number of OMC server.
    private int _portNo = 10501;

    // UtilSocket Object for accessing a unique source of instance.
    private static UtilSocket _utilSocket = new UtilSocket();
}
