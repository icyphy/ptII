/* Applet to display the Java Version in a text area

 Copyright (c) 2000-2001 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/


import javax.swing.JTextArea;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

//////////////////////////////////////////////////////////////////////////
//// JavaVersionApplet
/**
Applet to display the java.version property in a TextArea.

@author  Christopher Hylands
@version $Id$
*/
public class JavaVersionApplet extends JApplet {
    /** Constructor.
     */
    public JavaVersionApplet() {
        super();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read applet parameters, and display the java.version Property
     *  in a TextArea.
     */
    public void init() {
        super.init();

        JFrame _frame = new JFrame();
        _jTextArea = new JTextArea(1,40);
        _jTextArea.setEditable(false);
        //_scrollPane = new JScrollPane(_jTextArea);
        //getContentPane().add(_scrollPane);
        getContentPane().add(_jTextArea);

        String newline = System.getProperty("line.separator");
        String javaVersion = System.getProperty("java.version");
        if (javaVersion.startsWith("1.3")) {
            _jTextArea.append("Congratulations, you are running Java "
                    + javaVersion + ", " + newline
                    + "which is sufficient to run Ptolemy II." + newline
                    + "You need only download the Ptolemy II distribution.");
        } else {
            _jTextArea.append("You are running Java "
                    + javaVersion + ". " + newline
                    + "Ptolemy II requires JDK 1.3, you should upgrade." 
                    + newline
                    + "Java and then download Ptolemy II.");
        }
        validate();
        repaint();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private JTextArea _jTextArea;
    private JScrollPane _scrollPane;
}
