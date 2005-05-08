/* Java source code output stream.

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
   Java source code output stream. This output stream is directly created with
   an output file name. Necessary parent directories are created if they do not
   exist yet.

   @author Thomas Feng
   @version $Id$
   @since Ptolemy II 5.1
   @Pt.ProposedRating Red (tfeng)
   @Pt.AcceptedRating Red (tfeng)
*/
public class SourceOutputStream extends FileOutputStream {

    ///////////////////////////////////////////////////////////////////
    ////                       public methods                      ////

    /** Get an output stream with a file name. If the parent directories of the
     *  output file does not exist yet, they are created automatically.
     * 
     *  @param fileName The output file name.
     *  @param overwrite Whether to an overwrite existing file, if any.
     *  @return The output stream of the file.
     *  @exception IOException If error occurs when creating the output stream.
     *   An {@link IOException} is thrown if <tt>overwrite</tt> is
     *   <tt>false</tt>and the output file already exists; {@link
     *   FileNotFoundException} is thrown if the output file cannot be created
     *   for other reasons.
     */
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

    /** Get an output stream with a output root directory, the name of the
     *  package which the output class is in, and an output file name. If the
     *  parent directories of the final output file (computed from the given
     *  arguments) does not exist yet, they are created automatically.
     * 
     *  @param root The root of the output class.
     *  @param packageName The name of the package which the output class is
     *   in.
     *  @param fileName The name of the file. It may contain path information,
     *   but only the simple file name is used.
     *  @param overwrite Whether to an overwrite existing file, if any.
     *  @return The output stream of the file.
     *  @exception IOException If error occurs when creating the output stream.
     *   An {@link IOException} is thrown if <tt>overwrite</tt> is
     *   <tt>false</tt>and the output file already exists; {@link
     *   FileNotFoundException} is thrown if the output file cannot be created
     *   for other reasons.
     */
    public static SourceOutputStream getStream(String root, String packageName,
            String fileName, boolean overwrite) throws IOException {
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

    ///////////////////////////////////////////////////////////////////
    ////                        constructor                        ////

    /** Construct an output stream with the given file name. Users should use
     *  {@link #getStream(String, boolean)} or {@link #getStream(String,
     *  String, String, boolean)} to obtain the output stream, instead of
     *  creating it with the <tt>new</tt> operator.
     * 
     *  @param fileName The name of the output file.
     *  @exception FileNotFoundException If the output file cannot be created.
     */
    private SourceOutputStream(String fileName) throws FileNotFoundException {
        super(fileName);
    }
}
