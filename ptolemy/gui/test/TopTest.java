/* Driver for ptolemy.gui.Top

Copyright (c) 2004-2005 The Regents of the University of California.
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
package ptolemy.gui.test;

import ptolemy.gui.Top;

import java.io.File;
import java.io.IOException;
import java.net.URL;


//////////////////////////////////////////////////////////////////////////
//// TopTest

/**
   Test driver for ptolemy.gui.Top.

   @author Christopher Brooks
   @version $Id$
   @since Ptolemy II 4.1
   @Pt.ProposedRating Red (cxh)
   @Pt.AcceptedRating Red (cxh)
*/
public class TopTest extends Top {
    /** Read the specified URL.
     *  @param url The URL to read.
     *  @exception Exception If the URL cannot be read.
     */
    protected void _read(URL url) throws Exception {
        throw new RuntimeException("TopTest._readFile() not implemented");
    }

    /** Write the model to the specified file.
     *  @param file The file to write to.
     *  @exception IOException If the write fails.
     */
    protected void _writeFile(File file) throws IOException {
        throw new RuntimeException("TopTest._writeFile() not implemented");
    }
}
