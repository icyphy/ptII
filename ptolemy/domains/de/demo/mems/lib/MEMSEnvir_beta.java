
package ptolemy.domains.de.demo.mems.lib;
import ptolemy.domains.de.demo.mems.gui.*;
import ptolemy.actor.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import java.util.Enumeration;

public class MEMSEnvir_beta extends MEMSEnvir {

  public MEMSEnvir_beta(TypedCompositeActor container, String name, 
          MEMSDevice mems,
          double x, double y, double z, double temperature,
          MEMSPlot plot)
          throws IllegalActionException, NameDuplicationException  {
      super(container,name,mems,x,y,z,temperature,plot);
  }
    
    protected double getNewTemp() {
        double newTemp;
        if((curTime == 50)) {
            newTemp = 75.0;
        } else {
            newTemp = 69.0;
        }
        return newTemp;
    }

}
  
