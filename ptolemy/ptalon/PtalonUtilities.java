package ptolemy.ptalon;

import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import ptolemy.actor.lib.Scale;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;

/**
 * A set of utility methods to integrate Ptalon and Ptolemy.
 * 
 * 
 * @author acataldo
 */
public abstract class PtalonUtilities {
    
    /**
     * This method generates a Ptalon file for a Ptolemy actor.
     * 
     * @param input The java class representation of the actor.
     * @param outputFilename The absolute filename to write the Ptalon code to.
     */
    public static void PtolemyToPtalon(Class input,
            String outputFilename) {
        File output = new File(outputFilename);
        String outputCode;
        String name = input.getName();
        outputCode = name.concat(" is {\n");
        Field[] fields = input.getFields();
        Class type;
        Class portType = (new Port()).getClass();
        for (int i = 0; i < fields.length; i++) {
            type = fields[i].getType();
            if (portType.isAssignableFrom(type)) {
                name = fields[i].getName();
                outputCode = outputCode.concat("\tport " + name 
                        + ";\n");
            }
        }
        outputCode = outputCode.concat("\t$actorSource " 
                + input.getName() + "$\n}\n");
        try {
            FileWriter writer = new FileWriter(output);
            writer.write(outputCode);
            writer.close();
        } catch(IOException e) {
            System.out.println(e.getMessage());
            return;
        }  
    }
    
    /**
     * This method is used to test the methods.
     * 
     * FIXME: Replace this with a tcl test.
     * @param args
     */
    public static void main(String[] args) {
        CompositeEntity foo = new CompositeEntity();
        Scale scale;
        try {
            scale = new Scale(foo, "Foo");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }
        String ptii = System.getenv("PTII");
        String outputDir = ptii.concat("/ptolemy/ptalon/");
        String output = outputDir.concat("Foo.ptln");
        PtolemyToPtalon(scale.getClass(), output);
    }
}
