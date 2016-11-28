/* A model of a track in Train control systems.

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



import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.TypedAtomicActor;
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
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.vergil.icon.EditorIcon;
import ptolemy.vergil.kernel.attributes.EllipseAttribute;
import ptolemy.vergil.kernel.attributes.RectangleAttribute;
import ptolemy.vergil.kernel.attributes.ResizablePolygonAttribute;

///////////////////////////////////////////////////////////////////
////AbstractTrack

/** A model of a track in Train  control systems.
 *  This track can have no more than one Train in transit.
 *  If there is one in transit, then it rejects all inputs.
 *  @author Maryam Bagheri
 *  @version $Id$
 *  @since Ptolemy II 11.0
 */
public class AbstractTrack extends  TypedAtomicActor implements Rejecting {

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
    public AbstractTrack(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        input = new TypedIOPort(this, "input", true, false);

        output =new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(new RecordType(_labels, _types));

        trackId = new Parameter(this, "trackId");
        trackId.setTypeEquals(BaseType.INT);
        trackId.setExpression("-1");

        lineSymbol = new Parameter(this, "lineSymbol");
        lineSymbol.setTypeEquals(BaseType.STRING);

        broken = new Parameter(this, "broken");
        broken.setTypeEquals(BaseType.BOOLEAN);

        // Create an icon for this Track node.
        EditorIcon node_icon = new EditorIcon(this, "_icon");

        // This part of icon is used to show the broken zone.
        _circle = new EllipseAttribute(node_icon, "_circleShap");
        _circle.centered.setToken("true");
        _circle.width.setToken("40");
        _circle.height.setToken("40");
        _circle.fillColor.setToken("{0.0, 0.0, 0.0, 0.0}");
        _circle.lineColor.setToken("{0.0, 0.0, 0.0, 0.0}");

        //This part of icon shows shape of the train.
        _shape = new ResizablePolygonAttribute(node_icon, "_trainShape");
        _shape.centered.setToken("true");
        _shape.width.setToken("50");
        _shape.height.setToken("30");
        _shape.fillColor.setToken("{0.0, 0.0, 0.0, 0.0}");
        _shape.lineColor.setToken("{0.0, 0.0, 0.0, 0.0}");

        //This part of  icon is used to show the shape of the track.
        _rectangle=new RectangleAttribute(node_icon, "_trackShape");
        _rectangle.centered.setToken("true");
        _rectangle.width.setToken("30");
        _rectangle.height.setToken("10");
        _rectangle.fillColor.setToken("{1.0, 1.0, 1.0, 1.0}");

    }

    ///////////////////////////////////////////////////////////////////
    ////                       ports and parameters                ////

    /** The input port. */
    public TypedIOPort input;

    /** The output port.  The type is a that of a Record of String
     *  labels and Types.
     */
    public TypedIOPort output;

    /** The id of the track.  The default value is an int with
     *  the value of -1, indicating that the id has not been set.
     */
    public Parameter trackId;

    /** The line symbol.  The default type is that of String. */
    public Parameter lineSymbol;

    /** True if the track is broken.  The default is false. */
    public Parameter broken;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    @Override
    public boolean reject(Token token, IOPort port) {
        boolean unAvailable=(_inTransit != null || ((BooleanToken)_isBroken).booleanValue());
        return unAvailable;
    }

    /**This method handles changing in the broken and symbol parameter of the track.
     * Symbol is the symbol of the line.
     */
    @Override
    public void attributeChanged(Attribute attribute) throws IllegalActionException {
        Director director=getDirector();
        if (attribute == broken) {
             if (broken.getToken()!=null) {
                _isBroken=broken.getToken();

                //Change color of the storm zone.
                if (((BooleanToken)_isBroken).booleanValue()==true) {
                    _circle.fillColor.setToken("{1.0,0.2,0.2,1.0}");
                }
                else {
                    _circle.fillColor.setToken("{0.0, 0.0, 0.0, 0.0}");
                }
                ((TCSDirector)director).handleTrackAttributeChanged(this);
            }
        } else if (attribute==lineSymbol && lineSymbol.getToken()!=null) {
            _symbol=((StringToken)lineSymbol.getToken()).stringValue();
            if (_symbol.length()>1)
                throw new IllegalActionException("Inappropriate line symbol");
            _color=((TCSDirector)director).getColor(_symbol);
            _rectangle.fillColor.setToken(_color);
            _rectangle.lineColor.setToken(_color);
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
        Time currentTime = _director.getModelTime();
        if (currentTime.equals(_transitExpires) && _inTransit != null) {
            try {
                    output.send(0, _inTransit);
            }
            catch (NoRoomException ex) {
             // Token rejected by the destination.
                if (!(_director instanceof TCSDirector)) {
                    throw new IllegalActionException(this, "Track must be used with an TCSDirector.");
                }
                double additionalDelay = ((TCSDirector)_director).handleRejectionWithDelay(this);
                if (additionalDelay < 0.0) {
                    throw new IllegalActionException(this, "Unable to handle rejection.");
                }
                _transitExpires = _transitExpires.add(additionalDelay);
                _director.fireAt(this, _transitExpires);
                return;
            }
            // Token has been sent successfully.
            _inTransit = null;
            _changeIcon(_symbol);
            return;
        }
        // Handle any input that have been accepted.

            if (input.hasNewToken(0))
            {
                // This if is used for checking safety. Instead of throwing exception we can write a record to the file.
                if (_inTransit!=null)
                {
                    throw new IllegalActionException("two train in one track");
                }

                _inTransit=input.get(0);
                _setIconForTrain(((RecordToken)_inTransit).get("trainId"));
                double movingTime=((TCSDirector)_director).movingTimeOfTrain(_inTransit,_id);
                if (movingTime<=0.0)
                    throw new IllegalActionException("Minstake in calculating moving time of Train");
                _transitExpires=currentTime.add(movingTime);
                _director.fireAt(this, _transitExpires);
            }
    }



    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _director=getDirector();
        _inTransit = null;
        _id=trackId.getToken();
        _isBroken=broken.getToken();
        if (_isBroken==null)
            _isBroken=(Token)(new BooleanToken(false));
       ((TCSDirector)_director).handleInitializedTrack(this);
           if (lineSymbol.getToken()==null)
           _symbol="";
       else
           _symbol=((StringToken)lineSymbol.getToken()).stringValue();

       _changeIcon(_symbol);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Change icon of the track from train shape to rectangle.
     *  @param symbol The symbol of the line.
     *  @exception IllegalActionException
     */
    protected void _changeIcon(String symbol) throws IllegalActionException {
        _shape.fillColor.setToken("{0.0, 0.0, 0.0, 0.0}");
        _shape.lineColor.setToken("{0.0, 0.0, 0.0, 0.0}");
        if (symbol.equals(""))
        {
            _rectangle.fillColor.setToken("{1.0, 1.0, 1.0, 1.0}");
            _rectangle.lineColor.setToken("{0.0, 0.0, 0.0, 1.0}");
        }
        else {
            _color=((TCSDirector)_director).getColor(symbol);
            _rectangle.fillColor.setToken(_color);
            _rectangle.lineColor.setToken(_color);
        }
    }

    /** Change icon of the track from rectangle to icon of the train.
    *  @param idOfTrain The id of the train .
    *  @exception IllegalActionException
    */
    protected void _setIconForTrain(Token idOfTrain) throws IllegalActionException {
        int id=((IntToken)idOfTrain).intValue();
        _rectangle.fillColor.setToken("{0.0, 0.0, 0.0, 0.0}");
        _rectangle.lineColor.setToken("{0.0, 0.0, 0.0, 0.0}");
        _shape.fillColor.setToken(_setIcon(id));
        _shape.lineColor.setToken("{0.0, 0.0, 0.0, 1.0}");
    }

    /** Determine the color of the track/train .
     *  @param id The train ID or -1 to indicate no train.
     *  @exception IllegalActionException If thrown while getting the director.
     *  @return The color.
     */
    protected ArrayToken _setIcon(int id) throws IllegalActionException {
        ArrayToken color = _noTrainColor;
            if (id > -1) {
                Director _director = getDirector();
                color = ((TCSDirector)_director).handleTrainColor(id);
                if (color == null) {
                    throw new IllegalActionException("Color for the train " + id + " has not been set");
                }
            }
        return color;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The shape. */
    protected ResizablePolygonAttribute _shape;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////


    private EllipseAttribute _circle;
    private ArrayToken _color;
    private Director _director;
    private Token _id;
    private Token _isBroken;
    private Token _inTransit;
    private String[] _labels={"trainId","trainSymbol","movingMap","trainSpeed","fuel","arrivalTimeToStation","dipartureTimeFromStation"};
    private DoubleToken _one = new DoubleToken(1.0);
    private Token[] _white = {_one, _one, _one, _one};
    private ArrayToken _noTrainColor = new ArrayToken(_white);
    private RectangleAttribute _rectangle;
    private String _symbol;
    private Time _transitExpires;
    private Type[] _types={BaseType.INT,BaseType.STRING,new ArrayType(BaseType.STRING), BaseType.INT,BaseType.DOUBLE,BaseType.DOUBLE,BaseType.DOUBLE};

}
