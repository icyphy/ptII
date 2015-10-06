// Copyright (c) 2015 The Regents of the University of California.
// All rights reserved.

// Permission is hereby granted, without written agreement and without
// license or royalty fees, to use, copy, modify, and distribute this
// software and its documentation for any purpose, provided that the above
// copyright notice and the following two paragraphs appear in all copies
// of this software.

// IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
// FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE.

// THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
// INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
// ENHANCEMENTS, OR MODIFICATIONS.

package ptolemy.actor.lib.jjs.modules.contextAware;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

///////////////////////////////////////////////////////////////////
//// ContextAwareHelper

/**
 * Helper for the contextAware.js. This class in its full implementation should perform discovery of 
 * IoT services and create the appropriate dialog to obtain the needed context
 * for invoking a specific REST service. The context includes the required input to the service and the
 * expected response from the service. No dialog is implemented yet!!
 * @author Anne H. Ngu (angu@txstate.edu) 
 * @version 
 *
 */
public class ContextAwareHelper {
    /** Create an instance of a GUI object to collect details about a service.
     */
    public ContextAwareHelper() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the list of services.  
     * Eventually this should be replaced by querying a known registry or a 
     * discovery process. Return a list of iot REST service names that are available to the user to choose from
     * @return the list of of services.
     */
    public String[] availableServices() {
        return _iotServiceList;
    }
    
    /** convert an XML data format to JSON data format using the XML class from org.json.xml
     * 
     * @param response is the input in xml format
     * @return the json formatted data in an array 
     */
    public ArrayList  convertXMLtoJSON(String response) {
        try{
            JSONObject xmlJson = XML.toJSONObject(response);
            String prettyString = xmlJson.toString(4);
            System.out.println(prettyString);
            Iterator x = xmlJson.keys();
            ArrayList jsonArray=new ArrayList();
            while(x.hasNext()){
                String key=(String)x.next();
                jsonArray.add(xmlJson.get(key));
            }
            return jsonArray;
        } catch (JSONException je) {
            System.out.println(je.toString());
            return null;
        }
    }
    /** Return the discovered services.
     * Currently, this method sets the selected service to the list of services and
     * then returns the string "pluto.cs.txstate.edu:22001"
     *
     * @return the discovered services
     */
    public String discoverServices() {
        // Need to implement a discovery process that takes into account user's preferences and locations
        // currently, this just presents the set of known services to users and returns the set of
        // parameters associated with the specific service. Not used by the accessor.
        setSelectedService(_iotServiceList);
        return "pluto.cs.txstate.edu:22001";
    }
    
    /** Return the list of output choices of GSN service. Currently, the list of choices is 
     * hard coded. Eventually, this list should come from a discovery process
     * @return The list of data type to be extracted from the service
     */
    public String[] getGsnOutput() {
        return _gsnOutputPort;
    }
    
    /** Return the list of output choices of Firebase service. Currently, the list of choices is 
     * hard coded. Eventually, this list should come from a discovery process
     * @return The list of data type to be extracted from Firebase.
     */
    public String[] getFirebaseOutput(){
        return _firebaseOutputPort;
    }
    /** Return the list of output choices of Paraimpu service. Currently, the list of choices is 
     * hard coded. Eventually, this list should come from a discovery process
     * @return The list of data type to be extracted from Paraimpu.
     */
  
    public String[] getParaimpuOutput() {
        return _paraimpuOutputPort;
    }
    /** Return the name of the selected service. 
     * Currently, this method returns the string "GSN".
     * Eventually, this method will return data from the GUI.
     * @return The name of the service chosen by the user.
     */
    public String getSelectedService() {
        //return _selectedService;
        return "GSN";
    }

    /** Return the parameters associated with the selected service. Currently just return 
     *  a hard wired url for testing.
     * @param selectedService The name of the service that was selected.
     * @return An array of service parameters. Not used now
     */
    public String getSelectedServiceParameter(String selectedService) {
        //for (int i=0; i< defaultParamList.length; i++) {
        //  _selectedServiceParam[i] = defaultParamList[i];
        //}
        return "pluto.cs.txstate.edu:22001";
    }

    /** Initializes the list of available iot REST services and creates a GUI
     *  for a user to make the selection. Not used currently
     *  
     *  Currently, this method does nothing.
     *  
     * @param list known list of services
     */
    public void setSelectedService(String[] list) {
        //_GUI = new ContextAwareGUI(list);
        // addListeners();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The names of the parameters for the service that was selected by the user. */
    protected String[] _selectedServiceParameters;

    /** The name of the service selected by the user. */
    protected String _selectedService;

    /** The specific data that is wanted from the selected service. */
    protected String _sensorService;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    /// The list for gsn, firebase and paraimpu  must match with what//
    /// is defined in the respective service interfaces             ///
    
    public String[] _gsnOutputPort = {"sound", "sensorName"};
    public String[] _firebaseOutputPort = {"microwave", "microwaveStatus", "pastValues"};
    public String[] _paraimpuOutputPort = {"payload","producer", "sensorId"};
    private String[] _iotServiceList = { "GSN", "Paraimpu", "Firebase" };

}
