/*

Copyright (c) 2011 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA LIABLE TO ANY PARTY
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
package ptolemy.vergil.basic.layout.kieler;

import java.util.LinkedList;
import java.util.List;

import ptolemy.kernel.Relation;
import ptolemy.kernel.undo.UndoStackAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.NamedObj;

/**
 * A change request specialized for application of automatically computed layout.
 * This is used to set new locations for graph elements and to set layout hints
 * for connection routing.
 *
 * @author Miro Spoenemann (<a href="mailto:msp@informatik.uni-kiel.de">msp</a>)
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (msp)
 * @Pt.AcceptedRating Red (msp)
 */
public class ApplyLayoutRequest extends ChangeRequest {

    /**
     * Create a request for applying layout.
     * 
     * @param source The source object, which is typically the parent composite actor.
     */
    public ApplyLayoutRequest(Object source) {
        super(source, "KIELER Automatic Layout", true);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     * Add a new location change to the request.
     * 
     * @param locatable The locatable that will be changed
     * @param x The new x coordinate
     * @param y The new y coordinate
     */
    public void addLocation(Locatable locatable, double x, double y) {
        _locationEntries.add(new LocationEntry(locatable, x, y));
    }
    
    /**
     * Add a new connection routing change to the request.
     * 
     * @param relation The relation that owns the connection.
     * @param head The head object of the connection.
     * @param tail The tail object of the connection.
     * @param bendPoints The new bend points.
     */
    public void addConnection(Relation relation, NamedObj head, NamedObj tail,
            double[] bendPoints) {
        _connectionEntries.add(new ConnectionEntry(relation, head, tail, bendPoints));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    
    /**
     * Execute the request. This sets the previously configured locations and connections.
     */
    @Override
    protected void _execute() throws Exception {
        NamedObj source = (NamedObj) getSource();
        UndoLayoutAction undoLayoutAction = new UndoLayoutAction(source);
        
        for (LocationEntry entry : _locationEntries) {
            double[] oldLoc = entry._locatable.getLocation();
            undoLayoutAction.addLocation(new LocationEntry(entry._locatable,
                    oldLoc[0], oldLoc[1]));
            entry._locatable.setLocation(new double[] { entry._x, entry._y } );
        }
        
        for (ConnectionEntry entry : _connectionEntries) {
            Attribute attribute = entry._relation.getAttribute("_layoutHint");
            if (attribute == null) {
                attribute = new LayoutHint(entry._relation, "_layoutHint");
            }
            if (attribute instanceof LayoutHint) {
                LayoutHint layoutHint = (LayoutHint) attribute;
                layoutHint.setLayoutHintItem(entry._head, entry._tail, entry._bendPoints);
            }
        }
        
        UndoStackAttribute undoInfo = UndoStackAttribute.getUndoInfo(source);
        undoInfo.push(undoLayoutAction);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The configured locations that will be changed. */
    private List<LocationEntry> _locationEntries = new LinkedList<LocationEntry>();
    /** The configured connections that will be changed. */
    private List<ConnectionEntry> _connectionEntries = new LinkedList<ConnectionEntry>();
    
    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /**
     * An entry that contains data for changing a location.
     */
    static class LocationEntry {
        Locatable _locatable;
        double _x;
        double _y;
        
        LocationEntry(Locatable locatable, double x, double y) {
            this._locatable = locatable;
            this._x = x;
            this._y = y;
        }
    }
    
    /**
     * An entry that contains data for changing a connection.
     */
    static class ConnectionEntry {
        Relation _relation;
        NamedObj _head;
        NamedObj _tail;
        double[] _bendPoints;
        
        ConnectionEntry(Relation relation, NamedObj head, NamedObj tail,
                double[] bendPoints) {
            this._relation = relation;
            this._head = head;
            this._tail = tail;
            this._bendPoints = bendPoints;
        }
    }

}
