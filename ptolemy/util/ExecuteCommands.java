/* Execute commands in a subprocess.

 Copyright (c) 2003-2005 The Regents of the University of California.
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
package ptolemy.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.util.StringUtilities;

/** Interface for classes execute commands in a subprocess.

 <p>Loosely based on Example1.java from
 http://java.sun.com/products/jfc/tsc/articles/threads/threads2.html
 <p>See also
 http://developer.java.sun.com/developer/qow/archive/135/index.jsp
 and
 http://jw.itworld.com/javaworld/jw-12-2000/jw-1229-traps.html

 @see JTextAreaExec
 @see StreamExec

 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 5.2;
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public interface ExecuteCommands {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Cancel any running commands. */
    public void cancel();

    /** Clear the text area, status bar and progress bar. */
    public void clear();


    /** Return the value of the Process.  Typically the return value
     *  of this method is used to have the caller wait for the process
     *  to exit.
     *  @return The value of the process.
     */
    public Process getProcess();

    /** Set the list of commands.
     *  @param commands A list of Strings, where each element is a command.
     */
    public void setCommands(List commands);


    /** Start running the commands. */
    public void start();


    /** Append the text message to stderr.  A derived class could
     *  append to a StringBuffer.  @link{JTextAreaExec} appends to a
     *  JTextArea. The output automatically gets a trailing newline
     *  appended.
     *  @param text The text to append to stdandard error.
     */
    public void stderr(final String text);

    /** Append the text message to the output.  A derived class could
     *  append to a StringBuffer.  @link{JTextAreaExec} appends to a
     *  JTextArea.
     *  The output automatically gets a trailing newline appended.
     *  @param text The text to append to standard out.
     */
    public void stdout(final String text);


    /** Set the text of the status bar.  In this base class, do
     *  nothing, derived classes may update a status bar.
     *  @param text The text with which the status bar is updated.
     */
    public void updateStatusBar(final String text);
}
