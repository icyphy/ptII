/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 1995-2014 The Regents of the University of California.
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

Ptolemy II includes the work of others, to see those copyrights, follow
the copyright link on the splash page or see copyright.htm.
 */
/** An attribute used to store a relative visual location. */
package ptolemy.kernel.util;

import java.util.List;

import ptolemy.kernel.CompositeEntity;
import ptolemy.vergil.basic.LocatableNodeDragInteractor;

/** An attribute used to store a relative visual location.
 *  The location is relative to an object specified by
 *  the <i>relativeTo</i> attribute, which gives the name
 *  of an object that is expected to be contained by the
 *  container of the container of this attribute.
 *  In addition, the <i>relativeToElementName</i> specifies
 *  what kind of object this is relative to (an "entity",
 *  "property" (attribute), "port", or "relation").
 *
 @author Edward A. Lee, Christian Motika, Miro Spoenemann
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Yellow (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class RelativeLocation extends Location {

    /** Construct an instance.
     *  @param container The container (the object to have a relative location).
     *  @param name The name of this instance.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    public RelativeLocation(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        relativeTo = new StringAttribute(this, "relativeTo");
        relativeTo.setVisibility(Settable.EXPERT);
        relativeToElementName = new StringAttribute(this,
                "relativeToElementName");
        relativeToElementName.setExpression("entity");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The name of the object this location is relative to. */
    public StringAttribute relativeTo;

    /** The element name of the object this location is relative to.
     *  This defaults to "entity".
     */
    public StringAttribute relativeToElementName;

    /** The initial offset for new relative locatable objects. */
    public static final double INITIAL_OFFSET = 40.0;

    /** The maximal distance of the relative location. If this is exceeded
     *  after moving the relative locatable, the link is broken (see
     *  {@link LocatableNodeDragInteractor#mouseReleased(diva.canvas.event.LayerEvent)}).
     */
    public static final double BREAK_THRESHOLD = 300.0;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the location in some cartesian coordinate system.
     *  This method returns the absolute location of the object.
     *  If the relative location was previously attached to an object
     *  referenced in the {@link #relativeTo} property and that object
     *  is gone, then the internally stored location is updated so it
     *  contains the correct absolute location.
     *  @return The location.
     *  @see #setLocation(double[])
     */
    @Override
    public double[] getLocation() {
        double[] offset = super.getLocation();
        NamedObj relativeToObject = getRelativeToNamedObj();
        if (relativeToObject == null || offset == null) {
            return offset;
        }
        double[] relativeToLocation = _getRelativeToLocation(relativeToObject);
        if (relativeToLocation != null) {
            double[] result = new double[offset.length];
            for (int i = 0; i < offset.length; i++) {
                result[i] = offset[i] + relativeToLocation[i];
            }
            return result;
        }
        // If we get to here, then the relativeTo object is gone, so
        // update the relative location to an absolute one if possible.
        if (_cachedReltoLoc != null) {
            for (int i = 0; i < offset.length; i++) {
                // The offset array is also referenced by the superclass, so
                // changes to its content affect the actual location.
                offset[i] += _cachedReltoLoc[i];
            }
            _cachedReltoLoc = null;
        }
        return offset;
    }

    /** Get the relative location, relative to the <i>relativeTo</i>
     *  object, if there is one, and otherwise return the absolute
     *  location.
     *  @return The relative location.
     *  @see #setLocation(double[])
     */
    public double[] getRelativeLocation() {
        return super.getLocation();
    }

    /** If the <i>relativeTo</i> object exists, return it.
     *  Otherwise, return null and clear the <i>relativeTo</i>
     *  parameter value.
     *  @return The relativeTo object, or null if it
     *   does not exist.
     */
    public NamedObj getRelativeToNamedObj() {
        String relativeToName = relativeTo.getExpression();
        if (relativeToName.trim().equals("")) {
            return null;
        }
        NamedObj result = null;
        NamedObj container = getContainer();
        if (container != null) {
            NamedObj containersContainer = container.getContainer();
            if (containersContainer instanceof CompositeEntity) {
                CompositeEntity composite = (CompositeEntity) containersContainer;
                String elementName = relativeToElementName.getExpression();
                // The relativeTo object is not necessarily an Entity.
                if (elementName.equals("property")) {
                    result = composite.getAttribute(relativeToName);
                } else if (elementName.equals("port")) {
                    result = composite.getPort(relativeToName);
                } else if (elementName.equals("relation")) {
                    result = composite.getRelation(relativeToName);
                } else {
                    result = composite.getEntity(relativeToName);
                }
            }
        }
        if (result == null) {
            // The relativeTo object could not be found, so the attributes holding
            // the reference are no longer valid. Erase their content.
            try {
                relativeTo.setExpression("");
                relativeToElementName.setExpression("");
            } catch (IllegalActionException exception) {
                throw new InternalErrorException(exception);
            }
        }
        return result;
    }

    /** Set the location in some cartesian coordinate system, and notify
     *  the container and any value listeners of the new location. Setting
     *  the location involves maintaining a local copy of the passed
     *  parameter. No notification is done if the location is the same
     *  as before. This method propagates the value to any derived objects.
     *  If the relative location is attached to an object referenced in the
     *  {@link #relativeTo} property, then only the relative location is
     *  stored internally.
     *  @param location The location.
     *  @exception IllegalActionException Thrown when attributeChanged() is called.
     *  @see #getLocation()
     */
    @Override
    public void setLocation(double[] location) throws IllegalActionException {
        NamedObj relativeToObject = getRelativeToNamedObj();
        if (relativeToObject == null) {
            super.setLocation(location);
            return;
        }
        double[] relativeToLocation = _getRelativeToLocation(relativeToObject);
        if (relativeToLocation != null) {
            double[] result = new double[location.length];
            for (int i = 0; i < location.length; i++) {
                result[i] = location[i] - relativeToLocation[i];
            }
            super.setLocation(result);
            return;
        }
        // If we get to here, then the relativeTo object is gone, so delete
        // the cached value.
        _cachedReltoLoc = null;
        super.setLocation(location);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** If the <i>relativeTo</i> object exists, return its location.
     *  Otherwise, return null.
     *  @param relativeToObject The relativeTo object.
     *  @return The location of the relativeTo object, or null if it
     *   does not exist.
     */
    private double[] _getRelativeToLocation(NamedObj relativeToObject) {
        List<Locatable> locatables = relativeToObject
                .attributeList(Locatable.class);
        if (locatables.size() > 0) {
            _cachedReltoLoc = locatables.get(0).getLocation();
            return _cachedReltoLoc;
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The cached relativeTo location. This is used to restore the absolute position
     *  after the relativeTo object has been deleted.
     */
    private double[] _cachedReltoLoc;

}
