/* A director for modeling air traffic control systems.

 Copyright (c) 2015-2016 The Regents of the University of California.
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
package ptolemy.domains.atc.kernel;

import java.util.Map;

import ptolemy.data.ArrayToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.domains.atc.lib.Airport;
import ptolemy.domains.atc.lib.DestinationAirport;
import ptolemy.domains.atc.lib.Track;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/** An air traffic control (ATC) Director.
 *  @author Maryam Bagheri
 *  @version $Id$
 *  @since Ptolemy II 11.0
 */
public abstract class AbstractATCDirector extends DEDirector {

    /** Create a new director in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If this actor cannot be contained
     *   by the proposed container (see the setContainer() method).
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public AbstractATCDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Return an additional delay for a track to keep an aircraft in
     *  transit.
     *  @param track The track.
     *  @return An additional delay, or -1.0 to indicate that a rerouting is possible.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public abstract double handleRejectionWithDelay(Track track) throws IllegalActionException;

    /** Update _stormyTracks array because of a change in condition of a track.
     *  @param track The track.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public abstract void handleTrackAttributeChanged(Track track) throws IllegalActionException;

    /** Put an entry into _neighbors , _stormyTrack  and _inTransit for the initialized track.
     *  @param track The track.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public abstract void handleInitializedTrack(Track track) throws IllegalActionException;

    /** Routing an aircraft based on its flight map.
     *  @param aircraft (this token is a record of "aircraftId","aircraftSpeed","flightMap" and "priorTrack"and ...)
     *  @param trackId The trackid.
     *  @return A RecordToken representing the routing.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public abstract RecordToken routing(Token aircraft, Token trackId) throws IllegalActionException;

    /** Return status of the track.
     *  @param trackId The trackid.
     *  @return The status of the track.
     */
    public abstract boolean returnTrackStatus(Token trackId);

    /** Update inTransit status of a track.
     *  @param trackId The trackid
     *  @param trackStatus The status
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public abstract void setInTransitStatusOfTrack(Token trackId, boolean trackStatus)
            throws IllegalActionException;

    /** Reroute an aircraft.
     *  @param aircraft The aircraft
     *  @return a Map of rerouted aircraft.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public abstract Map<String, Token> rerouteUnacceptedAircraft(Token aircraft)
            throws IllegalActionException;

    /** Return airplane's color. If the airplane has not color, set a color for that and store it.
     *  @param id id of the airplane
     *  @return The color of the airplane.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public abstract ArrayToken handleAirplaneColor(int id) throws IllegalActionException;

    /** Handle initializing of an airport.
     *  @param airport The airport
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public abstract void handleInitializedAirport(Airport airport) throws IllegalActionException;

    /** Handle initializing of a destination airport. This function stores airport id in _airportsId
     *  @param destinationAirport The destination.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public abstract void handleInitializedDestination( DestinationAirport destinationAirport)
            throws IllegalActionException;
}
