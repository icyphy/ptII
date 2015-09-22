package ptolemy.actor.lib.jjs.modules.contextAware;


    import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;

    import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.EditorPaneFactory;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

    import javax.script.Invocable;
import javax.script.ScriptException;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.xml.transform.TransformerConfigurationException;

import org.terraswarm.accessor.JSAccessor;

    public class ContextAwareHelper {
        
        //This class sets up the dialog with the user and discovers details of a specific REST service
        
        public String[] iotServiceList = {"GSN", "Paraimpu", "Firebase"};
        public String [] defaultParamList =  {"username", "password","ipAddress", "port"};

        public ContextAwareHelper() {
          // GUI = new ContextAwareGUI(iotServiecList);
        }  
        
        //Factory class that allows us to create our own GUI
        // do not know how to use it with accessor
        /*
        public class Factory extends EditorPaneFactory throws Exceptions {
            public Factory(NamedObj, String name) 
                    throws IllegalActionException, NameDuplicationException {
                        super(container, name);
            }

            @Override
            public Component createEditorPane() {
                return GUI._panel;
            }
        }
        
       */
    
        /** Return an array of iot REST  service names that are available to the user.
         */
       
        public  String[] availableServices() {
        	return iotServiceList;
           
        }
        
        //Dialog Box to select a service the accessor mimics. 
        public void chooseSensor(String[] sensors) {
            
            _sensorService = (String)JOptionPane.showInputDialog(null,
                                                         "Choose sensor to retreive data from:\n",
                                                         "Sensor Selection",
                                                         JOptionPane.PLAIN_MESSAGE,
                                                         null, sensors,
                                                         "1");
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
        
        
        //get name of a particular sensor in a service
        public String getService() {
            return _sensorService;
        }
        
       
        /** FIXME: need to implement a discovery process that takes into account user's preferences and locations
         * currently, just present the set of known services to users and return the selected service
         * @return _selectedServiceParam 
         */
       public String discoverServices() {
           this.setSelectedService(iotServiceList);
            return _selectedServiceParam;
       }
        
        //initializes the list of available iot REST services and creates GUI
        public void setSelectedService(String[] list) {
            
            GUI = new ContextAwareGUI(list);
      
            addListeners();
            
        }
      
        
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
 
        
        
        //////////////////////////////////////////////////////
        ///                Private Methods                 ///
        
        
        //adds list and button listeners
       
        private void addListeners() {
            
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
      
        protected ContextAwareGUI GUI;
        
        protected String _selectedServiceParam;
        protected String _sensorService;
    }

