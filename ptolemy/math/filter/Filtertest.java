package ptolemy.math;

public class Filtertest {

   public void run() {

       /* tests for designFilter
        */
       
       RealDigitalFilter desFilter = DigitalFilter.designRealIIR(1,3,
               1, new double[] {1.26, 1.88}, new double[] {0.99,0.001}, 1.0);
       //double[] num = {1,0,6};
       //double[] denom = {1,0,2};
              
       double[] input3 = {1, 1, 0, 0};
       double[] input4 = {1, 2, 3};
       //RealZFactor newFactor = new RealZFactor(num, denom, 1);
       //test.addFactor(newFactor);
       
       desFilter.resetState();
       
       double[] output = desFilter.getResponse(input3, input3.length);
       //output = test2.getResponse(output,output.length);
       for (int i = 0; i < output.length; i++) {
           System.out.println("output:  " + output[i]);
       }
       
       //RealAnalogFilter desFilter = new RealAnalogFilter();
       //desFilter.clearFactors();
       //test.resetState();
       //desFilter.addPolePair(new ConjugateComplex(new Complex(-2,3)));
       //desFilter.addZeroPair(new ConjugateComplex(new Complex(-3,4)));
       //desFilter.addPole(-4);
       //desFilter.addZero(-5);
       //desFilter.resetState();
       //output = test.getResponse(input2,input2.length);
       Complex[] poles = desFilter.getPoles();
       Complex[] zeroes = desFilter.getZeroes();
       for (int i = 0; i<poles.length; i++){
           System.out.println(
                   "poles are = " + poles[i].real+"and "+poles[i].imag);
       }
       for (int i = 0; i<zeroes.length; i++){
           System.out.println(
                   "zeroes are = " + zeroes[i].real+"and "+zeroes[i].imag);
       }
       System.out.println("poleLength = " + poles.length);
       
       
       /*
       desFilter.movePole(poles[3], -5, 5);
       poles = desFilter.getPoles();
       zeroes = desFilter.getZeroes();
       for (int i = 0; i<poles.length; i++){
           System.out.println(
                   "poles now are = " + poles[i].real+"and "+poles[i].imag);
       }
       for (int i = 0; i<zeroes.length; i++){
           System.out.println(
                   "zeroes now are = " + zeroes[i].real+"and "+zeroes[i].imag);
       }
       //desFilter.deletePole(poles[0]);
       //System.out.println("poles delete");
       poles = desFilter.getPoles();
       zeroes = desFilter.getZeroes();
       desFilter.moveZero(zeroes[0],-5,5);
       poles = desFilter.getPoles();
       zeroes = desFilter.getZeroes();
       desFilter.resetState();
       for (int i = 0; i<poles.length; i++){
           System.out.println(
                   "poles now are = " + poles[i].real+"and "+poles[i].imag);
       }
       for (int i = 0; i<zeroes.length; i++){
           System.out.println(
                   "zeroes now are = " + zeroes[i].real+"and "+zeroes[i].imag);
       }
       //desFilter.addPole(-3);
       output = desFilter.getResponse(input3,input3.length);
       for (int i = 0; i < output.length; i++) {
           System.out.println("output2: " + output[i]);
       }
       

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

 
