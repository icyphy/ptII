/* An object that can create a new Effigy from an HSIF file.

 Copyright (c) 1997-2002 The Regents of the University of California.
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
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// EffigyFactory
/**
An object that can create a new Effigy from an HSIF file.

@author Haiyang Zheng and Edward A. Lee
@version $Id$
@since Ptolemy II 1.0
@see Configuration
@see Effigy
*/
public class EffigyFactory extends CompositeEntity {

    /** Create a factory with the given name and container.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the container is incompatible
     *   with this entity.
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public EffigyFactory(EffigyFactory container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return false, indicating that this effigy factory is not
     *  capable of creating an effigy without a URL being specified. 
     *  @return False.
     */
    public boolean canCreateBlankEffigy() {
        return false;
    }

    /** Create a new effigy in the given container by reading the specified
     *  URL. If the specified URL is refers to an HSIF file, then invoke
     *  the HSIF to MoML translator to create a MoML temporary file, and
     *  then delegate to the container of this effigy factory to open that
     *  file. If the specified file is not HSIF, return null.
     *  @param container The container for the effigy.
     *  @param base The base for relative file references, or null if
     *   there are no relative file references.
     *  @param in The input URL.
     *  @return A new effigy.
     *  @exception Exception If the stream cannot be read, or if the data
     *   is malformed in some way.
     */
    public Effigy createEffigy(CompositeEntity container, URL base, URL in)
	    throws Exception {
        // FIXME: Check whether the URL refers to an HSIF file.
        if (_inCreateEffigy) return null;
        if (it is HSIF) {
            try {
                _inCreateEffigy = true;

                // FIXME: invoke translator.
                return ((EffigyFactory)getContainer()).createEffigy(container,
                       temporaryMoMLFileURL, temporaryMoMLFileURL);
           } finally {
               _inCreateEffigy = false;
           }
       }
       return null;
   }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private boolean _inCreateEffigy;
}
