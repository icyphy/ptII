/* A model of a destination airport in air traffic control systems.
 
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



import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.ArrayToken;
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
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.vergil.icon.EditorIcon;
import ptolemy.vergil.kernel.attributes.RectangleAttribute;
import ptolemy.vergil.kernel.attributes.ResizablePolygonAttribute;

/** This actor models a destination airport. It just receives an airplane.
 *  @author Maryam Bagheri
 */
public class DestinationAirport extends TypedAtomicActor implements Rejecting{

    public DestinationAirport(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);
        
        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(new RecordType(_lables, _types));
        
        airportId= new Parameter(this, "airportId");
        airportId.setTypeEquals(BaseType.INT);
        airportId.setExpression("-1");
        
        delay= new Parameter(this, "delay");
        delay.setTypeEquals(BaseType.DOUBLE);
        delay.setExpression("1");
		
		EditorIcon node_icon = new EditorIcon(this, "_icon");
        
        //rectangle
        _rectangle=new RectangleAttribute(node_icon, "_rectangleShape");
        _rectangle.centered.setToken("true");
        _rectangle.width.setToken("60");
        _rectangle.height.setToken("50");
        _rectangle.rounding.setToken("10");
        _rectangle.lineColor.setToken("{0.0, 0.0, 0.0, 1.0}");
        _rectangle.fillColor.setToken("{0.8,0.8,1.0,1.0}");
        
        //inner triangle of the icon
        _shape = new ResizablePolygonAttribute(node_icon, "_triangleShape");
        _shape.centered.setToken("true");
        _shape.width.setToken("40");
        _shape.height.setToken("40");
        _shape.vertices.setExpression("{0.0,1.0,0.0,-1.0,2.0,0.0}");
        _shape.fillColor.setToken("{1.0, 1.0, 1.0, 1.0}");
        
    }
    
    public TypedIOPort input, output;
    public Parameter airportId,delay;
    
    
    @Override
    public boolean reject(Token token, IOPort port) {
        if(_inTransit != null)
            return true;
        
        if(_called==false){
                _called=true;
                return (_inTransit != null);
        }
        else{
                return true;
        }
    }
    
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        Director director=getDirector();
        Time currentTime = director.getModelTime();
        if (currentTime.equals(_transitExpires) && _inTransit != null) {
                output.send(0, _inTransit);
		//Set icon to white color
                _setIcon(-1);
				
                _inTransit = null;
                _called=false;
                return;
        }
       
        for(int i=0; i< input.getWidth();i++)
            if(input.hasNewToken(i)){
                _inTransit=input.get(i);
		//Set icon to color of the airplane
                int id=((IntToken)((RecordToken)_inTransit).get("aircraftId")).intValue();
                _setIcon(id);
                //
                _transitExpires = currentTime.add(((DoubleToken)delay.getToken()).doubleValue());
                director.fireAt(this, _transitExpires);
            }
    }
    
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        Director _director=getDirector();
        ((AbstractATCDirector)_director).handleInitializedDestination(this);
        _inTransit=null;
        _called=false;
		_setIcon(-1);
    }
    
	/** Set the visual indication of the icon for the specified ID.
     *  @param id The aircraft ID or -1 to indicate no aircraft.
     *  @throws IllegalActionException
     */
    protected void _setIcon(int id) throws IllegalActionException {
        ArrayToken color = _noAircraftColor;
            if (id > -1) {
                Director _director=getDirector();
                color = ((AbstractATCDirector)_director).handleAirplaneColor(id);
                if(color==null)
                    throw new IllegalActionException("Color for the airplane "+id+" has not been set");
            } 
        _shape.fillColor.setToken(color);
    }
	
    //to change color of the icon
    private ResizablePolygonAttribute _shape;
    private RectangleAttribute _rectangle;
    private DoubleToken _one = new DoubleToken(1.0);
    private Token[] _white = {_one, _one, _one, _one};
    private ArrayToken _noAircraftColor = new ArrayToken(_white);
    //
	
    private Token _inTransit;
    private Time _transitExpires;
    private boolean _called;
    private String[] _lables={"aircraftId","aircraftSpeed","flightMap","priorTrack","fuel","arrivalTimeToAirport","dipartureTimeFromAirport"};
    private Type[] _types={BaseType.INT,BaseType.INT,new ArrayType(BaseType.INT),BaseType.INT,BaseType.DOUBLE,BaseType.DOUBLE,BaseType.DOUBLE};

   
}
