/* A tableau representing a Web Browser window.

 Copyright (c) 2002 The Regents of the University of California.
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
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import ptolemy.gui.Top;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

import java.net.URL;
import java.util.Iterator;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// BrowserTableau
/**
A tableau representing a web browser window.

There can be any number of instances of this class in an effigy.

@author Christopher Hylands
@version $Id$
@since Ptolemy II 2.0
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
    public void setEditable(boolean flag) {
        super.setEditable(flag);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A factory that creates text editor tableaux for Ptolemy models.
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
         *  If the specified effigy is not an instance of BrowserEffigy,
         *  but contains an instance of BrowserEffigy, then open a tableau
         *  for that effigy.  If it is a PtolemyEffigy, then create a
         *  browser effigy with the MoML representation of the model.
         *  Finally, if is not a BrowserEffigy or a PtolemyEffigy,
         *  and it does not contain a BrowserEffigy, then attempt to
         *  open its URL and display its date by creating a browser effigy,
         *  which will then be contained by the specified effigy. If all
         *  of this fails, then do not create a tableau and return null.
         *  It is the responsibility of callers of this method to check the
         *  return value and call show().
         *
	 *  @param effigy The effigy.
	 *  @return A browser editor tableau, or null if one cannot be
	 *    found or created.
         *  @exception Exception If the factory should be able to create a
         *   tableau for the effigy, but something goes wrong.
	 */
	public Tableau createTableau(Effigy effigy) throws Exception {
	    if (effigy instanceof BrowserEffigy) {
                // First see whether the effigy already contains a
                // BrowserTableau with the appropriate name.
                BrowserTableau tableau =
                    (BrowserTableau)effigy.getEntity("browserTableau");
                if (tableau == null) {
                    tableau = new BrowserTableau(
                            (BrowserEffigy)effigy, "browserTableau");
                }
                tableau.setEditable(effigy.isModifiable());
                return tableau;
	    } else {
                // The effigy is not an instance of BrowserEffigy.
                // See whether it contains an instance of BrowserEffigy
                // named "browserEffigy", and if it does return that instance.
                Iterator effigies = effigy
                    .entityList(BrowserEffigy.class).iterator();
                while (effigies.hasNext()) {
                    BrowserEffigy browserEffigy =
                        (BrowserEffigy)effigies.next();
                    if (browserEffigy.getName().equals("browserEffigy")) {
                        return createTableau(browserEffigy);
                    }
                }
                // It does not contain an instance of BrowserEffigy with
                // the name "browserEffigy".
                // Attempt to use it's url attribute and create a new
                // instance of BrowserEffigy contained by the specified one.
                URL url = effigy.url.getURL();
                BrowserEffigy browserEffigy =
		    BrowserEffigy.newBrowserEffigy(effigy, url, url);
		browserEffigy.setName("browserEffigy");

                BrowserTableau browserTableau =
                    (BrowserTableau)createTableau(browserEffigy);
                if (url != null) {
                    browserEffigy.identifier.setExpression(
                            url.toExternalForm());
                }
                return browserTableau;
            }
	}
    }
}
