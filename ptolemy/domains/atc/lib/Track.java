/* A model of a track in air traffic control systems.
 
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


import java.util.Map;
import java.util.TreeMap;

import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.domains.atc.kernel.AbstractATCDirector;
import ptolemy.domains.atc.kernel.Rejecting;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.vergil.icon.EditorIcon;
import ptolemy.vergil.kernel.attributes.ResizablePolygonAttribute;
import ptolemy.vergil.kernel.attributes.EllipseAttribute;

/** A model of a track in air traffic control systems.
 *  This track can have no more than one aircraft in transit.
 *  If there is one in transit, then it rejects all inputs.
 *  @author Maryam Bagheri
 */
public class Track extends  TrackWriter implements Rejecting{

    public Track(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
       
        
        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);
                
        northOutput = new TypedIOPort(this, "northOutput", false, true);
        northOutput.setTypeEquals(new RecordType(_lables, _types));
        StringAttribute cardinality = new StringAttribute(northOutput, "_cardinal");
        cardinality.setExpression("NORTH");
                

        eastOutput=new TypedIOPort(this, "eastOutput", false, true); 
        eastOutput.setTypeEquals(new RecordType(_lables, _types));
        cardinality = new StringAttribute(eastOutput, "_cardinal");
        cardinality.setExpression("EAST");
        
                
        southOutput = new TypedIOPort(this, "southOutput", false, true);
        southOutput.setTypeEquals(new RecordType(_lables, _types));
        cardinality = new StringAttribute(southOutput, "_cardinal");
        cardinality.setExpression("SOUTH");
    
        
        trackId= new Parameter(this, "trackId");
        trackId.setTypeEquals(BaseType.INT);
        trackId.setExpression("-1");
        
        neighbors = new Parameter(this, "neighbors{North,East,South");
        neighbors.setExpression("{-1,-1,-1}");
        neighbors.setTypeEquals(new ArrayType(BaseType.INT));
        
        stormy=new Parameter(this, "stormy");
        stormy.setTypeEquals(BaseType.BOOLEAN);
        
        _attachText("_iconDescription", "<svg> <path d=\"M 194.67321,2.8421709e-14 L 70.641958,53.625 "
                + "C 60.259688,46.70393 36.441378,32.34961 31.736508,30.17602 C -7.7035221,11.95523 "
                + "-5.2088921,44.90709 11.387258,54.78122 C 15.926428,57.48187 39.110778,71.95945 "
                + "54.860708,81.15624 L 72.766958,215.09374 L 94.985708,228.24999 L 106.51696,107.31249 "
                + "L 178.04821,143.99999 L 181.89196,183.21874 L 196.42321,191.84374 L 207.51696,149.43749 "
                + "L 207.64196,149.49999 L 238.45446,117.96874 L 223.57946,109.96874 L 187.95446,126.87499 "
                + "L 119.67321,84.43749 L 217.36071,12.25 L 194.67321,2.8421709e-14 z\" "
                + "style=\"fill:#000000;fill-opacity:1;fill-rule:evenodd;stroke:none;stroke-width:1px;stroke-linecap:butt;"
                + "stroke-linejoin:miter;stroke-opacity:1\" id=\"path5724\"/></svg>");

        // Create an icon for this sensor node.
        EditorIcon node_icon = new EditorIcon(this, "_icon");

	_circle = new EllipseAttribute(node_icon, "_circleShap");
        _circle.centered.setToken("true");
        _circle.width.setToken("40");
        _circle.height.setToken("40");
        _circle.fillColor.setToken("{0.0, 0.0, 0.0, 0.0}");
        _circle.lineColor.setToken("{0.0, 0.0, 0.0, 0.0}");
		
        _shape = new ResizablePolygonAttribute(node_icon, "_trackShape");
        _shape.centered.setToken("true");
        _shape.width.setToken("40");
        _shape.height.setToken("40");
        _shape.vertices.setExpression("{194.67321,2.8421709e-14, 70.641958,53.625, "
                + "60.259688,46.70393, 36.441378,32.34961, 31.736508,30.17602, -7.7035221,11.95523, "
                + "-5.2088921,44.90709, 11.387258,54.78122, 15.926428,57.48187, 39.110778,71.95945, "
                + "54.860708,81.15624, 72.766958,215.09374, 94.985708,228.24999, 106.51696,107.31249, "
                + "178.04821,143.99999, 181.89196,183.21874, 196.42321,191.84374, 207.51696,149.43749, "
                + "207.64196,149.49999, 238.45446,117.96874, 223.57946,109.96874, 187.95446,126.87499, "
                + "119.67321,84.43749, 217.36071,12.25, 194.67321,2.8421709e-14}");
        _shape.fillColor.setToken("{1.0, 1.0, 1.0, 1.0}");
    }
    
    public TypedIOPort input;
    public TypedIOPort northOutput, eastOutput, southOutput;//westOutputToPrior;
    public Parameter trackId, neighbors, stormy;
    
    @Override
    public boolean reject(Token token, IOPort port) {
        boolean unAvailable=(_inTransit != null || ((BooleanToken)_isStormy).booleanValue());
        if(unAvailable==true)
            return true;
        
        if(_called==false){
                _called=true;
                return (_inTransit != null || ((BooleanToken)_isStormy).booleanValue() );
        }
        else{
                return true;
        }
    }
    
    @Override
    public void attributeChanged(Attribute attribute) throws IllegalActionException {
        Director director=getDirector();
        if (attribute == stormy) {
             if(stormy.getToken()!=null){
                _isStormy=stormy.getToken();
		//change color of the storm zone
                if(((BooleanToken)_isStormy).booleanValue()==true)
                    _circle.fillColor.setToken("{1.0,0.2,0.2,1.0}");
                else
                    _circle.fillColor.setToken("{0.0, 0.0, 0.0, 0.0}");
                //
                ((AbstractATCDirector)director).handleTrackAttributeChanged(this);
            }
        }else {
            super.attributeChanged(attribute);
        }
    }
    
    @Override
 public void declareDelayDependency() throws IllegalActionException {
     _declareDelayDependency(input, northOutput, 0.0);
     _declareDelayDependency(input, eastOutput, 0.0);
     _declareDelayDependency(input, southOutput, 0.0);         
 }
    
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        Time currentTime = _director.getModelTime();
        if (currentTime.equals(_transitExpires) && _inTransit != null) {
            ////**************write to file
            _valuesForFile[2]=new DoubleToken(currentTime.getDoubleValue());
            _valuesForFile[4]=new DoubleToken(_delayOfEachAirplanes);
            _writeToFile=new RecordToken(_lablesOfFile,_valuesForFile);
            _writingToFile((Token)_writeToFile);
            ////**************
            try{
                if(_OutRoute==0) {
                    northOutput.send(0, _inTransit);
                } else if(_OutRoute==1) {
                    eastOutput.send(0, _inTransit);
                } else {
                    southOutput.send(0, _inTransit);
                }
                _setIcon(-1);
            }
            catch (NoRoomException ex){
             // Token rejected by the destination.
                if (!(_director instanceof AbstractATCDirector)) {
                    throw new IllegalActionException(this, "Track must be used with an ATCDirector.");
                }
                Map<String, Token> temp=new TreeMap<>();
                temp=((AbstractATCDirector)_director).rerouteUnacceptedAircraft(_inTransit);
                if(((IntToken)temp.get("route")).intValue()==-1)
                { // Stay in track
                    double additionalDelay = ((AbstractATCDirector)_director).handleRejectionWithDelay(this);
                    if (additionalDelay < 0.0) {
                        throw new IllegalActionException(this, "Unable to handle rejection.");
                    }
                    _delayOfEachAirplanes+=additionalDelay;
                    _transitExpires = _transitExpires.add(additionalDelay);
                    _director.fireAt(this, _transitExpires);
                }
                else
                {// Send airplane through another route
                    Map<String, Token> newAircraft=new TreeMap<String, Token>();
                    newAircraft.put("aircraftId",( (RecordToken)_inTransit).get("aircraftId"));
					//remove the following setIcon: because airplane is flying in this track until reaches to another
                    //_setIcon(-1);
                    newAircraft.put("aircraftSpeed", ((RecordToken)_inTransit).get("aircraftSpeed"));
                    newAircraft.put("flightMap", (Token)temp.get("flightMap"));
                    newAircraft.put("priorTrack", ((RecordToken)_inTransit).get("priorTrack"));
                    //***New added fields
                    newAircraft.put("fuel",((RecordToken)_inTransit).get("fuel"));
                    newAircraft.put("arrivalTimeToAirport", ((RecordToken)_inTransit).get("arrivalTimeToAirport"));
                    newAircraft.put("dipartureTimeFromAirport", ((RecordToken)_inTransit).get("dipartureTimeFromAirport"));
                    //end of new...
                    Token transmitedAircraft=(Token) new RecordToken(newAircraft);
                    _inTransit=transmitedAircraft;
                    _transitExpires=_transitExpires.add(((DoubleToken)temp.get("delay")).doubleValue());
                    _delayOfEachAirplanes+=((DoubleToken)temp.get("delay")).doubleValue();
                    _OutRoute=((IntToken)temp.get("route")).intValue();
                    _director.fireAt(this, _transitExpires);
                }//end of else
               return; 
            }
            // Token has been sent successfully
            _inTransit = null;
            _called=false;
            ((AbstractATCDirector)_director).setInTransitStatusOfTrack(_id, false);
        }
        // Handle any input that have been accepted.
        for(int i=0;i<input.getWidth();i++)
            if(input.hasNewToken(i))
            {
                // This if is for chacking safety. Instead of throwing exception we can write a record to the file.
                if(_inTransit!=null)
                {
                    throw new IllegalActionException("two airplanes in one track");
                }
                //
                Token inputAircraft=input.get(i);
                _counter++;
                _delayOfEachAirplanes=0.0;
                ///////////////////////////////////*************write to file
                _valuesForFile[0]=_id;
                _valuesForFile[1]=((RecordToken)inputAircraft).get("aircraftId");
                
                _setIcon(((IntToken)_valuesForFile[1]).intValue());
                
                _valuesForFile[2]=new DoubleToken(currentTime.getDoubleValue());
                _valuesForFile[3]=new IntToken(_counter);
                _valuesForFile[4]=new DoubleToken(_delayOfEachAirplanes);
                _writeToFile=new RecordToken(_lablesOfFile,_valuesForFile);
                _writingToFile((Token)_writeToFile);
                ////////////////////////////////////************
                ((AbstractATCDirector)_director).setInTransitStatusOfTrack(_id, true);
                RecordToken aircraftWithInformation=((AbstractATCDirector)_director).routing(inputAircraft, _id);
                _transitExpires = currentTime.add(((DoubleToken)aircraftWithInformation.get("delay")).doubleValue());
                _OutRoute=((IntToken)aircraftWithInformation.get("route")).intValue();
                _delayOfEachAirplanes+=((DoubleToken)aircraftWithInformation.get("delay")).doubleValue();
                
              //creating a new aircraft to sent to output from aircraftWighInformation
                Map<String, Token> newAircraft=new TreeMap<String, Token>();
                newAircraft.put("aircraftId", aircraftWithInformation.get("aircraftId"));
                newAircraft.put("aircraftSpeed", aircraftWithInformation.get("aircraftSpeed"));
                newAircraft.put("flightMap", aircraftWithInformation.get("flightMap"));
                newAircraft.put("priorTrack", aircraftWithInformation.get("priorTrack"));
                //***New added fields
                newAircraft.put("fuel",aircraftWithInformation.get("fuel"));
                newAircraft.put("arrivalTimeToAirport", aircraftWithInformation.get("arrivalTimeToAirport"));
                newAircraft.put("dipartureTimeFromAirport", aircraftWithInformation.get("dipartureTimeFromAirport"));
                //end of new...
                _inTransit=(Token) (new RecordToken(newAircraft));
                _director.fireAt(this, _transitExpires);

            }
  
    }

    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _director=getDirector();
        _inTransit = null;
        _OutRoute=-1;
        _id=trackId.getToken();
        _isStormy=stormy.getToken();
       ((AbstractATCDirector)_director).handleInitializedTrack(this);
       _called=false;
       _counter=0;
       _delayOfEachAirplanes=0.0;
       _setIcon(-1);
    }
    
    /** Set the visual indication of the icon for the specified ID.
     *  @param id The aircraft ID or -1 to indicate no aircraft.
     *  @throws IllegalActionException
     */
    protected void _setIcon(int id) throws IllegalActionException {
        ArrayToken color = _noAircraftColor;
        if(id>-1){
            color = ((AbstractATCDirector)_director).handleAirplaneColor(id);
        }
        _shape.fillColor.setToken(color);
    }
    
    private EllipseAttribute _circle;
    private ResizablePolygonAttribute _shape;
    private DoubleToken _one = new DoubleToken(1.0);
    private Token[] _white = {_one, _one, _one, _one};
    private ArrayToken _noAircraftColor = new ArrayToken(_white);
   
    //** New added variables to measure some parameters
    private int _counter;
    private double _delayOfEachAirplanes;
    //
    private Token _inTransit;
    private Time _transitExpires;
    private Token _isStormy;
    private int _OutRoute;
    private Token _id;
    private Director _director;
    private boolean _called;
    private RecordToken _writeToFile;
    private String[] _lablesOfFile={"trackId","aircraftId","currentTime","trackLoad","delayOfaircraft"};
    private Token[] _valuesForFile={null,null,null,null,null};
    private String[] _lables={"aircraftId","aircraftSpeed","flightMap","priorTrack","fuel","arrivalTimeToAirport","dipartureTimeFromAirport"};
    private Type[] _types={BaseType.INT,BaseType.INT,new ArrayType(BaseType.INT),BaseType.INT,BaseType.DOUBLE,BaseType.DOUBLE,BaseType.DOUBLE};
    
}
