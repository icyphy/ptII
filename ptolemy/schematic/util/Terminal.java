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

import ptolemy.kernel.util.*;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import collections.*;
import ptolemy.schematic.xml.XMLElement;

//////////////////////////////////////////////////////////////////////////
//// Terminal
/**

An Terminal is the graphical representation of a schematic entity.
Terminals are stored hierarchically in Terminal libraries.   Every Terminal has a 
name, along with a graphical representation.

@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public class Terminal extends PTMLObject {

    /**
     * Create a new Terminal with the name "Terminal". 
     * By default, the Terminal contains no graphic
     * representations.
     */
    public Terminal () {
        _x = 0;
        _y = 0;
        _input = false;
        _output = false;
        _multi = false;
    }

    /**
     * Return the X position of this Terminal
     */
    public double getX() {
        return _x;
    }

    /**
     * Return the Y position of this Terminal
     */
    public double getY() {
        return _y;
    }

    /**
     * Return whether or not this is an input terminal
     */
    public boolean isInput() {
        return _input;
    }

    /**
     * Return whether or not this is an output terminal
     */
    public boolean isOutput() {
        return _output;
    }

    /**
     * Return whether or not this is a multi terminal
     */
    public boolean isMulti() {
        return _multi;
    }

   /**
     * Set the X location of this Terminal.
     */
    public void setX(double x) {
        _x = x;
    }

   /**
     * Set the Y location of this Terminal.
     */
    public void setY(double y) {
        _y = y;
    }

   /**
     * Set whether this terminal is an input terminal.
     */
    public void setInput(boolean input) {
        _input = input;
    }

   /**
     * Set whether this terminal is an output terminal.
     */
    public void setOutput(boolean output) {
        _output = output;
    }

    /**
     * Set whether this terminal is a multi-terminal.
     */
    public void setMulti(boolean multi) {
        _multi = multi;
    }

      private double _x, _y;
    private boolean _input, _output, _multi;
}

