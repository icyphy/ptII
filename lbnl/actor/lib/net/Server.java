// Server that opens a BSD socket for communication with simulation engine.

/*
 ********************************************************************
Copyright Notice
----------------

Building Controls Virtual Test Bed (BCVTB) Copyright (c) 2008-2009, The
Regents of the University of California, through Lawrence Berkeley
National Laboratory (subject to receipt of any required approvals from
the U.S. Dept. of Energy). All rights reserved.

If you have questions about your rights to use or distribute this
software, please contact Berkeley Lab's Technology Transfer Department
at TTD@lbl.gov

NOTICE.  This software was developed under partial funding from the U.S.
Department of Energy.  As such, the U.S. Government has been granted for
itself and others acting on its behalf a paid-up, nonexclusive,
irrevocable, worldwide license in the Software to reproduce, prepare
derivative works, and perform publicly and display publicly.  Beginning
five (5) years after the date permission to assert copyright is obtained
from the U.S. Department of Energy, and subject to any subsequent five
(5) year renewals, the U.S. Government is granted for itself and others
acting on its behalf a paid-up, nonexclusive, irrevocable, worldwide
license in the Software to reproduce, prepare derivative works,
distribute copies to the public, perform publicly and display publicly,
and to permit others to do so.


Modified BSD License agreement
------------------------------

Building Controls Virtual Test Bed (BCVTB) Copyright (c) 2008-2009, The
Regents of the University of California, through Lawrence Berkeley
National Laboratory (subject to receipt of any required approvals from
the U.S. Dept. of Energy).  All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

   1. Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
   2. Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in
      the documentation and/or other materials provided with the
      distribution.
   3. Neither the name of the University of California, Lawrence
      Berkeley National Laboratory, U.S. Dept. of Energy nor the names
      of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

You are under no obligation whatsoever to provide any bug fixes,
patches, or upgrades to the features, functionality or performance of
the source code ("Enhancements") to anyone; however, if you choose to
make your Enhancements available either publicly, or directly to
Lawrence Berkeley National Laboratory, without imposing a separate
written license agreement for such Enhancements, then you hereby grant
the following license: a non-exclusive, royalty-free perpetual license
to install, use, modify, prepare derivative works, incorporate into
other computer software, distribute, and sublicense such enhancements or
derivative works thereof, in binary and source code form.

 ********************************************************************
 */

package lbnl.actor.lib.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/** Server that opens a BSD socket for communication with simulation engine.
 *
 * @author Michael Wetter
 * @version $Id$
 * @since Ptolemy II 8.0
 */
public class Server {

    /** Construct a server on any available port.
     *
     * @param timOut Socket time out in milliseconds.
     * @exception IOException If the server socket cannot be opened.
     */
    public Server(int timOut) throws IOException {
        this(0, timOut);
    }

    /** Construct a server on the port specified by <tt>portNo</tt>.
     *
     * @param portNo Port number for BSD socket.
     * @param timOut Socket time out in milliseconds.
     * @exception IOException If the server socket cannot be opened.
     */
    public Server(int portNo, int timOut) throws IOException {
        serSoc = new ServerSocket();
        serSoc.bind(new java.net.InetSocketAddress(portNo));

        flaFroCli = 0;
        if (!serSoc.isBound()) {
            String em = "Server socket failed to bind to an address.";
            throw new IOException(em);
        }
        serSoc.setSoTimeout(timOut);
    }

    /** Get the port number.
     *
     *@return The port number.
     */
    public int getLocalPort() {
        return serSoc.getLocalPort();
    }

    /** Write data to the socket.
     *
     * @param flagToClient The communication flag.
     * @param curTim The current simulation time.
     * @param dblVal The array with double values.
     * @exception IOException If a communication problems occur.
     */
    public void write(int flagToClient, double curTim, double[] dblVal)
            throws IOException {
        simTimWri = curTim;
        ////////////////////////////////////////////////////
        // Set up string
        // add zeros for number of integers and booleans
        final int nDbl = dblVal != null ? dblVal.length : 0;
        StringBuffer strBuf = new StringBuffer(Integer.toString(verNo));
        strBuf.append(" " + Integer.toString(flagToClient)); // the communication flag
        strBuf.append(" " + Integer.toString(nDbl)); // then number of doubles
        strBuf.append(" 0 0 "); // the number of integers and booleans
        strBuf.append(curTim); // the current simulation time
        strBuf.append(" ");
        for (int i = 0; i < nDbl; i++) {
            strBuf.append(String.valueOf(dblVal[i]));
            strBuf.append(" ");
        }
        // Add line termination for parsing in client.
        // Don't use line.separator here as the client searches for \n
        strBuf.append("\n");
        _write(strBuf);
    }

    /** Write the data to the socket.
     *
     * @param strBuf The string buffer to be sent to the socket.
     * @exception IOException If communication problems occur.
     */
    private void _write(StringBuffer strBuf) throws IOException {
        if (cliSoc == null) {
            System.out
                    .println("lbnl.actor.lib.net.Server._write(\""
                            + strBuf
                            + "\"): the client socket is null, this can happen if _write() "
                            + "is called before _read(), such as when exporting to JNLP.");
            return;
        }
        BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(
                cliSoc.getOutputStream()));
        final String str = new String(strBuf);
        // We write 8192 characters at once since the client only
        // receives that many characters, even if setsockopt is called
        // to increase the socket buffer.
        // The number 8192 needs to be the same as in utilSocket.c
        final int maxCha = 8192;
        final int strLen = str.length();
        final int nWri = strLen / maxCha + 1;
        for (int i = 0; i < nWri; i++) {
            wr.write(str.substring(i * maxCha,
                    java.lang.Math.min(strLen, (i + 1) * maxCha)));
            wr.flush();
        }
    }

    /** Returns the last communication flag read from the socket.
     *
     * @return The communication flag.
     */
    public int getClientFlag() {
        return flaFroCli;
    }

    /** Return the last double array read from the socket.
     *
     * @return dblVal The array with double values.
     */
    public double[] getDoubleArray() {
        return dblVal;
    }

    /** Return the last simulation time written to the client.
     *
     * @return The last simulation time written to the client.
     */
    public double getSimulationTimeWrittenToClient() {
        return simTimWri;
    }

    /** Return the last simulation time read from the client.
     *
     * @return The last simulation time read from the client.
     */
    public double getSimulationTimeReadFromClient() {
        return simTimRea;
    }

    /** Read data from the socket.
     *
     * @exception IOException If communication problems occur.
     * @exception SocketTimeoutException If the socket does not respond.
     */
    public void read() throws IOException, SocketTimeoutException {
        if (cliSoc == null) { // first call
            cliSoc = serSoc.accept(); // accept calls from client
        }

        ////////////////////////////////////////////////////////
        final InputStreamReader inpStrRea = new InputStreamReader(
                cliSoc.getInputStream());
        final BufferedReader d = new BufferedReader(inpStrRea);
        final String line = d.readLine();
        if (line == null) {
            final String em = "Client did not send any new values." + LS
                    + "It appears that it terminated without sending" + LS
                    + "an error message to the server." + LS
                    + "Check log files of client program for possible errors.";
            throw new IOException(em);
        }
        ////////////////////////////////////////////////////
        // get elements from the line
        String[] ele = line.split(" ");
        Integer.parseInt(ele[0]);
        flaFroCli = Integer.parseInt(ele[1]); // the communication flag
        if (flaFroCli == 0) { // read further if flag is nonzero
            final int nDbl = Integer.parseInt(ele[2]);
            Integer.parseInt(ele[3]);
            Integer.parseInt(ele[4]);
            simTimRea = Double.parseDouble(ele[5]);
            dblVal = new double[nDbl];
            // check sufficient array length
            if (nDbl != dblVal.length) {
                throw new IOException("Received " + nDbl
                        + " doubles, but expected " + dblVal.length
                        + " elements.");
            }
            for (int i = 0; i < nDbl; i++) {
                dblVal[i] = Double.parseDouble(ele[6 + i]);
            }
        }
    }

    /** Close the socket.
     *
     * @exception IOException If an I/O error occurs when closing the socket.
     */
    public void close() throws IOException {
        if (cliSoc != null) {
            cliSoc.close();
        }
        if (serSoc != null) {
            serSoc.close();
        }
    }

    /** Main method that can be used for testing.
     *  @param args An array of length 1 that names the port
     *  to be used
     *  @exception Exception If anything goes wrong.
     */
    public static void main(String[] args) throws Exception {
        int timOut = 10000; // time out in milliseconds
        Server[] ser = new Server[2];
        ser[0] = new Server(Integer.parseInt(args[0]), timOut);
        ser[1] = new Server(Integer.parseInt(args[0]) + 1, timOut);
        double[] dbl = new double[1];
        for (int i = 0; i < dbl.length; i++) {
            dbl[i] = i;
        }
        int iLoo = 0;
        while (true) {
            for (int iSer = 0; iSer < 2; iSer++) {
                Thread.sleep(10); // in milliseconds
                ser[iSer].read();
                //                Thread.sleep(10);
                // FindBugs: Primitive value is boxed and then immediately unboxed.
                ser[iSer].write(0, Double.valueOf(iLoo).doubleValue(), dbl);
                System.out.println("Loop number: " + iLoo);
            }
            iLoo++;
        }
    }

    /** The client socket. */
    Socket cliSoc;

    /** The array that contains the last double values read from the socket .*/
    protected double[] dblVal;

    /** The communication flag read during the socket read command. */
    protected int flaFroCli;

    /** The server socket. */
    protected ServerSocket serSoc;

    /** The current simulation time as received from the client. */
    protected double simTimRea;

    /** The simulation time last written to the client. */
    protected double simTimWri;

    /** The version number of the socket implementation. */
    protected int verNo = 2;

    /** System dependent line separator */
    private final static String LS = System.getProperty("line.separator");

}
