/* Editable plotter application that is capable of reading PlotML files.

@Author: Edward A. Lee

@Version: $Id$

@Copyright (c) 1997-1999 The Regents of the University of California.
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
@ProposedRating red (eal@eecs.berkeley.edu)
@AcceptedRating red (cxh@eecs.berkeley.edu)
*/
package ptolemy.plot.plotml;

import ptolemy.plot.EditablePlot;
import ptolemy.gui.*;

import java.awt.MenuItem;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

//////////////////////////////////////////////////////////////////////////
//// EditablePlotMLApplication

/**
An application that can plot data in PlotML format from a URL or
from files specified on the command line, and can then permit the
user to edit the plot.
To compile and run this application, do the following (in Unix):
<pre>
    setenv CLASSPATH ../..
    javac EditablePlotMLApplication.java
    java ptolemy.plot.plotml.EditablePlotMLApplication
</pre>
or in a bash shell in Windows NT:
<pre>
    CLASSPATH=../..
    export CLASSPATH
    javac EditablePlotMLApplication.java
    java ptolemy.plot.plotml.EditablePlotMLApplication
</pre>

@author Edward A. Lee
@version $Id$
@see PlotBox
@see Plot
*/
public class EditablePlotMLApplication extends PlotMLApplication {

    /** Construct a plot with no command-line arguments.
     *  It initially displays a sample plot.
     *  @exception Exception If command line arguments have problems.
     */
    public EditablePlotMLApplication() throws Exception {
        this(null);
    }

    /** Construct a plot with the specified command-line arguments.
     *  @param args The command-line arguments.
     *  @exception Exception If command line arguments have problems.
     */
    public EditablePlotMLApplication(String args[]) throws Exception {
        this(new EditablePlot(), args);
    }

    /** Construct a plot with the specified command-line arguments
     *  and instance of plot.
     *  @param plot The instance of EditablePlot to use.
     *  @param args The command-line arguments.
     *  @exception Exception If command line arguments have problems.
     */
    public EditablePlotMLApplication(EditablePlot plot, String args[])
            throws Exception {
        super(plot, args);

        // Edit menu
        MenuItem select = new MenuItem("Select Dataset");
        SelectListener selectListener = new SelectListener();
        select.addActionListener(selectListener);
        _editMenu.add(select);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a new plot window and map it to the screen.
     */
    public static void main(String args[]) {
        try {
            EditablePlotMLApplication plot =
                   new EditablePlotMLApplication(new EditablePlot(), args);
        } catch (Exception ex) {
            System.err.println(ex.toString());
            ex.printStackTrace();
        }

        // If the -test arg was set, then exit after 2 seconds.
        if (_test) {
            try {
                Thread.currentThread().sleep(2000);
            }
            catch (InterruptedException e) {
            }
            System.exit(0);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Display basic information about the application.
     */
    protected void _about() {
        Message message = new Message(
                "EditablePlotMLApplication class\n" +
                "By: Edward A. Lee, eal@eecs.berkeley.edu\n" +
                "and Christopher Hylands, cxh@eecs.berkeley.edu\n" +
                "Version 3.0, Build: $Id$\n\n"+
                "For more information, see\n" +
                "http://ptolemy.eecs.berkeley.edu/java/ptplot\n" +
                "Copyright (c) 1997-1999,\n" +
                "The Regents of the University of California.");
        message.setTitle("About Ptolemy Plot");
    }

    /** Display more detailed information than given by _about().
     */
    protected void _help() {
        // Use newlines here since we are displaying with scrollbars.
        Message message = new Message(
                "EditablePlotMLApplication is a standalone plot " +
                " application.\n" +
                "It can read files in the PlotML format (an XML extension).\n" +
                "Drag the right mouse button to edit the plot.\n" +
                "Use the File menu to open and edit plot files.\n" +
                "Use the Edit menu to select a dataset to edit " +
                "(if there is more than one dataset)." +
                _usage());
        message.setTitle("Usage of Ptolemy Plot");
    }

    /** Open a dialog to select a dataset to edit.
     */
    protected void _selectDataset() {
        Query query = new Query();
        int numSets = ((EditablePlot)plot).getNumDataSets();
        String[] choices = new String[numSets + 1];
        for (int i = 0; i < numSets; i++) {
            choices[i] = plot.getLegend(i);
            if (choices[i] == null) {
                choices[i] = "" + i;
            }
        }
        choices[numSets] = "none";
        query.addChoice("choice", "Choice", choices, choices[0]);
        PanelDialog dialog = new PanelDialog(this, "Select dataset", query);
        String buttonPressed = dialog.buttonPressed();
        if (buttonPressed.equals("OK")) {
            int result = query.intValue("choice");
            if (result < numSets) {
                ((EditablePlot)plot).setEditable(result);
            } else {
                // none...
                ((EditablePlot)plot).setEditable(-1);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    class SelectListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            _selectDataset();
        }
    }
}
