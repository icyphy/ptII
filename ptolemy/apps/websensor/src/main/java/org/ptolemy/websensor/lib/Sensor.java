/* A Java class for an individual sensor

 Copyright (c) 1997-2011 The Regents of the University of California.
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

package org.ptolemy.websensor.lib;

import java.util.ArrayList;
import java.util.List;

/** A sensor which provides a reading and information about this reading.
 *  Information includes type of measurement (for example, temperature), 
 *  units of measurement, and time interval between between readings)
 * 
 * @author ltrnc
 *
 */
public class Sensor {
    
    /** Create a sensor with an undefined type, undefined units, undefined units
     *  and no readings. 
     */
    Sensor() {
        _type = "undefined";
        _unit = "undefined";
        _interval = "undefined";
        _readings = new ArrayList<Double>();
        // TODO:  Here's a value for testing.  Delete this line in the future
        _readings.add(Double.valueOf(3.14));
        _readings.add(Double.valueOf(1.23));
    }
    
    /** Create a sensor with the given type, units of measurement, and 
     *  time interval between measurements, which has no readings.
     *  
     * @param type  The type of measurement, for example, temperature
     * @param unit  The units of measurement, for example, degC
     * @param interval  The time interval between measurements
     */
    Sensor(String type, String unit, String interval) {
        // The type cannot contain ".json" - see notes in setType
        setType(type);
        _unit = unit;
        _interval = interval;
        _readings = new ArrayList<Double>();
         // TODO:  Here's a value for testing.  Delete this line in the future
        _readings.add(new Double(5.67));
        _readings.add(new Double(7.89));
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Returns the interval between sensor readings (for example, 1 second)
     * 
     * @return The interval between sensor readings
     */
    public String getInterval() {
        return _interval;
    }
    
    /** Returns the readings from this sensor
     * 
     * @return The sensor readings
     */
    // FIXME:  Will this need to be changed to return an array, in order to be
    // configured in the Spring context file?
    public ArrayList getReadings() {
        return _readings;
    }
    
    /** Returns the type of the sensor (for example, temperature)
     * 
     * @return The type of the sensor
     */
    public String getType() {
        return _type;
    }
    
    /** Returns the physical unit of the measurement (for example, degC)
     * 
     * @return The physical unit of the measurement
     */
    public String getUnit() {
        return _unit;
    }
    
    /** Sets the time interval between sensor readings
     * 
     * @param interval The time interval between sensor readings
     */
    public void setInterval(String interval){
        _interval = interval;
    }
    
    /** Sets the readings of this sensor
     * 
     * @param readings  The list of sensor readings
     */
    public void setReadings(List readings) {
        _readings = new ArrayList<Double>();
        if (readings != null) {
            _readings.addAll(readings);
        }
    }
    
    /** Sets the type of the sensor.  The type name cannot contain '.json'.
     * If it does, all instances of .json will be deleted
     * 
     * @param type  The type of the sensor 
     */
    // TODO:  Need a test for this
    public void setType(String type) {
        // The type cannot contain ".json" - this would cause problems in the
        // controller since the controller uses the type as part of the URL
        // and adds a ".json" to the type name for the URL that returns a
        // JSON representation.  Case-insensitive.
        _type = type.replaceAll("(?i).json", "");      
    }
    
    /** Sets the physical unit of the sensor
     * 
     * @param unit The physical unit of the sensor
     */
    public void setUnit(String unit) {
        _unit = unit;
    }
            
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    // These can be changed to protected variables if subclasses are introduced
    /** The time interval between readings.  For example, 1 second
     *  This could be split into a numeric value plus a unit in the future, if 
     *  we want to support math operations on the value.  */
    private String _interval;
    
    /** The readings from this sensor.  Assumes the sensor returns a numeric
     * value.
     */
    private ArrayList<Double> _readings;
    
    /** The type of sensor.  For example, temperature */
    private String _type;
    
    /** The physical unit of this sensor.  For example, degC */
    private String _unit;
}
