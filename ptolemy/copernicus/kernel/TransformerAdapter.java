

package ptolemy.copernicus.kernel;

import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.invoke.SiteInliner;
import soot.jimple.toolkits.invoke.StaticInliner;
import soot.jimple.toolkits.invoke.InvokeGraphBuilder;
import soot.jimple.toolkits.scalar.ConditionalBranchFolder;
import soot.jimple.toolkits.scalar.ConstantPropagatorAndFolder;
import soot.jimple.toolkits.scalar.CopyPropagator;
import soot.jimple.toolkits.scalar.DeadAssignmentEliminator;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;
import soot.jimple.toolkits.scalar.Evaluator;
import soot.toolkits.graph.*;
import soot.toolkits.scalar.*;
import soot.dava.*;
import soot.util.*;
import java.io.*;
import java.util.*;

import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.moml.*;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.data.*;
import ptolemy.data.type.Typeable;
import ptolemy.data.expr.Variable;
import ptolemy.copernicus.kernel.SootUtilities;


/**
An adapter that turns a body transformer into a scene transformer.  
This applies the transformer specified in the constructor to
all of the bodies in the scene.
*/
public class TransformerAdapter extends SceneTransformer {
    /** Construct a new transformer
     */
    public TransformerAdapter(BodyTransformer transformer) {
        _transformer = transformer;
    }

    protected void internalTransform(String phaseName, Map options) {
        System.out.println("TransformerAdapter.internalTransform("
                + phaseName + ", " + options + ")");
        
        Iterator classes = Scene.v().getApplicationClasses().iterator();
        while(classes.hasNext()) {
            SootClass theClass = (SootClass)classes.next();
            Iterator methods = theClass.getMethods().iterator();
            while(methods.hasNext()) {   
                SootMethod m = (SootMethod) methods.next();
                if(!m.isConcrete())
                    continue;
                
                JimpleBody body = (JimpleBody) m.retrieveActiveBody();
              
                // FIXME: pass in the options.
                _transformer.transform(body, phaseName, "");
            }
        }    
    }
    
    private BodyTransformer _transformer;
}














