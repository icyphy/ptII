/* Interface for all classes that want to be notified when a Parameter changes
or is removed.

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
@ProposedRating Red (nsmyth@eecs.berkeley.edu)
@AcceptedRating none (reviewmoderator@eecs.berkeley.edu)

*/

package ptolemy.data.expr;

//////////////////////////////////////////////////////////////////////////
//// ParameterListener
/**
Interface for all classes that want to be notified when the Token 
stored in a Parameter changes or when a Parameter is removed(deleted).

@author  Neil Smyth
@version $Id$
@see Parameter

*/
public interface ParameterListener {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Called to report that the Token stored in the Parameter has
     *  changed.
    **/
    public void parameterChanged(ParameterEvent event);

    /** Called  to report that a Parameter has been removed(deleted).
    **/
    public void parameterRemoved(ParameterEvent event);

}
