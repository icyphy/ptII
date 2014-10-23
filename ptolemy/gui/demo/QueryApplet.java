/* Applet demonstrating the Query class.

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
package ptolemy.gui.demo;

import javax.swing.UIManager;

import ptolemy.gui.BasicJApplet;
import ptolemy.gui.Query;
import ptolemy.gui.QueryListener;

///////////////////////////////////////////////////////////////////
//// QueryApplet

/**
 Applet demonstrating the Query class.

 @author  Edward A. Lee, Manda Sutijono
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (eal)
 @see ptolemy.gui.Query
 */
@SuppressWarnings("serial")
public class QueryApplet extends BasicJApplet implements QueryListener {
    /** Constructor.
     */
    public QueryApplet() {
        super();

        // The Java look & feel is pretty lame, so we use the native
        // look and feel of the platform we are running on.
        // NOTE: This creates the only dependence on Swing in this
        // class.  Should this be left to derived classes?
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            // Ignore exceptions, which only result in the wrong look and feel.
            System.err
                    .println("Failed to set the look and feel? Exception was: "
                            + ex);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Called to notify that one of the entries has changed.
     *  The name of the entry is passed as an argument.
     *  @param name The name of the entry.
     */
    @Override
    public void changed(String name) {
        showStatus("Changed " + name + " to: " + _query.getStringValue(name));
    }

    /** Create a Query object and configure it.
     */
    @Override
    public void init() {
        super.init();
        _query = new Query();
        _query.addCheckBox("check", "Check box", true);
        _query.setTextWidth(20);
        _query.addLine("line", "Entry box", "default entry");
        _query.addDisplay("display", "Display", "displayed string");

        String[] choices = { "a", "b", "c" };
        _query.addChoice("choice", "Choice", choices, "b");

        String[] moreChoices = { "d", "e", "f" };
        _query.addChoice("editchoice", "Editable Choice", moreChoices, "d",
                true);
        _query.addSlider("slider", "Slider", 0, -100, 100);

        String[] options = { "mayonnaise", "mustard", "both", "none" };
        _query.addRadioButtons("radio", "Radio buttons", options, "none");

        try {
            _query.addFileChooser("fileChooser", "FileChooser", "default",
                    null, null);
        } catch (SecurityException security) {
            System.out.println("addFileChooser failed: " + security);
        }

        _query.addColorChooser("colorChooser", "ColorChoser",
                "{0.0, 0.0, 0.0, 1.0}");
        _query.addQueryListener(this);
        _query.setBackground(getBackground());
        getContentPane().add(_query);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    Query _query;
}
