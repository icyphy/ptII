/*
 * Created on 01 sept. 2003
 *
 * @ProposedRating Yellow (jerome.blanc@thalesgroup.com)
 * @AcceptedRating
 */
package thales.actor.gui;

import java.io.IOException;
import java.net.URL;

import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.HTMLEffigy;
import ptolemy.actor.gui.HTMLViewer;
import ptolemy.actor.gui.JNLPUtilities;
import ptolemy.actor.gui.MoMLApplication;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFactory;

import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;

/**
 * <p>Titre : SingleWindowHTMLViewerTableau</p>
 * <p>Description : Main Tableau for the SingleWindowHTMLViewer.</p>
Copyright (c) 2003 THALES.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

IN NO EVENT SHALL THALES BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE
OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THALES HAS BEEN
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

THALES SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS"
BASIS, AND THALES HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT,
UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 * <p>Soci@eacute;t@eacute; : Thales Research and technology</p>
 * @author J&eacute;r&ocirc;me Blanc & Benoit Masson
 * 01 sept. 2003
 */
public class SingleWindowHTMLViewerTableau extends Tableau {

    /** Construct a new tableau for the model represented by the given effigy.
     *  This creates an instance of HTMLViewer.  It does not make the frame
     *  visible.  To do that, call show().
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the container does not accept
     *   this entity (this should not occur).
     *  @exception NameDuplicationException If the name coincides with an
     *   attribute already in the container.
     */
    public SingleWindowHTMLViewerTableau(Effigy container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        url = new StringAttribute(this, "url");

        SingleWindowHTMLViewer frame = new SingleWindowHTMLViewer();
        setFrame(frame);
        frame.setTableau(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public parameters                 ////

    /** The URL to display. */
    public StringAttribute url;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the argument is the <i>url</i> parameter, then open the
     *  specified URL and display its contents.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the URL cannot be opened,
     *   or if the base class throws it.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == url) {
            String urlSpec = ((Settable) attribute).getExpression();
            try {
                                // NOTE: This cannot handle a URL that is relative to the
                                // MoML file within which this attribute might be being
                                // defined.  Is there any way to do that?
                URL toRead = MoMLApplication.specToURL(urlSpec);
                ((HTMLViewer) getFrame()).setPage(toRead);
            } catch (IOException ex) {
                throw new IllegalActionException(
                        this,
                        ex,
                        "Cannot open URL: " + urlSpec);
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A factory that creates HTML viewer tableaux for Ptolemy models.
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
         *  "htmlTableau", then return that tableau; otherwise, create
         *  a new instance of HTMLViewerTableau in the specified
         *  effigy, and name it "htmlTableau".  If the specified
         *  effigy is not an instance of HTMLEffigy, then do not
         *  create a tableau and return null.  It is the
         *  responsibility of callers of this method to check the
         *  return value and call show().
         *
         *  @param effigy The effigy.
         *  @return A HTML viewer tableau, or null if one cannot be
         *    found or created.
         *  @exception Exception If the factory should be able to create a
         *   tableau for the effigy, but something goes wrong.
         */
        public Tableau createTableau(Effigy effigy) throws Exception {
            if (effigy instanceof HTMLEffigy) {

                                // Indicate to the effigy that this factory contains effigies
                                // offering multiple views of the effigy data.
                effigy.setTableauFactory(this);

                                // First see whether the effigy already contains an
                                // HTMLViewerTableau.
                SingleWindowHTMLViewerTableau tableau =
                    (SingleWindowHTMLViewerTableau) effigy.getEntity(
                            "SingleWHtmlTableau");
                if (tableau == null) {
                    tableau =
                        new SingleWindowHTMLViewerTableau(
                                (HTMLEffigy) effigy,
                                "SingleWHtmlTableau");
                }
                                // Unfortunately, if we have a jar url, (for example
                                // jar:file:/C:/foo.jar!/intro.htm
                                // then the java.net.URI toURL() method will return
                                // a URL like jar:, which is missing the file: part
                                // This breaks Ptolemy II under WebStart.
                URL pageURL = new URL(effigy.uri.getURI().toString());
                try {
                    ((HTMLViewer) tableau.getFrame()).setPage(pageURL);
                } catch (IOException io) {
                    // setPage() throws an IOException if the page can't
                    // be found.  If we are under Web Start, it could be
                    // that we are looking in the wrong Jar file, so
                    // we try again.
                    String urlString = effigy.uri.getURI().toString();
                    URL anotherURL =
                        JNLPUtilities.jarURLEntryResource(urlString);
                    if (anotherURL == null) {
                        throw io;
                    }
                    ((HTMLViewer) tableau.getFrame()).setPage(anotherURL);
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
