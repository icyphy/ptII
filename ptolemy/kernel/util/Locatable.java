/* An interface for objects storing a location.

 Copyright (c) 2002-2003 The Regents of the University of California.
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
@ProposedRating Green (cxh@eecs.berkeley.edu)
@AcceptedRating Green (cxh@eecs.berkeley.edu)

*/

package ptolemy.kernel.util;

//////////////////////////////////////////////////////////////////////////
//// Locatable
/**
An interface for objects storing a location.

<p>This interface is generally implemented by attributes of objects
in a model and is used by the Vergil user interface to store 
the location of objects in the visual editor.

@author Edward A. Lee
@version $Id$
@since Ptolemy II 2.1
@see Location
*/
public interface Locatable extends Settable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the location in some cartesian coordinate system.
     *  @return The location.
     */
    public double[] getLocation();

    /** Set the location in some cartesian coordinate system, and notify
     *  the container and any value listeners of the new location.
     *  @param location The location.
     *  @exception IllegalActionException If the location is rejected.
     */
    public void setLocation(double[] location) throws IllegalActionException;
}
