/* Butterfly applet

 Copyright (c) 1999-2000 The Regents of the University of California.
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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.demo.Butterfly;

import ptolemy.actor.Manager;
import ptolemy.actor.gui.Placeable;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.domains.sdf.gui.SDFApplet;

import java.awt.BorderLayout;
import java.awt.BorderLayout;
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// ButterflyApplet
/**
Butterfly Applet.
@author Christopher Hylands
@version : $Id$
*/
public class ButterflyApplet extends SDFApplet {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Initialize the applet.
     */
    public void init() {
        super.init();
        try {
	    Butterfly butterfly = new Butterfly(_workspace);
	    butterfly.setContainer(_toplevel);

	    // Put placeable objects that we created in the
	    // constructor in a reasonable place.
	    // Note that this code will not look inside
	    // opaque entities.
	    for(Iterator i = butterfly.entityList().iterator();
		i.hasNext();) {
		Object o = i.next();
		if(o instanceof Placeable) {
		    ((Placeable) o).place(getContentPane());
		}
	    }
        } catch (Exception ex) {
            report("Error constructing model.", ex);
        }
	getContentPane().add(_createRunControls(2), BorderLayout.SOUTH);
    }
}
