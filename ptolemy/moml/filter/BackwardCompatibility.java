/* Return a list containing all the backward compatibility filters

 Copyright (c) 2002-2014 The Regents of the University of California.
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
package ptolemy.moml.filter;

import java.util.LinkedList;
import java.util.List;

import ptolemy.moml.MoMLFilter;

//////////////////////////////////////////////////////////////////////////
//// BackwardCompatibility

/** Return a list where each element is a backward compatibility filter
 to be applied by the MoMLParser.

 <p>When this class is registered with</p>
 <pre>
 MoMLParser.addMoMLFilters(BackwardCompatibility.allFilters())
 </pre>
 <p>method, it will cause MoMLParser to filter so that models from
 earlier releases will run in the current release.</p>

 @see ptolemy.moml.MoMLFilter
 @author Christopher Hylands, Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class BackwardCompatibility {

    /** Add a MoMLFilter to the list of filters.
     *  @param filter The filter to be added.
     */
    public static void addFilter(MoMLFilter filter) {
        _filterList.add(filter);
    }

    /** Return a shallow copy of the list where each element of the
     *  list is a MoMLFilter to be applied to handle backward
     *  compatibility.
     *
     *  @return a list of all the filters.
     */
    public static List allFilters() {
        // Return a clone of the list and not the list itself.
        // The reason is that callers might add to the list
        // and we don't want to modify the base list.
        // To replicate, use:
        //  cd moml/filter/test
        //  $PTII/bin/ptjacl
        //  source ActorIndex.tcl
        //  source GRColorChanges.tcl
        return (List) ((LinkedList) _filterList).clone();
    }

    /** Clear the list of filters.
     */
    public static void clear() {
        _filterList = new LinkedList();
    }

    /** Return a string that describes all the filters.
     *  @return the String that describes all the filters and that ends with a
     *  newline.
     */
    @Override
    public String toString() {
        // This is a little strange because when we call
        // BackwardCompatibility.allFilters(), we add the individual filters
        // so when we iterate through the filters and call toString, we never
        // actually call BackwardCompatibility.toString().
        // Ideally, we would like to make toString() static, but we
        // can't do that because Object.toString() is not static
        StringBuffer results = new StringBuffer(
                "This filter contains the following filters:\n");

        for (MoMLFilter filter : _filterList) {
            results.append(filter.toString() + "\n");
        }

        return results.toString();
    }

    // List of MoMLFilters to be applied.
    private static List<MoMLFilter> _filterList;

    static {
        _filterList = new LinkedList<MoMLFilter>();
        // AddEditorFactory is deprecated, use AddMissingParameter instead.
        //_filterList.add(new AddEditorFactory());
        _filterList.add(new AddMissingParameter());
        _filterList.add(new AddIcon());
        _filterList.add(new ClassChanges());
        //_filterList.add(new UpdateAnnotations());
        _filterList.add(new HideAnnotationNames());
        _filterList.add(new MultiportToSinglePort());
        _filterList.add(new ParameterNameChanges());
        _filterList.add(new PortClassChanges());
        _filterList.add(new PortNameChanges());
        _filterList.add(new PropertyClassChanges());
        _filterList.add(new GRColorChanges());
        _filterList.add(new RemoveProperties());
        //System.out.println("Filtering and converting to LazyTypedCompositeActors");
        //_filterList.add(new LazyTypedCompositeActorChanges());
        _filterList.add(new RelationWidthChanges());
    }
}
