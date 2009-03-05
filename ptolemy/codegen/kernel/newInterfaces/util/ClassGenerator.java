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
public class ClassGenerator {


    static String targetClassName = "ptolemy.actor.sched.StaticSchedulingDirector";    
    static String extendsValue = ""; // " extends E";
    static String implementsValue = ""; // " implements I1, I2, I3";
    
    static ArrayList excludeMethods = new ArrayList();
    static {
        excludeMethods.add("clone");
        excludeMethods.add("toString");
    }
    
    
    public static void main(String[] args) throws Exception {
        Class c = Class.forName(targetClassName);
        String className = c.getSimpleName();
        
        String filePath = "C:/eclipse/workspace/ptII/ptolemy/codegen/kernel/newInterfaces/";
        File file = new File(filePath + className + ".java");
        FileWriter writer = new FileWriter(file);
        
        writer.write("package ptolemy.codegen.kernel.newInterfaces;" + _eol);
        writer.write("import ptolemy.codegen.util.PartialResult;" + _eol);
        writer.write("public class " + className + extendsValue + implementsValue + " {" + _eol);

        for (Method method : c.getDeclaredMethods()) {
            if (excludeMethods.contains(method.getName())) {
                continue;
            }
            
            String declaration = "    ";
            
            if (Modifier.isAbstract(method.getModifiers())) {
                declaration += "abstract ";
            }

            if (Modifier.isStatic(method.getModifiers())) {
                declaration += "static ";
            }

            String scope = "";
            
            if (Modifier.isPublic(method.getModifiers())) {
                scope = "public";
            } else if (Modifier.isProtected(method.getModifiers())) {
                scope = "protected";
            
            } else if (Modifier.isPrivate(method.getModifiers())) {
                scope = "private";
            }
            
            declaration += scope + " PartialResult " + method.getName() + "(";
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
            declaration += ") {" + _eol;
            declaration += "        throw new RuntimeException(\"Not supported yet.\");" + _eol;
            declaration +=  "    }" + _eol;
            
            
            writer.write(declaration); 
        }
        writer.write("}" + _eol);
        writer.close();
        
        System.out.println(className + " created.");
        
    }

    
    static String _eol;
    static {
        _eol = StringUtilities.getProperty("line.separator");
        _eol += _eol;
    }
    
}
