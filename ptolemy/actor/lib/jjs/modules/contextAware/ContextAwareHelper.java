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
        
        //This class sets up the GUI panel
        protected ContextAwareGUI GUI;
      
        protected String iotmw;
        protected String service;
        public String[] middlewareList = {"GSN", "Paraimpu","GWeb", "GG", "JJ"};
        public String [] paramList =  {"username", "password","ipAddress", "port"};

        public ContextAwareHelper() {
          // GUI = new ContextAwareGUI(middlewareList);
        }  
        
        //Factory class that allows us to create our own GUI
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
        //////////////////////////////////////////////////////
        ///        Functions Called from the Accessor      ///
        
        
        //Dialog Box to select a service the accessor mimics. 
        public void chooseSensor(String[] sensors) {
            
            service = (String)JOptionPane.showInputDialog(null,
                                                         "Choose service to retreive data from:\n",
                                                         "Service Selection",
                                                         JOptionPane.PLAIN_MESSAGE,
                                                         null, sensors,
                                                         "1");
        }
        
        public String getMiddleware() {
          // return iotmw;
            return "GSN";
        }    
        
        //retrieve data from a text field in the GUI
        // need to know which MW and which index of the parameter is needed
        
        //public String getParameterData(int index) {
        public String getParameterData(String iotmw){
            //return GUI.textFields.get(index).getText();
        	if (iotmw.equals("GSN")) {
        		return "pluto.cs.txstate.edu";}
        	else return "";
        }
        
        
        //get name of sensor
        public String getService() {
            return service;
        }
        
       public String listMiddleware() {
           this.setMiddleware(middlewareList);
            return iotmw;
       }
        
        //initializes the list of available middleware and creates GUI
        public void setMiddleware(String[] list) {
            
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
        
            //when ever the type of middleware changes, get the parameters required
            GUI._list.addListSelectionListener(new ListSelectionListener() {
                @Override        
                public void valueChanged(ListSelectionEvent e) {
                    iotmw = new String((String) GUI._list.getSelectedValue());               
                    try {
                        System.out.println("getParameters" + iotmw);
                        setParameters(paramList);
                        for (int i = 0; i< paramList.length; i++) {
                           System.out.println( GUI.textFields.get(i).getText());
                    
                        }
                       // ((Invocable)_engine).invokeFunction("getParameters", MW);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                      }
                    
                    
                }
            });
        }
      
    }

