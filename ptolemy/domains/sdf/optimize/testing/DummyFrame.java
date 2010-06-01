/**
 * 
 */
package ptolemy.domains.sdf.optimize.testing;

/**
 * @author mgeilen
 *
 */
public class DummyFrame {
    public int value;

    @Override
    public String toString() {
        return "Frame("+Integer.toString(this.value)+")";
    }
    
    public DummyFrame clone(){
        DummyFrame f = new DummyFrame();
        f.value = this.value;
        return f;
    }

}
