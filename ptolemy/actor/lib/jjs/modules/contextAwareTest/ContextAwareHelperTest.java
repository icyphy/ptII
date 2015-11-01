// Set up the parameters with the details of the specific REST service (Test version).

/* Copyright (c) 2015 The Regents of the University of California.
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

    // FIXME: Alphabetize these methods.
    
    /** Return an array of iot REST  service names that are available to the user.
     *  @return FIXME: returns what?   
     */
    public  String[] availableServices() {
        return iotServiceList;
           
    }
   
        
    /** Dialog Box to select a service the accessor mimics. 
     *  @param sensors FIXME
     */
    public void chooseSensor(String[] sensors) {
            
        _sensorService = (String)JOptionPane.showInputDialog(null,
                "Choose sensor to retreive data from:\n",
                "Sensor Selection",
                JOptionPane.PLAIN_MESSAGE,
                null, sensors,
                "1");
    }
        
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
    /** FIXME: need to implement a discovery process that takes into account user's preferences and locations
     * currently, just present the set of known services to users and return the selected service
     * @return _selectedServiceParam 
     */
    public String discoverServices() {
        this.setSelectedService(iotServiceList);
        return _selectedServiceParam;
    }
    
    public String[] getGsnOutput() {
        return gsnOutputPort;
    }
    public String[] getFirebaseOutput(){
        return firebaseOutputPort;
    }
    
    public String[] getParaimpuOutput() {
        return paraimpuOutputPort;
    }
    
    public String getSelectedService() {
        //return _selectedService;
        return "GSN";
    }
    //FIXME:retrieve data entered by a user from a text field in the GUI
  
    public String getSelectedServiceParameter(String selectedService){
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
        
       
    // FIXME: Use a Javadoc comment here.  Complete sentence that ends in a period.  Add @param.
    //initializes the list of available iot REST services and creates GUI
    public void setSelectedService(String[] list) {
        GUI = new ContextAwareGUI(list);
        addListeners();
    }
        
    // FIXME: Use a Javadoc comment here.  Complete sentence that ends in a period.  Add @param.
    //creates list of parameters specific to the middleware chosen
    public void setParameters(String[] parameters) {
            
        int length = Array.getLength(parameters);
            
        for(int i = 0; i < GUI.labels.size(); i++) {
            if(i < length) {
                GUI.labels.get(i).setText(parameters[i]);
                GUI.labels.get(i).setVisible(true);
                GUI.textFields.get(i).setVisible(true);
            }
            else {
                GUI.labels.get(i).setVisible(false);
                GUI.textFields.get(i).setVisible(false);
            }
            GUI.textFields.get(i).setText(null);
        }
    }
 
    // FIXME:Need comments
    public String[] iotServiceList = {"GSN", "Paraimpu", "Firebase"};
    public String[] gsnOutputPort = {"sound", "sensorName"};
    public String[] firebaseOutputPort = {"microwave", "microwaveStatus", "pastValues"};
    public String[] paraimpuOutputPort = {"payload","producer", "thingId"};
    public String [] defaultParamList =  {"username", "password","ipAddress", "port"};
        
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // FIXME: Use a Javadoc comment here.  Complete sentence that ends in a period.  Add @param.
    //adds list and button listeners
    private void addListeners() {
        // FIXME: use a complete sentence below.
        //when button is pressed, call getSensors from accessor
        GUI.btnSearch.addActionListener(new ActionListener() {
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
        GUI._list.addListSelectionListener(new ListSelectionListener() {
                @Override        
                public void valueChanged(ListSelectionEvent e) {
                    _selectedServiceParam = new String((String) GUI._list.getSelectedValue());               
                    try {
                        System.out.println("getParameters" + _selectedServiceParam);
                        setParameters(defaultParamList);
                        for (int i = 0; i< defaultParamList.length; i++) {
                            System.out.println( GUI.textFields.get(i).getText());
                    
                        }
                        // ((Invocable)_engine).invokeFunction("getParameters", MW);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    
                    
                }
            });
    }
      
    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    // FIXME: Protected fields go above private methods.  Add Javadoc for each of these.
    protected ContextAwareGUI GUI;
        
    protected String _selectedServiceParam;
    protected String _sensorService;
}
