/* Applet demonstrating the Query class.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

package ptolemy.gui.demo;

import javax.swing.JApplet;
import java.awt.Color;
import ptolemy.gui.*;

//////////////////////////////////////////////////////////////////////////
//// QueryApplet
/**
Applet demonstrating the Query class.

@author  Edward A. Lee, Manda Sutijono
@version $Id$
@see ptolemy.gui.Query
*/
public class QueryApplet extends BasicJApplet implements QueryListener {

    /** Constructor.
     */
    public QueryApplet() {
        super();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Called to notify that one of the entries has changed.
     *  The name of the entry is passed as an argument.
     *  @param name The name of the entry.
     */
    public void changed(String name) {
        showStatus("Changed " + name + " to: " + _query.stringValue(name));
    }

    /** Create a Query object and configure it.
     */
    public void init() {
        super.init();
        _query = new Query();
        getContentPane().add(_query);
        _query.addCheckBox("check", "Check box", true);
        _query.setTextWidth(20);
        _query.addLine("line", "Entry box", "default entry");
        _query.addDisplay("display", "Display", "displayed string");
        String[] choices = {"a", "b", "c"};
        _query.addChoice("choice", "Choice", choices, "b");
        _query.addSlider("slider", "Slider", 0, -100, 100);
        String[] options = {"mayonnaise", "mustard", "both", "none"};
        _query.addRadioButtons("radio", "Radio buttons", options, "none");
        _query.addQueryListener(this);
        _query.setBackground(getBackground());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    Query _query;
}
