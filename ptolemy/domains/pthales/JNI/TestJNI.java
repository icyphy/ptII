package ptolemy.domains.pthales.JNI;

public class TestJNI {

    /**
     * @param args
     */
    public static void main(String[] args) {
        System.loadLibrary("ABF");
        float[] tabs = new float[4*ABF.ATL_lgth_chirp*2];
                       
        ABF.Calc_Chirp(ABF.ATL_lgth_chirp, tabs, 0.2f);
        
        for (int i = 0; i < 4*ABF.ATL_lgth_chirp*2; i ++)
            System.out.println(tabs[i]);
     }
}
