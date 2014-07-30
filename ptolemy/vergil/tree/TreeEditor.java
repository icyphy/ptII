/* An application that shows the elements of a Ptolemy II model in a JTree.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.vergil.tree;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// TreeEditor

/**
 An application that shows the elements of a Ptolemy II model in a JTree.

 @author Edward Lee
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (ctsay)
 @Pt.AcceptedRating Red (ctsay)
 */
@SuppressWarnings("serial")
public class TreeEditor extends JFrame {
    /** Construct a display of the Ptolemy II model given by the
     *  specified MoML file.
     *  @param filename The name of a MoML file.
     *  @exception Exception If the parser cannot parse the file.
     */
    public TreeEditor(String filename) throws Exception {
        super();

        // Handle window closing by exiting the application.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                StringUtilities.exit(0);
            }
        });

        getContentPane().add(new TreeEditorPanel(filename));
        pack();
        setVisible(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create an instance of this class to display the abstract syntax
     *  tree for the specified file.
     *  @param args An array of size one where the first element contains
     *  the name of a MoML file.
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out
            .println("usage : ptolemy.vergil.tree.TreeEditor file.java");
            return;
        }

        try {
            new TreeEditor(args[0]);
        } catch (Exception ex) {
            System.err.println(ex.toString());
        }
    }
}
