/* Return a list containing all the backward compatibility filters

 Copyright (c) 2002-2003 The Regents of the University of California.
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
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.moml.filter;

import ptolemy.moml.MoMLFilter;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// BackwardCompatibility
/** Return a list where each element is a backward compatibility filter
to be applied by the MoMLParser.

<p>When this class is registered with
<pre>
MoMLParser.addMoMLFilters(BackwardCompatibility.allFilters())
</pre>
method, it will cause MoMLParser to filter so that models from
earlier releases will run in the current release.

@see ptolemy.moml.MoMLFilter
@author Christopher Hylands, Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
*/
public class BackwardCompatibility {
    /** Return a list where each element of the list is a
     *  MoMLFilter to be applied to handle backward compatibility
     */
    public static List allFilters() {
        return _filterList;
    }

    /** Return a string that describes all the filters
     *  @return the String that describes all the filters and that ends with a
     *  newline.
     */
    public String toString() {
        // This is a little strange because when we call
        // BackwardCompatibility.allFilters(), we add the individual filters
        // so when we iterate through the filters and call toString, we never
        // actually call BackwardCompatibility.toString().

        // Ideally, we would like to make toString() static, but we
        // can't do that because Object.toString() is not static

        StringBuffer results =
            new StringBuffer("This filter contains the following filters:\n");
        Iterator filters = _filterList.iterator();
        while (filters.hasNext()) {
            results.append(((MoMLFilter)filters.next()).toString() + "\n");
        }
        return results.toString();
    }

    // List of MoMLFilters to be applied.
    private static List _filterList;

    static {
        _filterList = new LinkedList();
        _filterList.add(new AddEditorFactory());
        _filterList.add(new AddIcon());
        _filterList.add(new ClassChanges());
        _filterList.add(new HideAnnotationNames());
        _filterList.add(new MultiportToSinglePort());
        _filterList.add(new ParameterNameChanges());
        _filterList.add(new PortClassChanges());
        _filterList.add(new PortNameChanges());
        _filterList.add(new PropertyClassChanges());
    }
}
