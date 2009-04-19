/*

 Copyright (c) 2008 The Regents of the University of California.
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
package ptolemy.apps.naomi.command;

import java.io.BufferedReader;
import java.io.InputStreamReader;

//////////////////////////////////////////////////////////////////////////
//// Execute

/**


 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class Execute {

    /**
     *  @param args
     */
    public static void main(String[] args) throws Exception {
        for (int i = 0; i < args.length;) {
            int j = _processOption(args, i);
            if (j == i) {
                throw new Exception("Unknown option: " + args[i]);
            }
            i = j;
        }

        if (_command == null) {
            _usage();
        }

        String command = _command;
        if (_dosPathSeparator) {
            command = command.replace("/", "\\");
        } else if (_unixPathSeparator) {
            command = command.replace("\\", "/");
        }

        if (!_quiet) {
            System.out.println("Executing: " + command);
        }

        Runtime runtime = Runtime.getRuntime();
        Process process;
        if (_shell == null) {
            process = runtime.exec(command);
        } else {
            process = runtime.exec(new String[]{_shell, "-c", command});
        }
        BufferedReader input = new BufferedReader(new InputStreamReader(
                process.getInputStream()));
        String line = "";
        while (line != null) {
            line = input.readLine();
            if (line != null) {
                System.out.println(line);
            }
        }
        input.close();
        System.exit(process.waitFor());
    }

    private static int _processOption(String[] args, int index) {
        String arg = args[index];
        if (arg.equalsIgnoreCase("-dps") && !_unixPathSeparator) {
            _dosPathSeparator = true;
            return index + 1;
        } else if (arg.equalsIgnoreCase("-q")) {
            _quiet = true;
            return index + 1;
        } else if (arg.equalsIgnoreCase("-s")) {
            if (index + 1 < args.length) {
                _shell = args[index + 1];
                return index + 2;
            }
        } else if (arg.equalsIgnoreCase("-ups") && !_dosPathSeparator) {
            _unixPathSeparator = true;
            return index + 1;
        } else if (_command == null) {
            _command = arg;
            return index + 1;
        }
        return index;
    }

    private static void _usage() {
        System.out.println("Usage: java " + Execute.class.getName() + " [-q] " +
                "[-s shell] command");
        System.exit(1);
    }

    private static String _command;

    private static boolean _dosPathSeparator = false;

    private static boolean _quiet = false;

    private static String _shell = null;

    private static boolean _unixPathSeparator = false;
}
