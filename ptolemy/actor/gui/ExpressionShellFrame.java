/* A frame for evaluating expressions interactively.

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
package ptolemy.actor.gui;

import java.awt.BorderLayout;
import java.net.URL;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import ptolemy.gui.ShellTextArea;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// ExpressionShellFrame

/**
 A frame that provides an interactive shell for evaluating expressions.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 3.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 @see ExpressionShellTableau
 @see ShellTextArea
 @see ExpressionShellEffigy
 */
public class ExpressionShellFrame extends TableauFrame {
    /** Construct a frame to display the ExpressionShell window.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  This is typically accomplished by calling show() on
     *  enclosing tableau.
     *  @param tableau The tableau responsible for this frame.
     *  @exception IllegalActionException If the model rejects the
     *   configuration attribute.
     *  @exception NameDuplicationException If a name collision occurs.
     */
    public ExpressionShellFrame(ExpressionShellTableau tableau)
            throws IllegalActionException, NameDuplicationException {
        super(tableau);

        JPanel component = new JPanel();
        component.setLayout(new BoxLayout(component, BoxLayout.Y_AXIS));

        tableau.shell = new ShellTextArea();
        tableau.shell.setInterpreter(tableau);
        component.add(tableau.shell);
        getContentPane().add(component, BorderLayout.CENTER);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    protected void _help() {
        try {
            URL doc = getClass().getClassLoader().getResource(
                    "doc/expressions.htm");
            getConfiguration().openModel(null, doc, doc.toExternalForm());
        } catch (Exception ex) {
            System.out.println("ExpressionShellTableau._help(): " + ex);
            _about();
        }
    }
}
