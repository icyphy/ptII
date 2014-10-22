/* Run a list of commands and save the output in a StringBuffer.

 Copyright (c) 2007-2014 The Regents of the University of California.
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

/** Execute commands in a subprocess and accumulate the output in a
 StringBuffer.

 <p>As an alternative to this class, see
 {@link ptolemy.gui.JTextAreaExec}, which uses Swing; and
 {@link ptolemy.util.StreamExec}, which writes to stderr and stdout.

 <p>Sample usage:
 <pre>
 List execCommands = new LinkedList();
 execCommands.add("date");
 execCommands.add("sleep 3");
 execCommands.add("date");
 execCommands.add("notACommand");

 final StringBufferExec exec = new StringBufferExec();
 exec.setCommands(execCommands);

 exec.start();
 </pre>


 <p>Loosely based on Example1.java from
 <a href="http://java.sun.com/products/jfc/tsc/articles/threads/threads2.html">http://java.sun.com/products/jfc/tsc/articles/threads/threads2.html</a>
 <p>See also
 <a href="http://developer.java.sun.com/developer/qow/archive/135/index.jsp">http://developer.java.sun.com/developer/qow/archive/135/index.jsp</a>
 and
 <a href="http://jw.itworld.com/javaworld/jw-12-2000/jw-1229-traps.html">http://jw.itworld.com/javaworld/jw-12-2000/jw-1229-traps.html</a>

 @see ptolemy.gui.JTextAreaExec

 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class StringBufferExec extends StreamExec {

    /** Create a StringBufferExec.  As the commands are executed,
     *  output is appended to the StringBuffer - no output will appear
     *  on stderr and stdout.
     */
    public StringBufferExec() {
        super();
        buffer = new StringBuffer();
    }

    /** Create a StringBufferExec and optionally append to stderr
     *  and stdout as the commands are executed.
     *  @param appendToStderrAndStdout If true, then as the commands
     *  are executed, the output is append to stderr and stdout.
     */
    public StringBufferExec(boolean appendToStderrAndStdout) {
        super();
        _appendToStderrAndStdout = appendToStderrAndStdout;
        buffer = new StringBuffer();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Append the text message to the StringBuffer.  The output
     *  automatically gets a trailing end of line character(s)
     *  appended.
     *  Optionally, the text also appears on stderr
     *  @param text The text to append.
     */
    @Override
    public void stderr(final String text) {
        if (_appendToStderrAndStdout) {
            super.stderr(text);
        }
        _appendToBuffer(text);
    }

    /** Append the text message to the StringBuffer.  The output
     *  automatically gets end of line character(s) appended.
     *  Optionally, the text also appears on stdout.
     *  @param text The text to append.
     */
    @Override
    public void stdout(final String text) {
        if (_appendToStderrAndStdout) {
            super.stdout(text);
        }
        _appendToBuffer(text);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The StringBuffer to which the output is appended.  This
     *  variable is public so that callers can clear the buffer as
     *  necessary.
     */
    public StringBuffer buffer;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Append to the internal StringBuffer.
     *  @param text The text to append.  If the text does not
     *  end with an end of line character(s), then _eol is appended.
     */
    private void _appendToBuffer(final String text) {
        buffer.append(text);
        if (!text.endsWith(_eol)) {
            buffer.append(_eol);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** If true, append to stderr and stdout as the commands are executed.
     */
    private boolean _appendToStderrAndStdout = false;

    /** End of line character.  Under Unix: "\n", under Windows: "\n\r".
     *  We use a end of line character so that the files we generate
     *  have the proper end of line character for use by other native tools.
     */
    private static String _eol;
    static {
        _eol = StringUtilities.getProperty("line.separator");
    }

}
