/* An attribute that represents the location of a object.

 Copyright (c) 1998 The Regents of the University of California.
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
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)

*/

package ptolemy.moml;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// LocationAttribute
/** 
An attribute that represents the location of an object. 
The attribute contains an (x,y) coordinate position, and always has the name
_location. The coordinates are
expressed relative to the origin of the object's container.

@author Steve Neuendorffer
@version $Id$
*/
public class LocationAttribute extends Attribute {

    /** Construct an attribute with the name <i>_location</i> and the specified
     *  contents for the the specified container.  If the container already
     *  contains an attribute by that name, replace it.  If the container
     *  rejects the attribute with an IllegalActionException, then
     *  the attribute is not forced upon the container, but rather is
     *  created with no container.
     *  @param container The container.
     *  @param x The x location of the object.
     *  @param y The y location of the object.
     */	
    public LocationAttribute(NamedObj container, int x, int y) {
        super(container.workspace());
        try {
            setName(LOCATION_ATTRIBUTE_NAME);
	    _x = x;
	    _y = y;
	
            Attribute previous = 
		container.getAttribute(LOCATION_ATTRIBUTE_NAME);
            if (previous != null) {
                previous.setContainer(null);
            }
            setContainer(container);
        } catch (NameDuplicationException ex) {
            throw new InternalErrorException(ex.getMessage());
        } catch (IllegalActionException ex) {
            // The container rejects the attribute; create with no container.
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The name of a doc attribute. */
    public static final String LOCATION_ATTRIBUTE_NAME = "_location";

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the x coordinate of this location.
     */
    public int getX() {
	return _x;
    }
 
    /** Return the y coordinate of this location.
     */
    public int getY() {
	return _y;
    }

    /** Set the x coordinate of this location.
     */
    public void setX(int value) {
	_x = value;
    }

    /** Set the y coordinate of this location.
     */
    public void setY(int value) {
	_y = value;
    }

    /** Get the vertex as a string.
     */	
    public String toString() {
        return "Location(" + _x + ", " + _y + ")";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The coordinates of the location.
    private int _x;
    private int _y;
}
