/* A standalone plot application.

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
@ProposedRating Yellow (cxh@eecs.berkeley.edu)
@AcceptedRating Yellow (cxh@eecs.berkeley.edu)
*/

package ptolemy.kvm.plot;
import java.awt.Frame;
import java.awt.event.*;
import java.io.*;
import java.util.StringTokenizer;
import java.net.URL;
import java.net.MalformedURLException;

//////////////////////////////////////////////////////////////////////////
//// PlotApplication
/**
@see Plot
@see PlotBox
@author Christopher Hylands and Edward A. Lee
@version $Id$
*/
public class PlotApplication extends Frame {

    /** Construct a plot with no command-line arguments.
     *  It initially displays a sample plot.
     *  @exception Exception Not thrown.
     */
    public PlotApplication() throws Exception {
        this(null);
    }

    /** Construct a plot with the specified command-line arguments.
     *  @param args The command-line arguments.
     *  @exception Exception If command line arguments have problems.
     */
    public PlotApplication(String args[]) throws Exception {
        this(new Plot(), args);
    }

    /** Construct a plot with the specified command-line arguments
     *  and instance of plot.
     *  @param plot The instance of Plot to use.
     *  @param args The command-line arguments.
     *  @exception Exception If command line arguments have problems.
     */
    public PlotApplication(PlotBox plotArg, String args[]) throws Exception {

        if (plotArg == null) {
            plot = new Plot();
        } else {
            plot = plotArg;
        }

        if (args == null || args.length == 0) {
            plot.samplePlot();
        }
        setVisible(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a new plot window and map it to the screen.
     */
    public static void main(String args[]) {
        try {
            PlotApplication plot = new PlotApplication(new Plot(), args);
        } catch (Exception ex) {
            System.err.println(ex.toString());
            ex.printStackTrace();
        }
    }


    /** @serial The plot object held by this frame. */
    // FIXME: uncomment final when we upgrade to jdk1.2
    public /*final*/ PlotBox plot;

}
