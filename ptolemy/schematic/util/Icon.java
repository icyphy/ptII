/* An Icon is the graphical representation of a schematic entity.

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
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.schematic.util;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import collections.*;
import ptolemy.schematic.xml.XMLElement;
import diva.canvas.Figure;
import diva.canvas.toolbox.*;
import java.awt.*;

//////////////////////////////////////////////////////////////////////////
//// Icon
/**

An icon is the graphical representation of a schematic entity.
Icons are stored hierarchically in icon libraries.   Every icon has a 
name, along with a graphical representation.

@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public class Icon extends Attribute {

    /**
     * Create a new icon with the name "Icon" in the default workspace. 
     * By default, the icon contains no graphic
     * representations.
     */
    public Icon () 
        throws IllegalActionException, NameDuplicationException {
        super();
        setName("Icon");
    }

    /**
     * Create a new icon with the name "Icon" in the given container. 
     * By default, the icon contains no graphic
     * representations.
     */
    public Icon (Entity container) 
        throws IllegalActionException, NameDuplicationException {
        super(container, "Icon");
    }

    /**
     * Create a figure based on this icon.
     */
    public Figure createFigure() {
        Figure figure = new BasicRectangle(-10, -10, 20, 20, Color.green);
        return figure;
    }
}

