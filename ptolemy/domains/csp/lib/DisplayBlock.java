/* DisplayBlock

 Copyright (c) 1997-1999 The Regents of the University of California.
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

@ProposedRating Red (davisj@eecs.berkeley.edu)

*/

package ptolemy.domains.csp.lib;

import ptolemy.domains.csp.kernel.*;
import ptolemy.domains.csp.lib.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.data.*;
import java.awt.*;

//////////////////////////////////////////////////////////////////////////
//// DisplayBlock
/**


@author John S. Davis II
@version $Id$
*/
public class DisplayBlock extends Canvas {

    /**
     */
    public DisplayBlock() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     */
    public void paint(Graphics g) {
        int w = getSize().width;
        int h = getSize().height;

        g.setColor( SystemColor.window );
        g.fillRect( 0, 0, w, h );

        g.setColor( SystemColor.black );
        Font font = new Font( "Helvetica", Font.BOLD, 40 );
        g.setFont(font);
        FontMetrics fMetrics = g.getFontMetrics();
        int ascent = fMetrics.getAscent();
        int width = fMetrics.charWidth( '?' );
        g.drawString( "?", (w - width)/2 , (h + ascent)/2 );
    }


    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////


}
