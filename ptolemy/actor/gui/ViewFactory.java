/* An object that can create a view fora model proxy.

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
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// ViewFactory
/**
An object that will create a view for a Model Proxy.  Instances of this
class will usually be contained in an application hierarchy.

This base class assumes that it contains other view factories.  It defers
to each contained factory in order until one is capable of creating a
view.  Subclasses of this class will usually be inner classes of a View,
and create a View appropriate with a given subclass of model proxy.

@author Steve Neuendorffer
@version $Id$
*/
public class ViewFactory extends CompositeEntity {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a view in the default workspace with no name for the 
     *  given ModelProxy.  The view will created with a new unique name
     *  in the given model proxy.  If this factory cannot create a view
     *  for the given proxy (perhaps because the proxy is not of the
     *  appropriate subclass) then return null.
     *  This base class assumes that it contains other instances of
     *  ViewFactory and returns the view returned by the first one of those
     *  contained factories that does not return null, or null if they
     *  all return null.
     *  @param proxy The model proxy.
     */
    public View createView(ModelProxy proxy) {
	View view = null;
	Iterator factories = entityList(ViewFactory.class).iterator();
	while(factories.hasNext() && view == null) {
	    ViewFactory factory = (ViewFactory)factories.next();
	    view = factory.createView(proxy);
	}
	return view;
    }
}
