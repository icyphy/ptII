/* An Interface for Objects that wish to be notified of debugging events.
@ProposedRating Red (neuendor@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.kernel;

/** I wrote this class in an effort to provide a nice way to deal with 
    inserting debugging print statements into code.   It is often a nicer
    way to trace code than by stepping through with jdb and this mechanism 
    allows the more useful of these trace statements to remain in code with 
    little overhead.

@author Steve Neuendorffer
@version $Id$
*/

public class DebugListener {

    public void tell(String s) {
        System.out.print(s);
    }

}
