/* An object that can create a tableau for a model proxy.

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
import ptolemy.kernel.util.NameDuplicationException;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// TableauFactory
/**
An object that will create a tableau for a Model Proxy.  Instances of this
class will usually be contained in an application hierarchy.

This base class assumes that it contains other tableau factories.  It defers
to each contained factory in order until one is capable of creating a
tableau.  Subclasses of this class will usually be inner classes of a Tableau,
and create a Tableau appropriate with a given subclass of model proxy.

@author Steve Neuendorffer
@version $Id$
*/
public class TableauFactory extends CompositeEntity {

    /** Create an factory with the given name and container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This entity will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container entity.
     *  @param name The name of the entity.
     *  @exception IllegalActionException If the container is incompatible
     *   with this entity.
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public TableauFactory(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a tableau in the default workspace with no name for the 
     *  given Effigy.  The tableau will created with a new unique name
     *  in the given model proxy.  If this factory cannot create a tableau
     *  for the given proxy (perhaps because the proxy is not of the
     *  appropriate subclass) then return null.
     *  This base class assumes that it contains other instances of
     *  TableauFactory and returns the tableau returned by the first one of those
     *  contained factories that does not return null, or null if they
     *  all return null.
     *  @param proxy The model proxy.
     */
    public Tableau createTableau(Effigy proxy) {
	Tableau tableau = null;
	Iterator factories = entityList(TableauFactory.class).iterator();
	while(factories.hasNext() && tableau == null) {
	    TableauFactory factory = (TableauFactory)factories.next();
	    tableau = factory.createTableau(proxy);
	}
	return tableau;
    }
}
