/* A model of a Station in train control systems.

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
package ptolemy.domains.tcs.lib;


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
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.domains.tcs.kernel.Rejecting;
import ptolemy.domains.tcs.kernel.TCSDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.vergil.icon.EditorIcon;
import ptolemy.vergil.kernel.attributes.AttributeValueAttribute;
import ptolemy.vergil.kernel.attributes.EllipseAttribute;
import ptolemy.vergil.kernel.attributes.RectangleAttribute;
import ptolemy.vergil.kernel.attributes.ResizablePolygonAttribute;

///////////////////////////////////////////////////////////////////
////AbstractStation

/** This abstract actor models a Station. It receives a train and send
 * it out.  If this station is in junction, it should have address of
 * the next stations in form of "symbolId".  If it has more than one
 * output channel, then routes the train based on moving map of the
 * train.
 *  @author Maryam Bagheri
 *  @version $Id$
 *  @since Ptolemy II 11.0
 */
public class AbstractStation extends StationWriter implements Rejecting{

    /** Create a new actor in the specified container with the specified
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
    public AbstractStation(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);

        output=new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(new RecordType(_labels, _types));
        output.setMultiport(true);

        stationId = new Parameter(this, "stationId");
        stationId.setTypeEquals(BaseType.INT);
        stationId.setExpression("-1");

        settlingTime = new Parameter(this, "settlingTime");
        settlingTime.setTypeEquals(BaseType.DOUBLE);
        settlingTime.setExpression("1");

        lineSymbol = new Parameter(this, "lineSymbol");
        lineSymbol.setTypeEquals(BaseType.STRING);

        inJunction = new Parameter(this,"inJunction");
        inJunction.setTypeEquals(BaseType.BOOLEAN);

        neighbors = new Parameter(this,"neighbors");
        neighbors.setDisplayName("idOfNeighborStationOrJunction");
        neighbors.setExpression("{}");
        neighbors.setTypeEquals(new ArrayType(BaseType.STRING));

        broken =new Parameter(this, "broken");
        broken.setTypeEquals(BaseType.BOOLEAN);

        EditorIcon node_icon = new EditorIcon(this, "_icon");


      //This part of icon is used to show the broken zone.
        _circle = new EllipseAttribute(node_icon, "_circleShapOfStation");
        _circle.centered.setToken("true");
        _circle.width.setToken("40");
        _circle.height.setToken("40");
        _circle.fillColor.setToken("{0.0, 0.0, 0.0, 0.0}");
        _circle.lineColor.setToken("{0.0, 0.0, 0.0, 0.0}");

        // This part of icon shows border of the station.
        _border=new ResizablePolygonAttribute(node_icon, "_borderShape");
        _border.centered.setToken("true");
        _border.width.setToken("30");
        _border.height.setToken("40");
        _border.lineColor.setToken("{0.0, 0.0, 0.0, 1.0}");
        _border.fillColor.setToken("{1.0,1.0,1.0,1.0}");
        _border.vertices.setExpression("{46.0,10.0,45.0,21.0,38.0,39.0,-38.0,39.0," +
                        "-45.0,21.0,-46.0,10.0,-46.0,-10.0,-45.0,-21.0,-38.0,-39.0,39.0" +
                        ",-39.0,45.0,-21.0,46.0,-10.0,46.0,10.0,33.0,10.0,33.0,-10.0,32.0," +
                        "-21.0,25.0,-31.0,-25.0,-31.0,-32.0,-21.0,-33.0,-10.0,-33.0,10.0," +
                        "-32.0,21.0,-25.0,31.0,25.0,31.0,32.0,21.0,33.0,10.0}");

       // This part of the icon is used as an inner box for showing color of train.
        _trainColor= new RectangleAttribute(node_icon, "_trainColor");
        _trainColor.centered.setToken("true");
        _trainColor.width.setToken("20");
        _trainColor.height.setToken("20");
        _trainColor.lineColor.setToken("{0.0, 0.0, 0.0, 0.0}");
        _trainColor.fillColor.setToken("{0.0,0.0,0.0,0.0}");

     // This part of the icon shows id of the station.
        _valueId=new AttributeValueAttribute(node_icon, "_IdInStation");
        _valueId.textSize.setToken("15");
        Location l1 = new Location(_valueId,"_location");
        l1.setLocation(new double[]{-10.0, -2.0});
        _valueId.textColor.setToken("{0.0, 0.0, 0.0, 1.0}");
        _valueId.attributeName.setExpression("stationId");

     // This part of the icon shows symbol of the station.
        _valueSymbol=new AttributeValueAttribute(node_icon, "_SymbolInStation");
        _valueSymbol.textSize.setToken("15");
        Location l2 = new Location(_valueSymbol,"_location");
        l2.setLocation(new double[]{-12.0, -16.0});
        _valueSymbol.textColor.setToken("{0.0, 0.0, 0.0, 1.0}");
        _valueSymbol.attributeName.setExpression("lineSymbol");


    }

    ///////////////////////////////////////////////////////////////////
    ////                       ports and parameters                ////

    /** The input port, which is a multiport. */
    public TypedIOPort input;

    /** The output port, which is a multiport consisting of RecordTokens. */
    public TypedIOPort output;

    /** The station ID.  The initial value is an integer with value -1. */
    public Parameter stationId;


    /** The settling time.  The initial value is an integer with
     *  the value 1.
     */
    public Parameter settlingTime;

    /** The line symbol, which is a string. */
    public Parameter lineSymbol;

    /** True if this is an input junction. */
    public Parameter inJunction;

    /** True if this station is broken. */
    public Parameter broken;

    /** The neighbors of this station.  The display name is
     *  "idOfNeighborStationOrJunction".  The initial value of this
     *  parameter is a string consisting of the open curly bracket
     *  followed by the close curly bracket: "{}"
     */
    public Parameter neighbors;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    @Override
    public boolean reject(Token token, IOPort port) {
        boolean unavailable=(_inTransit!=null || ((BooleanToken)_isBroken).booleanValue());
        if (unavailable==true)
            return true;
        else {
            if (_called==false) {
                _called=true;
                return unavailable;
            }
            else
                return true;
        }
    }

    /**This method handles changing in the broken and symbol parameter of the station.
     * Symbol is the symbol of the line.
     */
    @Override
    public void attributeChanged(Attribute attribute) throws IllegalActionException {
        Director director=getDirector();
        if (attribute==lineSymbol && lineSymbol.getToken()!=null) {
            _symbol=((StringToken)lineSymbol.getToken()).stringValue();
            if (_symbol.length()>1)
                throw new IllegalActionException("Inappropriate line symbol");
            ArrayToken color=((TCSDirector)director).getColor(_symbol);
            _border.fillColor.setToken(color);

        }
        else if (attribute == broken) {
             if (broken.getToken()!=null) {
                _isBroken=broken.getToken();
                //Change color of the storm zone.
                if (((BooleanToken)_isBroken).booleanValue()==true)
                    _circle.fillColor.setToken("{1.0,0.2,0.2,1.0}");
                else
                    _circle.fillColor.setToken("{0.0, 0.0, 0.0, 0.0}");
                //
                ((TCSDirector)director).handleStationAttributeChanged(this);
             }
        }
        else {
            super.attributeChanged(attribute);
        }
    }

    @Override
    public void declareDelayDependency() throws IllegalActionException {
        _declareDelayDependency(input, output, 0.0);
    }

    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        Director director=getDirector();
        Time currentTime = director.getModelTime();
        if (currentTime.equals(_transitExpires) && _inTransit != null) {
            try {
                // Station has more than one output channel and output channel of the train has not been determined.
                if (output.getWidth()>1 && _outRoute==-1)
                {
                    Map<String, Token> temp=new TreeMap<>();
                    temp=((TCSDirector)_director).routing(_neighbors,_inTransit);
                    _outRoute=((IntToken)temp.get("outputChannel")).intValue();
                    if (_outRoute>=output.getWidth())
                        throw new IllegalActionException("Output port has not this channel "+_outRoute);
                    _inTransit=temp.get("train");
                }
                // Station has one output channel.
                else if (output.getWidth()==1 || output.getWidth()==0)
                {
                    _outRoute=0;
                }

                 output.send(_outRoute, _inTransit);
            } catch (NoRoomException ex) {
                // Train has been rejected by the next track.
                double additionalDelay = ((TCSDirector)_director).handleRejectionWithDelayStation(this);
                if (additionalDelay < 0.0)
                {
                    throw new IllegalActionException(this, "Unable to handle rejection.");
                }
                _transitExpires = _transitExpires.add(additionalDelay);
                _director.fireAt(this, _transitExpires);
                return;
            }
            //Train has been sent successfully.
            _inTransit = null;
            _outRoute=-1;
            _called=false;
            _setIcon(-1);
            return;
        }
        for (int i=0;i<input.getWidth();i++) {
            if (input.hasNewToken(i))
            {
                _inTransit=input.get(i);
                int id=((IntToken)(((RecordToken)_inTransit).get("trainId"))).intValue();
                _setIcon(id);
                double time=((DoubleToken)settlingTime.getToken()).doubleValue();
                double movingTime=((TCSDirector)_director).movingTimeOfTrain(_inTransit,_id);
                if (movingTime<=0.0)
                    throw new IllegalActionException("Minstake in calculating moving time of Train");

                 _transitExpires = currentTime.add(time+movingTime);
                 _director.fireAt(this, _transitExpires);
            }
        }

    }


    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _director=getDirector();
        _id=stationId.getToken();
        ((TCSDirector)_director).handleInitializedStation(this);
        _inTransit=null;
        _called=false;
        _setIcon(-1);
        _isBroken=broken.getToken();
        if (_isBroken==null)
            _isBroken=(Token)(new BooleanToken(false));
        _neighbors=(ArrayToken)neighbors.getToken();
        _outRoute=-1;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Change the color of the inner box in station to show the moving train.
     *  @param id The train ID or -1 to indicate no train.
     *  @exception IllegalActionException
     */
    protected void _setIcon(int id) throws IllegalActionException {
        ArrayToken color = _noTrainColor;
            if (id > -1)
            {
                Director _director=getDirector();
                color = ((TCSDirector)_director).handleTrainColor(id);
                if (color==null)
                    throw new IllegalActionException("Color for the train "+id+" has not been set");
            }
        _trainColor.fillColor.setToken(color);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private ResizablePolygonAttribute _border;
    private EllipseAttribute _circle;
    private Boolean _called;
    private Director _director;
    private Token _inTransit;
    private Token _isBroken;
    private Token _id;
    private String[] _labels={"trainId","trainSymbol","movingMap","trainSpeed","fuel","arrivalTimeToStation","dipartureTimeFromStation"};
    private ArrayToken _noTrainColor = new ArrayToken("{0.0, 0.0, 0.0, 0.0}");
    private ArrayToken _neighbors;
    private int _outRoute;
    private String _symbol;
    private Time _transitExpires;
    private Type[] _types={BaseType.INT,BaseType.STRING,new ArrayType(BaseType.STRING), BaseType.INT,BaseType.DOUBLE,BaseType.DOUBLE,BaseType.DOUBLE};
    private RectangleAttribute _trainColor;
    private AttributeValueAttribute _valueSymbol;
    private AttributeValueAttribute _valueId;
}
