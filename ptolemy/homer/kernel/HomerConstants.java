/* Constants used in the layout file.

 Copyright (c) 2011 The Regents of the University of California.
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

/**
 * 
 */
package ptolemy.homer.kernel;

///////////////////////////////////////////////////////////////////
//// HomerConstants

/** Constants used in the layout file. 
*
*  @author Peter Foldes
*  @version $Id$
*  @since Ptolemy II 8.1
*  @Pt.ProposedRating Red (pdf)
*  @Pt.AcceptedRating Red (pdf)
*/
public final class HomerConstants {

    /** Hide the constructor of the utility class.
     */
    private HomerConstants() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                public variables                           ////

    /** The name of the attribute that defines which tab to use for the
     *  node.
     */
    public static final String TAB_NODE = "droid_tab";

    /** The name of the attribute containing the tab definitions in the
     *  layout file.
     */
    public static final String TABS_NODE = "droid_tabs";

    /** Name of the default tab where elements should be put.
     */
    public static final String TAG = "Default";

    /** The name of the attribute containing the screen orientation
     *  definition in the layout file.
     */
    public static final String ORIENTATION_NODE = "droid_orientation";

    /** The name of the attribute containing the position definition in
     *  the layout file.
     */
    public static final String POSITION_NODE = "droid_location";

    /** The name of the attribute defining that a node should be run on the client.
     */
    public static final String REMOTE_NODE = "_remote";

    /** The value of a remote node indicating that the element is an attribute.
     */
    public static final String REMOTE_ATTRIBUTE = "attribute";

    /** The value of a remote node indicating that the element is a source.
     */
    public static final String REMOTE_SOURCE = "source";

    /** The value of a remote node indicating that the element is a sink.
     */
    public static final String REMOTE_SINK = "sink";

    /** The name of the attribute defining the node's style in the layout file.
     */
    public static final String STYLE_NODE = "droid_style";

}
