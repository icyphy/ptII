package ptolemy.codegen.util;

public class DynamicResult implements PartialResult {

    public DynamicResult(Object result) {
        _result = result;
    }

    Object _result;
    
    public Object getResult() {
        return _result;
    }
    
    public boolean isStatic() {
        return false;
    }

    
}
