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
package ptolemy.domains.atc.kernel.policy1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
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
import ptolemy.domains.atc.kernel.AbstractATCDirector;
import ptolemy.domains.atc.lib.Airport;
import ptolemy.domains.atc.lib.DestinationAirport;
import ptolemy.domains.atc.lib.Track;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/** A director for modeling air traffic control systems.
 *  This director provides a receiver that consults the destination actor
 *  to determine whether it can accept an input, and provides mechanisms
 *  for handling rejection of an input.
 *  @author Maryam Bagheri
 */
public class ATCDirector extends AbstractATCDirector {

    public ATCDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        // TODO Auto-generated constructor stub
    }

    @Override
    public Receiver newReceiver() {
        return new ATCReceiver();
    }
    
          @Override
    public void initialize() throws IllegalActionException {
        _stormyTracks=new TreeMap<>();
        _neighbors=new TreeMap<>();
        _inTransit=new TreeMap<>();
        _airportsId= new ArrayList<>();
        _airplanesId=new ArrayList<>();
        _airplanesColor=new HashMap<Integer,ArrayToken>();
        super.initialize();
    }
        
    /** Return an additional delay for a track to keep an aircraft in
     *  transit.
     *  @param track
     *  @param rejector
     *  @return An additional delay, or -1.0 to indicate that a rerouting is possible.
     *  @throws IllegalActionException 
     */
    public double handleRejectionWithDelay(Track track) throws IllegalActionException {
        // FIXME: what value should be returned here?
        return 1.0;
    }
    
    /** Update _stormyTracks array because of a change in condition of a track
     *  @param track
     *  @throws IllegalActionException 
     */
    public void handleTrackAttributeChanged(Track track) throws IllegalActionException {
        int id=((IntToken)track.trackId.getToken()).intValue();
        if(_stormyTracks.size()!=0)
        {
            if(_stormyTracks.containsKey(id))
            {
                _stormyTracks.put(id, track.stormy.getToken());
            }else
                throw new IllegalActionException("The entry for this track has not been set in stormyTrack array "); 
        }
    }
    
    /** Put an entry into _neighbors , _stormyTrack  and _inTransit for the initialized track
     *  @param track
     *  @throws IllegalActionException 
     */
    public void handleInitializedTrack(Track track) throws IllegalActionException{
        int id=((IntToken)track.trackId.getToken()).intValue();
        if(id==-1)
            throw new IllegalActionException("Id of the track "+id+" is invalid (-1)");
        if(_stormyTracks.containsKey(id))
            throw new IllegalActionException("Track with the id "+id+" has been duplicated");
                if(_airportsId.contains(id))
            throw new IllegalActionException("Track id is same as airport id");
        else {
            if(track.stormy.getToken()==null)
                throw new IllegalActionException("Stormy parameter of track "+id+" has not been filled");
            _stormyTracks.put(id, track.stormy.getToken());
        }
        
        _inTransit.put(id, false);
        _neighbors.put(id, (ArrayToken)track.neighbors.getToken());
    }
    
    /** Routing an aircraft based on its flight map
     *  @param aircraft (this token is a record of "aircraftId","aircraftSpeed","flightMap" and "priorTrack"and ...)
     *  @throws IllegalActionException 
     */
    public RecordToken routing(Token aircraft, Token trackId) throws IllegalActionException{
        RecordToken airplane=(RecordToken) aircraft;
        ArrayToken flightMap=(ArrayToken)airplane.get("flightMap");
        int id=((IntToken)trackId).intValue();
        if(!flightMap.getElement(0).equals(trackId))
            throw new IllegalActionException("There is a mistake in routing: mismatch of track id "+id+" with first element in flight map "+((IntToken)flightMap.getElement(0)).intValue());
        Token nextTrackInFlight=flightMap.getElement(1);
        int route=-1;
        if(_neighbors.containsKey(id))
        {
            ArrayToken trackNeighbors=_neighbors.get(id);
            for(int i=0;i<trackNeighbors.length();i++)
                if(trackNeighbors.getElement(i).equals(nextTrackInFlight))
                {
                    route=i;
                    break;
                }
            if(route==-1)
                throw new IllegalActionException("Mistake in routing. track "+id+" has not neighbor track "+nextTrackInFlight);
        }else
            throw new IllegalActionException("Neighbors of the current track with id "+id+" have not been set.");
        
        Token [] newFlightMap=new Token[flightMap.length()-1];
        int j=0;
        for(int i=1;i<flightMap.length();i++)
            newFlightMap[j++]=flightMap.getElement(i);
        
        //creating a new airplane record
        Map<String, Token> newAirplane=new TreeMap<String, Token>();
        newAirplane.put("aircraftId", airplane.get("aircraftId"));
        newAirplane.put("aircraftSpeed", airplane.get("aircraftSpeed"));
        newAirplane.put("flightMap", (Token)(new ArrayToken(BaseType.INT, newFlightMap)));
        newAirplane.put("priorTrack", (Token)(new IntToken(id)));
        newAirplane.put("arrivalTimeToAirport", airplane.get("arrivalTimeToAirport"));
        newAirplane.put("dipartureTimeFromAirport", airplane.get("dipartureTimeFromAirport"));
        newAirplane.put("fuel", airplane.get("fuel"));
        //
        //add some infromation to newAirplane and then exploit and remove them from newAirplane (for transfer information to Track actor).
        newAirplane.put("delay", new DoubleToken(1.0));
        newAirplane.put("route", new IntToken(route));
        return (new RecordToken(newAirplane));
    }
    /** Return status of the track
     *  @param trackId
     *  @throws IllegalActionException 
     */
    public boolean returnTrackStatus(Token trackId)  {
        int id=((IntToken)trackId).intValue();
        return (_inTransit.get(id) || ((BooleanToken)_stormyTracks.get(id)).booleanValue());
    }
      
    /** Update inTransit status of a track
     *  @param trackId
     *  @param trackStatus
     *  @throws IllegalActionException 
     */
    public void setInTransitStatusOfTrack(Token trackId, boolean trackStatus) throws IllegalActionException{
        int id=((IntToken)trackId).intValue();
        if(_inTransit.containsKey(id))
            _inTransit.put(id, trackStatus);
        else if(!_airportsId.contains(id))
            throw new IllegalActionException("There is no track with id "+id);
    }
    /** Reroute an aircraft 
     *  @param aircraft
     *  @throws IllegalActionException 
     */
    public Map<String, Token> rerouteUnacceptedAircraft(Token aircraft) throws IllegalActionException {
        RecordToken airplane=(RecordToken) aircraft;
        ArrayToken flightMap=(ArrayToken)airplane.get("flightMap");
        if(flightMap.length()==1){// it just contains id of the destination airport,
            //it should send the airplane to that again.
            Map<String, Token> map=new TreeMap<String, Token>();
            map.put("flightMap", (Token)flightMap);
            map.put("route", new IntToken(-1));
            map.put("delay", new DoubleToken(1.0));
            return map;
        }
            
        Token choosedNeighbor=null;
        int route=-1;
		boolean baseOnDestination=false;
        boolean neighborChoosed=false;
        int priorTrack=((IntToken)airplane.get("priorTrack")).intValue();
        Token currentTrack=flightMap.getElement(0);
        Token nextTrack=flightMap.getElement(1);
        Token destination=flightMap.getElement(flightMap.length()-1);
        ArrayToken neighborsOfPriorTrack=_neighbors.get(priorTrack);
        for(int i=0;i<neighborsOfPriorTrack.length();i++){
            Token temp=neighborsOfPriorTrack.getElement(i);
            int tempId=((IntToken)temp).intValue();
            if(temp.equals(destination))
            {
                route=i;
                neighborChoosed=true;
		choosedNeighbor=temp;
		baseOnDestination=true;
                break;
            }
            if( tempId!=-1 && !_airportsId.contains(tempId) &&  !temp.equals(currentTrack) && !_inTransit.get(tempId) &&
                    !((BooleanToken)_stormyTracks.get(tempId)).booleanValue()){
                ArrayToken tempNeighbors=_neighbors.get(tempId);
                for(int j=0; j<tempNeighbors.length(); j++)
                   if(tempNeighbors.getElement(j).equals(nextTrack)) {
                       neighborChoosed=true;
                       route=i;
                       choosedNeighbor=temp;
                       break;
                   }
               if(neighborChoosed) 
                   break;
            }//end of outer if
        }//end of for
        
        if(neighborChoosed && !baseOnDestination){
          flightMap=flightMap.update(0, choosedNeighbor);
       }else if(neighborChoosed && baseOnDestination){
	flightMap=flightMap.subarray(flightMap.length()-1);
       }else if(airplane.get("priorTrack").equals(nextTrack)){
           Token nextOfnextTrack=flightMap.getElement(2);
           for(int i=0;i<neighborsOfPriorTrack.length();i++){
               Token temp=neighborsOfPriorTrack.getElement(i);
               if(temp.equals(nextOfnextTrack))
               {
                   route=i;
                   neighborChoosed=true;
                   flightMap=flightMap.subarray(2);
               }
           }
       }
           
       Map<String, Token> map=new TreeMap<String, Token>();
       map.put("flightMap", (Token)flightMap);
       map.put("route", new IntToken(route));
       map.put("delay", new DoubleToken(1.0));
       return map;
    }
    
     /** Handle initializing of an airport
     *  @param airport
     *  @throws IllegalActionException 
     */
    
    public void handleInitializedAirport(Airport airport) throws IllegalActionException{
        // TODO Auto-generated method stub
       
        int airportId=((IntToken)airport.airportId.getToken()).intValue();
//        if(airportId==-1)
//            throw new IllegalActionException("invalid id for airplane");
//		if(_airplanesId.contains(airplaneId))
//            throw new IllegalActionException("duplication in  airplanes id");
//        _airplanesId.add(airplaneId);
        if(airportId==-1)
            throw new IllegalActionException("Invalid id for source airport");
        if(_stormyTracks.containsKey(airportId))
            throw new IllegalActionException("Airport id is same as track id");
        if(!_airportsId.contains(airportId))
            _airportsId.add(airportId);
        
//        if(((ArrayToken)airplane.flightMap.getToken())==null)
//            throw new IllegalActionException("flightMap is empty");    
    }
    /** Handle initializing of a destination airport. This function stores airport id in _airportsId 
     *  @param destinationAirport
     *  @throws IllegalActionException 
     */
    public void handleInitializedDestination( DestinationAirport destinationAirport) throws IllegalActionException {
        // TODO Auto-generated method stub
        int airportId=((IntToken)destinationAirport.airportId.getToken()).intValue();
        if(airportId==-1)
            throw new IllegalActionException("Invalid id for destination airport");
        if(_airportsId.contains(airportId))
            throw new IllegalActionException("Duplication in airports id");
        if(_stormyTracks.containsKey(airportId))
            throw new IllegalActionException("Airport id is same as track id");
        _airportsId.add(airportId);
        
    }
	
	/** Return airplane's color. If the airplane has not color, set a color for that and store it.  
     *  @param id id of the airplane
     *  @throws IllegalActionException 
     */
    public ArrayToken handleAirplaneColor(int id) throws IllegalActionException{
        ArrayToken color = _airplanesColor.get(id);
        
        if (color == null) {
            Token[] colorSpec = new DoubleToken[4];
            colorSpec[0] = new DoubleToken(_random.nextDouble());
            colorSpec[1] = new DoubleToken(_random.nextDouble());
            colorSpec[2] = new DoubleToken(_random.nextDouble());
            colorSpec[3] = new DoubleToken(1.0);
            color = new ArrayToken(colorSpec);
            _airplanesColor.put(id, color);
        }
        
        return color;
    }
    
     //private variables which show situation of tracks 
	private Random _random=new Random();
    
    /**  _stormyTracks stores which track is stormy:first element is id of the track and last is a boolean token*/
    private Map<Integer, Token> _stormyTracks=new TreeMap<>();
    
   /**  _neighbors stores neighbors of each track:first element is id of the track and last is array of its neighbors*/
    private Map<Integer, ArrayToken> _neighbors=new TreeMap<>();
    
   /** _inTransit stores existance of one aircraft in the track: first element is id and last is a boolean */
    private Map<Integer, Boolean> _inTransit=new TreeMap<>();
    
    /** _airportsId stores airports id */
    private ArrayList<Integer> _airportsId= new ArrayList<>();
    
    /** _airplanesId stores airplanes id */
    private ArrayList<Integer> _airplanesId=new ArrayList<>();
	
	/** _airplanesColor stores a color for each airplane*/
    private Map<Integer,ArrayToken> _airplanesColor = new HashMap<Integer,ArrayToken>();
}
