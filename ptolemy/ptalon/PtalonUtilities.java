package ptolemy.ptalon;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import ptolemy.actor.lib.Ramp;
import ptolemy.actor.lib.BooleanSelect;
import ptolemy.actor.lib.Scale;
import ptolemy.actor.Actor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.actor.IOPort;

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
     * @param input An instance of the actor to generate code for.
     * @param output The absolute filename to write the Ptalon code to.
     */
    public static String ptolemyToPtalon(Actor input) {
        String output;
        Class inputClass = input.getClass();
        String name = inputClass.getSimpleName();
        output = name.concat(" is {\n");
        Field[] fields = inputClass.getFields();
        Class type;
        Class ioPortType = IOPort.class;
        IOPort port;
        String portFlowType;
        try {
            for (int i = 0; i < fields.length; i++) {
                type = fields[i].getType();
                if (ioPortType.isAssignableFrom(type)) {
                    name = fields[i].getName();
                    port = ((IOPort) fields[i].get(input));
                    if (port.isInput()) {
                        if (port.isOutput()) {
                            portFlowType = "port";
                        }
                        else {
                            portFlowType = "inport";
                        }
                    } else {
                        portFlowType = "outport";
                    }
                    output = output.concat("\t" + 
                            portFlowType + " " + name + ";\n");
                }
            }
        } catch(Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
        output = output.concat("\t$actorSource " 
                + inputClass.getName() + "$\n}\n");
        return output;  
    }
    
    /**
     * This method is used to test the ptolemyToPtalon method.
     * 
     * FIXME: Replace this with a tcl test.
     * @param args
     */
    public static void main(String[] args) {
        CompositeEntity foo = new CompositeEntity();
        try {
            Actor[] actors = {new Scale(foo, "Foo"),
                    new Ramp(foo, "Bar"),
                    new BooleanSelect(foo, "Baz")};
            String ptii = System.getenv("PTII");
            String outputDir = ptii.concat("/ptolemy/ptalon/");
            String outputFile, name;
            for (int i = 0; i < actors.length; i++) {
                name = actors[i].getClass().getSimpleName();
                outputFile = outputDir.concat(name + ".ptln");
                String output = ptolemyToPtalon(actors[i]);
                File file = new File(outputFile);
                FileWriter writer = new FileWriter(file);
                writer.write(output);
                writer.close();
           }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
