package org.ptolemy.machineLearning.hsmm;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.cureos.numerics.Calcfc;
import com.cureos.numerics.Cobyla;

import ptolemy.actor.parameters.FilePortParameter;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.math.SignalProcessing;
import ptolemy.util.StringUtilities;

public class HSMMTimeAwareMultinomialEstimatorWithCodegen extends
HSMMTimeAwareMultinomialEstimator {

    public HSMMTimeAwareMultinomialEstimatorWithCodegen(CompositeEntity container,
            String name) throws NameDuplicationException,
            IllegalActionException {
        super(container, name);

        maxStep = new Parameter(this, "maxStep");
        maxStep.setExpression("288");

        filename = new FilePortParameter(this, "filename");
        filename.setExpression("edhmm.pm");
        filename.setTypeEquals(BaseType.STRING);
        new SingletonParameter(filename.getPort(), "_showName")
        .setToken(BooleanToken.TRUE);


        propertyFile = new FilePortParameter(this, "propertyFile"); 
        propertyFile.setExpression("properties.pctl");


        modelChecking = new Parameter(this, "modelChecking");
        modelChecking.setTypeEquals(BaseType.BOOLEAN);
        modelChecking.setExpression("true");
        _useModelChecking = true;

        optimize = new Parameter(this,"optimize");
        optimize.setTypeEquals(BaseType.BOOLEAN);
        optimize.setExpression("false");
        _useOptimization = false;
    }


    /** The name of the file to write to.
     *  By default, this parameter contains an empty string, which
     *  is interpreted to mean that output should be directed to the
     *  standard output.
     *  See {@link ptolemy.actor.parameters.FilePortParameter} for
     *  details about relative path names.
     */
    public FilePortParameter filename;

    public FilePortParameter propertyFile;

    public Parameter maxStep;

    /**
     * If set to true, model will be parameterized and parameter synthesis will be
     * carried out using PRISM
     */
    public Parameter optimize;

    /**
     * If set to true, PRISM will be used to model check defined model properties
     */
    public Parameter modelChecking;

    public void attributeChanged(Attribute attribute) throws IllegalActionException {
        if (attribute == maxStep) {
            _maxStep = ((IntToken)maxStep.getToken()).intValue();
        } else if(attribute==optimize) {
            _useOptimization = ((BooleanToken)optimize.getToken()).booleanValue();
        } else if(attribute==modelChecking) {
            _useModelChecking = ((BooleanToken)modelChecking.getToken()).booleanValue();
        } else {
            super.attributeChanged(attribute);
        }
    }
    public void fire() throws IllegalActionException { 

        super.fire(); 

        if (_useModelChecking) {
            _writeProperties(); 
            double[] powerSpecs = _computeHourlyPowerSpecs(SPEC_TYPE.MAXIMUM);
            for ( int validHour=0; validHour < NUM_CATEGORIES; validHour++) { 
                if (powerSpecs[validHour] == 0.0) {
                    _code = generateCode(validHour);  
                    _writeModelFile(); 
                    double[] optima = _modelCheck(validHour);
                    if (_useOptimization& !_optimizeOverAll) {
                        _updateAt(optima,validHour);
                    }
                }
            }
        }

        Token[] Atokens = new Token[NUM_CATEGORIES];
        for (int i = 0 ; i < NUM_CATEGORIES; i++) { 
            Atokens[i] = new DoubleMatrixToken(At[i]);
        }

        empiricalStartTimes.send(0, new ArrayToken(Atokens));

    } 

    @Override
    public void _sendEmpiricalMatrix() {
        return;
    }
    private void _updateAt(double[] optima,int validHour) {
        int k =0;
        for (int[] z :incompleteCategories) {
            if (z[0] == validHour) {
                int i = z[1]; 
                At[validHour][i][0] = 1.0-optima[k];
                At[validHour][i][i] = optima[k]; 
                k++;
            }
        }
    }

    private double[] _modelCheck(int hour) throws IllegalActionException { 

        if (_useOptimization) {
            int nVariables;
            int numConstraints;

            List indices = new ArrayList<Integer>();
            if (_optimizeOverAll) { 
                int pCount =0;
                for (int[] z :incompleteCategories) {
                    if (z[0] == hour) { 
                        for (int j = 0 ; j < _nStates-1; j++) {
                            pCount++;
                        }
                    }
                }
                nVariables = (pCount+1);
                numConstraints = nVariables*2 + (nVariables/7)*2;
            } else {
                for (int[] z :incompleteCategories) {
                    if (z[0] == hour) { 
                        indices.add(z[1]);
                    }
                } 
                nVariables = indices.size();
                numConstraints = 2*nVariables;
            } 


            Calcfc calcfc = new Calcfc() {
                @Override
                public double Compute(int n, int m, double[] x, double[] con,
                        boolean[] terminate) throws IllegalActionException {
                    String paramValues = "hTest="+hour+",";
                    boolean safeToCall = true;
                    // assign probabilities to transition probs
                    if (_optimizeOverAll) {
                        double consSum=0.0;
                        int k=0;
                        for (int i =0; i < x.length; i++) {
                            paramValues += "p"+i+"="+x[i]+",";
                            con[k++] = x[i];
                            con[k++] = 1.0-x[i];
                            consSum +=x[i];
                            System.out.print(x[i]+ " ");
                            if (i%7 == 6) {
                                con[k++] = 1.0-consSum;
                                con[k++] = consSum;
                                consSum = 0.0;
                                System.out.println();
                            }
                        }
                        for (int i =0; i < m; i++) {
                            if (con[i] < 0.0) {
                                safeToCall = false;
                                break;
                            }
                        }
                    } else {
                        int k=0;
                        for (int i =0; i < x.length; i++) {
                            paramValues += "p"+indices.get(i)+"="+x[i]+",";
                            con[k++] = x[i];
                            con[k++] = 1.0-x[i];
                        }
                        for (int i =0; i < m; i++) {
                            if (con[i] < 0.0) {
                                safeToCall = false;
                                break;
                            }
                        }
                    }
                    if (safeToCall) {
                        paramValues = paramValues.substring(0,paramValues.length()-1); //omit last comma
                        ProcessBuilder pb = new ProcessBuilder("prism",filename.getExpression(),
                                propertyFile.getExpression(),"-const",paramValues,"-prop",""+(hour+1),"-exportresults","stdout:csv");
                        pb.directory(new File("/Users/oldilge/Documents/Control Improvisation - Lighting multiInputData/lightingImproModels"));

                        try {
                            Process pr = pb.start(); 
                            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(pr.getInputStream())); 
                            String line = null;
                            while ((line = in.readLine()) != null) {
                                //System.out.println(line);
                                if (line.equals("Result")) {
                                    double d = Double.parseDouble(in.readLine());
                                    System.out.println(d);
                                    if(d>_optimum) {
                                        _optimum=d;
                                    }
                                    // trying to maximize
                                    return -d;
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return 0;
                }
            };

            boolean[] term = {_stopRequested};
            double[] start = new double[nVariables];
            if (!_optimizeOverAll) {
                for (int i=0; i < start.length; i++) {
                    start[i]=1.0;
                }
            }
            Cobyla.FindMinimum(calcfc, nVariables,
                    numConstraints, start, 1.0, 1E-2, 0,
                    200, term); 

            System.out.println("Optimal Probabilities For Hour " + hour +" = ");
            for (int i = 0; i <start.length; i++) {
                System.out.print(start[i]+ " ");
            }
            System.out.println();
            System.out.println(_optimum);
            _optimum=0.0;
            return start;

        } else {
            try{
                ProcessBuilder pb = new ProcessBuilder("prism",filename.getExpression(),
                        propertyFile.getExpression(),"-const", "hTest="+hour,"-prop",""+(hour+1),"-exportresults","resultNoOpt"+hour+".txt:csv");
                pb.directory(new File("/Users/oldilge/Documents/Control Improvisation - Lighting multiInputData/lightingImproModels"));
                Process pr = pb.start(); 
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(pr.getInputStream())); 
                String line = null;
                while ((line = in.readLine()) != null) { 
                    System.out.println(line); 
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void _writeProperties() throws IllegalActionException {
        // write code

        double[] powerSpecs = _computeHourlyPowerSpecs(SPEC_TYPE.MAXIMUM);
        java.io.Writer writer;


        String properties = "";
        // check some properties
        for (int i = 0 ; i< NUM_CATEGORIES; i++) {
            if (powerSpecs[i] > 0.0) {
                properties += "P=?[G pow <= Plimit_" + i +"]" + _eol;
            } else {
                properties += "P=?[G pow <= 20.0]" + _eol;
            }
        }

        try {
            writer = propertyFile.openForWriting();
            writer.write(properties);
            writer.close();  
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void _writeModelFile() throws IllegalActionException {
        // write code

        java.io.Writer writer = filename.openForWriting();
        try {
            writer.write(_code.toString());
            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
    }
    /**
     * Compute the avg power consumption for each hour 
     * @return
     */
    private double[] _computeHourlyPowerSpecs(SPEC_TYPE type) {
        double[] hourSpecs = new double[NUM_CATEGORIES];
        int[] counts = new int[NUM_CATEGORIES];

        if (type == SPEC_TYPE.AVERAGE) {
            for (int i = 0; i < _observations.length; i ++) {
                double totalPower = 0.0;
                for (int j=0; j< _observations[i].length; j++) {
                    totalPower += _observations[i][j];
                }
                hourSpecs[_hourOfDay[i]] += totalPower;
                counts[_hourOfDay[i]] ++;
            }
            for (int i = 0; i < hourSpecs.length; i++) {
                if (counts[i] >0 ) {
                    hourSpecs[i] /= counts[i];
                }
            }
        } else if(type == SPEC_TYPE.MAXIMUM) {
            for (int i = 0; i < _observations.length; i ++) { 
                double totalPower = 0.0;
                for (int j=0; j< _observations[i].length; j++) {
                    totalPower += _observations[i][j];
                }  
                if (totalPower > hourSpecs[_hourOfDay[i]]) {
                    hourSpecs[_hourOfDay[i]] = totalPower;
                } 
            } 
        }
        return hourSpecs;
    }
    private StringBuffer generateCode(int validHour) throws IllegalActionException {
        // Generate PRISM code 
        StringBuffer code = new StringBuffer();

        // Must be called first, since PMAX is determined by this method.
        List stateEmissions = _computeTotalPowerConsumptionDistributions();   

        if (_useOptimization) {
            code.append(_getHeader(validHour)); 
        } else {
            code.append(_getHeader());
        } 

        code.append(_getStateSpaceAutomaton(stateEmissions,validHour));
        code.append(_getDurationAutomaton()); 

        code.append(_getStepCountAutomaton());
        code.append(_getTimeOfDayAutomaton());

        return code;
    }

    private String _getDurationAutomaton() {
        String code = INDENT1 + "module durationAutomaton" + _eol +
                INDENT2 + "d : [0..DMAX] init 0;" + _eol ;

        code+=(" " + _eol); 
        String ins = "";
        for (int i = 0; i < D_new.length; i++) {
            // initDist=false &
            ins = INDENT2 +  "[tr] d=0 & s=" + i + " -> "; 
            D_new[i] = _cleanAndTruncate(D_new[i], threshold, precision);  

            for (int j=0; j < D_new[0].length; j++) {
                if (D_new[i][j] > 0.0) {
                    ins += D_new[i][j] + " : (d'=" + j + ") + "; 
                } 
            }
            ins = ins.substring(0,ins.length()-3) + ";" + _eol;
            code+=(ins);
        } 

        code+=(INDENT2 + "[step] d > 0 -> (d'=d-1);" + _eol);

        code+=(INDENT1 +"endmodule" + _eol+ " " + _eol); 
        return code;
    }

    private String _getStateSpaceAutomaton(List stateEmissions, int validHour) {
        String code = INDENT1 + "module stateSpace" + _eol + 
                INDENT2 + "s : [0.. " + (_nStates-1) + "] init 0;" + _eol +
                INDENT2 + "pow : [0..PMAX] init 0; " + _eol + 
                INDENT2 + "initState : bool init true;"+_eol;

        code += _getPriors();

        // write emissions
        for (int s = 0; s <_nStates; s++) {
            String emissions = INDENT2 +  "[step] s=";
            emissions += s + "& d>0 & initState = false & testThisHour = true -> ";
            //       + "initDist=false -> ";
            double[] p = (double[])stateEmissions.get(s);
            p = _cleanAndTruncate(p, threshold, precision);
            for (int d=0; d< p.length; d++) {
                if (p[d] > 0.0) {
                    emissions += p[d] + ": (pow' =" + d + ") + ";
                }
            }
            // remove the extra plus
            emissions = emissions.substring(0,emissions.length()-3) + ";" + _eol;
            code+=(emissions);
        } 

        code += INDENT2 + "[step] testThisHour = false & initState = false -> (pow' = 0);" +_eol;



        String guard;

        if (!_useOptimization) {
            for (int hour = 0; hour < NUM_CATEGORIES; hour ++) {
                double[][] A = At[hour]; 
                for ( int i = 0 ; i < _nStates; i++) {
                    //initDist=false & 
                    guard = INDENT2 +  "[tr] d=0 & (s=" + i + ") & h = " + hour + " -> "; 
                    A[i] = _cleanAndTruncate(A[i], threshold, precision);
                    for (int j=0; j < _nStates; j++) {
                        if(A[i][j]>0) {
                            guard += A[i][j] + " : (s'=" + j + ") + ";
                        }
                    }
                    guard = guard.substring(0,guard.length()-3) + ";" + _eol;
                    code+=(guard);
                }  
                code+=(" " + _eol);
            } 
        } else { 
            for (int hour = 0; hour < NUM_CATEGORIES; hour ++) {
                double[][] A = At[hour];
                if (hour != validHour) {
                    for ( int i = 0 ; i < _nStates; i++) {
                        //initDist=false & 
                        guard = INDENT2 +  "[tr] d=0 & (s=" + i + ") & h = " + hour + " -> "; 
                        A[i] = _cleanAndTruncate(A[i], threshold, precision);
                        for (int j=0; j < _nStates; j++) {
                            if(A[i][j]>0) {
                                guard += A[i][j] + " : (s'=" + j + ") + ";
                            }
                        }
                        guard = guard.substring(0,guard.length()-3) + ";" + _eol;
                        code+=(guard);
                    } 
                } else { 
                    if (_optimizeOverAll) {
                        // for valid hour, generate parametric state transition matrix
                        // ONLY for those states that haven't been generated by the 
                        // original learning method.
                        int pIndex=0;
                        List done = new ArrayList<Integer>();
                        for (int[] z :incompleteCategories) {
                            if (z[0] == validHour) {
                                int i = z[1];
                                done.add(i);
                                String stateSum = "(" ;
                                guard = INDENT2 +  "[tr] d=0 & (s=" + i + ") & h = " + hour + " -> ";  
                                for (int j=1; j < _nStates; j++) { 
                                    guard += "p"+pIndex + " : (s'=" + j + ") + "; 
                                    stateSum += "p"+pIndex+"+";
                                    pIndex ++;
                                }
                                stateSum = stateSum.substring(0,stateSum.length()-1)+")";
                                guard += "1-"+stateSum+": (s'=" + 0+");" + _eol;


                                //guard += "1-p"+i+" : (s'=" + 0 + ") + p"+i+" : (s'=" + i + ");" + _eol;  
                                code+=(guard); 
                            } 
                        }
                        for ( int i = 0 ; i < _nStates; i++) {
                            if (!done.contains(i)) {
                                guard = INDENT2 +  "[tr] d=0 & (s=" + i + ") & h = " + hour + " -> "; 
                                A[i] = _cleanAndTruncate(A[i], threshold, precision); 
                                for (int j=0; j < _nStates; j++) {
                                    if(A[i][j]>0) {
                                        guard += A[i][j] + " : (s'=" + j + ") + ";
                                    }
                                }
                                guard = guard.substring(0,guard.length()-3) + ";" + _eol;
                                code+=(guard);
                            }
                        }
                    } else {
                        List done = new ArrayList<Integer>();
                        for (int[] z :incompleteCategories) {
                            if (z[0] == validHour) {
                                int i = z[1];
                                done.add(i);
                                guard = INDENT2 +  "[tr] d=0 & (s=" + i + ") & h = " + hour + " -> "; 
                                guard += "1-p"+i+": (s'=" + 0 + ") + p"+i+" : (s'="+i+");"+_eol;
                                code+=(guard);
                            }
                        }
                        for ( int i = 0 ; i < _nStates; i++) {
                            if (!done.contains(i)) {
                                guard = INDENT2 +  "[tr] d=0 & (s=" + i + ") & h = " + hour + " -> "; 
                                A[i] = _cleanAndTruncate(A[i], threshold, precision); 
                                for (int j=0; j < _nStates; j++) {
                                    if(A[i][j]>0) {
                                        guard += A[i][j] + " : (s'=" + j + ") + ";
                                    }
                                }
                                guard = guard.substring(0,guard.length()-3) + ";" + _eol;
                                code+=(guard);
                            }
                        }
                    }


                }
                code+=(" " + _eol);
            }
        }
        code+=(" " + _eol);
        code+=(INDENT1 +"endmodule" + _eol+ " " + _eol);
        return code;
    }
    private String _getPriors() {
        String priors = INDENT2 +  "[step] initState = true  -> ";
        double[] pr =_cleanAndTruncate(prior_new, threshold, precision);
        for (int i =0; i < pr.length; i++) {
            if (pr[i]>0){
                priors += pr[i] + ":(s'=" + i +")&(initState'=false) + ";
            }
        }
        priors = priors.substring(0,priors.length()-3) + ";" + _eol;
        return priors;
    }
    //    private String _getStateSpaceAutomaton(List stateEmissions) {
    //        String code = INDENT1 + "module stateSpace" + _eol + 
    //                INDENT2 + "s : [0.. " + (_nStates-1) + "] init 0;" + _eol +
    //                INDENT2 + "pow : [0..PMAX] init 0; " + _eol + 
    //                INDENT2 + "initState : bool init true;"+_eol;
    //
    //        String priors = INDENT2 +  "[step] initState = true  -> ";
    //        double[] pr =_cleanAndTruncate(prior_new, threshold, precision);
    //        for (int i =0; i < pr.length; i++) {
    //            if (pr[i]>0){
    //                priors += pr[i] + ":(s'=" + i +")&(initState'=false) + ";
    //            }
    //        }
    //        priors = priors.substring(0,priors.length()-3) + ";" + _eol;
    //        code += priors;
    //
    //        for (int s = 0; s <_nStates; s++) {
    //            String emissions = INDENT2 +  "[step] s=";
    //            emissions += s + "& d>0 & initState = false & testThisHour = true -> ";
    //            //       + "initDist=false -> ";
    //            double[] p = (double[])stateEmissions.get(s);
    //            p = _cleanAndTruncate(p, threshold, precision);
    //            for (int d=0; d< p.length; d++) {
    //                if (p[d] > 0.0) {
    //                    emissions += p[d] + ": (pow' =" + d + ") + ";
    //                }
    //            }
    //            // remove the extra plus
    //            emissions = emissions.substring(0,emissions.length()-3) + ";" + _eol;
    //            code+=(emissions);
    //        } 
    //
    //        code += INDENT2 + "[step] testThisHour = false & initState = false -> (pow' = 0);" +_eol;
    //
    //        String guard;
    //        for (int hour = 0; hour < NUM_CATEGORIES; hour ++) {
    //            double[][] A = At[hour]; 
    //            for ( int i = 0 ; i < _nStates; i++) {
    //                //initDist=false & 
    //                guard = INDENT2 +  "[tr] d=0 & (s=" + i + ") & h = " + hour + " -> "; 
    //                A[i] = _cleanAndTruncate(A[i], threshold, precision);
    //                for (int j=0; j < _nStates; j++) {
    //                    if(A[i][j]>0) {
    //                        guard += A[i][j] + " : (s'=" + j + ") + ";
    //                    }
    //                }
    //                guard = guard.substring(0,guard.length()-3) + ";" + _eol;
    //                code+=(guard);
    //            }  
    //            code+=(" " + _eol);
    //        }
    //        code+=(" " + _eol);
    //        code+=(INDENT1 +"endmodule" + _eol+ " " + _eol);
    //        return code;
    //    }
    private String _getHeader(int validHour) {

        double[] powerSpecs = _computeHourlyPowerSpecs(SPEC_TYPE.MAXIMUM);

        String str =  "dtmc" + _eol + 
                "const int MAX_STEP = " + _maxStep + "; // 288 time steps per improvisation" + _eol + 
                "const int PMAX = " + PMAXi + "; // maximum power consumption" + _eol+
                "const int DMAX = " + _maxDuration + ";// maximum duration consumption" + _eol+
                "const int T = 5; // sampling period is 5 minutes." + _eol +
                "const int samplesPerHour = floor(60/T);" + _eol  +
                "const int hTest;" + _eol; 

        if ( _optimizeOverAll) {
            int pCount =0;
            for (int[] z :incompleteCategories) {
                if (z[0] == validHour) { 
                    for (int j = 0 ; j < _nStates-1; j++) {
                        str += "const double p"+(pCount++)+";"+_eol;
                    }
                }
            } 
        } else {
            // zero or self
            for (int[] z :incompleteCategories) {
                if (z[0] == validHour) {
                    str += "const double p"+z[1]+";"+_eol;
                }
            }
        }

        //str +="const double probz;"+_eol;

        for (int i =0; i < powerSpecs.length; i++) {
            str += "const int Plimit_" + i + " = floor(" + powerSpecs[i] + ");" + _eol;
        }
        return str;
    }

    private String _getHeader() {
        double[] powerSpecs = _computeHourlyPowerSpecs(SPEC_TYPE.MAXIMUM);
        String str =  "dtmc" + _eol + 
                "const int MAX_STEP = " + _maxStep + "; // 288 time steps per improvisation" + _eol + 
                "const int PMAX = " + PMAXi + "; // maximum power consumption" + _eol+
                "const int DMAX = " + _maxDuration + ";// maximum duration consumption" + _eol+
                "const int T = 5; // sampling period is 5 minutes." + _eol +
                "const int samplesPerHour = floor(60/T);" + _eol  +
                "const int hTest;" + _eol; 

        for (int i =0; i < powerSpecs.length; i++) {
            str += "const int Plimit_" + i + " = floor(" + powerSpecs[i] + ");" + _eol;
        }
        return str;
    }
    private String _getStepCountAutomaton() {
        return INDENT1 + "module nMod " + _eol +
                // number of outputs produced;
                INDENT2+"n : [0..MAX_STEP] init 0;" +_eol+ 
                INDENT2+"[step] n<MAX_STEP -> (n'=n+1);" + _eol +
                INDENT1 +"endmodule" + _eol;
    }

    private String _getTimeOfDayAutomaton() { 

        // denotes the step within the hour

        return  "formula i = mod(n, samplesPerHour)+1;" + _eol +
                "formula testThisHour = (h=hTest)&(i < samplesPerHour) | ((hTest>0)&(h=hTest-1)|(hTest=0)&(h=23))&(i = samplesPerHour);" + _eol +
                INDENT1 +"module timeOfDay" + _eol +
                INDENT2 + "h : [0..23] init " + _hourOfDay[0] +";" +_eol + 
                INDENT2 + "[step] i = samplesPerHour & h < 23 -> (h'=h+1);" + _eol +
                INDENT2 +" [step] i = samplesPerHour & h = 23 -> (h'=0);" + _eol +
                INDENT2 +"[step] i < samplesPerHour -> true;" + _eol +
                INDENT2 + "endmodule" + _eol; 
    }
    /**
     * Given PMF's for multinomial estimates on each dimension of the observation model,
     * create a single joint PMF on the "sum" of observations at every time step. 
     * @throws IllegalActionException 
     */
    private List<double[]> _computeTotalPowerConsumptionDistributions() throws IllegalActionException {

        // _B : double[][] with multinomial estimates
        // _nCategories: double[] of length nObservations that denotes how many distinct
        // power levels each channel can have
        List<double[]> pmfStates = new ArrayList<>();
        for ( int s = 0 ; s < _nStates ; s++ ) {
            // PDFs of all states;
            double[] probs = _B[s];
            List pmfList = new ArrayList<double[]>();
            int baseCount = 0;
            // splitting up all the PMFs of different appliances that have been learned
            for ( int appliance = 0 ; appliance < _nCategories.length; appliance++) {
                int count = 0;
                double[] appliancePMF = new double[_nCategories[appliance]];
                while ( count < _nCategories[appliance]) {
                    appliancePMF[count] = probs[baseCount + count];
                    count ++;
                }
                baseCount += _nCategories[appliance];
                pmfList.add(appliancePMF); 
            }
            // now will convolve all entries of pmfList. 
            while (pmfList.size() > 1) {
                double[] a1 = (double[]) pmfList.remove(0);
                double[] a2 = (double[]) pmfList.remove(0);
                double[] convolution = SignalProcessing.convolve(a1, a2);
                pmfList.add(convolution);
            }

            double[] raw =(double[])pmfList.remove(0);
            raw = _cleanAndTruncate( raw, threshold,precision);

            for(int i = 0; i <raw.length; i++) {
                if (raw[i] > 0.0 && i > PMAXi) {
                    PMAXi = i;
                }
            }
            pmfStates.add(raw);
        }

        return pmfStates; 
    }

    private double[] _cleanAndTruncate(double[] input, double threshold, int decimalPlaces) {


        double[] output = new double[input.length];
        double sum =0.0;
        double scale = Math.pow(10,decimalPlaces);
        for (int i=0; i < input.length; i++) {
            if (input[i] < threshold) {
                output[i] = 0.0;
            } else { 
                output[i] = input[i];
                sum += input[i];
            }
        }

        double newSum = 0.0;
        for (int i=0; i < input.length; i++) {
            output[i]/=sum;
            if (precision > 0) {
                output[i] = Math.round(output[i]*scale)/scale;
            }
            newSum += output[i];
        }

        if (precision > 0) {
            output[0] += 1.0-newSum; 
            output[0] = Math.floor(output[0]*scale)/scale; 
        }
        return output;
    }



    protected static final String _eol;
    public static final String INDENT1 = StringUtilities.getIndentPrefix(1);
    public static final String INDENT2 = StringUtilities.getIndentPrefix(2);
    static {
        _eol = StringUtilities.getProperty("line.separator");
    }
    StringBuffer _code;

    int _maxStep;

    /** Maximum power consumption allowable over the state space*/
    int PMAXi = 0;
    double threshold = 1E-4;
    int precision = 0;
    private enum SPEC_TYPE {
        AVERAGE,
        MAXIMUM
    }
    double _optimum; 

    boolean _optimizeOverAll = false;

    boolean _useOptimization = true;

    boolean _useModelChecking;
}
