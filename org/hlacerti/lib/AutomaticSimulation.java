/* An automatic simulation to track the performance of distributed models.

@Copyright (c) 2013-2015 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
package org.hlacerti.lib;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
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


/**Implement an automatic simulation of any model chosen by the user.
 * It allows the change of any parameter, making the values go from a start value
 * to an end value with a fixed increment.
 * 
 * The user will be able to choose the models, the parameter and the range during
 * the execution. The parameter to be changed must be specified with exactly the 
 * same name in an item parameter in the ptolemy model, otherwise this automatic 
 * simulation will not work.
 * @author Tarciana Cabral de Brito Guerra
 * @version $Id$
 * @since Ptolemy II 11.0
 *
 */
public class AutomaticSimulation extends VergilApplication implements ExecutionListener{

    /** Parse the specified command-line arguments, creating models
     *  and frames to interact with them.
     *  Look for configurations in "ptolemy/configs"
     *  @param args The command-line arguments.
     *  @exception Exception If command line arguments have problems.
     */
    public AutomaticSimulation(String[] args) throws Exception {
        super(args);
    }

    /**
     * 
     * @param file The file whose content is going to be turned into String.
     * @return ArrayList
     */
    public static ArrayList<String> convertFileToString(File file){
        String content ="";
        ArrayList<String> lines = new ArrayList<String>();
        try{
            BufferedReader br = new BufferedReader(new FileReader(file));
            content=br.readLine();
            while(content != null) {
                lines.add(content);
                content=br.readLine();
            }
            br.close();
            return lines;
        }catch(IOException e){
            e.printStackTrace();
            return null;
        }

    }
    
    private static ArrayList<String> _readFile(File file){
        ArrayList<String> lines= new ArrayList<String>();
        try{
            BufferedReader br = new BufferedReader(new FileReader(file));
            String content=br.readLine();
            while (content != null) {
                lines.add(content);
                content=br.readLine();
            }
            br.close();
            return lines;
        }catch(Exception e){
            System.out.println("Couldn't read the file.");
            System.exit(0);
            return null;
        }
    }
    
    private static ArrayList<String> _changeRKSolver(ArrayList<String> file, int newSolver){
        if(newSolver >0 ){
            String oldParameter="<property name=\"solver\" class=\"ptolemy.data.expr.StringParameter\" value=\"RK";
            String newParameter="<property name=\"solver\" class=\"ptolemy.data.expr.StringParameter\" value=\"RK"+newSolver+"\">";
            String oldPropertyLine ="<property name=\"ODESolver\" class=\"ptolemy.data.expr.StringParameter\" value=\"ExplicitRK";
            String newPropertyLine ="<property name=\"ODESolver\" class=\"ptolemy.data.expr.StringParameter\" value=\"ExplicitRK"+newSolver+"Solver\">"; 
            
            return _findAndChangePropertyLines(file, new String[]{oldPropertyLine, oldParameter}, new String[]{newPropertyLine, newParameter});   
        }else{
            return file;
        }
    }
    /**
     * Find a parameter value from a file.
     * @param file The file to be read
     * @param propertyLine The property line to be found
     * @param type The type of the data. 0 for String, 1 for int, 2 for float.
     * @return The parameter value.
     */
    public static String findParameterValue(File file, String propertyLine, int type){
        ArrayList<String> fileContent = _readFile(file);
        String value="UNDEFINED";
        for(String s: fileContent){
            if(s.contains(propertyLine)){
                try{
                    value=s.substring(s.lastIndexOf("value=\"")+7, s.lastIndexOf("\""));
                    if(type == 1){
                        Integer.parseInt(value);
                    }else if(type==2){
                        Float.parseFloat(value);
                    }
                    break;
                }
                catch(Exception e){
                    value ="UNDEFINED";
                }
            }
        }
        return value;
    }
 
    

    /**Change a parameter in the .xml, making it assume previously chosen values. Run a ptolemy simulation for each value that the 
     * parameter has to assume.
     * @param waitingTime The time the system will wait to close all the windows of a federation, after its execution has finished. 
     * This parameter is intended to give the user the ability to choose how long he will have to look at the simulation's results. 
     * If the variable is given a negative value, the user will be asked repetedly if he havalued time to look at the models and they will 
     * only close when he answers "yes".
     * @param vergil An instance of vergil.
     * @param modelPath The path to the model you want to run.
     * @param propertyLine The xml line of the parameter that we want to change. 
     * @param values The values the new property will assume.
     **/
    public static void changeParameter(int waitingTime,AutomaticSimulation vergil,String[] modelPath, String propertyLine, double[] values , int solver){
        int numberOfFederates = modelPath.length;
        File[] file = new File[numberOfFederates];
        String[][] data = new String[numberOfFederates][3];

        for(int i=0;i<numberOfFederates;i++){   
            file[i] = new File(modelPath[i]);
            ArrayList<String> content= _readFile(file[i]);
            content=_changeRKSolver(content, solver);
            String[] info=  _findPropertyLine(content, propertyLine);
            data[i][0]=info[0];
            data[i][1]=info[1];
            data[i][2]=info[2];  
        }
        int numberOfInteractions = values.length;
        for(int i=0;i<numberOfInteractions;i++){   
            double x = values[i];
            final CompositeEntity[] model= new CompositeEntity[numberOfFederates];
            for(int j=0;j<numberOfFederates;j++){
                System.out.println("Reading file "+j +".");
                String line = data[j][1] + x + "\">";
                _writeInFile(file[j], data[j][0] +"\n" + line +"\n" + data[j][2]);
                model[j]=_openModel(modelPath[j]);

            }
            //Executing the file
            runAllModels(vergil);
            if(waitingTime<0){
                while(true){
                    Scanner input = new Scanner(System.in);
                    System.out.println("Have you had enough time to see the graphics?");
                    String answer = input.next();
                    if(answer.equalsIgnoreCase("yes"))
                        break;
                }
                _sleep(2000);
            }else if(waitingTime>0){
                _sleep(waitingTime);
            }
            for (int j = 0; j < numberOfFederates; j++) {
                _closeModel(model[j]);
            }
            _killRTIG();

        }
        try {
            Configuration.closeAllTableaux();
        } catch (IllegalActionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**Change a parameter in the .xml, making it vary within an interval defined by the
     * parameters start and end. Run a ptolemy simulation for each step between the interval. 
     * @param waitingTime The time the system will wait to close all the windows of a federation, after its execution has finished. 
     * This parameter is intended to give the user the ability to choose how long he will have to look at the simulation's results. 
     * If the variable is given a negative value, the user will be asked repetedly if he had time to look at the models and they will 
     * only close when he answers "yes".
     * @param vergil An instance of vergil.
     * @param modelPath The path to the model you want to run.
     * @param propertyLine The xml line of the parameter that we want to change. 
     * @param start The value of the parameter.
     * @param end The end value of the parameter.
     * @param step The increment of the parameter value.
     */
    public static void changeParameter(int waitingTime,AutomaticSimulation vergil,String[] modelPath, String propertyLine, float start, float end, float step, int solver){
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
            ArrayList<String> content= _readFile(file[i]);
            content=_changeRKSolver(content, solver);
            String[] info=  _findPropertyLine(content, propertyLine);
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
                System.out.println("Reading file "+j +".");
                String line = data[j][1] + x + "\">";
                _writeInFile(file[j], data[j][0] +"\n" + line +"\n" + data[j][2]);
                model[j]=_openModel(modelPath[j]);

            }
            //Executing the file
            runAllModels(vergil);
            if(waitingTime<0){
                while(true){
                    Scanner input = new Scanner(System.in);
                    System.out.println("Have you had enough time to see the graphics?");
                    String answer = input.next();
                    if(answer.equalsIgnoreCase("yes"))
                        break;
                }
                _sleep(2000);
            }else if(waitingTime>0){
                _sleep(waitingTime);
            }
            for (int j = 0; j < numberOfFederates; j++) {
                _closeModel(model[j]);
            }
            _killRTIG();

        }
        try {
            Configuration.closeAllTableaux();
        } catch (IllegalActionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**Change a parameter in the .xml, making it vary within an interval defined by the
     * parameters start and end. Run a ptolemy simulation for each step between the interval. 
     * @param waitingTime The time the system will wait to close all the windows of a federation, after its execution has finished. 
     * This parameter is intended to give the user the ability to choose how long he will have to look at the simulation's results. 
     * If the variable is given a negative value, the user will be asked repetedly if he had time to look at the models and they will 
     * only close when he answers "yes".
     * @param vergil An instance of vergil.
     * @param modelPath The path to the model you want to run.
     * @param propertyLines The xml line of the parameter that we want to change. 
     * @param values The values the new property will assume.
     * @param solver The Runge-Kutta solver order number.
     **/
    public static void changeParameters(int waitingTime,AutomaticSimulation vergil,String[] modelPath, String[] propertyLines, double[][] values, int solver){
        if(propertyLines.length!= values.length){
            System.out.println("The variable #propertyLines must have the number of elements equal to the number of lines of the variable #values.");
            return;
        }
        int numberOfFederates = modelPath.length;
        int numberOfParameters= propertyLines.length;

        File[] file = new File[numberOfFederates];
        String[][][] data = new String[numberOfFederates][numberOfParameters+1][2];

        for(int i=0;i<numberOfFederates;i++){   
            file[i] = new File(modelPath[i]);
            ArrayList<String> content= _readFile(file[i]);
            content=_changeRKSolver(content, solver);
            data[i]=_findPropertyLines(content, propertyLines); 
        }
        int numberOfInteractions = values[0].length;
        for(int i=0;i<numberOfInteractions;i++){   
            final CompositeEntity[] model= new CompositeEntity[numberOfFederates];
            for(int j=0;j<numberOfFederates;j++){
                StringBuffer info = new StringBuffer();
                for (int y = 0; y < numberOfParameters; y++) {
                    info.append("\n" + data[j][y][0] +"\n"+ data[j][y][1]+values[y][i]+ "\">");
                }
                System.out.println("Reading file "+j +".");

                _writeInFile(file[j], info.substring(1) + "\n"+ data[j][numberOfParameters][0]);
                model[j]=_openModel(modelPath[j]);

            }
            //Executing the file
            runAllModels(vergil);
            if(waitingTime<0){
                while(true){
                    Scanner input = new Scanner(System.in);
                    System.out.println("Have you had enough time to see the graphics?");
                    String answer = input.next();
                    if(answer.equalsIgnoreCase("yes"))
                        break;
                }
                _sleep(2000);
            }else if(waitingTime>0){
                _sleep(waitingTime);
            }
            for (int j = 0; j < numberOfFederates; j++) {
                _closeModel(model[j]);
            }
            _killRTIG();

        }
        try {
            Configuration.closeAllTableaux();
        } catch (IllegalActionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**The main method.
     * @param args The command-line arguments.
     */
    public static void main(String[] args) {
        try {
//            String[] modelPath = {"TestModels/sender.xml","TestModels/receiver.xml"};
//            String[] modelPath2 = {"TestModels/f14Aircraft.xml",
//                    "TestModels/f14AutoPilot.xml","TestModels/f14PilotStick.xml"};
            
//            File file = new File("toto.xml");
//            String toPrint[]= _readFile(file);
//            File file2 = new File("toto2.xml");
//            file2.createNewFile();
//            StringBuffer content= new StringBuffer("");
//            for(String s:toPrint){
//                System.out.println(s);
//                content.append(s + "\n");
//            }
//            _writeInFile(file2, content.toString());
            

            AutomaticSimulation vergil = new AutomaticSimulation(args);
            Scanner input = new Scanner(System.in);
            System.out.println("How many ptolemy models will be a part of this simulation?");
            int numberOfModels = input.nextInt();
            String[] modelPath = new String[numberOfModels];
            System.out.println("You will now e requested to type the name of the models. "
                    + "They will run in the same order that you've type, so remember that the syncronization point register must be the last.");
            for (int i = 0; i < numberOfModels; i++) {
                System.out.println("Type the name of the model " + i + ":");
                modelPath[i]= input.next();
            }System.out.println("How many seconds would you like to have to see the results? "
                    + "(Type a negative value if you want to be asked if you are ready to close the models.) ");
            int waitingTime=input.nextInt();
            waitingTime = waitingTime*1000;
            System.out.println("Would you like to change the RK solver of the models?\n(This will only change something if there's a continuous director in at least one of the federates)\n"
                    + "(Type a positive number to change the RK solver)");
            int solver=input.nextInt();
            System.out.println("How many parameterers would you like to change in the models?");
            int numberOfParameters = input.nextInt();
            if(numberOfParameters==1){
                System.out.println("What is the name of this parameter(remember that the ptolemy model must have a variable 'parameter'(that can be found on "
                        + "the library utilities/Parameters) with exacly the same name for this simulation to work.)?");
                String parameter= input.next();
                parameter = "<property name=\""+parameter+ "\" class=\"ptolemy.data.expr.Parameter\" value=\"";
                System.out.println("Would you like to:\n1 - Type all the values your parameter will assume, or\n"
                        + "2 - Make it vary within a range that you are going to be requested to choose?");
                int answer = input.nextInt();
                if(answer == 2){
                    System.out.println("What is the start value of the parameter?");
                    float startValue = input.nextFloat();
                    System.out.println("What is the final value of the parameter?");
                    float endValue = input.nextFloat();
                    System.out.println("What is the value of the parameter's increment?");
                    float stepValue = input.nextFloat();
                    System.out.println("The simulation is about to start...");
                    changeParameter(waitingTime, vergil, modelPath, parameter, startValue, endValue, stepValue, solver);
                }else{
                    System.out.println("How many values would you like the parameter to assume ?");
                    int numberOfValues = input.nextInt();
                    double[] values = new double[numberOfValues];
                    for (int i = 0; i < numberOfValues; i++) {
                        System.out.println("Type the name of the value "+ i + ":");
                        values[i]= input.nextDouble();
                    }
                    System.out.println("The simulation is about to start...");
                    changeParameter(waitingTime, vergil, modelPath, parameter, values, solver);
                }

            }else{
                String[] parameters = new String[numberOfParameters];
                System.out.println("You will now e requested to type the name of the parameters. "
                        + "Remember that the ptolemy model must have a variable 'parameter'(that can be found on "
                        + "the library utilities/Parameters) with exacly the same name for this simulation to work. Also,"
                        + "the parameters must be listed here in the same order as they've been created in the model. "
                        + "If you are not sure about who comes first, look in the .xml files.");
                System.out.println("How many values would you like each parameter to assume ?");
                int numberOfValues = input.nextInt();
                double[][] values = new double[numberOfParameters][numberOfValues];
                for (int i = 0; i < numberOfParameters; i++) {
                    System.out.println("Type the name of the parameter "+ i + ":");
                    parameters[i]= input.next();
                    parameters[i] = "<property name=\""+parameters[i]+ "\" class=\"ptolemy.data.expr.Parameter\" value=\"";
                    for (int j = 0; j < numberOfValues; j++) {
                        System.out.println("Type the name of the value "+ j + ":");
                        values[i][j]= input.nextDouble();
                    }
                }
                System.out.println("The simulation is about to start...");
                changeParameters(waitingTime, vergil, modelPath, parameters, values, solver);
            }
//            String param = "lookAhead";
//            param = "<property name=\""+param+ "\" class=\"ptolemy.data.expr.Parameter\" value=\"";
//            float startValue = (float) 0.1;
//            float endValue = (float) 0;
//            float stepValue =(float) 5;
//            double[] values = {0.005,0.001};

            //changeParameter(2000,vergil,modelPath, param,values);
        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(0);
        }

    }

    /**Verify if the model is initializing, changing the variable _wait to false if 
     * this execution phase has been achieved. 
     * @see ExecutionListener
     * @param manager The manager.
     */
    @Override
    public void managerStateChanged(Manager manager) {
        String state = manager.getState().toString();
        if(state.contains("The model is initializing")){
            _wait = false;
        }
        System.out.println(manager.getFullName() +" "+ manager.getState());
    }

    public static boolean runAllModels(AutomaticSimulation vergil){
        Runnable runModels = new Runnable() { 
            @Override
            public void run() {
                try {
                    vergil._runModels();
                    vergil.waitForFinish();
                } catch (Throwable e) {
                    e.printStackTrace();
                }  
            }
        };

        try{
            SwingUtilities.invokeAndWait(runModels);
            return true;
        }catch(Throwable e){
            _killRTIG();
            return false;
        }
    }

    private static boolean _closeModel(CompositeEntity model){
        final Effigy eff = Configuration.findEffigy(model.toplevel());
        System.out.println("Closing the model " + model.getDisplayName() + ".");
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
                    _killRTIG();
                    System.exit(0);
                }  
            }
        };

        try {
            SwingUtilities.invokeAndWait(run);
            System.out.println("Model " + model.getDisplayName() + " is closed.");
            return true;

        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            _killRTIG();
            System.exit(0);
            return false;
        }
    }
    
    private static ArrayList<String> _findAndChangePropertyLines(ArrayList<String> file, String[] oldPropertyLines, String[] newPropertyLines){
        ArrayList<String> newContent = new ArrayList<String>();
        for(String s: file){
            boolean lineFound=false;
            for(int i=0;i<oldPropertyLines.length;i++){
                if(s.contains(oldPropertyLines[i])){
                    String identation = s.substring(0, s.lastIndexOf(oldPropertyLines[i]));
                    newContent.add(identation+newPropertyLines[i]);
                    lineFound=true;
                    break;
                }
            }if(!lineFound){
                newContent.add(s);
            }
        }return newContent;
       
    }

    /** Find some property line in a file.
     * @param file The file that is going to be searched.
     * @param propertyLine The line that is going be be found.
     * @return An array with 3 strings in the respective order: the data before the propertyLine, 
     * the propertyLine with the indentation it possesses in the file, and the data after it.
     * it
     */
    private static String[] _findPropertyLine(ArrayList<String> file, String propertyLine){
        //number of the line
        boolean lineFound = false;
        String dataBefore="";
        String dataAfter="";
        String line="";
        for(String content: file){
            if(!content.isEmpty()||!(content.equals("\n"))||!(content.equals(" "))){
                if(content.contains(propertyLine) && !lineFound){
                    line = content.substring(0,content.length()-2);
                    line = line.substring(line.lastIndexOf("\"")+1);
                    try{
                        Double.valueOf(line);
                        line=content.substring(0,content.indexOf(line));
                        lineFound=true;
                        System.out.println(true);
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
        }
        String[] data={dataBefore,line,dataAfter};
        if(line.equals("")){
            System.out.println("Could not find the parameter.");
            System.exit(0);
            return null;
        }
        return data;
    }

    /** Find some property lines in a file.
     * @param file The file that is going to be searched.
     * @param propertyLines The line that is going be be found.
     * @return A two-dimensional array with the length of the @param propertyLines increased by 1 and 2 columns. The first one 
     * represents the information written on the file after the one property line and before the next one. The second column 
     * represents the property lines with the same indentation as they were found on the file. The second column of the last 
     * line contains nothing but "", as there's no property left to be written. 
     */
    private static String[][] _findPropertyLines(ArrayList<String> file, String[] propertyLines){
        //number of the line


        int numberOfLines=file.size();
        int numberOfProperties=propertyLines.length;
        String[][] result= new String[ numberOfProperties+1][2];
        int linesFound=0 ;
        String content ="";
        result[0][0]="";
        for (int i = 0; i < numberOfLines; i++) {
            content=file.get(i);
            if(linesFound<numberOfProperties){
                if(content.contains(propertyLines[linesFound])){
                    String line = content.substring(0,content.length()-2);
                    line = line.substring(line.lastIndexOf("\"")+1);
                    try{
                        Double.valueOf(line);
                        result[linesFound][1]=content.substring(0,content.indexOf(line));                                        
                        linesFound++;
                        result[linesFound][0]="";
                    }catch(Exception e){
                        if(!result[linesFound][0].equals("")){
                            content = "\n" + content;
                        }
                        result[linesFound][0] = result[linesFound][0] + content;
                    }     
                }else{
                    if(!result[linesFound][0].equals("")){
                        content = "\n" + content;
                    }
                    result[linesFound][0] = result[linesFound][0] + content;
                }
            }else{
                if(!result[linesFound][0].equals("")){
                    content = "\n" + content;
                }
                result[linesFound][0]=result[linesFound][0] + content;
            }
        }result[linesFound][1]="";
        if(result[numberOfProperties][0]==null ||result[numberOfProperties][0].equals("")){
            System.out.println("Could not find all the parameters.");
            System.exit(0);
        }
        return result;
    }



    private static void _killRTIG(){
        try {
            Process p = Runtime.getRuntime().exec("pkill rtig");
            p.waitFor();
        } catch (InterruptedException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.exit(0);
        }
    }

    private static CompositeEntity _openModel(String  modelPath){
        CompositeEntity[] model=new CompositeEntity[1];
        Runnable openModel = new Runnable() { 
            @Override
            public void run() {
                try {
                    model[0] = VergilApplication.openModelOrEntity(modelPath);
                    model[0].setPersistent(false);
                    System.out.println("The model " +model[0].getDisplayName()+" is ready.");
                } catch (Throwable e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    System.exit(0);
                }  
            }
        };
        try {
            SwingUtilities.invokeAndWait(openModel);
            return model[0];
        } catch (InvocationTargetException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

    }

    /**Run all the models opened in different threads, waiting for the initialization 
     * of the current one to start executing a following one. 
     * @throws KernelException
     */
    private void _runModels() throws KernelException{
        Iterator<NamedObj> models = super.models().iterator();
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
                _sleep(50);
            }
        }
    }

    private static void _sleep(int miliSeconds){
        try {
            Thread.sleep(miliSeconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(0);
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

    private boolean _wait;



}
