

// A simple class that tests translation of Ptolemy II ASTs to
// C code.
public class TestModule {

    // Constructor with no arguments.
    public TestModule() { _privatevar1 = _privatevar2 = 0; }

    // Constructor with arguments.
    public TestModule(int val1, int val2) {
       _privatevar1 = val1;
       _privatevar2 = val2;
    }

    // Method with no arguments
    public void method1() {
        int var1 = 5;
        int var2 = 7;
        float y=2;
        var1 = var1 + var2;
        var2 = var2 - var1;
        _privatevar1 = var1 * var2;
        _privatevar2 = var2 / var1;
    }


    // Method with arguments
    public int method2(int denom) {
        if (denom <= 0) return -1;
        else {
            _privatevar1 = 17;
            _privatevar2 = 22;
            return (_privatevar1 * _privatevar2) / denom;
        }
    }

    // Private fields
    private int _privatevar1;
    private int _privatevar2;

}
