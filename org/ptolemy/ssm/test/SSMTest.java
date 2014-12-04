package org.ptolemy.ssm.test;

import java.util.Iterator;
import org.ptolemy.ssm.StateSpaceModel;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Workspace;
/** Iterate through the attributes of a StateSpaceModel
<pre>
bash-3.2$ javac -classpath $PTII SSMTest.java
bash-3.2$ java -classpath $PTII org.ptolemy.ssm.test.SSMTest
Attribute: ptolemy.kernel.util.SingletonConfigurableAttribute {..myStateSpace._iconDescription}
Attribute: ptolemy.data.expr.Parameter {..myStateSpace.stateVariableNames} {"x", "y"}
Exception in thread "main" java.util.ConcurrentModificationException
	at java.util.LinkedList$ListItr.checkForComodification(LinkedList.java:966)
	at java.util.LinkedList$ListItr.next(LinkedList.java:888)
	at java.util.Collections$UnmodifiableCollection$1.next(Collections.java:1042)
	at org.ptolemy.ssm.test.SSMTest.main(SSMTest.java:17)
bash-3.2$ 
</pre>
 */
public class SSMTest {
    public static void main(String[] args) throws Throwable {
        Workspace workspace = new Workspace();
        CompositeEntity toplevel = new CompositeEntity(workspace);
        StateSpaceModel ssm = new StateSpaceModel(toplevel, "myStateSpace");
        Iterator attributes = ssm.attributeList().iterator();
        while (attributes.hasNext()) {
            Attribute attribute = (Attribute)attributes.next();
            System.out.println("Attribute: " + attribute);
            ssm.attributeChanged(attribute);
        }
    }
}