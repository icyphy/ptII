/* An object that can create a new Effigy

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
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// EffigyFactory
/**
A configuration contains multiple instances of this class, and uses them
to create new effigies.  Toplevel frames will usually provide a menu for
ubclasses

@author Steve Neuendorffer
@version $Id$
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
    public EffigyFactory(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a new effigy in the given model directory 
     *  The new Effigy should be
     *  of a type appropriate for this factory.  Subclasses will
     *  override this method to create an effigy of an appropriate type.
     *  @return A new effigy.
     */
    public Effigy createEffigy(ModelDirectory directory) 
	throws NameDuplicationException, IllegalActionException {
	return new Effigy(directory, directory.uniqueName("effigy"));
    }
}
