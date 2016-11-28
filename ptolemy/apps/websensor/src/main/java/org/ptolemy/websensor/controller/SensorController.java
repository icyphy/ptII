/* A Java Spring controller for a web sensor controller

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

package org.ptolemy.websensor.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ptolemy.websensor.lib.Sensor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;

/**
 * Models a sensor with a web server.  The sensor supplies temperature,
 * humidity and pressure readings.  It sends this information back as either
 * a web page or as a JSON object.
 *
 * @author ltrnc
 */

@RequestMapping(value = "/{type}")
public class SensorController
    extends org.springframework.web.servlet.mvc.AbstractController {

    /** Generate a web page or JSON response for the given sensor type,
     *  if the controller contains sensors of this type.  If the type contains
     *  .json, return a JSON response; otherwise, return a web page.
     *
     * @param request  The HTTP request.  The requestURI from the request
     *  is used to determine whether to return an HTML page or JSON
     * @param type  The sensor type
     * @return  A web page or JSON response for the current sensor type
     */

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView getReading(HttpServletRequest request,
            @PathVariable String type) {

        // Create a ModelAndView object to return, using
        // /src/main/webapp/presentation/views/sensorReading.jsp
        // as a template.
        ModelAndView modelAndView = new ModelAndView("sensorReading");

        if (type != null && !type.isEmpty()) {

            // Check for .json, case-insensitive
            // The "type" parameter will not contain the extension (e.g. .html
            // or .json) since this is automatically stripped by Spring
            // Get the original request URI to check if the request is for
            // .html or .json
            String requestURI = request.getRequestURI();

            Pattern pattern = Pattern.compile("(?i).json");
            Matcher matcher = pattern.matcher(requestURI);

            boolean isJSON = matcher.find();

            // Check for all sensors of this type and save the first readings
            // For HTML view
            StringBuffer readingsString = new StringBuffer();
            // For JSON view.
            ArrayList readingsList = new ArrayList();

            Iterator sensorsIterator = sensors.iterator();
            String typeName = "";
            String unit = "";
            while (sensorsIterator.hasNext()) {
                Sensor sensor = (Sensor) sensorsIterator.next();
                if (sensor.getType().equalsIgnoreCase(type)) {
                    // Set the displayed type name according to what is
                    // stored in the sensor, for e.g. proper capitalization,
                    // and the units.
                    typeName = sensor.getType();
                    // Assumes all sensors of the same type use the same units
                    unit = sensor.getUnit();

                    if (sensor.getReadings().size() > 0) {
                        for (int i = 0; i < sensor.getReadings().size(); i++) {
                            readingsString.append(sensor.getReadings().get(i) + " ");
                            readingsString.append(sensor.getUnit() + " ");
                        }
                       readingsList.addAll(sensor.getReadings());
                    }
                }
            }

            // If any sensors found of this type, assemble readings into a
            // string
            if (readingsString.length() > 0) {
                // Display the results

                if (isJSON) {
                    // For JSON, specification is to return unit and value, e.g.
                    // { "unit":"%", "value": 26.82}
                    // Create ModelAndView as usual, and view resolver will
                    // automatically return JSON for URLs ending in .json
                    // (see websensor-servlet.xml)
                    modelAndView.setView(new MappingJacksonJsonView());
                    modelAndView.addObject("unit", unit);
                    modelAndView.addObject("values", readingsList.toArray());
                } else {
                    modelAndView.addObject("type", typeName);
                    modelAndView.addObject("readings", readingsString);
                }

            } else {
                // Return not found, if there are no sensors of this type
                // TODO:  Need a test for this
                modelAndView.addObject("type", type + " was not found");
                modelAndView.addObject("readings", "Not found");
            }
        } else {
            // Return a blank view if no sensor specified
            // TODO:  Need a test for this - how though?  empty string will
            // just map to the index.jsp file
            modelAndView.addObject("type", "Undefined");
            modelAndView.addObject("readings", "Undefined");
        }
      return modelAndView;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Adds the given sensor to the set of sensors associated with this
     * sensor controller.  If it is a duplicate, it will not be added.
     *
     * @param sensor  The sensor to add to the controller's set of sensors
     */
    public void addSensor(Sensor sensor) {
        sensors.add(sensor);
    }

    /** Returns the geographical location of this sensor controller.
     *  (Assumes that all of its sensors are co-located).
     *
     * @return  The location of this sensor controller
     */
    public String getLocation() {
        return _location;
    }

    /** Returns the set of sensors associated with this sensor controller.
     * This returns an array, since the return type of the getter must match
     * the parameter of the setter in order to use the setters in the Spring
     * configuration file.
     *
     * @return  The set of sensors associated with this sensor controller.
     */
    public Sensor[] getSensors() {
        // Avoid FindBugs BC_IMPOSSIBLE_CAST
        return sensors.toArray(new Sensor[sensors.size()]);
    }

    /** Removes the given sensor from the sensor controller's set, if present.
     *
     * @param sensor  The sensor to remove
     */
    // FIXME:  Will this work?  Will the object really be the same or have
    // to implement some kind of .equals()?
    public void removeSensor(Sensor sensor) {
        // TODO
    }

    /** Removes all sensors of the given type from the sensor controller's set,
     *  if any.
     *
     * @param type  The type of sensor to remove
     */
    public void removeSensors(String type) {
        // TODO
    }

    /** Set the geographical location of this sensor controller.
     *
     * @param location  The geographical location of this sensor controller.
     */
    public void setLocation(String location) {
        _location = location;
    }

    /** Set the sensors associated with this sensor controller.  The input array
     * will be checked for duplicates.  If any duplicates occur, they will be
     * removed. This accepts an array so that it can be set in the Spring
     * configuration file. (I don't think Spring will allow a List or Set?)
     *
     * @param sensors  The sensors associated with this sensor controller.
     */
    public void setSensors(Sensor[] sensors) {
        this.sensors = new HashSet();
        for (int i = 0; i < sensors.length; i++) {
            this.sensors.add(sensors[i]);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // These can be changed to protected variables if subclasses are introduced

    /** The geographical location of the sensor.  For example, "Pittsburgh",
     *  "Edward's desk", ...
     */
    private String _location;

    /** The set of sensors associated with this sensor controller.  This is a
     *  set, since a controller cannot have multiple instances of the same
     *  sensor object.  (It could, however, have multiple sensors of the same
     *  type - for example, three temperature sensors.)
     */
    private HashSet<Sensor> sensors = null;

    /**
     * Required by AbstractController, but doesn't do anything in this example.
     */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest arg0,
            HttpServletResponse arg1) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }
}


