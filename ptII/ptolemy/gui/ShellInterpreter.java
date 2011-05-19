/* An interface for interpreters that work with the ShellTextArea.

 Copyright (c) 1998-2005 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

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
package ptolemy.gui;

//////////////////////////////////////////////////////////////////////////
//// ShellInterpreter

/**
 An interface for interpreters that work with the ShellTextArea.

 @see ShellTextArea
 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 3.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public interface ShellInterpreter {
    /** Evaluate the specified command. If this method returns null, then
     *  the associated ShellTextArea will not display a prompt until
     *  returnResult() is called. If this method returns an empty string,
     *  then the associated ShellTextArea will display the prompt.
     *  @see ShellTextArea#returnResult(String)
     *  @param command The command.
     *  @return The return value of the command, or null if there is none.
     *  @exception Exception If something goes wrong processing the command.
     */
    public String evaluateCommand(String command) throws Exception;

    /** Return true if the specified command is complete (ready
     *  to be interpreted).
     *  @param command The command.
     *  @return True if the command is complete.
     */
    public boolean isCommandComplete(String command);
}
