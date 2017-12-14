// Class for writing xml documents.

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

package lbnl.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;

/** Class for writing xml documents.
 *
 * @author Michael Wetter
 * @version $Id$
 * @since Ptolemy II 8.0
 */
public class XMLWriter {

    /** Construct an instance and assign all data members.
     *
     * @param fileDirectory The system-dependent directory name.
     * @param fileName The system-dependent filename.
     * @param portNo The port number for BSD socket.
     */
    public XMLWriter(String fileDirectory, String fileName, int portNo) {
        filDir = fileDirectory;
        filNam = fileName;
        porNo = portNo;
    }

    /** Write the xml file.
     *
     * @exception FileNotFoundException If the file exists but is a directory rather
     *              than a regular file, does not exist but cannot be created,
     *              or cannot be opened for any other reason.
     * @exception IOException if an I/O error occurs.
     */
    public synchronized void write() throws FileNotFoundException, IOException {
        // Prepare string
        final InetAddress localHost = InetAddress.getLocalHost();
        final String hosNam = localHost.getHostName();
        final String s = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + LS
                + "<BCVTB-client>" + LS + "  <ipc>" + LS + "    <socket port=\""
                + porNo + "\" hostname=\"" + hosNam + "\"/>" + LS + "  </ipc>"
                + LS + "</BCVTB-client>" + LS;

        // Write string
        final File fil = new File(filDir, filNam);
        final FileOutputStream fos = new FileOutputStream(fil);
        final PrintWriter priWri = new PrintWriter(fos, true);
        priWri.println(s);
        priWri.close();
    }

    /** Integer port number for BSD socket. */
    protected int porNo;

    /** System-dependent directory name of the xml file. */
    protected String filDir;

    /** Name of the xml file. */
    protected String filNam;

    /** System dependent line separator. */
    private final static String LS = System.getProperty("line.separator");

    /** Main method that can be used for testing.
     *  @param arg An array of Strings, the first argument being a integer
     *  that names a file called test-XXX.txt.
     */
    public static void main(String[] arg) {
        String fileDirectory = ".";
        int portNo = 1;
        int n = Integer.parseInt(arg[0]);
        for (int i = 0; i < n; i++) {
            XMLWriter w = new XMLWriter(fileDirectory, "test-" + i + ".txt",
                    portNo);
            try {
                w.write();
            } catch (Exception e) {
                System.err.println("Exception: " + e.getMessage());
            }
        }

    }
}
