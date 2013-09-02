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
            result#ID#Tn#                   
            var1=Val:var2=Val# 
            par1=Val:par2=Val# 
            end

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

            // Change IP and port of Transfer Server. 
            String _controlRequest = "settransferclienturl#1#127.0.0.1#10502#end";
            if (_controlRequest != null) {
                toServer.write(_controlRequest);
                toServer.flush();
                System.out.println("IP and port of Transfer Server is set!");
            }
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

            char[] serverBuffer = new char[1024];
            _inFromTransferServer.read(serverBuffer);
            serverBuffer.toString().trim();

            System.out.println("Message from Transfer Server : ");

            // Read simulation result from Transfer Server char by char until 
            // there is no char to read. Then write them to the console.

            for (char readChar : serverBuffer) {
                if (readChar == 'd')
                    System.out.println(readChar);
                else
                    System.out.print(readChar);
            }

            _transferServer.close();
            System.out.println("Transfer Server is closed!");
            _inFromTransferServer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private variables                    ////
    // An input stream to receive the simulation result back from Transfer Server.
    private BufferedReader _inFromTransferServer = null;

    // Transfer Server.
    private ServerSocket _transferServer = null;

}
