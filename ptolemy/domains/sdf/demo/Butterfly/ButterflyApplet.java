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

import ptolemy.domains.sdf.gui.*;
import ptolemy.plot.*;

import java.awt.BorderLayout;

//////////////////////////////////////////////////////////////////////////
//// ButterflyApplet
/**

@author Christopher Hylands
@version : ptmkmodel,v 1.7 1999/07/16 01:17:49 cxh Exp ButterflyApplet.java.java,v 1.1 1999/05/06 20:14:28 cxh Exp $
*/
public class ButterflyApplet extends SDFApplet {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Initialize the applet.
     */
    public void init() {
        super.init();
        try {
	    // Pass in the panel so that we can pass it to the plotter.
	    Butterfly.init(_toplevel, getContentPane(), getSize());
        } catch (Exception ex) {
            report("Error constructing model.", ex);
        }
	getContentPane().add(_createRunControls(2), BorderLayout.SOUTH);
    }
}
