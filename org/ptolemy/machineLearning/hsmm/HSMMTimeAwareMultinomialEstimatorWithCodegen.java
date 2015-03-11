package org.ptolemy.machineLearning.hsmm;

import java.util.ArrayList;
import java.util.List;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.StringToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
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
        code = new TypedIOPort(this, "code", false, true);
        code.setTypeEquals(BaseType.STRING);

        maxStep = new Parameter(this, "maxStep");
        maxStep.setExpression("20");
    }

    public TypedIOPort code;

    public Parameter maxStep;

    public void attributeChanged(Attribute attribute) throws IllegalActionException {
        if (attribute == maxStep) {
            _maxStep = ((IntToken)maxStep.getToken()).intValue();
        } else {
            super.attributeChanged(attribute);
        }
    }
    public void fire() throws IllegalActionException { 

        super.fire(); 
        _code = generateCode();  
        code.send(0, new StringToken(_code.toString()));
    } 

    private StringBuffer generateCode() throws IllegalActionException {
        // Generate PRISM code 
        StringBuffer code = new StringBuffer();

        List stateEmissions = computeTotalPowerConsumptionDistributions();  
        // header
        String header = "dtmc" + _eol + 
                "const int MAX_STEP = " + _maxStep + "; // 288 time steps per improvisation" + _eol + 
                "const int PMAX = " + PMAXi + "; // maximum power consumption" + _eol+
                "const int DMAX = " + _maxDuration + ";// maximum duration consumption" + _eol+
                "const int T = 5; // sampling period is 5 minutes." + _eol;
                
        code.append(header);
 
        // variables
        String automaton1 = INDENT1 + "module stateSpace" + _eol +
                INDENT2+"// local state " + _eol +
                INDENT2 + "s : [0.. " + (_nStates-1) + "] init 0;" + _eol +   INDENT2 + "// value of output " + _eol +                                                                                                                                                                         
                INDENT2 + "pow : [0..PMAX] init 0; " + _eol + INDENT2 + "// duration of state"  + _eol;                                                                                                                                                                       


        code.append(automaton1);

        // prior state distribution

        String guard ;

        for (int s = 0; s <_nStates; s++) {
            String emissions = INDENT2 +  "[step] s=";
            emissions += s + "& d>0 & n < MAX_STEP -> ";
            //       + "initDist=false -> ";
            double[] p = (double[])stateEmissions.get(s);
            p = cleanAndTruncate(p, 0.01, precision);
            for (int d=0; d< p.length; d++) {
                if (p[d] > 0.0) {
                    emissions += p[d] + ": (pow' =" + d + ") + ";
                }
            }
            // remove the extra plus
            emissions = emissions.substring(0,emissions.length()-3) + ";" + _eol;
            code.append(emissions);
        } 
        
        double[][] A = At[0];
        for ( int i = 0 ; i < _nStates; i++) {
            //initDist=false & 
            guard = INDENT2 +  "[tr] d=0 & (s=" + i + ") -> "; 
            A[i] = cleanAndTruncate(A[i], threshold, precision);
            for (int j=0; j < _nStates; j++) {
                if(A[i][j]>0) {
                    guard += A[i][j] + " : (s'=" + j + ") + ";
                }
            }
            guard = guard.substring(0,guard.length()-3) + ";" + _eol;
            code.append(guard);
        }

        code.append(" " + _eol);
        code.append(INDENT1 +"endmodule" + _eol+ " " + _eol);
        
        String newModule = INDENT1 + "module durationAutomaton" + _eol +
              INDENT2 + "d : [0..DMAX] init 0;" + _eol ;// INDENT1 + "// number of outputs produced;" + _eol +
            //  INDENT2 + "n : [0..MAX_STEP] init 0;" + _eol; 

        code.append(newModule);
        code.append(" " + _eol);
        for (int i = 0; i < D_new.length; i++) {
            // initDist=false &
            newModule = INDENT2 +  "[tr] d=0 & s=" + i + " -> "; 
            D_new[i] = cleanAndTruncate(D_new[i], threshold, precision);  

            for (int j=0; j < D_new[0].length; j++) {
                if (D_new[i][j] > 0.0) {
                    newModule += D_new[i][j] + " : (d'=" + j + ") + "; 
                } 
            }
            newModule = newModule.substring(0,newModule.length()-3) + ";" + _eol;
            code.append(newModule);
        } 
        
        code.append(INDENT2 + "[step] d > 0 -> (d'=d-1);" + _eol);
        
        code.append(INDENT1 +"endmodule" + _eol+ " " + _eol); 
        // state transitions -- first, only use At[0], not the time dependent A.
        
        String nCount = INDENT1 + "module nMod " + _eol +
                // number of outputs produced;
                INDENT2+"n : [0..MAX_STEP] init 0;" +_eol+ 
               INDENT2+"[step] n<MAX_STEP -> (n'=n+1);" + _eol +
            INDENT1 +"endmodule" + _eol;
        
        code.append(nCount);
        
        
        return code;
    }

    /**
     * Given PMF's for multinomial estimates on each dimension of the observation model,
     * create a single joint PMF on the "sum" of observations at every time step. 
     * @return
     * @throws IllegalActionException 
     */
    private List<double[]> computeTotalPowerConsumptionDistributions() throws IllegalActionException {

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
            raw = cleanAndTruncate( raw, threshold,precision);

            for(int i = 0; i <raw.length; i++) {
                if (raw[i] > 0.0 && i > PMAXi) {
                    PMAXi = i;
                }
            }
            pmfStates.add(raw);
        }

        return pmfStates; 
    }

    private double[] cleanAndTruncate(double[] input, double threshold, int decimalPlaces) {

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
            output[i] = Math.round(output[i]*scale)/scale;
            newSum += output[i];
        }

        output[0] += 1.0-newSum;
        output[0] = Math.floor(output[0]*scale)/scale; 
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
    int precision = 5;

}
