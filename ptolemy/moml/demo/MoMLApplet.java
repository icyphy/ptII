/* Applet demonstrating the MoML class.

 Copyright (c) 1998-1999 The Regents of the University of California.
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
@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (eal@eecs.berkeley.edu)

*/

package ptolemy.moml.demo;

import java.net.URL;

import ptolemy.moml.*;
import ptolemy.gui.*;

//////////////////////////////////////////////////////////////////////////
//// MoMLApplet
/**
Applet demonstrating the MoML parser.

@author  Edward A. Lee
@version $Id$
@see ptolemy.gui.MoML
*/
public class MoMLApplet extends BasicJApplet {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a MoML parser and parse a file.
     */
    public void init() {
        super.init();
        // FIXME: Bogus catch
        try {
            MoMLParser parser = new MoMLParser();
            URL docBase = getDocumentBase();
            URL xmlFile = new URL(docBase, "example.xml");
            parser.parse(docBase.toExternalForm(), xmlFile.openStream());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
}
