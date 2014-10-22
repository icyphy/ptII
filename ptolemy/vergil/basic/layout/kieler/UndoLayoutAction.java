/*

Copyright (c) 2011-2014 The Regents of the University of California.
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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.undo.UndoAction;
import ptolemy.kernel.undo.UndoStackAttribute;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.basic.layout.kieler.ApplyLayoutRequest.CurveEntry;
import ptolemy.vergil.basic.layout.kieler.ApplyLayoutRequest.LocationEntry;

/**
 * An undo action that is able to revert the changes made by automatic layout, or to
 * repeat them in the case of redo.
 *
 * @author Miro Spoenemann (<a href="mailto:msp@informatik.uni-kiel.de">msp</a>)
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (msp)
 * @Pt.AcceptedRating Red (msp)
 */
public class UndoLayoutAction implements UndoAction {

    /**
     * Create an undo action for automatic layout.
     *
     * @param source The source object, which is typically the parent composite entity.
     */
    public UndoLayoutAction(NamedObj source) {
        this._source = source;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Add a location to the undo action. The action will set the location to the
     * coordinates stored in the given location entry.
     *
     * @param entry A location entry with all required data
     */
    public void addLocation(LocationEntry entry) {
        _locationEntries.add(entry);
    }

    /**
     * Add a curve to the undo action. The action will set the exit angle to the
     * value stored in the given curve entry.
     *
     * @param entry A curve entry with stored exit value
     */
    public void addCurve(CurveEntry entry) {
        _curveEntries.add(entry);
    }

    /**
     * Mark the given connection routing hint for removal. The action will remove
     * the layout hint from its containing relation.
     *
     * @param layoutHint A connection routing hint contained in a relation
     */
    public void removeConnection(LayoutHint layoutHint) {
        _connRemoveEntries.add(layoutHint);
    }

    /**
     * Execute the undo or redo action. This sets all previously configured locations,
     * removes connection routing hints that are marked for removal, and adds
     * connection routing hints that are marked for adding.
     */
    @Override
    public void execute() throws Exception {
        UndoLayoutAction undoLayoutAction = new UndoLayoutAction(_source);

        // Process layout hints that shall be removed.
        for (LayoutHint layoutHint : this._connRemoveEntries) {
            NamedObj container = layoutHint.getContainer();
            if (container != null) {
                layoutHint.setContainer(null);
                undoLayoutAction._connAddEntries.add(new ConnectionHintEntry(
                        container, layoutHint));
            }
        }

        // Process locations.
        for (LocationEntry entry : this._locationEntries) {
            double[] oldLoc = entry._locatable.getLocation();
            undoLayoutAction.addLocation(new LocationEntry(entry._locatable,
                    oldLoc[0], oldLoc[1]));
            entry._locatable.setLocation(new double[] { entry._x, entry._y });
        }

        // Process layout hints that shall be added.
        for (ConnectionHintEntry entry : this._connAddEntries) {
            entry._layoutHint.setContainer(entry._container);
            undoLayoutAction.removeConnection(entry._layoutHint);
        }

        // Process transition curves.
        for (CurveEntry entry : this._curveEntries) {
            Parameter exitAngleParam = entry._transition.exitAngle;
            DoubleToken token = DoubleToken.convert(exitAngleParam.getToken());
            undoLayoutAction.addCurve(new CurveEntry(entry._transition, token
                    .doubleValue()));
            exitAngleParam.setExpression(Double.toString(entry._exitAngle));
        }

        UndoStackAttribute undoInfo = UndoStackAttribute.getUndoInfo(_source);
        undoInfo.push(undoLayoutAction);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The configured locations that will be changed. */
    private List<LocationEntry> _locationEntries = new LinkedList<LocationEntry>();
    /** The configures curves that will be changed. */
    private List<CurveEntry> _curveEntries = new LinkedList<CurveEntry>();
    /** Layout hints that should be removed from their containing relations. */
    private Set<LayoutHint> _connRemoveEntries = new HashSet<LayoutHint>();
    /** Layout hints that should be added to relations. */
    private List<ConnectionHintEntry> _connAddEntries = new LinkedList<ConnectionHintEntry>();
    /** The source object, which is typically the parent composite entity. */
    private NamedObj _source;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /**
     * An entry that contains data for adding a connection routing hint.
     */
    private static class ConnectionHintEntry {
        NamedObj _container;
        LayoutHint _layoutHint;

        ConnectionHintEntry(NamedObj container, LayoutHint layoutHint) {
            this._container = container;
            this._layoutHint = layoutHint;
        }
    }

}
