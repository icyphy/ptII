/*
 Copyright (c) 2001-2003 The Regents of the University of California.
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

@@ProposedRating Green (cxh@@eecs.berkeley.edu)
@@AcceptedRating Red
*/
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;

/** Given the pathname to the matlab executable, return
the pathname to the Matlab installation.

Under Matlab 5.3.0, if the matlab executable is found
at matlabr11/bin/matlab.exe, then the top level
directory is matlabr11, and the matlab includes
can be found at
matlabr11/extern/include/...

<p>Under Matlab 6.0, if the matlab executable is found
at matlabr12/bin/win32/matlab.exe, then the top
level directory would be matlabr12, and the
matlab includes would be found at
matlabr12/extern/include/...

@@author Christopher Hylands
@@version $Id$
@@since Ptolemy II 2.0
 */
public class MatlabRootDirectory {
    public static void main(String args[]) throws IOException {
        try {
            System.out.print(_getMatlabRootDirectory(args[0]));
        } catch (Exception exception) {
            System.err.print("MatlabRootDirectory.main(): " + exception);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Given the pathname to the matlab executable, return the path name
    // of the Matlab directory.
    private static String _getMatlabRootDirectory(String matlabExecutable)
        throws Exception {
        // Return the directory above the bin/ directory in
        // the path to the matlab executable

        // Under Solaris, we might pass this: 
        //   /usr/sww/bin/matlab
        // Under Windows: 
        //   /cygdrive/c/matlab6p1/bin/win32/matlab

        // We could use java.io.File here, but the problem is that
        // if we pass in /cygwin/c/ptII/bin/matlab, then
        // the name we will return will be \cygwin\c\ptII, which
        // will cause problems with the backslash when we do
        // 'if test -d \cygwin\c\ptII'
        
        File matlabFile = new File(matlabExecutable);
        File matlabCanonicalFile = matlabFile.getCanonicalFile();

        // We need to be careful and not try call new URI() with
        // a String that contains backslashes.
        URI matlabURI = matlabCanonicalFile.toURI();

        String matlabPath = matlabURI.getPath();
        if (File.separatorChar == '\\' 
            && matlabPath.startsWith("/")) {
            // We probably have /c:/foo, strip off the leading /
            matlabPath = matlabPath.substring(1);
        }
        return matlabPath.substring(0,matlabPath.lastIndexOf("/bin/"));
    }
}
