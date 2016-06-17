/**
 * 
 */
package org.hlacerti.lib;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import javax.swing.SwingUtilities;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.ExecutionListener;
import ptolemy.actor.Manager;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.VergilApplication;


/**This class implements an automatic simulation of any model chosen by the user.
 * It allows the change of any parameter, making the values go from a start value
 * to an end value with a fixed increment.
 * 
 * The user will be able to choose the models, the parameter and the range during
 * the execution. The parameter to be changed must be specified with exactly the 
 * same name in an item parameter in the ptolemy model, otherwise this automatic 
 * simulation will not work.
 * @author Tarciana Cabral de Brito Guerra
 * @version $Id:
 *
 */
public class AutomaticSimulation extends VergilApplication implements ExecutionListener{

    private static final String _stopTimeLine= "<property name=\"stopTime\" class=\"ptolemy.data.expr.Parameter\" value=\"";
    private static final String _timeStepLine= "<property name=\"timeStep\" class=\"ptolemy.data.expr.Parameter\" value=\"";
    private static final String _lookAheadLine= "<property name=\"lookAhead\" class=\"ptolemy.data.expr.Parameter\" value=\"";
    private boolean _wait;
    
    /* (non-Javadoc)
     * @see ptolemy.actor.gui.ConfigurationApplication#managerStateChanged(ptolemy.actor.Manager)
     */
    /**Verify if the model is initializing, changing the variable _wait to false if 
     * this execution phase has been achieved. 
     * @see ExecutionListener
     */
    @Override
    public void managerStateChanged(Manager manager) {
        String state = manager.getState().toString();
        if(state.contains("The model is initializing")){
            _wait = false;
        }
        System.out.println(manager.getFullName() +" "+ manager.getState());
    }

    /**
     * Construct an AutomaticSimulation object.
     * @param args The arguments of the execution.
     * @throws Exception
     */
    public AutomaticSimulation(String[] args) throws Exception {
        super(args);
    }

    /** Find some property line in a file.
     * @param file The file that is going to be searched.
     * @param propertyLine The line that is going be be found.
     * @return An array with 3 strings in the respective order: the data before the propertyLine, 
     * the propertyLine with the indentation it possesses in the file, and the data after it.
     * it
     */
    private static String[] _findPropertyLine(File file, String propertyLine){
	//number of the line
	boolean lineFound = false;
	String content ="";
	String dataBefore="";
	String dataAfter="";
	String line="";
	try{
	    BufferedReader br = new BufferedReader(new FileReader(file));
	    content=br.readLine();
	    while (content != null) {
		if(!content.isEmpty()||!(content.equals("\n"))||!(content.equals(" "))){
		    if(content.contains(propertyLine) && !lineFound){
			line = content.substring(0,content.length()-2);
			line = line.substring(line.lastIndexOf("\"")+1);
			try{
			    Double.valueOf(line);
			    line=content.substring(0,content.indexOf(line));
			    lineFound=true;
			}catch(Exception e){
			    if(dataBefore.isEmpty()){
				dataBefore=content;
			    }else{
				dataBefore = dataBefore+ "\n"+ content;
			    }
			}     
		    }else if(!lineFound){
			if(dataBefore.isEmpty()){
			    dataBefore=content;
			}else{
			    dataBefore = dataBefore+ "\n"+ content;
			}
		    }else{
			if(dataAfter.isEmpty()){
			    dataAfter=content;
			}else{
			    dataAfter = dataAfter+ "\n"+ content;
			}
		    }
		}
		content=br.readLine();
	    }
	    br.close();
	    String[] data={dataBefore,line,dataAfter};
	    return data;
	}
	catch(Exception e){
	    System.out.println("Couldn't find the string.");
	    return null;
	}
    }

    /** Write some information in a file.
     * 
     * @param file The file were the information is going to be written.
     * @param data The information that is going to be written.
     */
    private static void _writeInFile(File file,String data){
	try{
	    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
	    writer.write(data);
	    writer.flush();
	    writer.close();
	}catch(Exception e){
	    System.out.println("Couldn't write in the file.");
	}
    }
    /**Change a parameter in the .xml, making it vary within an interval defined by the
     * parameters start and end. Run a ptolemy simulation for each step between the interval.  
     * @param vergil An instance of vergil.
     * @param modelPath The path to the model you want to run
     * @param propertyLine The xml line of the parameter that we want to change. 
     * @param start The value of the parameter.
     * @param end The end value of the parameter.
     * @param step The increment of the parameter value.
     */
    public static void changeParameter(AutomaticSimulation vergil,String[] modelPath, String propertyLine, float start, float end, float step){
        int numberOfFederates = modelPath.length;
        File[] file = new File[numberOfFederates];
        String[][] data = new String[numberOfFederates][3];
        
      //In order to avoid precision errors.
        //I will use it later on to round the result and make sure the new value of
        //the property doesn't get more digits than the step
        String s = "" + step;
        s= s.substring(s.indexOf("."));
        int numberOfDecimalDigits = s.length();
        
        for(int i=0;i<numberOfFederates;i++){   
            file[i] = new File(modelPath[i]);
            String[] info=  _findPropertyLine(file[i], propertyLine);
            data[i][0]=info[0];
            data[i][1]=info[1];
            data[i][2]=info[2];  
        }
        int numberOfInteractions = Math.round(((end -start)/step) +1);
        for(int i=0;i<numberOfInteractions;i++){   
            //Changing the file

            //To avoid precision errors
            float x = start + i*step;
            x = (float) (Math.round(x*Math.pow(10,numberOfDecimalDigits ))/Math.pow(10,numberOfDecimalDigits));
            final CompositeEntity[] model= new CompositeEntity[numberOfFederates];
                    for(int j=0;j<numberOfFederates;j++){
                        final int y= j;
                        System.out.println("Reading file "+j +".");
                        String line = data[j][1] + x + "\">";
                        System.out.println(line);
                        _writeInFile(file[j], data[j][0] +"\n" + line +"\n" + data[j][2]);
                        
                        Runnable openModels = new Runnable() { 
                            @Override
                            public void run() {
                                try {
                                    model[y] = VergilApplication.openModelOrEntity(modelPath[y]);
                                    model[y].setPersistent(false);
                                    System.out.println("The model " +model[y].getDisplayName()+" is ready.");
                                } catch (Throwable e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }  
                            }
                        };try {
                            SwingUtilities.invokeAndWait(openModels);
                        } catch (InvocationTargetException | InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        
                    }
                    //Executing the file
                    try{
                        Thread.sleep(3000);
                        Runnable runModels = new Runnable() { 
                            @Override
                            public void run() {
                                try {
                                    vergil._runModels();
/*                                    List<NamedObj> activeModels = vergil.models();
                                    for(int j=0;j<activeModels.size();j++){
                                        System.out.println(activeModels.get(j).getFullName());
                                    }*/
                                    vergil.waitForFinish();
                                } catch (Throwable e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }  
                            }
                        };
                        
                        try{
                            SwingUtilities.invokeAndWait(runModels);
                        }catch(Throwable e){
                            Process p = Runtime.getRuntime().exec("pkill rtig");
                            p.waitFor();
                            i--;
                        }
                        /*try{
                            Thread control = new Thread(runModels);
                            control.start();
                            long endTime= System.currentTimeMillis() + 20000;
                            while(control.isAlive()){
                                if(System.currentTimeMillis()>endTime){
                                    control.interrupt();
                                    Process p = Runtime.getRuntime().exec("pkill rtig");
                                    p.waitFor();
                                    control.start();
                                    endTime= System.currentTimeMillis() + 20000;
                                }
                            }
                        }catch(Throwable e){
                            i--;
                        }*/
                        
                        Thread.sleep(2000);
                        for (int j = 0; j < numberOfFederates; j++) {
                            final Effigy eff = Configuration.findEffigy(model[j].toplevel());
                            System.out.println("Closing the model " + model[j].getDisplayName() + ".");
                            Runnable run = new Runnable() { 
                                @Override
                                public void run() {
                                    try {
                                        //eff.topEffigy();
                                        eff.setModified(false);
                                        eff.closeTableaux();
                                    } catch (Throwable e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }  
                                }
                            };
                            
                            try {
                                SwingUtilities.invokeAndWait(run);
                                Process p = Runtime.getRuntime().exec("pkill rtig");
                                p.waitFor();
                                System.out.println("Model " + model[j].getDisplayName() + " closed.");
                                
                            } catch (Throwable e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }

                    }catch(Exception e){
                        System.out.println("Could not close the file.");
                        e.printStackTrace();
                    }catch(Throwable e){
                        System.out.println("Could not close the file.");
                        e.printStackTrace();
                    }
        }
        try {
            Configuration.closeAllTableaux();
        } catch (IllegalActionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    /**Run all the models opened in different threads, waiting for the initialization 
     * of the current one to start executing a following one. 
     * @throws KernelException
     * @throws InterruptedException
     */
    private void _runModels() throws KernelException, InterruptedException {
        Iterator models = super.models().iterator();
        NamedObj model=null;
        Manager manager;
        _wait = false;

        while (models.hasNext()) {
            if(!_wait){
                model = (NamedObj) models.next();
                if (model instanceof CompositeActor) {
                    CompositeActor actor = (CompositeActor) model;

                    if (_statistics) {
                        System.out.println("Statistics for " + model.getFullName());
                        System.out.println(((CompositeEntity) model)
                                .statistics(null));
                    }

                    // Create a manager if necessary.
                    manager = actor.getManager();

                    if (manager == null) {
                        manager = new Manager(actor.workspace(), "manager");
                        actor.setManager(manager);
                    }

                    manager.addExecutionListener(this);
                    this.setActiveCount(this.getActiveCount()+1);

                    // Run the model in a new thread.
                    _wait = true;
                    manager.startRun();
                    System.out.println("Executing the model " + model.getDisplayName() + ".");

                }
            }else{
                System.out.println("Waiting for initialization of the model " + model.getDisplayName()+".");
                Thread.sleep(50);
            }
        }
    }
    



    /**The main method.
     * @param args The execution arguments.
     */
    public static void main(String[] args) {
	try {
	    String[] modelPath = {"sender.xml","receiver.xml"};
	   /* String[] modelPath = {"f14_IMA04_Aircraft.xml",
	            "f14_IMA04_AutoPilot.xml","f14_IMA04_PilotStick.xml"};*/
	    
/*	    Scanner input = new Scanner(System.in);
	    System.out.println("How many ptolemy models will be a part of this simulation?");
	    int numberOfModels = input.nextInt();
	    String[] modelPath = new String[numberOfModels];
	    for (int i = 0; i < numberOfModels; i++) {
	        System.out.println("Type the name of the model " + i + ":");
	        modelPath[i]= input.next();
            }
	    System.out.println("What parameter do you want to change in the models?");
	    String param = input.next();
	    param = "<property name=\""+param+ "\" class=\"ptolemy.data.expr.Parameter\" value=\"";
	    System.out.println("What is the start value of the parameter?");
	    float startValue = input.nextFloat();
	    System.out.println("What is the final value of the parameter?");
            float endValue = input.nextFloat();
            System.out.println("What is the value of the parameter's increment?");
            float stepValue = input.nextFloat();
            System.out.println("The simulation is about to start...");*/
            
            String param = _timeStepLine;
            float startValue = (float) 0.01;
            float endValue = (float)0.02;
            float stepValue =(float) 0.01;
            
	 
	    AutomaticSimulation vergil = new AutomaticSimulation(args);
	    changeParameter(vergil,modelPath, 
	    param,startValue, endValue, stepValue);

	} catch (Throwable e) {
	    e.printStackTrace();
	}

    }



}
