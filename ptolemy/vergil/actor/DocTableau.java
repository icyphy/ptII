/* A tableau representing a Doc window.

 Copyright (c) 2006-2014 The Regents of the University of California.
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
package ptolemy.vergil.actor;

import java.net.MalformedURLException;
import java.net.URL;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.MoMLApplication;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFactory;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.basic.DocAttribute;

///////////////////////////////////////////////////////////////////
//// DocTableau

/**
 A tableau representing a documentation view in a toplevel window.
 The URL that is viewed is given by the <i>url</i> parameter, and
 can be either an absolute URL, a system fileName, or a resource that
 can be loaded relative to the classpath.  For more information about how
 the URL is specified, see MoMLApplication.specToURL().
 <p>
 The constructor of this
 class creates the window. The text window itself is an instance
 of DocViewer, and can be accessed using the getFrame() method.
 As with other tableaux, this is an entity that is contained by
 an effigy of a model.
 There can be any number of instances of this class in an effigy.

 @author  Edward A. Lee
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 @see Effigy
 @see DocViewer
 @see MoMLApplication#specToURL(String)
 */
public class DocTableau extends Tableau {

    /** Construct a new tableau for the model represented by the given effigy.
     *  This creates an instance of DocViewer.  It does not make the frame
     *  visible.  To do that, call show().
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the container does not accept
     *   this entity (this should not occur).
     *  @exception NameDuplicationException If the name coincides with an
     *   attribute already in the container.
     */
    public DocTableau(Effigy container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        if (!(container instanceof DocEffigy)) {
            throw new IllegalActionException(container,
                    "Needs to be an instance of DocEffigy to contain a DocTableau.");
        }
        DocAttribute docAttribute = ((DocEffigy) container).getDocAttribute();
        if (docAttribute != null) {
            // Have a doc attribute.
            DocViewer frame = new DocViewer(docAttribute.getContainer(),
                    (Configuration) container.toplevel());
            setFrame(frame);
            frame.setTableau(this);
        } else {
            // No doc attribute. Find the URL of the enclosing effigy.
            try {
                URL effigyURL = container.uri.getURL();
                DocViewer frame = new DocViewer(effigyURL,
                        (Configuration) container.toplevel());
                setFrame(frame);
                frame.setTableau(this);
            } catch (MalformedURLException e) {
                throw new IllegalActionException(this, container, e,
                        "Malformed URL");
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A factory that creates Doc viewer tableaux for Ptolemy models.
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
         *  "DocTableau", then return that tableau; otherwise, create
         *  a new instance of DocTableau in the specified
         *  effigy, and name it "DocTableau".  If the specified
         *  effigy is not an instance of DocEffigy, then do not
         *  create a tableau and return null.  It is the
         *  responsibility of callers of this method to check the
         *  return value and call show().
         *
         *  @param effigy The effigy.
         *  @return A Doc viewer tableau, or null if one cannot be
         *    found or created.
         *  @exception Exception If the factory should be able to create a
         *   tableau for the effigy, but something goes wrong.
         */
        @Override
        public Tableau createTableau(Effigy effigy) throws Exception {
            if (effigy instanceof DocEffigy) {
                // Indicate to the effigy that this factory contains effigies
                // offering multiple views of the effigy data.
                effigy.setTableauFactory(this);

                // First see whether the effigy already contains an
                // DocTableau.
                DocTableau tableau = (DocTableau) effigy
                        .getEntity("DocTableau");

                if (tableau == null) {
                    tableau = new DocTableau(effigy, "DocTableau");
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
