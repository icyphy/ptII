
/* A model of a source station in train control system.

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
package ptolemy.domains.tcs.lib;

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
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.tcs.kernel.TCSDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.vergil.icon.EditorIcon;
import ptolemy.vergil.kernel.attributes.ResizablePolygonAttribute;
import ptolemy.vergil.kernel.attributes.AttributeValueAttribute;

/** This abstract actor receives a record token which shows the train decides to move.
 * Therefore, this actor just sends the train out. At each (take off) time, just one train can move. 
 * If the destination track  is unavailable,
 * then station try to send it after a period of time. 
 *  @author Maryam Bagheri
*/
public class AbstractSourceStation extends TypedAtomicActor{ 

   public AbstractSourceStation(CompositeEntity container, String name)
           throws IllegalActionException, NameDuplicationException {
       super(container, name);
       
       input = new TypedIOPort(this, "input", true, false);
       input.setTypeEquals(BaseType.RECORD);
               
       output = new TypedIOPort(this, "Output", false, true);
       output.setTypeEquals(BaseType.RECORD);
       
       delay= new Parameter(this, "delay");
       delay.setTypeEquals(BaseType.DOUBLE);
       delay.setExpression("1");
       
       takeOff= new Parameter(this, "takeOff");
       takeOff.setTypeEquals(BaseType.DOUBLE);
       takeOff.setExpression("1");
       
       stationId= new Parameter(this, "stationId");
       stationId.setTypeEquals(BaseType.INT);
       stationId.setExpression("-1");
       
       lineSymbol= new Parameter(this, "lineSymbol");
       lineSymbol.setTypeEquals(BaseType.STRING);
       
       
       EditorIcon node_icon = new EditorIcon(this, "_icon");
       
       //border
       _sourceStationBorder=new ResizablePolygonAttribute(node_icon, "_stationBorder");
       _sourceStationBorder.centered.setToken("true");
       _sourceStationBorder.width.setToken("30");
       _sourceStationBorder.height.setToken("40");
       _sourceStationBorder.lineColor.setToken("{0.0, 0.0, 0.0, 1.0}");
       _sourceStationBorder.fillColor.setToken("{1.0,1.0,1.0,1.0}");
       _sourceStationBorder.vertices.setExpression("{46.0,10.0,45.0,21.0,38.0,39.0,-38.0,39.0," +
                        "-45.0,21.0,-46.0,10.0,-46.0,-10.0,-45.0,-21.0,-38.0,-39.0,39.0" +
                        ",-39.0,45.0,-21.0,46.0,-10.0,46.0,10.0,33.0,10.0,33.0,-10.0,32.0," +
                        "-21.0,25.0,-31.0,-25.0,-31.0,-32.0,-21.0,-33.0,-10.0,-33.0,10.0," +
                        "-32.0,21.0,-25.0,31.0,25.0,31.0,32.0,21.0,33.0,10.0}");
		
		//show symbol of the station
       _valueSymbol=new AttributeValueAttribute(node_icon, "_SymbolInSourceStation");
       _valueSymbol.textSize.setToken("15");
       _valueSymbol.textColor.setToken("{0.0, 0.0, 0.0, 1.0}");
       _valueSymbol.anchor.setToken("south");
       _valueSymbol.attributeName.setExpression("lineSymbol");		
		 //show id of the station
       _valueId=new AttributeValueAttribute(node_icon, "_IdInSourceStation");
       _valueId.textSize.setToken("15");
       _valueId.textColor.setToken("{0.0, 0.0, 0.0, 1.0}");
       _valueId.anchor.setToken("north");
       _valueId.attributeName.setExpression("stationId");
       
       
   }
   
   public TypedIOPort input;
   public TypedIOPort output;
   public Parameter delay,stationId,takeOff,lineSymbol;
   
   
   @Override
   public void attributeChanged(Attribute attribute) throws IllegalActionException {
       Director director=getDirector();
       if(attribute==lineSymbol && lineSymbol.getToken()!=null){
           _symbol=((StringToken)lineSymbol.getToken()).stringValue();
           if(_symbol.length()>1)
               throw new IllegalActionException("Inappropriate line symbol");
           ArrayToken color=((TCSDirector)director).getColor(_symbol);
          
           _sourceStationBorder.fillColor.setToken(color); 
       }
       else {
           super.attributeChanged(attribute);
       }
   }
   
   @Override
   public void fire() throws IllegalActionException {
       super.fire();
       Time currentTime = _director.getModelTime();
       if (currentTime.equals(_transitExpires) && _inTransit!=null ) {
                   try{
                     //***When station decides to send out an train it must set it's departure time.
                     //For this purpose, we make a new recordtoken
                      double departureTime=currentTime.getDoubleValue()-((DoubleToken)takeOff.getToken()).doubleValue();
                      RecordToken firstTrain=_trains.get(0);
                       Map<String, Token> tempTrain=new TreeMap<String, Token>();
                       tempTrain.put("trainId", firstTrain.get("trainId"));
                       tempTrain.put("trainSymbol",firstTrain.get("trainSymbol"));
                       tempTrain.put("trainSpeed", firstTrain.get("trainSpeed"));
                       tempTrain.put("movingMap", firstTrain.get("movingMap"));
                       tempTrain.put("fuel", firstTrain.get("fuel"));
                       tempTrain.put("arrivalTimeToStation",firstTrain.get("arrivalTimeToStation"));
                       tempTrain.put("dipartureTimeFromStation", new DoubleToken(departureTime));
                       _trains.set(0, new RecordToken(tempTrain));
                       output.send(0, _trains.get(0));
                       _trains.remove(0);
                       _inTransit=null;
                   } catch(NoRoomException ex){
                       double additionalDelay = ((DoubleToken)delay.getToken()).doubleValue();
                       if (additionalDelay < 0.0) {
                           throw new IllegalActionException(this, "Unable to handle rejection.");
                       }
                       _transitExpires = _transitExpires.add(additionalDelay);
                       _director.fireAt(this, _transitExpires);
                       return;
                   }
                  
                   if(_inTransit==null &&  _trains.size()!=0){
                       _inTransit=_trains.get(0);
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
          RecordToken train=(RecordToken) input.get(0);
          if(train.get("trainSymbol").equals(lineSymbol.getToken())){
              Map<String, Token> tempTrain=new TreeMap<String, Token>();
              tempTrain.put("trainId", train.get("trainId"));
              tempTrain.put("trainSymbol",train.get("trainSymbol"));
              tempTrain.put("trainSpeed", train.get("trainSpeed"));
              tempTrain.put("movingMap", train.get("movingMap"));
              tempTrain.put("fuel", train.get("fuel"));
              double arrivalTime=currentTime.getDoubleValue();
              tempTrain.put("arrivalTimeToStation",new DoubleToken(arrivalTime));
              tempTrain.put("dipartureTimeFromStation", new DoubleToken(arrivalTime));
              _trains.add(new RecordToken(tempTrain));
              
              if(_inTransit==null)
              {
                  double additionalDelay = ((DoubleToken)takeOff.getToken()).doubleValue();
                  if (additionalDelay < 0.0) {
                      throw new IllegalActionException(this, "Delay is negative in sourceStation.");
                  }
                  _inTransit=_trains.get(0);
                  _transitExpires = currentTime.add(additionalDelay);
                  _director.fireAt(this, _transitExpires);
              }
          }
       }
   }
   
   @Override
   public void initialize() throws IllegalActionException {
       super.initialize();
       _director=getDirector();
      ((TCSDirector)_director).handleInitializedSourceStation(this);
      _inTransit=null;
       _trains=new ArrayList<RecordToken>();
       
   }
   

	private AttributeValueAttribute _valueSymbol;
   private AttributeValueAttribute _valueId;   
   private String _symbol;
   private ResizablePolygonAttribute _sourceStationBorder;
   private Token _inTransit;
   private Time _transitExpires;
   private Director _director;
   private ArrayList<RecordToken> _trains;
}
