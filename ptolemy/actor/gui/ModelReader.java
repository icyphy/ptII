/* An object that can read models from a URL.

 Copyright (c) 1997-2000 The Regents of the University of California.
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

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;

//////////////////////////////////////////////////////////////////////////
//// ModelReader
/**
An object that can read models from a URL.
A configuration contains one instance of this class, and delegates
reading URLs to that instance.  By convention, that instance is named
"reader", although that is not enforced by this class.
The configuration is responsible for
registering the model with the ModelDirectory.

@author Steve Neuendorffer and Edward A. Lee
@version $Id$
@see Configuration
@see ModelDirectory
*/
public class ModelReader extends CompositeEntity {

    /** Create a new reader in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this reader within the container.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public ModelReader(Configuration container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read the specified input URL, and return an effigy
     *  for the data at that URL.  An effigy represents that data in
     *  the model directory, and contains all open tableaux of that data.
     *  The effigy is created in the same workspace as this model reader,
     *  but it is not given a name.
     *  @param base The base for relative file references, or null if
     *   there are no relative file references.
     *  @param in The input URL.
     *  @exception Exception If the stream cannot be read, or if the data
     *   is malformed in some way.
     */
    public Effigy read(URL base, URL in) throws Exception {
        // FIXME: Currently this only reads MoML files.  Need to use
        // MIME types to determine how to read this.
        MoMLParser parser = new MoMLParser();
        NamedObj toplevel = parser.parse(base, in.openStream());

        // Create an effigy for the model.
        PtolemyEffigy effigy = new PtolemyEffigy(workspace());
        effigy.setModel(toplevel);

        // Identify the URL from which the model was read by inserting
        // an attribute into both the model and the effigy.
        URLAttribute url
                = new URLAttribute(toplevel, toplevel.uniqueName("url"));
        url.setURL(in);
        // This is used by TableauFrame in its _save() method.
        effigy.url.setURL(in);

        return effigy;
    }
}
