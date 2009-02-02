package ptolemy.codegen.util;

public class StaticResult implements PartialResult {

    public StaticResult(Object result) {
        _result = result;
    }

    Object _result;
    
    public Object getResult() {
        return _result;
    }
    
    public boolean isStatic() {
        return true;
    }

    
}
