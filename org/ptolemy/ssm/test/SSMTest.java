/* Test that illustrates a ConcurrentModificationException

   Copyright (c) 2014 The Regents of the University of California.
   All rights reserved.
   Permission is hereby granted, without written agreement and without
   license or royalty fees, to use, copy, modify, and distribute this
   software and its documentation for any purpose, provided that the above
   copyright notice and the following two paragraphs appear in all copies
   of this software.

   IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
   FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
   ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
   THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
   SUCH DAMAGE.

   THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
   INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
   MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
   PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
   CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
   ENHANCEMENTS, OR MODIFICATIONS.

   PT_COPYRIGHT_VERSION_2
   COPYRIGHTENDKEY

*/

package org.ptolemy.ssm.test;

import java.util.Iterator;
import org.ptolemy.ssm.StateSpaceModel;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Workspace;

/** Iterate through the attributes of a StateSpaceModel.
 *
 * <p>To replicate:</p>
 *
 * <pre>
 * bash-3.2$ javac -classpath $PTII SSMTest.java
 * bash-3.2$ java -classpath $PTII org.ptolemy.ssm.test.SSMTest
 * Attribute: ptolemy.kernel.util.SingletonConfigurableAttribute {..myStateSpace._iconDescription}
 * Attribute: ptolemy.data.expr.Parameter {..myStateSpace.stateVariableNames} {"x", "y"}
 * Exception in thread "main" java.util.ConcurrentModificationException
 *	at java.util.LinkedList$ListItr.checkForComodification(LinkedList.java:966)
 *	at java.util.LinkedList$ListItr.next(LinkedList.java:888)
 *	at java.util.Collections$UnmodifiableCollection$1.next(Collections.java:1042)
 *	at org.ptolemy.ssm.test.SSMTest.main(SSMTest.java:17)
 * </pre>
 *
 * @author Christopher Brooks
 * @version $Id: FMULog.java 70970 2014-12-13 01:32:38Z cxh $
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
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