package ptolemy.math.filter;
 
import ptolemy.math.*;
public class Filtertest {

   public void run() {
       /*
       RealAnalogFilter afilter = new RealAnalogFilter();
       afilter.addPoleZero(new Complex(-3,-4),new Complex(
               Double.POSITIVE_INFINITY), true);
       afilter.addPoleZero(new Complex(-2,-5), 
               new Complex(-2,-3), true);
       afilter.addPoleZero(new Complex(-3,0), new Complex(-5), false);
       Complex[] poles = afilter.getPoles();
       Complex[] zeroes = afilter.getZeroes();
       System.out.println(poles.length);
       for (int i=0;i<poles.length;i++){
           System.out.println(
                   "poles are "+poles[i].real+" and "+poles[i].imag);
       }
       for (int i=0;i<zeroes.length;i++){
           System.out.println(
                   "zeroes are "+zeroes[i].real+" and "+zeroes[i].imag);
       }
       */


       RealDigitalFilter filter = DigitalFilter.designRealIIR(1,2,1,new 
               double[] {1.25,1.88}, new double[] {0.99,.001},1);
       System.out.println("hehe");
       Complex [] poles = filter.getPoles();
       Complex [] zeroes = filter.getZeroes();
       System.out.println(poles.length);
       for (int i=0;i<poles.length;i++){
           System.out.println(
                   "poles are "+poles[i].real+" and "+poles[i].imag);
       }
       for (int i=0;i<zeroes.length;i++){
           System.out.println(
                   "zeroes are "+zeroes[i].real+" and "+zeroes[i].imag);
       }
       /*
       Complex[] fq = filter.getFrequencyResponse();
       for (int i = 0; i < fq.length; i++) {
           System.out.println("freqResponse: " + fq[i].real+" "+fq[i].imag);
       }
       
       RealDigitalFilter dfilter = new RealDigitalFilter();
       dfilter.addPoleZero(new Complex(0),new Complex(1),false);
       fq = dfilter.getFrequencyResponse();
       for (int i = 0; i < fq.length; i++) {
           System.out.println("freqResponse: " + fq[i].real+" "+fq[i].imag);
       }
       */
       /*
       RealDigitalFilter filter = new RealDigitalFilter();
       filter.addFactor(new RealZFactor(new double[] {1}, new double[] {1},
       2));
       
       filter.addPoleZero(new Complex(-0.5,-0.5), new Complex(-0.1,-0.2), true);
       double[] transferNum;
       double[] transferDen;
       transferNum = filter.getNumerator();
       transferDen = filter.getDenominator();
       
       for (int i = 0; i < transferNum.length; i++) {
           System.out.println("transferNum " + i + " " + transferNum[i]);
       }
       for (int i = 0; i < transferDen.length; i++) {
           System.out.println("transferDen " + i + " " + transferDen[i]);
       }
       double gain = filter.getGain();
       System.out.println("gain = " + gain);
       filter.resetState();
       RealZFactor[] factors = filter.getFactors();
       double[] numer = factors[0].getNumerator();
       for (int i = 0; i < numer.length; i++) {
           System.out.println("numer" + numer[i]);
       }
       double[] den = factors[0].getDenominator();
       for (int i = 0; i < den.length; i++) {
           System.out.println("den" + den[i]);
       }
       double o = factors[1].computeOutput(1);
       System.out.println(o);
       double[] output;
       double[] input = new double[] {1,0,0,0};
       double out = filter.getOutput(1.5);
       */
       /*
       Complex[] poles = filter.getPoles();
       Complex[] zeroes = filter.getZeroes();
       for (int i=0;i<poles.length;i++){
           System.out.println(
                   "poles are "+poles[i].real+" and "+poles[i].imag);
       }
       for (int i=0;i<zeroes.length;i++){
           System.out.println(
                   "zeroes are "+zeroes[i].real+" and "+zeroes[i].imag);
       }
       */
       /*
       System.out.println("out is " + out);
              output = filter.getResponse(input, input.length);
       for (int i = 0; i < output.length; i++) {
           System.out.println("output is" + output[i]);
       }
       output = filter.getImpulseResponse();
       for (int i = 0; i < output.length; i++) {
           System.out.println("output is" + output[i]);
       }
       
       Complex[] freq = filter.getFreqResponse();
       for (int i = 0; i < 30; i++) {
           System.out.println("freq = " + freq[i].real + " " + freq[i].imag);
       }
       */
                   /**
       //test.resetState();
       //desFilter.resetState();
       //Complex pole = new Complex(-1,2);
       //ConjugateComplex conjPole = new ConjugateComplex(pole);
       //desFilter.addPolePair(conjPole);
       //output = test.getResponse(input3,input3.length);
       //desFilter.movePole(poles[0],-2,0);
       output = desFilter.getResponse(input3,input3.length);
       for (int i = 0; i < output.length; i++) {
           System.out.println("output3: " + output[i]);
       }
       //test.resetState();
       desFilter.resetState();
       desFilter.addZero(-6);
       desFilter.addZeroPair(conjPole);
       RealZFactor newFactor = new RealZFactor(num, denom, 1);
       RealZFactor[] ff = {newFactor};
       desFilter.setTransferFn(ff);
       double[] poles = {-3};
       double[] zeros = {-6};
       ConjugateComplex[] polePairs = {conjPole};
       ConjugateComplex[] zeroPairs = {conjPole};

       RealDigitalFilter test = 
           new RealDigitalFilter(poles,zeros,polePairs,zeroPairs,1);
       output = test.getResponse(input4,input4.length);
       //output = desFilter.getResponse(input3,input3.length);
       for (int i = 0; i < output.length; i++) {
           System.out.println("output4: " + output[i]);
       }
       */
       //test.resetState();
       //desFilter.resetState();
       //Complex pole = new Complex(-1,2);
       //ConjugateComplex conjPole = new ConjugateComplex(pole);
       //desFilter.addPolePair(conjPole);
       //output = test.getResponse(input3,input3.length);
       //desFilter.movePole(poles[0],-2,0);
 
       /* tests for Analog
         double[] num = {1,5};
       double[] denom = {1,4};
       RealAnalogFilter test = new RealAnalogFilter(num,denom,1);
       Complex[] poles = test.getPoles();
       Complex[] zeroes = test.getZeroes();
       for (int i=0;i<poles.length;i++){
           System.out.println(
                   "poles are "+poles[i].real+" and "+poles[i].imag);
       }
       for (int i=0;i<zeroes.length;i++){
           System.out.println(
                   "zeroes are "+zeroes[i].real+" and "+zeroes[i].imag);
       }
       //test.movePole(poles[0],-4,0);
       //test.moveZero(zeroes[0],-5,0);
       poles = test.getPoles();
       zeroes = test.getZeroes();
       test.addPole(-3);
       test.addZero(-6);
       poles = test.getPoles();
       zeroes = test.getZeroes();
       for (int i=0;i<poles.length;i++){
           System.out.println(
                   "poles are "+poles[i].real+" and "+poles[i].imag);
       }
       for (int i=0;i<zeroes.length;i++){
           System.out.println(
                   "zeroes are "+zeroes[i].real+" and "+zeroes[i].imag);
       }
       */
       /*tests for Digital
        */
       /*
         double[] num = {1,0,6};
       double[] denom = {1,0,2};
       //double[] num2 = {3, 1};
       //double[] denom2 = {1};
       //RealFilter test = new RealFilter(num2,denom2,1);
       
       double[] input3 = {1, 1, 0, 0};
       double[] input4 = {1, 2, 3};
       //RealZFactor newFactor = new RealZFactor(num2, denom2, 1);
       //test.addFactor(newFactor);
       RealDigitalFilter test2 = new RealDigitalFilter(num,denom,1);
       
       //test.resetState();
       test2.resetState();
       double[] output = test2.getResponse(input3, input3.length);
       //output = test2.getResponse(output,output.length);
       for (int i = 0; i < output.length; i++) {
           System.out.println("output:  " + output[i]);
       }
       //test.resetState();
       test2.resetState();
       //output = test.getResponse(input2,input2.length);
       Complex[] poles = test2.getPoles();
       Complex[] zeroes = test2.getZeroes();
       for (int i = 0; i<poles.length; i++){
           System.out.println(
                   "poles are = " + poles[i].real+"and "+poles[i].imag);
       }
       for (int i = 0; i<zeroes.length; i++){
           System.out.println(
                   "zeroes are = " + zeroes[i].real+"and "+zeroes[i].imag);
       }
       System.out.println("poleLength = " + poles.length);
       
       test2.movePole(poles[3], -5, 5);
       poles = test2.getPoles();
       zeroes = test2.getZeroes();
       for (int i = 0; i<poles.length; i++){
           System.out.println(
                   "poles now are = " + poles[i].real+"and "+poles[i].imag);
       }
       for (int i = 0; i<zeroes.length; i++){
           System.out.println(
                   "zeroes now are = " + zeroes[i].real+"and "+zeroes[i].imag);
       }
       //test2.deletePole(poles[0]);
       //System.out.println("poles delete");
       poles = test2.getPoles();
       zeroes = test2.getZeroes();
       test2.moveZero(zeroes[0],-5,5);
       poles = test2.getPoles();
       zeroes = test2.getZeroes();
       test2.resetState();
       for (int i = 0; i<poles.length; i++){
           System.out.println(
                   "poles now are = " + poles[i].real+"and "+poles[i].imag);
       }
       for (int i = 0; i<zeroes.length; i++){
           System.out.println(
                   "zeroes now are = " + zeroes[i].real+"and "+zeroes[i].imag);
       }
       //test2.addPole(-3);
       output = test2.getResponse(input3,input3.length);
       for (int i = 0; i < output.length; i++) {
           System.out.println("output2: " + output[i]);
       }
       */

       /**
       //test.resetState();
       //test2.resetState();
       //Complex pole = new Complex(-1,2);
       //ConjugateComplex conjPole = new ConjugateComplex(pole);
       //test2.addPolePair(conjPole);
       //output = test.getResponse(input3,input3.length);
       //test2.movePole(poles[0],-2,0);
       output = test2.getResponse(input3,input3.length);
       for (int i = 0; i < output.length; i++) {
           System.out.println("output3: " + output[i]);
       }
       //test.resetState();
       test2.resetState();
       test2.addZero(-6);
       test2.addZeroPair(conjPole);
       RealZFactor newFactor = new RealZFactor(num, denom, 1);
       RealZFactor[] ff = {newFactor};
       test2.setTransferFn(ff);
       double[] poles = {-3};
       double[] zeros = {-6};
       ConjugateComplex[] polePairs = {conjPole};
       ConjugateComplex[] zeroPairs = {conjPole};
       RealDigitalFilter test = 
           new RealDigitalFilter(poles,zeros,polePairs,zeroPairs,1);
       output = test.getResponse(input4,input4.length);
       //output = test2.getResponse(input3,input3.length);
       for (int i = 0; i < output.length; i++) {
           System.out.println("output4: " + output[i]);
       }
       */
       /*
         test.resetState();
         output[0] = test.getOutput(1);
         output[1] = test.getOutput(2);
         output[2] = test.getOutput(3);
         for (int i = 0; i < output.length; i++) {
         System.out.println("output5: " + output[i]);
         }
       */
         /*
       System.out.println("=========== Solved Poles: ");
       Complex [] pole = filter.getPole();
       for (int i=0;i<pole.length; i++){
           System.out.println("pole:  "+pole[i].real+" "+pole[i].imag+"j");
       }
       Complex [] zero = filter.getZero();
       System.out.println("========== Solved Zeroes: ");
       for (int i=0;i<zero.length; i++){
           System.out.println("zero:  "+zero[i].real+" "+zero[i].imag+"j");
       }
       System.out.println("========== Numerator: ");
       Complex [] numer = filter.getNumerator();
       for (int i=0;i<numer.length; i++){
           System.out.print(" ("+numer[i].real+" "+numer[i].imag+"j) ");
       }
       System.out.println("");
       System.out.println("========== Denominator: ");
       Complex [] denom = filter.getDenominator();
       for (int i=0;i<denom.length; i++){
           System.out.print(" ("+denom[i].real+" "+denom[i].imag+"j) ");
       */
       }
    
    public static void main(String [] args){
        Filtertest t = new Filtertest();
        t.run();
    }

}

 
