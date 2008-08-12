package ptolemy.codegen.kernel.userMacro;

import java.util.List;

import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

public class car {

    public static String handleMacro(List<String> arguments) throws IllegalActionException {
        String list = arguments.get(0).trim();
        list = list.substring(list.indexOf('(') + 1, list.lastIndexOf(')'));
        return CodeGeneratorHelper._parseList(list).get(0);
    }
    
    public static boolean checkArguments(List<String> arguments) {
        if (arguments.size() != 1) {
            return false;
        }
        
        String list = arguments.get(0).trim();
        if (!list.startsWith("(") || !list.endsWith(")")) {
            return false;
        }
        
        return true;
    }
}
