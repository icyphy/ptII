/* A representative of a plot file.

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
package ptolemy.actor.gui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.plot.PlotBoxInterface;

///////////////////////////////////////////////////////////////////
//// PlotEffigy

/**
 An effigy for a plot file.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.1
 @Pt.ProposedRating Red (neuendor)
 @Pt.AcceptedRating Red (neuendor)
 */
public class PlotEffigy extends PtolemyEffigy {
    // FIXME: Why does this extend PtolemyEffigy?  It means that everywhere
    // we check for PtolemyEffigy, we need to make sure the graph is not a
    // PlotEffigy.  See vergil.actor.ActorGraphTableau

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
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public PlotEffigy(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the plot that this is an effigy of.
     *  @return The plot or null if none has been set.
     *  @see #setPlot(PlotBoxInterface)
     */
    public PlotBoxInterface getPlot() {
        return _plot;
    }

    /** Set the plot that this is an effigy of.
     *  @param plot The plot.
     *  @see #getPlot()
     */
    public void setPlot(PlotBoxInterface plot) {
        _plot = plot;
    }

    /** Write the text of the plot to the specified file.
     *  If no plot has been specified, then no write occurs.
     *  @param file The file to write to.
     *  @exception IOException If the write fails.
     */
    @Override
    public void writeFile(File file) throws IOException {
        if (_plot != null) {
            FileOutputStream stream = null;

            try {
                stream = new FileOutputStream(file);
                _plot.write(stream);
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (Throwable throwable) {
                        System.out.println("Ignoring failure to close stream "
                                + "on " + file);
                        throwable.printStackTrace();
                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    // The plot associated with this effigy.
    private PlotBoxInterface _plot;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A factory for creating new effigies.
     */
    public static class Factory extends EffigyFactory {
        /** Create a factory with the given name and container.
         *  @param container The container.
         *  @param name The name.
         *  @exception IllegalActionException If the container is incompatible
         *   with this entity.
         *  @exception NameDuplicationException If the name coincides with
         *   an entity already in the container.
         */
        public Factory(CompositeEntity container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        ///////////////////////////////////////////////////////////////
        ////                     public methods                    ////

        /** Return false, indicating that this effigy factory is not
         *  capable of creating an effigy without a URL being specified.
         *  @return False.
         */
        @Override
        public boolean canCreateBlankEffigy() {
            return false;
        }

        /** Create a new effigy in the given container by reading the
         *  specified URL. If the specified URL is null, or
         *  if the URL does not end with extension ".plt", ".plot", or
         *  ".xml", then return null.  If the extension is ".xml",
         *  then read the file and return null if it does not contain
         *  a line that starts with the string
         *  <code>&gt;!DOCTYPE plot PUBLIC "-//UC Berkeley//DTD PlotML</code>
         *  within the first five lines.
         *  Note that as of this writing, the plotter
         *  parses any file you give it without complaint, so we cannot
         *  rely on the plotter to report that this file is not a plot
         *  file.  Thus, we assume that if the extension matches,
         *  then it is.
         *  @param container The container for the effigy.
         *  @param base The base for relative file references, or null if
         *   there are no relative file references.  This is ignored in this
         *   class.
         *  @param input The input URL.
         *  @return A new instance of PlotEffigy, or null if the URL
         *   does not have a recognized extension.
         *  @exception Exception If the URL cannot be read.
         */
        @Override
        public Effigy createEffigy(CompositeEntity container, URL base,
                URL input) throws Exception {
            if (input != null) {
                String extension = getExtension(input);

                if (extension.equals("xml")) {
                    if (checkForDTD(
                            input,
                            "<!DOCTYPE plot PUBLIC \"-//UC Berkeley//DTD PlotML",
                            null)) {
                        // This is a plot file.
                        PlotEffigy effigy = new PlotEffigy(container,
                                container.uniqueName("effigy"));
                        effigy.uri.setURL(input);
                        return effigy;
                    }
                } else if (extension.equals("plt") || extension.equals("plot")) {
                    PlotEffigy effigy = new PlotEffigy(container,
                            container.uniqueName("effigy"));
                    effigy.uri.setURL(input);
                    return effigy;
                }
            }

            return null;
        }
    }
}
