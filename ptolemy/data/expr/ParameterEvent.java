/* Event containing information about how a Parameter changed.

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
//// ParameterEvent
/**
A ParameterEvent is an event object that is broadcast to ParameterListener
objects when a Parameter changes or is removed. It contains information 
which the Parameter can query about how the Parameter changed, or which 
Parameter was removed.

@author  Neil Smyth
@version $Id$
@see ptolemy.data.expr.ParameterListener
@see ptolemy.data.expr.Parameter

*/
public class ParameterEvent {

    ///////////////////////////////////////////////////////////////////
    ////                     public static fields                  ////

    /** The Parameter changed as a new expression was placed in 
     *  it and evaluated to a new Token.
     */
    public static final int SET_FROM_EXPRESSION = 1;

    /** The Parameter changed as a new Token was placed directly 
     *  in it.
     */
    public static final int SET_FROM_TOKEN = 2;

    /** The Parameter changed as another Parameter, which the 
     *  expression in the Parameter referenced, changed.
     */
    public static final int UPDATED = 4;

    ///////////////////////////////////////////////////////////////////
    ////                         constructors                      ////

    /** Create a new parameter event, with no information. This 
     *  constructor will mainly be used to notify ParameterListeners 
     *  that a Parameter was removed.
     *  @param removed The Parameter that was removed
     */
    public ParameterEvent(Parameter removed) {
        id = 0;
        _parameter = removed;
    }

    /** Create a new parameter event, with the specified ID for 
     *  the specified Parameter and the Parameters new value. This 
     *  constructor will mainly be used to notify ParameterListeners 
     *  that the Parameter changed.
     *  @param id The information representing why the value in 
     *   the Parameter changed.
     *  @param changed The Parameter that changed.
     */
    public ParameterEvent(int eventId, Parameter changed) {
        id = eventId;
        _parameter = changed;
    }
   
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the ID of this event. The ID may give some additional 
     *  information about why the Parameter changed.
     *
     *  @return The integer ID.
     */
    public int getID() {
        return id;
    }

    /** Get the Parameter associated with this event. 
     *
     *  @return The Parameter which changed or was removed.
     */
    public Parameter getParameter() {
        return _parameter;
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private fields                    ////

    /** The id of the event. Note: this field name does not have
     * a preceding underscore for consistency with AWT.
     */
    private int id;
    
    /** The Parameter that was removed or changed.
     */
    private Parameter _parameter;
}
