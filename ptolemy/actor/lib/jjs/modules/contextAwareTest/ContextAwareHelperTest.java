// Set up the parameters with the details of the specific REST service (Test version).

/* Copyright (c) 2015-2016 The Regents of the University of California.
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

package ptolemy.actor.lib.jjs.modules.contextAwareTest;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import ptolemy.actor.lib.jjs.modules.contextAware.ContextAwareGUI;

///////////////////////////////////////////////////////////////////
//// ContextAwareHelperTest
/** Set up the parameters with the details of the specific REST service.
 *
 * <p>This is a test version of ContextAwareHelper that
 * is used for experimentation.</p>
 *
 * @author Anne Ngu, Contributor: Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Green (eal)
 * @Pt.AcceptedRating Green (bilung)
 */
public class ContextAwareHelperTest {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return an array of iot REST service names that are available
     *  to the user.
     *  @return an array of iot REST service names that are available.
     */
    public  String[] availableServices() {
        return iotServiceList;
    }

    /** Dialog Box to select a service the accessor mimics.
     *  @param sensors The names of the sensors.
     */
    public void chooseSensor(String[] sensors) {
        _sensorService = (String)JOptionPane.showInputDialog(null,
                "Choose sensor to retreive data from:\n",
                "Sensor Selection",
                JOptionPane.PLAIN_MESSAGE,
                null, sensors,
                "1");
    }

    /** Convert an XML data format to JSON data format using the XML
     * class from org.json.xml.
     *
     * @param response is the input in xml format
     * @return the json formatted data in an array
     */
    public ArrayList  convertXMLtoJSON(String response) {
        try {
            JSONObject xmlJson = XML.toJSONObject(response);
            String prettyString = xmlJson.toString(4);
            System.out.println(prettyString);
            Iterator x = xmlJson.keys();
            ArrayList jsonArray=new ArrayList();
            while (x.hasNext()) {
                String key=(String)x.next();
                jsonArray.add(xmlJson.get(key));
            }
            return jsonArray;
        } catch (JSONException je) {
            System.out.println(je.toString());
            return null;
        }
    }

    /** Return the selected service.
     * @return The selected service.
     */
    public String discoverServices() {
         // FIXME: need to implement a discovery process that takes
         //into account user's preferences and locations  currently,
         //just present the set of known services to users and return
         //the selected service

        this.setSelectedService(iotServiceList);
        return _selectedServiceParam;
    }


    /** Return the list of output choices of GSN service. Currently,
     * the list of choices is hard coded. Eventually, this list should
     * come from a discovery process
     * @return The list of data type to be extracted from the service
     */
    public String[] getGsnOutput() {
        return gsnOutputPort;
    }

    /** Return the list of output choices of Firebase
     * service. Currently, the list of choices is hard
     * coded. Eventually, this list should come from a discovery
     * process
     * @return The list of data type to be extracted from Firebase.
     */
    public String[] getFirebaseOutput() {
        return firebaseOutputPort;
    }

    /** Return the list of output choices of Paraimpu
     * service. Currently, the list of choices is hard
     * coded. Eventually, this list should come from a discovery
     * process
     * @return The list of data type to be extracted from Paraimpu.
     */
    public String[] getParaimpuOutput() {
        return paraimpuOutputPort;
    }

    /** Return the name of the selected service.
     * Currently, this method returns the string "GSN".
     * Eventually, this method will return data from the GUI.
     * @return The name of the service chosen by the user.
     * @see #setSelectedService(String[])
     */
    public String getSelectedService() {
        //return _selectedService;
        return "GSN";
    }

    //FIXME:retrieve data entered by a user from a text field in the GUI

    /** Return the parameters associated with the selected
     *  service. Currently just return a hard wired url for testing.
     * @param selectedService The name of the service that was selected.
     * @return An array of service parameters. Not used now
     */
    public String getSelectedServiceParameter(String selectedService) {
        //return GUI.textFields.get(index).getText();
        if (selectedService.equals("GSN")) {
            return "pluto.cs.txstate.edu";}
        else return "";
    }

    /** Get the name of a particular sensor in a service.
     * @return The name of the service.
     */
    public String getService() {
        return _sensorService;
    }


    /** Initializes the list of available iot REST services and
     *  creates a GUI for a user to make the selection. Not used
     *  currently Currently, this method does nothing.
     *  @param list known list of services
     *  @see #getSelectedService()
     */

    public void setSelectedService(String[] list) {
        _GUI = new ContextAwareGUI(list);
        addListeners();
    }

    /** Create a list of parameters specific to the middleware chosen.
     * @param parameters The names of the parameters.
     */
    public void setParameters(String[] parameters) {

        int length = Array.getLength(parameters);

        for (int i = 0; i < _GUI.labels.size(); i++) {
            if (i < length) {
                _GUI.labels.get(i).setText(parameters[i]);
                _GUI.labels.get(i).setVisible(true);
                _GUI.textFields.get(i).setVisible(true);
            }
            else {
                _GUI.labels.get(i).setVisible(false);
                _GUI.textFields.get(i).setVisible(false);
            }
            _GUI.textFields.get(i).setText(null);
        }
    }


    /** The search button. */
    public JButton searchButton;

    /** The default parameters. */
    public String[] defaultParamList =  {"username", "password","ipAddress", "port"};

    /** The output choices of the FireBase service. */
    public String[] firebaseOutputPort = {"microwave", "microwaveStatus", "pastValues"};

    /** The output choices of the GSN service. */
    public String[] gsnOutputPort = {"sound", "sensorName"};

    /** The choices of the IoT service. */
    public String[] iotServiceList = {"GSN", "Paraimpu", "Firebase"};

    /** The output choices of the Paraimpu service. */
    public String[] paraimpuOutputPort = {"payload","producer", "thingId"};

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The GUI. */
    protected ContextAwareGUI _GUI;

    /** The service that was selected. */
    protected String _selectedServiceParam;

    /** The sensor that was selected. */
    protected String _sensorService;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // FIXME: Use a Javadoc comment here.  Complete sentence that ends in a period.  Add @param.
    //adds list and button listeners
    private void addListeners() {
        // FIXME: use a complete sentence below.
        //when button is pressed, call getSensors from accessor
        _GUI.searchButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        System.out.println("getService()");
                        //((Invocable)_engine).invokeFunction("getSensors");
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }

            });
        // FIXME: use a complete sentence below.
        //when ever the type of REST service changes, get the parameters required
        _GUI.servicesList.addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    _selectedServiceParam = _GUI.servicesList.getSelectedValue();
                    try {
                        System.out.println("getParameters" + _selectedServiceParam);
                        setParameters(defaultParamList);
                        for (int i = 0; i< defaultParamList.length; i++) {
                            System.out.println( _GUI.textFields.get(i).getText());

                        }
                        // ((Invocable)_engine).invokeFunction("getParameters", MW);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }


                }
            });
    }
}
