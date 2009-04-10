package ptolemy.codegen.kernel.userMacro;

import java.util.List;

import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

public class cdr {

    public static String handleMacro(List<String> arguments) throws IllegalActionException {
        String list = arguments.get(0).trim();
        int commaIndex = CodeGeneratorHelper._indexOf(",", list, 1);
        if (commaIndex >= 0) {
            String result = '(' + list.substring(commaIndex + 1
                , list.lastIndexOf(')')) + ')';
            return result;
        } else {
            return "()";
        }
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
