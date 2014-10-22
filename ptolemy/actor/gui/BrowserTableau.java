/* A tableau representing a Web Browser window.

 Copyright (c) 2002-2014 The Regents of the University of California.
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

import java.io.IOException;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// BrowserTableau

/**
 A tableau representing a web browser window.

 There can be any number of instances of this class in an effigy.

 @author Christopher Hylands
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Yellow (cxh)
 @Pt.AcceptedRating Red (cxh)
 @see BrowserEffigy
 @see BrowserLauncher
 */
public class BrowserTableau extends Tableau {
    /** Construct a new tableau for the model represented by the given effigy.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the container does not accept
     *   this entity (this should not occur).
     *  @exception NameDuplicationException If the name coincides with an
     *   attribute already in the container.
     */
    public BrowserTableau(BrowserEffigy container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Make the tableau editable or uneditable.  Notice that this does
     *  not change whether the effigy is modifiable, so other tableaux
     *  on the same effigy may still modify the associated file.
     *  @param flag False to make the tableau uneditable.
     */
    @Override
    public void setEditable(boolean flag) {
        super.setEditable(flag);
    }

    /** Make this tableau visible by calling
     *        {@link BrowserLauncher#openURL(String)}
     *  with URI from the effigy.  Most browsers are smart enough
     *  so that if the browser is already displaying the URI, then
     *  that window will be brought to the foreground.  We are limited
     *  by the lack of communication between Java and the browser,
     *  so this is the best we can do.
     *  If the URI ends in "#in_browser", we strip it off before
     *  passing the URI to the browser.  #in_browser is used by
     *  {@link ptolemy.actor.gui.HTMLViewer} to force a hyperlink to be
     *  opened in a browser.
     */
    @Override
    public void show() {
        // FIXME: Unfortunately, the _config.showAll() at the bottom
        // of MoMLApplication.parseArgs() will end up calling this method
        // a second time.
        // FIXME: Probably the following could make better use of URI
        // facilities (used to be URL based).
        String url = ((Effigy) getContainer()).uri.getURI().toString();

        try {
            if (url.startsWith("jar:")) {
                // If the URL begins with jar: then we are inside Web
                // Start, or the Windows installer // and we should
                // get the resource, and try to write the file to the
                // place where it would appear in the classpath.
                // For example,  if url is
                // jar:file:/D:/ptII/doc/design.jar!/doc/design/design.pdf
                // then we try to save the file as
                // d:/ptII/doc/design.pdf
                // if d:/ptII/doc is writable.
                String temporaryURL = null;

                try {
                    // We try to save the resource in the classpath, but
                    // if we fail, then we copy the resource to a temporary
                    // location.
                    // If we are successful, then note that the file
                    // that we create is not deleted when we exit.
                    temporaryURL = JNLPUtilities.saveJarURLInClassPath(url);
                } catch (Exception ex) {
                    // We print out the error and move on.
                    // Eventually, this could be logged as a warning.
                    System.out.println("Failed to save '" + url + "': " + ex);
                    ex.printStackTrace();
                }

                if (temporaryURL != null) {
                    url = temporaryURL;
                } else {
                    // For some reason we could not write the file, so
                    // save the jar file as a temporary file in the default
                    // platform dependent directory with the same suffix
                    // as that of the jar URL.
                    // In this case, the temporary file is deleted when
                    // we exit.
                    url = JNLPUtilities.saveJarURLAsTempFile(url, "tmp", null,
                            null);
                }
            }

            String inBrowser = "#in_browser";

            if (url.endsWith(inBrowser)) {
                // Strip off any trailing #in_browser, see HTMLViewer.
                url = url.substring(0, url.length() - inBrowser.length());
            }

            inBrowser = "%23in_browser";
            if (url.endsWith(inBrowser)) {
                // Strip off any trailing #in_browser, see HTMLViewer.
                url = url.substring(0, url.length() - inBrowser.length());
            }

            BrowserLauncher.openURL(url);

            try {
                // We set the container to null immediately because
                // once we spawn the browser process, we have no
                // way of communicating with it, so we have no way
                // of knowing when the browser has been closed.
                //
                // FIXME: this effectively destroys the Tableau/Effigy model
                // for BrowserTableaus, but there is not much to be done
                // about it since we do not have a platform independent way
                // of communicating with the browser that we invoke.
                setContainer(null);
            } catch (KernelException ex2) {
                throw new InvalidStateException((Nameable) null, ex2,
                        "setContainer(null) failed, url was " + url);
            }
        } catch (IOException ex) {
            throw new InvalidStateException((Nameable) null, ex,
                    "Failed to handle '" + url + "': ");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A factory that creates web browser tableaux for Ptolemy models.
     */
    public static class Factory extends TableauFactory {
        /** Create a factory with the given name and container.
         *  @param container The container entity.
         *  @param name The name of the entity.
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

        /** If the specified effigy is a BrowserEffigy and it
         *  already contains a tableau named
         *  "browserTableau", then return that tableau; otherwise, create
         *  a new instance of BrowserTableau in the specified
         *  effigy, and name it "browserTableau" and return that tableau.
         *  If the specified
         *  effigy is not an instance of BrowserEffigy, then do not
         *  create a tableau and return null.  It is the
         *  responsibility of callers of this method to check the
         *  return value and call show().
         *  @param effigy The effigy.
         *  @return A browser editor tableau, or null if one cannot be
         *    found or created.
         *  @exception Exception If the factory should be able to create a
         *   tableau for the effigy, but something goes wrong.
         */
        @Override
        public Tableau createTableau(Effigy effigy) throws Exception {
            if (effigy instanceof BrowserEffigy) {
                // First see whether the effigy already contains a
                // BrowserTableau with the appropriate name.
                BrowserTableau tableau = (BrowserTableau) effigy
                        .getEntity("browserTableau");

                if (tableau == null) {
                    tableau = new BrowserTableau((BrowserEffigy) effigy,
                            "browserTableau");
                }

                tableau.setEditable(effigy.isModifiable());
                return tableau;
            } else {
                return null;
            }
        }
    }
}
