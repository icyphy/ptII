package ptolemy.domains.openmodelica.lib.core;


public class CompilerResult implements ICompilerResult {

    public CompilerResult(String[] result, String error) {
        this.result = result;
        this.error = error;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public String getError() {
        return error;
    }

    public String getFirstResult() {
        return result[0];
    }

    public String[] getResult() {
        return result;
    }

    public static ICompilerResult makeResult(String[] result, String error) {
        return new CompilerResult(result, error);
    }

    public void trimFirstResult() {
        result[0] = result[0].trim();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private String error;
    private String[] result;
}
