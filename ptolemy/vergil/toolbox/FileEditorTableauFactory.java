/* An attribute that causes look inside to open a text editor to
   edit a file or URL specified by an attribute in the container.

 Copyright (c) 1997-2003 The Regents of the University of California.
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
@AcceptedRating Red (ptolemy@eecs.berkeley.edu)

*/

package ptolemy.vergil.toolbox;

import java.net.URL;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFactory;
import ptolemy.data.expr.FileParameter;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;

//////////////////////////////////////////////////////////////////////////
//// FileEditorTableauFactory
/**
This class is an attribute that creates a text editor to edit a specified
file or URL given by an attribute in the container of this attribute.
It is similar to TextEditorTableauFactory, but instead of editing an
attribute in the container, it edits a file or URL referenced by that
attribute.  The file or URL must be given in the container by an
instance of FileParameter.

@author Edward A. Lee
@version $Id$
@see TextEditorTableauFactory
@see FileParameter
*/
public class FileEditorTableauFactory
        extends TableauFactory {

    /** Create a factory with the given name and container.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the container is incompatible
     *   with this attribute.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public FileEditorTableauFactory(NamedObj container, String name)
        throws IllegalActionException, NameDuplicationException {
        super(container, name);

        attributeName = new StringAttribute(this, "attributeName");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The name of the file attribute giving the file name or URL. */
    public StringAttribute attributeName;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a tableau for the specified effigy. The tableau will be
     *  created with a new unique name with the specified effigy as its
     *  container.  If this factory cannot create a tableau
     *  for the given effigy (it is not an instance of PtolemyEffigy),
     *  then return null.
     *  @param effigy The component effigy.
     *  @return A tableau for the effigy, or null if one cannot be created.
     *  @exception Exception If the factory should be able to create a
     *   Tableau for the effigy, but something goes wrong.
     */
    public Tableau createTableau(Effigy effigy) throws Exception {
        
        // FIXME: Exceptions thrown here are ignored by the caller,
        // who then just goes to the next tableau factory...
        
        if (!(effigy instanceof PtolemyEffigy)) {
            return null;
        }
        NamedObj object = ((PtolemyEffigy) effigy).getModel();
        Attribute attribute =
            object.getAttribute(attributeName.getExpression());
        if (!(attribute instanceof FileParameter)) {
            throw new IllegalActionException(
                object,
                "Expected "
                    + object.getFullName()
                    + " to contain a FileParameter named "
                    + attributeName.getExpression()
                    + ", but it does not.");
        }

        URL url = ((FileParameter)attribute).asURL();
        Configuration configuration = (Configuration)effigy.toplevel();
        return configuration.openModel(null, url, url.toExternalForm());
    }
}
