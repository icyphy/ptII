
/* A model of a source airport in air traffic control systems.

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
package ptolemy.domains.atc.lib;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import ptolemy.actor.Director;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.domains.atc.kernel.AbstractATCDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/** This actor receives a record token which shows an airplane decides to fly.
 * Therefore, this actor just sends that to a proper direction based on the neighbors (of the airport)
 * If the destination track (first track in the airplane's flight map is unavailable,
 * then airport try to send it after a period of time. 
 *  @author Maryam Bagheri
*/
public class Airport extends TypedAtomicActor{ 

   public Airport(CompositeEntity container, String name)
           throws IllegalActionException, NameDuplicationException {
       super(container, name);
       
       input = new TypedIOPort(this, "input", true, false);
       input.setTypeEquals(BaseType.RECORD);
               
       Output = new TypedIOPort(this, "Output", false, true);
       Output.setTypeEquals(BaseType.RECORD);
       Output.setMultiport(true);
       
       delay= new Parameter(this, "delay");
       delay.setTypeEquals(BaseType.DOUBLE);
       delay.setExpression("1");
       
       takeOff= new Parameter(this, "takeOff");
       takeOff.setTypeEquals(BaseType.DOUBLE);
       takeOff.setExpression("1");
       
       airportId= new Parameter(this, "airportId");
       airportId.setTypeEquals(BaseType.INT);
       airportId.setExpression("-1");
       
       connectedTracks = new Parameter(this, "connectedTracks");
       connectedTracks.setExpression("{}");
       connectedTracks.setTypeEquals(new ArrayType(BaseType.INT));
       
   }
   
   public TypedIOPort input;
   public TypedIOPort Output;
   public Parameter connectedTracks, delay,airportId,takeOff;
   
   
   @Override
   public void fire() throws IllegalActionException {
       super.fire();
       Time currentTime = _director.getModelTime();
       if (currentTime.equals(_transitExpires) && _inTransit!=null ) {
                   try{
                     //***When airport decides to send out an airplane it must set it's departure time.
                     //For this purpose, we make a new recordtoken
                      double departureTime=currentTime.getDoubleValue()-((DoubleToken)takeOff.getToken()).doubleValue();
                      RecordToken firstAirplane=_airplanes.get(0);
                       Map<String, Token> tempAircraft=new TreeMap<String, Token>();
                       tempAircraft.put("aircraftId", firstAirplane.get("aircraftId"));
                       tempAircraft.put("aircraftSpeed", firstAirplane.get("aircraftSpeed"));
                       tempAircraft.put("flightMap", firstAirplane.get("flightMap"));
                       tempAircraft.put("fuel", firstAirplane.get("fuel"));
                       tempAircraft.put("priorTrack", firstAirplane.get("priorTrack"));
                       tempAircraft.put("arrivalTimeToAirport",firstAirplane.get("arrivalTimeToAirport"));
                       tempAircraft.put("dipartureTimeFromAirport", new DoubleToken(departureTime));
                       _airplanes.set(0, new RecordToken(tempAircraft));
                       int i=findDirection(_airplanes.get(0));
                       Output.send(i, _airplanes.get(0));
                       _airplanes.remove(0);
                       _inTransit=null;
                   } catch(NoRoomException ex){
                       double additionalDelay = ((DoubleToken)delay.getToken()).doubleValue();
                       if (additionalDelay < 0.0) {
                           throw new IllegalActionException(this, "Unable to handle rejection.");
                       }
                       _transitExpires = _transitExpires.add(additionalDelay);
                       _director.fireAt(this, _transitExpires);
                   }
                  
                   if(_inTransit==null &&  _airplanes.size()!=0){
                       _inTransit=_airplanes.get(0);
                       double additionalDelay = ((DoubleToken)takeOff.getToken()).doubleValue();
                       if (additionalDelay < 0.0) {
                           throw new IllegalActionException(this, "Unable to handle rejection.");
                       }
                       _transitExpires = _transitExpires.add(additionalDelay);
                       _director.fireAt(this, _transitExpires);
                   }
               }
           
       if(input.hasToken(0))
       { 
          RecordToken airplane=(RecordToken) input.get(0);
          Map<String, Token> Aircraft=new TreeMap<String, Token>();
          Aircraft.put("aircraftId", airplane.get("aircraftId"));
          Aircraft.put("aircraftSpeed", airplane.get("aircraftSpeed"));
          Aircraft.put("flightMap", airplane.get("flightMap"));
          Aircraft.put("priorTrack", airportId.getToken());
          //new added fields to the airplane packet
          Aircraft.put("fuel", airplane.get("fuel"));
          double arrivalTime=currentTime.getDoubleValue();
          Aircraft.put("arrivalTimeToAirport",new DoubleToken(arrivalTime));
          Aircraft.put("dipartureTimeFromAirport", new DoubleToken(arrivalTime));
          //end of new added...
          _airplanes.add(new RecordToken(Aircraft));
          
          if(_inTransit==null)
          {
              double additionalDelay = ((DoubleToken)takeOff.getToken()).doubleValue();
              if (additionalDelay < 0.0) {
                  throw new IllegalActionException(this, "Delay is negative in airport.");
              }
              _inTransit=_airplanes.get(0);
              _transitExpires = currentTime.add(additionalDelay);
              _director.fireAt(this, _transitExpires);
          }
       }
   }
   
   @Override
   public void initialize() throws IllegalActionException {
       super.initialize();
       _director=getDirector();
      ((AbstractATCDirector)_director).handleInitializedAirport(this);
      _inTransit=null;
       _Tracks=(ArrayToken)connectedTracks.getToken();
       if(_Tracks.length()==0)
           throw new IllegalActionException("there is no connected track to the airport in the airport's parameters ");
       _airplanes=new ArrayList<RecordToken>();
       
   }
   
   private int findDirection(RecordToken airplane) throws IllegalActionException{
       ArrayToken flightMap=(ArrayToken)airplane.get("flightMap");
       boolean finded=false;
       for(int i=0; i<_Tracks.length();i++)
           if(flightMap.getElement(0).equals(_Tracks.getElement(i))){
               finded=true;
               return i;
           }
       if(finded==false)
           throw new IllegalActionException("There is no route from the airport to the first track in flightMap");
       return -1;
   }
   
   private Token _inTransit;
   private Time _transitExpires;
   private Director _director;
   private ArrayToken _Tracks;
   private ArrayList<RecordToken> _airplanes;
}
