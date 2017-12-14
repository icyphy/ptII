/* A director for modeling Train control system.

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
package ptolemy.domains.tcs.kernel;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

import ptolemy.actor.Receiver;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.domains.tcs.lib.AbstractSourceStation;
import ptolemy.domains.tcs.lib.AbstractStation;
import ptolemy.domains.tcs.lib.AbstractTrack;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// TCSDirector

/** A director for modeling Train control systems.
 *  This director provides a receiver that consults the destination actor
 *  to determine whether it can accept an input, and provides mechanisms
 *  for handling rejection of an input.
 *  @author Maryam Bagheri
 *  @author $Id$
 *  @since Ptolemy II 11.0
 */
public class TCSDirector extends DEDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  @param container The container
     *  @param name The name of the director
     *  @exception NameDuplicationException If construction of Time objects fails.
     *  @exception IllegalActionException If construction of Time objects fails.
     */
    public TCSDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new receiver of the type TCSReceiver.
     *  @return A new TCSReceiver.
     */
    @Override
    public Receiver newReceiver() {
        return new TCSReceiver();
    }

    /** Initialize all the contained actors by invoke the initialize() method
     *  of the super class.
     */
    @Override
    public void initialize() throws IllegalActionException {
        _brokenTracks = new TreeMap<>();
        _brokenStations = new TreeMap<>();
        _lineColor = new HashMap<String, ArrayToken>();
        _trainsColor = new HashMap<>();
        super.initialize();
    }

    /** Return the color of the line.
     *  @param symbol symbol of the line.
     *  @return Return color of a line in form of ArrayToken.
     *  @exception IllegalActionException If thrown while coloring the lines.
     */
    public ArrayToken getColor(String symbol) throws IllegalActionException {
        lineColoring();
        return _lineColor.get(symbol);
    }

    /** Return an additional delay for a track to keep a train in transit.
     *  @param track The track
     *  @return An additional delay, or -1.0 to indicate that a rerouting is possible.
     *  This base class returns 1.0.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public double handleRejectionWithDelay(AbstractTrack track)
            throws IllegalActionException {
        // FIXME: what value should be returned here?
        return 1.0;
    }

    /** Return an additional delay for a Station to keep a Train in transit.
     *  @param station The station
     *  @return An additional delay, or -1.0 to indicate that a rerouting is possible.
     *  This base class returns 1.0.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public double handleRejectionWithDelayStation(AbstractStation station)
            throws IllegalActionException {
        // TODO Auto-generated method stub
        return 1.0;
    }

    /** Put an entry into _brokenTracks for the initialized track.
     *  @param track The track
     *  @exception IllegalActionException If thrown while putting the entry into _brokenTracks.
     */
    public void handleInitializedTrack(AbstractTrack track)
            throws IllegalActionException {
        int id = ((IntToken) track.trackId.getToken()).intValue();
        if (id == -1) {
            throw new IllegalActionException(
                    "Id of the track " + id + " is invalid (-1)");
        }
        if (track.lineSymbol.getToken() == null) {
            throw new IllegalActionException("Fill Symbol of Track " + id);
        }
        String symbol = ((StringToken) (track.lineSymbol.getToken()))
                .stringValue();
        if (symbol.length() > 1 || symbol.length() == 0) {
            throw new IllegalActionException(
                    "Inappropriate symbolLine for track " + id);
        }
        symbol = symbol + id;
        if (_brokenTracks.containsKey(symbol)) {
            throw new IllegalActionException(
                    "Track with the id " + id + " has been duplicated");
        }
        if (track.broken.getToken() == null) {
            _brokenTracks.put(symbol, (new BooleanToken(false)));
        } else {
            _brokenTracks.put(symbol, track.broken.getToken());
        }
    }

    /** Put an entry into _brokenStations for the initialized station.
     *  @param station The station
     *  @exception IllegalActionException If the entry cannot be put in to _brokenStations
     */
    public void handleInitializedStation(AbstractStation station)
            throws IllegalActionException {
        int stationId = ((IntToken) station.stationId.getToken()).intValue();
        if (stationId == -1) {
            throw new IllegalActionException("Invalid id for station (-1)");
        }
        if (station.lineSymbol.getToken() == null) {
            throw new IllegalActionException(
                    "Fill Symbol of station " + stationId);
        }
        String symbol = ((StringToken) (station.lineSymbol.getToken()))
                .stringValue();
        if (symbol.length() > 1 || symbol.length() == 0) {
            throw new IllegalActionException(
                    "Inappropriate symbolLine for station " + stationId);
        }
        symbol = symbol + stationId;
        if (_brokenStations.containsKey(symbol)) {
            throw new IllegalActionException("Station with the id " + stationId
                    + " has been duplicated");
        }
        if (station.broken.getToken() == null) {
            _brokenStations.put(symbol, (new BooleanToken(false)));
        } else {
            _brokenStations.put(symbol, station.broken.getToken());
        }
        if (station.inJunction.getToken() != null
                && ((BooleanToken) station.inJunction.getToken())
                        .booleanValue() == true) {
            if (station.neighbors.getToken() == null
                    || ((ArrayToken) station.neighbors.getToken())
                            .length() == 0) {
                throw new IllegalActionException(
                        "Fill the neighbors of the statoin " + symbol);
            }
        }
    }

    /** Update the _brokenTracks array because of a change in condition of a track.
     *  @param track The track
     *  @exception IllegalActionException If the track id is invalid or
     *  the entry for the track has not been set in the array of
     *  broken tracks.
     */
    public void handleTrackAttributeChanged(AbstractTrack track)
            throws IllegalActionException {
        int id = ((IntToken) track.trackId.getToken()).intValue();
        if (id == -1) {
            throw new IllegalActionException(
                    "Id of the track " + id + " is invalid (-1)");
        }
        String symbol = ((StringToken) (track.lineSymbol.getToken()))
                .stringValue();
        symbol = symbol + id;
        if (_brokenTracks.size() != 0) {
            if (_brokenTracks.containsKey(symbol)) {
                _brokenTracks.put(symbol, track.broken.getToken());
            } else {
                throw new IllegalActionException(
                        "The entry for this track has not been set in brokenTrack array ");
            }
        }
    }

    /** Update _brokenStations array because of a change in condition of a station.
     *  @param station The station
     *  @exception IllegalActionException If the station id is invalid or
     *  if the entry for the station has not been set in the array of
     *  broken stations.
     */
    public void handleStationAttributeChanged(AbstractStation station)
            throws IllegalActionException {
        int id = ((IntToken) station.stationId.getToken()).intValue();
        if (id == -1) {
            throw new IllegalActionException("Invalid id for station (-1)");
        }

        String symbol = ((StringToken) (station.lineSymbol.getToken()))
                .stringValue();
        symbol = symbol + id;
        if (_brokenStations.size() != 0) {
            if (_brokenStations.containsKey(symbol)) {
                _brokenStations.put(symbol, station.broken.getToken());
            } else {
                throw new IllegalActionException(
                        "The entry for this station " + symbol
                                + " has not been set in brokenStations array ");
            }
        }
    }

    /** Handle initializing of a SourceStation.
     *  @param abstractSourceStation The Abstract Source state
     *  @exception IllegalActionException If the line symbol cannot be obtained or if the stationID is -1.
     */
    public void handleInitializedSourceStation(
            AbstractSourceStation abstractSourceStation)
            throws IllegalActionException {

        int stationId = ((IntToken) abstractSourceStation.stationId.getToken())
                .intValue();
        if (abstractSourceStation.lineSymbol.getToken() == null) {
            throw new IllegalActionException(
                    "Fill Symbol of sourceStation " + stationId);
        }
        String symbol = ((StringToken) (abstractSourceStation.lineSymbol
                .getToken())).stringValue();
        symbol = symbol + stationId;

        if (stationId == -1) {
            throw new IllegalActionException("Invalid id for source station");
        }
        if (_brokenStations.containsKey(symbol)) {
            throw new IllegalActionException("Duplication in station id");
        }

        _brokenStations.put(symbol, (new BooleanToken(false)));
    }

    /** Return color of the train.
    *  @param id Id of the train.
    *  @return the color of the train.
    *  @exception IllegalActionException If thrown while creating an ArrayToken
    *  from the color specification.
    */
    public ArrayToken handleTrainColor(int id) throws IllegalActionException {
        ArrayToken color = _trainsColor.get(id);

        while (color == null) {
            Token[] colorSpec = new DoubleToken[4];
            colorSpec[0] = new DoubleToken(_random.nextDouble());
            colorSpec[1] = new DoubleToken(_random.nextDouble());
            colorSpec[2] = new DoubleToken(_random.nextDouble());
            colorSpec[3] = new DoubleToken(1.0);
            color = new ArrayToken(colorSpec);
            Boolean colorExist = false;
            for (Entry<String, ArrayToken> entry : _lineColor.entrySet()) {
                if (entry.getValue().equals(color)) {
                    colorExist = true;
                    break;
                }
            }
            if (colorExist == false) {
                _trainsColor.put(id, color);
                break;
            }
            color = null;
        }

        return color;
    }

    /** Set color of the lines in Metro.
     *  @exception IllegalActionException If there is a problem
     *  adding the lines to the set of line colors.
     */
    public void lineColoring() throws IllegalActionException {
        _lineColor.put("A", new ArrayToken("{1.0,0.6,0.6,1.0}"));
        _lineColor.put("I", new ArrayToken("{0.0,0.4,1.0,1.0}"));
        _lineColor.put("S", new ArrayToken("{0.6,0.8,0.0,1.0}"));
        _lineColor.put("E", new ArrayToken("{1.0,0.0,0.8,1.0}"));
        _lineColor.put("G", new ArrayToken("{1.0,0.4,0.0,1.0}"));
        _lineColor.put("m", new ArrayToken("{1.0,0.0,0.0,1.0}"));
        _lineColor.put("M", new ArrayToken("{1.0,0.0,0.0,1.0}"));
        _lineColor.put("H", new ArrayToken("{0.8,0.8,0.8,1.0}"));
        _lineColor.put("T", new ArrayToken("{0.2,0.8,1.0,1.0}"));
        _lineColor.put("C", new ArrayToken("{0.0,0.6,0.0,1.0}"));
        _lineColor.put("Y", new ArrayToken("{1.0,0.8,0.0,1.0}"));
        _lineColor.put("Z", new ArrayToken("{0.6,0.0,0.6,1.0}"));
        _lineColor.put("N", new ArrayToken("{0.0,1.0,0.8,1.0}"));
        _lineColor.put("F",
                new ArrayToken("{0.8235294,0.4117647,0.11764706,0.84705883}"));

    }

    /** Return moving time of a train in a track or station.
     *  @param  inTransit inTransit is the moving train.
     *  @param  id Id is the id of the track or station.
     *  @return Return time of traveling.
     */
    public double movingTimeOfTrain(Token inTransit, Token id) {
        // FIXME: Determine time of traveling based on speed.
        return 1.0;
    }

    /** Routing a train in a station with more than one output channel
    *   which is in the junction and by using the moving map of the
    *   train.  MovingMap of the train is an array in form of
    *   "symbolId", that just shows the next station (in trip) of a
    *   station with more than one output channel.
    *  @param lines lines show the stations which are neighbour of the
    *  current station, an array in form of "symbolId".
    *  @param token token shows the train.
    *  @return Returns a new train packet and the out channel.
    *  @exception IllegalActionException
    */
    public Map<String, Token> routing(ArrayToken lines, Token token)
            throws IllegalActionException {
        ArrayToken movingMap = (ArrayToken) ((RecordToken) token)
                .get("movingMap");
        int outRout = 0;
        if (movingMap.length() != 0) {
            Token first = movingMap.getElement(0);
            if (movingMap.length() == 1) {
                movingMap = new ArrayToken("{}");
            } else {
                movingMap = movingMap.subarray(1);
            }
            for (int i = 0; i < lines.length(); i++) {
                if (lines.getElement(i).equals(first)) {
                    outRout = i;
                    break;
                }
            }
        }

        Map<String, Token> newTrain = new TreeMap<String, Token>();
        newTrain.put("trainId", ((RecordToken) token).get("trainId"));
        newTrain.put("trainSymbol", ((RecordToken) token).get("trainSymbol"));
        newTrain.put("trainSpeed", ((RecordToken) token).get("trainSpeed"));
        newTrain.put("movingMap", movingMap);
        newTrain.put("fuel", ((RecordToken) token).get("fuel"));
        newTrain.put("arrivalTimeToStation",
                ((RecordToken) token).get("arrivalTimeToStation"));
        newTrain.put("dipartureTimeFromStation",
                ((RecordToken) token).get("dipartureTimeFromStation"));

        Map<String, Token> temp = new TreeMap<>();
        temp.put("outputChannel", new IntToken(outRout));
        temp.put("train", (new RecordToken(newTrain)));
        return temp;

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Random _random = new Random();

    /**  _brokenTracks stores which track has broken. First element is
     *  in form of "symbolId" in which symbol is symbol of the track
     *  (line) and id is id of the track, and last is a boolean
     *  token.
     */
    private Map<String, Token> _brokenTracks = new TreeMap<>();

    /**  _brokenStations stores which station has broken. First
     *  element is in form of "symbolId" in which symbol is symbol of
     *  the station (line) and id is id of the station, and last is a
     *  boolean token.
     */
    private Map<String, Token> _brokenStations = new TreeMap<>();

    /** _lineColor stors color of the lines. First element is symbol
     * of the line and last is its color.
     */
    private Map<String, ArrayToken> _lineColor = new HashMap<String, ArrayToken>();

    /** _trainsColor stores a color for each train. */
    private Map<Integer, ArrayToken> _trainsColor = new HashMap<Integer, ArrayToken>();
}
