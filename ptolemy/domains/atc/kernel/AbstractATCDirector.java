/* A director for modeling air traffic control systems.
 
 Copyright (c) 2015 The Regents of the University of California.
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

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import ptolemy.actor.Receiver;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.domains.atc.kernel.ATCReceiver;
import ptolemy.domains.atc.lib.Airport;
import ptolemy.domains.atc.lib.DestinationAirport;
import ptolemy.domains.atc.lib.Track;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/** FIXME: Abstract base class.
 *  @author Maryam Bagheri
 */
public abstract class AbstractATCDirector extends DEDirector {

    public AbstractATCDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }
        
    /** Return an additional delay for a track to keep an aircraft in
     *  transit.
     *  @param track
     *  @param rejector
     *  @return An additional delay, or -1.0 to indicate that a rerouting is possible.
     *  @throws IllegalActionException 
     */
    public abstract double handleRejectionWithDelay(Track track) throws IllegalActionException;
    
    /** Update _stormyTracks array because of a change in condition of a track
     *  @param track
     *  @throws IllegalActionException 
     */
    public abstract void handleTrackAttributeChanged(Track track) throws IllegalActionException;
    
    /** Put an entry into _neighbors , _stormyTrack  and _inTransit for the initialized track
     *  @param track
     *  @throws IllegalActionException 
     */
    public abstract void handleInitializedTrack(Track track) throws IllegalActionException;
    
    /** Routing an aircraft based on its flight map
     *  @param aircraft (this token is a record of "aircraftId","aircraftSpeed","flightMap" and "priorTrack"and ...)
     *  @throws IllegalActionException 
     */
    public abstract RecordToken routing(Token aircraft, Token trackId) throws IllegalActionException;
    
    /** Return status of the track
     *  @param trackId
     *  @throws IllegalActionException 
     */
    public abstract boolean returnTrackStatus(Token trackId);
      
    /** Update inTransit status of a track
     *  @param trackId
     *  @param trackStatus
     *  @throws IllegalActionException 
     */
    public abstract void setInTransitStatusOfTrack(Token trackId, boolean trackStatus)
            throws IllegalActionException;
    
    /** Reroute an aircraft 
     *  @param aircraft
     *  @throws IllegalActionException 
     */
    public abstract Map<String, Token> rerouteUnacceptedAircraft(Token aircraft)
            throws IllegalActionException;
                
     /** Handle initializing of an airport
     *  @param airport
     *  @throws IllegalActionException 
     */
    
    public abstract void handleInitializedAirport(Airport airport) throws IllegalActionException;
    
    /** Handle initializing of a destination airport. This function stores airport id in _airportsId 
     *  @param destinationAirport
     *  @throws IllegalActionException 
     */
    public abstract void handleInitializedDestination( DestinationAirport destinationAirport)
            throws IllegalActionException;
			
	/** Return airplane's color. If the airplane has not color, set a color for that and store it.  
     *  @param id id of the airplane
     *  @throws IllegalActionException 
     */
    public abstract ArrayToken handleAirplaneColor(int id) throws IllegalActionException;
}
