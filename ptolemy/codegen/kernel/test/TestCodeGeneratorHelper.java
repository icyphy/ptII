/** Test CodeGeneratorHelper */

package ptolemy.codegen.kernel.test;

import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.kernel.util.NamedObj;

public class TestCodeGeneratorHelper extends CodeGeneratorHelper {
    public TestCodeGeneratorHelper(NamedObj namedObj) {
        super(namedObj);
    }

    public VariableScope variableScope = new VariableScope();
}
