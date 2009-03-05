package ptolemy.codegen.kernel.newInterfaces.util;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import ptolemy.backtrack.util.java.util.ArrayList;
import ptolemy.util.StringUtilities;

/*
 * A tool for generating the CodeGenerator interface for a given 
 * Ptolemy class.
 */
public class InterfaceGenerator {


    static String className = "ptolemy.actor.IOPort";
    static String extendsValue = ""; // " extends E";
    
    static ArrayList excludeMethods = new ArrayList();
    static {
//        excludeMethods.add("clone");
//        excludeMethods.add("toString");
    }
    
    
    public static void main(String[] args) throws Exception {
        Class c = Class.forName(className);
        String interfaceName = c.getSimpleName() + "CodeGenerator";
        
        String filePath = "C:/eclipse/workspace/ptII/ptolemy/codegen/kernel/newInterfaces/";
        File file = new File(filePath + interfaceName + ".java");
        FileWriter writer = new FileWriter(file);
        
        writer.write("package ptolemy.codegen.kernel.newInterfaces;" + _eol);
        writer.write("import ptolemy.codegen.util.PartialResult;" + _eol);
        writer.write("public interface " + interfaceName + extendsValue + " {" + _eol);

        for (Method method : c.getDeclaredMethods()) {
            if (!Modifier.isPublic(method.getModifiers())) {
                continue;
            }

            if (excludeMethods.contains(method.getName())) {
                continue;
            }
            
            String declaration = "    ";
            
            if (Modifier.isStatic(method.getModifiers())) {
                declaration += "static ";
            }

            declaration += "public PartialResult " + method.getName() + "(";
            int i = 0;
            int length = method.getParameterTypes().length;
            for (Class type : method.getParameterTypes()) {
                declaration += "PartialResult"; // + "/*" + type.getSimpleName() + "*/";                                
                declaration += " " + "PARAM";
                i++;
                
                if (i < length) {
                    declaration += ", ";
                }
            }
            declaration += ");" + _eol;
            writer.write(declaration); 
        }
        writer.write("}" + _eol);
        writer.close();
        
        System.out.println(interfaceName + " created.");
        
    }

    
    static String _eol;
    static {
        _eol = StringUtilities.getProperty("line.separator");
        _eol += _eol;
    }
    
}
