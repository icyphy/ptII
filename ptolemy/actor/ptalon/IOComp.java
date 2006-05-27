package ptolemy.actor.ptalon;

import java.io.FileWriter;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIORelation;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.actor.lib.gui.Display;
import ptolemy.actor.lib.Ramp;
import ptolemy.domains.sdf.kernel.SDFDirector;

public class IOComp extends TypedCompositeActor {
   
   IOComp() throws IllegalActionException, NameDuplicationException {
       super();
       setName("IOComp");
       SDFDirector director = new SDFDirector(this, "SDF");
       Ramp r = new Ramp(this, "r");
       Display d = new Display(this, "d");
       d.columnsDisplayed.setExpression("50");
       TypedIORelation r_output__d_input = new TypedIORelation(this, "r_output__d_input");
       r.output.insertLink(0, r_output__d_input);
       d.input.insertLink(0, r_output__d_input);
   }
   
   public static void main(final String[] args) { 
       try {
           IOComp model = new IOComp();
           String output = model.exportMoML();
           FileWriter writer = new FileWriter("IOComp.xml");
           writer.write(output);
           writer.close();
           System.out.println("Generated IOComp.xml");
       } catch(Exception e) {
           System.err.println(e.getMessage());
           return;
       }
   }
   
}
