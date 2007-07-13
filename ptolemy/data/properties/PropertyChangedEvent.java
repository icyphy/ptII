/* An event that represents a property change.

 Copyright (c) 2006 The Regents of the University of California.
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

 */
package ptolemy.data.properties;

import ptolemy.actor.IOPort;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// PropertyChangedEvent

/**
 An event that is published by ...

 @author  Thomas Mandl, Man-Kit Leung
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Red (barney)
 @Pt.AcceptedRating Red (barney)
 */
public class PropertyChangedEvent {

    /** Create a new token changed event with the given parameters.  This
     *  constructor is used for property lattices.
     */
    public PropertyChangedEvent(NamedObj component, IOPort port, PropertyConstraintSolver solver, PropertyLattice lattice, Object property) {
        _component = component;
        _port = port;
        _solver = solver;
        _lattice = lattice;
        _property = property;
        _tokenProperty = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    public NamedObj getComponent() {
        return _component;
    }

    public IOPort getPort() {
        return _port;
    }

    public PropertySolver getSolver() {
        return _solver;
    }

    public PropertyLattice getLattice() {
        return _lattice;
    }

    public Object getProperty() {
        return _property;
    }

    public TokenProperty getTokenPropery() {
        return _tokenProperty;
    }

    /** Return a string representation of this event.
     *  @return A user-readable string describing the event.
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("The solver " + _solver.toString() + " changed property " + _property.toString());

        return buffer.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private NamedObj _component;
    private IOPort _port;
    private PropertySolver _solver;
    private PropertyLattice _lattice;
    private Object _property;
    private TokenProperty _tokenProperty;
}