/* FIXME
 * Created on Feb 1, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ptolemy.codegen.kernel;

/**
 * @author eal
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface CodeGeneratingActor {

    /** FIXME */
    public void generateFireCode();
    
    /** FIXME */
    public String getCode(String streamName);
}
