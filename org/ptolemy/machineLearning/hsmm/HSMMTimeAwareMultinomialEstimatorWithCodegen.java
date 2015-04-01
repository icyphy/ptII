package org.ptolemy.machineLearning.hsmm;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List; 
import java.util.Set;

import com.cureos.numerics.Calcfc;
import com.cureos.numerics.Cobyla;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.FilePortParameter;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.attributes.URIAttribute;
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

        optStep = new Parameter(this, "optStep");
        optStep.setExpression("10");

        filename = new FilePortParameter(this, "filename");
        filename.setExpression("edhmm.pm");
        filename.setTypeEquals(BaseType.STRING);
        new SingletonParameter(filename.getPort(), "_showName")
        .setToken(BooleanToken.TRUE);

        optvals = new TypedIOPort(this, "optvals", false, true);
        optvals.setTypeEquals(new ArrayType(BaseType.DOUBLE));

        minPowerThreshold = new Parameter(this,"minPowerThreshold");
        minPowerThreshold.setExpression("0");
        minPowerThreshold.setTypeEquals(BaseType.INT);

        propertyFile = new FilePortParameter(this, "propertyFile"); 
        propertyFile.setExpression("properties.pctl");


        modelChecking = new Parameter(this, "modelChecking");
        modelChecking.setTypeEquals(BaseType.BOOLEAN);
        modelChecking.setExpression("true");
        _useModelChecking = true;

        optimize = new Parameter(this,"optimize");
        optimize.setTypeEquals(BaseType.BOOLEAN);
        optimize.setExpression("true");
        _useOptimization = true;

        testPreset = new Parameter(this,"testPreset");
        testPreset.setTypeEquals(BaseType.BOOLEAN);
        testPreset.setExpression("true");
        _testPreset = true;


    }

    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        URI uri = URIAttribute.getModelURI(this);  
        if (uri == null) {
            throw new IllegalActionException(this,"Save model before running model checking.");
        }
        // get directory
        int pos = uri.getPath().lastIndexOf('/'); 
        String directory = uri.getPath().substring(0,pos);
        _uri = directory;        

        knownOptima.add(new double[]
                {0.9999998558870935, 0.7365133293652223, 
                0.7365133293652225, 0.5729570673625037, 
                0, 0.5729570673625037});
        knownOptima.add(new double[]{0 , 0.9871000428696297 , 0.9871000428696303 , 0.8668749276705995 , 0.8668749276705996 , 0.2852487541062735 , 0.6721797878012544});
        knownOptima.add(new double[]{0.6048189790671137 , 0.6622552721643232 , 0.6622552721643232 , 0.24912678210746997 , 0.24912678210747002 , 0.23353707829230622 , 0.24391543063710114 , 0.24392519246697586});
        knownOptima.add(new double[]{0.02088115270443884 , 0.012470145837708724 , 0.5102141618034919 , 0.8369713240736867 , 0.8369713240736865 , 0.7462417468724551 , 0.7462417468724553 , 0.48045797350554365 });

        knownOptima.add(new double[]{1.0,1.0,1.0,1.0,1.0,1.0});
        knownOptima.add(new double[]{0.0 , 0.8896103277415648 , 0.8896103277415648 , 0.8177485460744346 , 0.8177485460744346 , 0.7389640029258466 , 0.7389640029258466 , 0.31428322754111254 });
        knownOptima.add(new double[]{0.9999987275650416 , 0.8116212283556629 , 0.8439267270805016 , 0.4903733089210995 , 0.38749124874995544 , 0.03393785815668163 , 0.21071455345331846 , 0.8116212283556631 });
        knownOptima.add(new double[]{0.6048405227432726 , 0.2435671924127057 , 0.24381885831504893 , 0.6621608186530523 , 0.6621608186530519 , 0.24904374851078404 , 0.2490437485107841 , 0.2338721098797575 });
        knownOptima.add(new double[]{0.1952403432103702 , 0.5286686703585188, 0 , 0.17979429058525886 , 0.673136120939904 , 0.6731421246889602 , 0.6336272270046067 , 0.5286686703585182});
        knownOptima.add(new double[]{0.7894998944877564 , 0.7552127478744417 , 0.7552127478744415 , 0.36085044487134976 , 0.007297034230322075 , 0.528184342378425  });

        knownOptima.add(new double[]{0.8988735145704078 , 0.36948819006843914 , 0.36948819006843847 , 0, 0 , 0.17868154767341735 });
        knownOptima.add(new double[]{1.0,1.0,1.0,1.0,1.0,1.0});
        knownOptima.add(new double[]{1.0,1.0,1.0,1.0,1.0,1.0});
        knownOptima.add(new double[]{1.0,1.0,1.0,1.0});
        knownOptima.add(new double[]{1.0});
        knownOptima.add(new double[]{0.7551142738285981 , 0.3607322416110104 , 0.006990375111006586 , 0.5280469394226991 , 0.7895311361586331 , 0.7551142738285981 });
        knownOptima.add(new double[]{0, 0.8879362302016913 , 0.8879362302016912 , 0.9640621075775393 , 0.652095823148817 , 0.9149824265161544 , 0.5613780484104149 , 0.3085769909795451 });
        knownOptima.add(new double[]{0.9999872852316252 , 0.6671175193748322 , 0.6671175193748322 , 0.8116063383604155 , 0.8116063383604155 , 0.21068205763146267 , 0.3874587529280995 , 0.0339053623348258});
        knownOptima.add(new double[]{1.0 ,0, 0.14131905672344214 , 0.6997444778547147 , 0.6997444778547168 , 0.2666270916839593 , 0.5044540195983187 , 0.519527044900985 });
        knownOptima.add(new double[]{0.4077742459511125 , 0.9654683487625617 , 0.7895384235205317 , 0.0 , 0.9298314462241475 , 0.835269840184298 , 0.835269840184298 , 0.18308871886930334 });
        knownOptima.add(new double[]{0.37393827232897336 , 0.9377317313232556 , 0.9377317313232555 , 0.8658811593380646 , 0.771525052592074 , 0.771525052592074 ,0 , 0.8658622449013662 });
        knownOptima.add(new double[]{0.6066919133835842 , 0.6379861903160877 , 0.6379861903160875 , 0.5408912278225496 , 0.18699915566440145 , 0.20455720587403062 , 0.20455720587403065 , 0.13584089843773064 });
        knownOptima.add(new double[]{0.16263114037725163 , 0.9275349558931817 , 0.3968911544709274 , 0.7793100940176831 , 0.779310094017684,0 , 0.3406715441110433 , 0.5612228730860364 });
        knownOptima.add(new double[]{1.0,1.0,1.0,1.0,1.0,1.0});
        
        learningPattern = new HashMap<>();
        learningPattern.put(0, new int[]{0});
        learningPattern.put(1, new int[]{0,1});
        learningPattern.put(2, new int[]{0,2});
        learningPattern.put(3, new int[]{1,2});
        learningPattern.put(4, new int[]{0,4});
        learningPattern.put(5, new int[]{1,4});
        learningPattern.put(6, new int[]{2,4});
        learningPattern.put(7, new int[]{3,6});
        
    }

    @Override
    public void wrapup() {
        knownOptima.clear();
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

    public Parameter optStep;

    /**
     * If set to true, model will be parameterized and parameter synthesis will be
     * carried out using PRISM
     */
    public Parameter optimize;

    /**
     * If set to true, PRISM will be used to model check defined model properties
     */
    public Parameter modelChecking;

    public Parameter testPreset;

    public TypedIOPort optvals;

    public Parameter minPowerThreshold;


    public void attributeChanged(Attribute attribute) throws IllegalActionException {
        if (attribute == optStep) {
            _optStep = ((IntToken)optStep.getToken()).intValue();
        } else if(attribute==optimize) {
            _useOptimization = ((BooleanToken)optimize.getToken()).booleanValue();
        } else if(attribute==modelChecking) {
            _useModelChecking = ((BooleanToken)modelChecking.getToken()).booleanValue();
        } else if(attribute==testPreset) {
            _testPreset = ((BooleanToken)testPreset.getToken()).booleanValue();
        } else if(attribute==minPowerThreshold) {
            _pThreshold = ((IntToken)minPowerThreshold.getToken()).intValue();
        } else { 
            super.attributeChanged(attribute);
        }
    }
    public void fire() throws IllegalActionException { 

        super.fire(); 

        //        HashMap<Integer,double[]> previousOptima = new HashMap<>();
        if (_useModelChecking) {
            _writeProperties(); 

            if (_testPreset) {
                //String[] method = {INTERPOLATE, SELF_AND_ZERO, FORCE_SELF, FORCE_ZERO};
                String[] method = { FORCE_ZERO};
                _useOptimization = false;
                //double[] powerSpecs = _computeHourlyPowerSpecs(SPEC_TYPE.MAXIMUM);
                //for (int optLoop = 0; optLoop <500; optLoop++) {
                for (String strategy:method) { 
                    // set learned distributions first
                    At = new double[NUM_CATEGORIES][_nStates][_nStates];
                    for (int h = 0; h < NUM_CATEGORIES; h++) {
                        for (int i =0; i < _nStates; i++) {
                            for(int j=0; j< _nStates; j++) {
                                At[h][i][j] = Atlearned[h][i][j];
                            }
                        }
                        // fill in missing probabilities according to current strategy
                        _calculateTransitionScheme(strategy,h); 
                    }

                    _code = generateCode(0); 
                    _writeModelFile();

                    _modelCheckAll(strategy);  
                }
            } else if (! _useOptimization){
                Token[] optP = new Token[NUM_CATEGORIES];
                for ( int validHour=0; validHour < NUM_CATEGORIES; validHour++) { 

                    _code = generateCode(validHour);   
                    double[] optima = knownOptima.get(validHour); 
                    optP[validHour] = new DoubleToken(_optimum); 
                    _updateAt(optima,validHour); 
                } 
                _code = generateCode(23);  
                _writeModelFile(); 
                _modelCheckAll("justTesting");

                optvals.send(0, new ArrayToken(optP));
            } else {
                Token[] optP = new Token[NUM_CATEGORIES];
                for ( int validHour=0; validHour < NUM_CATEGORIES; validHour++) { 
                    System.out.println("Optimizing hour " + validHour);
                    _code = generateCode(validHour);  
                    _writeModelFile(); 
                    //double[] optima = knownOptima.get(validHour);
                    double[] optima = _modelCheck(validHour,null);
                    optP[validHour] = new DoubleToken(_optimum);
                    //previousOptima.put(validHour,optima);
                    if (_useOptimization && !_optimizeOverAll) {
                        _updateAt(optima,validHour);
                    }  
                }

                optvals.send(0, new ArrayToken(optP));
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
                int sourceState = z[1]; 
                if (sourceState > 0) {
                    int[] indices = learningPattern.get(sourceState);
                    //first, wipe the corresponding row of At;
                    for (int i = 0 ; i <_nStates; i ++) {
                        At[validHour][sourceState][i] =0.0;
                    }
                    At[validHour][sourceState][indices[0]] = 1.0-optima[k];
                    At[validHour][sourceState][indices[1]] = optima[k];  
                    k++;
                } 
            }
        }
    }

    private double[] _modelCheck(int hour, double[] prevOpt) throws IllegalActionException { 

        _optimum=0.0;
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
                    if (z[0] == hour && z[1]!=0) { 
                        indices.add(z[1]);
                    }
                } 
                nVariables = indices.size();
                numConstraints = 2*nVariables;
            } 


            Calcfc calcfc = new Calcfc() { 
                public double Compute(int n, int m, double[] x, double[] con,
                        boolean[] terminate) throws IllegalActionException {
                    System.out.print(".");
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
                            //System.out.print(x[i]+ " ");
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
                        //                        for (int i=0; i <x.length; i++) {
                        //                            System.out.print(x[i]+" ");
                        //                        }

                        paramValues = paramValues.substring(0,paramValues.length()-1); //omit last comma
                        ProcessBuilder pb = new ProcessBuilder("prism",filename.getExpression(),
                                propertyFile.getExpression(),"-const",paramValues,"-prop",""+(hour+1),"-exportresults","stdout:csv");
                        pb.directory(new File(_uri));
                        try {
                            Process pr = pb.start(); 
                            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(pr.getInputStream())); 
                            BufferedReader err = new BufferedReader(
                                    new InputStreamReader(pr.getErrorStream())); 

                            String line = null;
                            while( (line =err.readLine()) != null) {
                                System.out.println(err);
                            }
                            while ((line = in.readLine()) != null) {
                                //System.out.println(line);
                                if (line.equals("Result")) {
                                    double d = Double.parseDouble(in.readLine());
                                    //System.out.println(d);
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
            if (prevOpt == null) {
                start = new double[nVariables];
                if (!_optimizeOverAll) {
                    for (int i=0; i < start.length; i++) {
                        start[i]=1.0;
                    }
                }
            } else {
                start = Arrays.copyOf(prevOpt,prevOpt.length);
            }
            Cobyla.FindMinimum(calcfc, nVariables,
                    numConstraints, start, 1.0, 1E-2, 0,
                    _optStep, term); 

            //System.out.println("Optimal Probabilities For Hour " + hour +" = ");
            //            for (int i = 0; i <start.length; i++) {
            //                System.out.print(start[i]+ " ");
            //            }
            System.out.println();
            System.out.println(_optimum);

            return start;

        } else {
            try{
                ProcessBuilder pb = new ProcessBuilder("prism",filename.getExpression(),
                        propertyFile.getExpression(),"-const", "hTest="+hour,"-prop",""+(hour+1),"-exportresults","resultNoOpt"+hour+".txt:csv");
                pb.directory(new File(_uri));
                Process pr = pb.start(); 
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(pr.getInputStream())); 
                String line = null;
                while ((line = in.readLine()) != null) { 
                    System.out.println(line);
                    if (line.equals("Result")) {
                        double[] d = {Double.parseDouble(in.readLine())};
                        return d;
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void _modelCheckAll(String method) throws IllegalActionException { 

        //int[] possibleThresholds = {0,149,165,166};
        Set<Integer> possibleThresholds = new HashSet<>();
        double[] specs =_computeHourlyPowerSpecs(SPEC_TYPE.MAXIMUM);
        for (double s : specs ) {
            possibleThresholds.add((int)s);
            System.out.println(s);
        }

        for (int thre : possibleThresholds) {
            try{ 
                System.out.println("model checking P=" + thre + " with " + method);
                ProcessBuilder pb = new ProcessBuilder("prism",filename.getExpression(),
                        //propertyFile.getExpression(),
                        "-const", "hTest=0:23,Pthreshold="+thre, 
                        "-pf","P=?[G pow <= Pthreshold]",
                        "-exportresults","allResults"+method+thre+".txt:matrix");
                pb.directory(new File(_uri));
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
    }

    private void _writeProperties() throws IllegalActionException {
        // write code

        double[] powerSpecs = _computeHourlyPowerSpecs(SPEC_TYPE.MAXIMUM);
        java.io.Writer writer;


        String properties = "";
        // check some properties 
        for (int i = 0 ; i< NUM_CATEGORIES; i++) { 
            if (powerSpecs[i]==0) {
                properties += "P=?[G pow <= "+ 
                        (powerSpecs[i]+_pThreshold)+ 
                        "]" + _eol; 
            } else {
                properties += "P=?[G pow <= "+powerSpecs[i]+"]" + _eol; 
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
                        int[] tranStates;
                        for (int[] z :incompleteCategories) {
                            if (z[0] == validHour) {
                                int i = z[1];
                                done.add(i); 
                                guard = INDENT2 +  "[tr] d=0 & (s=" + i + ") & h = " + hour + " -> ";
                                if (i == 0) {
                                    guard += "1.0: (s'=0);" + _eol;
                                } else { 
                                    tranStates = learningPattern.get(i);
                                    guard += "1-p"+i+": (s'=" + tranStates[0] + ") + p"+i+" : (s'="+ tranStates[1] +");"+_eol;
                                }
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
                "const int MAX_STEP = " + 288 + "; // 288 time steps per improvisation" + _eol + 
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
                    if (z[1] != 0) {
                        str += "const double p"+z[1]+";"+_eol;
                    }
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
                "const int MAX_STEP = " + 288 + "; // 288 time steps per improvisation" + _eol + 
                "const int PMAX = " + PMAXi + "; // maximum power consumption" + _eol+
                "const int DMAX = " + _maxDuration + ";// maximum duration consumption" + _eol+
                "const int T = 5; // sampling period is 5 minutes." + _eol +
                "const int samplesPerHour = floor(60/T);" + _eol  +
                "const int Pthreshold;" +_eol +
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
                INDENT2 + "h : [0..23] init " + _hourOfDay[0]+  " ;" +_eol +  // should this be hour of day?
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

    int _optStep;

    /** Maximum power consumption allowable over the state space.*/
    private int PMAXi = 0;
    private double threshold = 1E-4;
    private int precision = 0;
    /** 
     * Power specification type.  
     */
    private enum SPEC_TYPE {
        AVERAGE,
        MAXIMUM
    }
    private double _optimum; 

    private boolean _optimizeOverAll = false;

    private boolean _useOptimization = true;

    private boolean _useModelChecking;

    private boolean _testPreset; 

    private List<double[]> knownOptima = new ArrayList<>();


    private int _pThreshold;
    
    private HashMap<Integer,int[]> learningPattern;

    private String _uri;
}

