/*

Copyright (c) 2005 The Regents of the University of California.
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

package ptolemy.backtrack.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

//////////////////////////////////////////////////////////////////////////
//// SourceOutputStream
/**


@author Thomas Feng
@version $Id$
@since Ptolemy II 5.1
@Pt.ProposedRating Red (tfeng)
@Pt.AcceptedRating Red (tfeng)
*/
public class SourceOutputStream extends FileOutputStream {

    public static SourceOutputStream getStream(String fileName,
            boolean overwrite)
            throws IOException {
        File file = new File(fileName);
        if (file.getParent() != null) {
            File path = new File(file.getParent());
            if (!path.exists())
                path.mkdirs();
        }
        if (!overwrite && new File(fileName).exists())
            throw new IOException("File \"" + fileName + "\" already exists.");
        else
            return new SourceOutputStream(fileName);
    }

    public static SourceOutputStream getStream(String root, String packageName,
            String fileName, boolean overwrite)
            throws FileNotFoundException, IOException {
        if (packageName != null && packageName.length() > 0)
            root = root +
                File.separator +
                packageName.replace('.', File.separatorChar);

        File rootFile = new File(root);
        if (!rootFile.exists())
            rootFile.mkdirs();

        String fullName = root + File.separator + fileName;
        if (!overwrite && new File(fullName).exists())
            throw new IOException("File \"" + fullName + "\" already exists.");
        else
            return new SourceOutputStream(root + File.separator + fileName);
    }

    private SourceOutputStream(String fileName) throws FileNotFoundException {
        super(fileName);
    }
}
