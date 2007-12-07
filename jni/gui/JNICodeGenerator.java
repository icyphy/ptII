/* Generate Java Native Interface support code.

 Copyright (c) 2006 The Regents of the University of California.
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
package jni.gui;

import jni.JNIUtilities;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.ExecuteCommands;

//////////////////////////////////////////////////////////////////////////
//// JNICodeGenerator

/** Generate Java Native Interface (JNI) support code and create ports
 *  that correspond with the native method arguments.
 *
 *  @author Christopher Brooks
 *  @version $Id$
 *  @since Ptolemy II 6.0
 *  @Pt.ProposedRating Red (cxh)
 *  @Pt.AcceptedRating Red (cxh)
 */
public class JNICodeGenerator extends Attribute {
    /** Create a new instance of JNI Generator.
     *  @param container The container.
     *  @param name The name of the JNI generator.
     *  @exception IllegalActionException If the super class throws the
     *   exception or error occurs when setting the file path.
     *  @exception NameDuplicationException If the super class throws the
     *   exception or an error occurs when setting the file path.
     */
    public JNICodeGenerator(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _attachText(
                "_iconDescription",
                "<svg>\n"
                        + "<rect x=\"-50\" y=\"-20\" width=\"100\" height=\"60\" "
                        + "style=\"fill:blue\"/>"
                        + "<text x=\"-40\" y=\"-5\" "
                        + "style=\"font-size:12; font-family:SansSerif; fill:white\">"
                        + "Double click to\ncreate ports and\ngenerate code.</text></svg>");
        new JNICodeGeneratorGUIFactory(this, "_JNICodeGeneratorGUIFactory");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate code and append it to the given string buffer.
     *
     *  Write the code to the directory specified by the codeDirectory
     *  parameter.  The file name is a sanitized version of the model
     *  name with a suffix that is based on last package name of the
     *  <i>generatorPackage</i> parameter.  Thus if the
     *  <i>codeDirectory</i> is <code>$HOME</code>, the name of the
     *  model is <code>Foo</code> and the <i>generatorPackage</i>
     *  is <code>ptolemy.codegen.c</code>, then the file that is
     *  written will be <code>$HOME/Foo.c</code>
     *  This method is the main entry point.
     *  @param code The given string buffer.
     *  @return The return value of the last subprocess that was executed.
     *  or -1 if no commands were executed.
     *  @exception Exception If there was a problem creating the JNI files.
     */
    public int generateCode(StringBuffer code) throws Exception {
        // FIXME: Need to append code to the code buffer
        JNIUtilities.generateJNI((CompositeEntity) getContainer());
        return getExecuteCommands().getLastSubprocessReturnCode();
    }

    /** Get the command executor, which can be either non-graphical
     *  or graphical.  The initial default is non-graphical, which
     *  means that stderr and stdout from subcommands is written
     *  to the console.
     *  @return executeCommands The subprocess command executor.
     *  @see #setExecuteCommands(ExecuteCommands)
     */
    public static ExecuteCommands getExecuteCommands() {
        return JNIUtilities.getExecuteCommands();
    }

    /** Set the command executor, which can be either non-graphical
     *  or graphical.  The initial default is non-graphical, which
     *  means that stderr and stdout from subcommands is written
     *  to the console.
     *  @param executeCommands The subprocess command executor.
     *  @see #getExecuteCommands()
     */
    public static void setExecuteCommands(ExecuteCommands executeCommands) {
        JNIUtilities.setExecuteCommands(executeCommands);
    }

    /** Set the container of this object to be the given container.
     *  @param container The given container.
     *  @exception IllegalActionException If the given container
     *   is not null and not an instance of CompositeEntity.
     *  @exception NameDuplicationException If there already exists a
     *   container with the same name.
     */
    public void setContainer(NamedObj container) throws IllegalActionException,
            NameDuplicationException {
        if ((container != null) && !(container instanceof CompositeEntity)) {
            throw new IllegalActionException(this, container,
                    "CodeGenerator can only be contained"
                            + " by CompositeEntity");
        }

        super.setContainer(container);
    }
}
