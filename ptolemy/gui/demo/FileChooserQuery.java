/* Standalone Application demonstrating the Query class.

Copyright (c) 1998-2005 The Regents of the University of California.
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
package ptolemy.gui.demo;

import ptolemy.gui.Query;
import ptolemy.gui.QueryListener;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;


//////////////////////////////////////////////////////////////////////////
//// FileChooserQuery

/**
   Demonstration of the addFileChooser() method in Query.
   This can't be in an applet because applets cannot read from the local files.

   @author  Christopher Hylands
   @version $Id$
   @since Ptolemy II 2.0
   @Pt.ProposedRating Red (cxh)
   @Pt.AcceptedRating Red (cxh)
   @see ptolemy.gui.Query
*/
public class FileChooserQuery extends JFrame implements QueryListener {
    /** Constructor.
     */
    public FileChooserQuery() {
        super("FileChooserQuery");

        JPanel contentPane = new JPanel();
        _query = new Query();
        contentPane.add(_query);

        _query.addCheckBox("check", "Check box", true);
        _query.setTextWidth(20);
        _query.addLine("line", "Entry box", "default entry");
        _query.addDisplay("display", "Display", "displayed string");

        String[] choices = { "a", "b", "c" };
        _query.addChoice("choice", "Choice", choices, "b");

        String[] moreChoices = { "d", "e", "f" };
        _query.addChoice("editchoice", "Editable Choice", moreChoices, "d", true);
        _query.addSlider("slider", "Slider", 0, -100, 100);

        String[] options = { "mayonnaise", "mustard", "both", "none" };
        _query.addRadioButtons("radio", "Radio buttons", options, "none");

        _query.addFileChooser("fileChooser", "FileChooser", "default", null,
                null);
        _query.addColorChooser("colorChooser", "ColorChoser",
                "{0.0, 0.0, 0.0, 1.0}");

        _query.addQueryListener(this);
        _query.setBackground(getBackground());
        setContentPane(contentPane);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Called to notify that one of the entries has changed.
     *  The name of the entry is passed as an argument.
     *  @param name The name of the entry.
     */
    public void changed(String name) {
        System.out.println("Changed " + name + " to: "
                + _query.getStringValue(name));
    }

    /** Create a FileChooserQuery and configure it
     */
    public static void main(String[] args) {
        JFrame frame = new FileChooserQuery();

        frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });

        frame.pack();
        frame.setVisible(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    Query _query;
}
