/* A tableau representing a plot window.

 Copyright (c) 2000-2003 The Regents of the University of California.
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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import javax.swing.JFrame;

import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.plot.Plot;
import ptolemy.plot.PlotBox;
import ptolemy.plot.plotml.PlotMLParser;
import ptolemy.util.MessageHandler;

//////////////////////////////////////////////////////////////////////////
//// PlotTableau
/**
A tableau representing a plot in a toplevel window.
The URL that is viewed is given by the <i>uri</i> parameter, and
can be either an absolute URL, a system fileName, or a resource that
can be loaded relative to the classpath.  For more information about how
the URL is specified, see MoMLApplication.specToURL().
<p>
The plot frame itself must be an instance of PlotTableauFrame,
and must be created by the caller.
As with other tableaux, this is an entity that is contained by
an effigy of a model.
There can be any number of instances of this class in an effigy.

@author  Edward A. Lee
@version $Id$
@since Ptolemy II 2.1
@see Effigy
@see PlotTableauFrame
@see MoMLApplication#specToURL(String)
*/
public class PlotTableau extends Tableau {

    /** Construct a new tableau for the model represented by the given effigy.
     *  Use setFrame() to specify the plot frame after construction.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the container does not accept
     *   this entity (this should not occur).
     *  @exception NameDuplicationException If the name coincides with an
     *   attribute already in the container.
     */
    public PlotTableau(Effigy container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        uri = new URIAttribute(this, "uri");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public parameters                 ////

    /** The URI to display. */
    public URIAttribute uri;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the argument is the <i>uri</i> parameter, then open the
     *  specified URI and display its contents.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the URL cannot be opened,
     *   or if the base class throws it.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == uri) {
            try {
                URL toRead = new URL(uri.getURI().toString());
                _parseURL(toRead);
            } catch (MalformedURLException ex) {
                throw new IllegalActionException(this, ex,
                        "Invalid URL specification.");
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Set the top-level window associated with this tableau.
     *  @param frame The top-level window associated with the tableau.
     *  @throws IllegalActionException If the frame is not an instance
     *   of PlotTableauFrame.
     */
    public void setFrame(JFrame frame) throws IllegalActionException {
        if (!(frame instanceof PlotTableauFrame)) {
            throw new IllegalActionException(this,
                    "Frame for PlotTableau must be an instance of "
                    + "PlotTableauFrame.");
        }
        super.setFrame(frame);
        ((PlotTableauFrame)frame).setTableau(this);
    }

    /** Make this tableau visible by calling setVisible(true), and
     *  raising or deiconifying its window.
     *  If no frame has been set, then create one, an instance of
     *  PlotTableauFrame.  If a URL has been specified but not yet
     *  processed, then process it.
     */
    public void show() {
        JFrame frame = getFrame();
        if (frame == null) {
            PlotTableauFrame newFrame = new PlotTableauFrame(this);
            newFrame.plot.setButtons(true);
            try {
                setFrame(newFrame);
            } catch (IllegalActionException ex) {
                throw new InternalErrorException(ex);
            }
        }
        if (_toRead != null) {
            _parseURL(_toRead);
        }
        super.show();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Read from the specified URL in PlotML format.
     *  If there is no plot frame yet, then defer.
     *  Report any errors.
     *  @param url The URL to read from.
     */
    private void _parseURL(URL url) {
        try {
            PlotTableauFrame frame = ((PlotTableauFrame)getFrame());
            if (frame != null) {
                // FIXME: Should use a HistogramMLParser to get a histogram
                // view... But how can we know that is what is wanted?
                PlotMLParser parser = new PlotMLParser((Plot)frame.plot);
                InputStream stream = url.openStream();
                parser.parse(url, stream);
                stream.close();
                _toRead = null;
            } else {
                // There is no plotter yet.  Have to defer.
                _toRead = url;
            }
        } catch (Exception ex) {
            MessageHandler.error(
                    "Failed to read plot data: " + url.toExternalForm(), ex);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // URL of deferred read.
    private URL _toRead = null;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A factory that creates a plot tableau for Ptolemy models.
     */
    public static class Factory extends TableauFactory {

        /** Create a factory with the given name and container.
         *  @param container The container.
         *  @param name The name.
         *  @exception IllegalActionException If the container is incompatible
         *   with this attribute.
         *  @exception NameDuplicationException If the name coincides with
         *   an attribute already in the container.
         */
        public Factory(NamedObj container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** If the specified effigy already contains a tableau named
         *  "plotTableau", then return that tableau; otherwise, create
         *  a new instance of PlotTableau in the specified
         *  effigy, and name it "plotTableau".  If the specified
         *  effigy is not an instance of PlotEffigy, then do not
         *  create a tableau and return null.  It is the
         *  responsibility of callers of this method to check the
         *  return value and call show().
         *
         *  @param effigy The effigy.
         *  @return A plot tableau, or null if one cannot be
         *    found or created.
         *  @exception Exception If the factory should be able to create a
         *   tableau for the effigy, but something goes wrong.
         */
        public Tableau createTableau(Effigy effigy) throws Exception {
            if (effigy instanceof PlotEffigy) {

                // Indicate to the effigy that this factory contains effigies
                // offering multiple views of the effigy data.
                effigy.setTableauFactory(this);

                // First see whether the effigy already contains an
                // PlotTableau.
                PlotTableau tableau =
                    (PlotTableau)effigy.getEntity("plotTableau");
                if (tableau == null) {
                    tableau = new PlotTableau(
                            (PlotEffigy)effigy, "plotTableau");
                }
                PlotBox plotBox = ((PlotEffigy)effigy).getPlot();
                if (plotBox != null) {
                    // Hook into the existing plot.
                    PlotTableauFrame plotterFrame =
                        new PlotTableauFrame(tableau, plotBox);
                    tableau.setFrame(plotterFrame);
                }
                URI uri = effigy.uri.getURI();
                if (uri != null) {
                    tableau.uri.setURI(uri);
                }
                // Don't call show() here.  If show() is called here,
                // then you can't set the size of the window after
                // createTableau() returns.  This will affect how
                // centering works.
                return tableau;
            } else {
                return null;
            }
        }
    }
}
