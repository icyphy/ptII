/* A representative of a plot file.

 Copyright (c) 1998-2002 The Regents of the University of California.
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
@ProposedRating Red (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (neuendor@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.plot.Plot;
import ptolemy.plot.PlotBox;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

//////////////////////////////////////////////////////////////////////////
//// PlotEffigy
/**
An effigy for a plot file.

@author Edward A. Lee
@version $Id$
@since Ptolemy II 1.0
*/
public class PlotEffigy extends Effigy {

    /** Create a new effigy in the specified workspace with an empty string
     *  for its name.
     *  @param workspace The workspace for this effigy.
     */
    public PlotEffigy(Workspace workspace) {
	super(workspace);
    }

    /** Create a new effigy in the given directory with the given name.
     *  @param container The directory that contains this effigy.
     *  @param name The name of this effigy.
     */
    public PlotEffigy(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
	super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the plot that this is an effigy of.
     *  @return The plot or null if none has been set.
     */
    public PlotBox getPlot() {
	return _plot;
    }

    /** Set the plot that this is an effigy of.
     *  @param plot The plot.
     */
    public void setPlot(PlotBox plot) {
        _plot = plot;
    }

    /** Write the text of the plot to the specified file.
     *  If no plot has been specified, then no write occurs.
     *  @param file The file to write to.
     *  @exception IOException If the write fails.
     */
    public void writeFile(File file) throws IOException {
        if (_plot != null) {
            FileOutputStream stream = new FileOutputStream(file);
            _plot.write(stream);
            stream.close();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // The plot associated with this effigy.
    private PlotBox _plot;
}
