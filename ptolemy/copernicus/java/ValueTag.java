

package ptolemy.copernicus.java;

import soot.*;
import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;
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
import ptolemy.data.expr.Variable;
import ptolemy.copernicus.kernel.SootUtilities;


/**

*/
public class ValueTag implements Tag {
    
    /** Construct a new tag that refers to the given object.
     */
    public ValueTag(Object object) {
        _object = object;
    }
                                 
    /** Return the name of the tag.
     */
    public String getName() {
        return "_CGValue";
    }

    /** Return the value of the tag.
     */
    public Object getObject() {
        return _object;
    }

    /** Returns the tag raw data. 
     */
    public byte[] getValue() throws AttributeValueException {
        return new byte[0];
    }

    private Object _object;

}
