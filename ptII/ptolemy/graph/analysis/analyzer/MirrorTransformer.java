/* Base interface for a mirror transformer for graphs.

 Copyright (c) 2003-2005 The University of Maryland. All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF MARYLAND BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF MARYLAND HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF MARYLAND SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 MARYLAND HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.


 */
package ptolemy.graph.analysis.analyzer;

import ptolemy.graph.Graph;

//////////////////////////////////////////////////////////////////////////
//// MirrorTransformerAnalyzer

/**
 Base interface for a mirror transformer for graphs.
 <p>
 In the {@link #cloneWeight} method, users can also specify whether to clone node
 and edge weights. For non cloneable
 weights a {@link java.lang.CloneNotSupportedException} will be thrown by
 the virtual machine.
 <p>
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (shahrooz)
 @Pt.AcceptedRating Red (ssb)
 @see ptolemy.graph.analysis.MirrorTransformation
 @author Shahrooz Shahparnia
 @version $Id$
 */
public interface MirrorTransformer extends Transformer {
    /** Changes the status of the graph returned by the {@link #mirror} method.
     *  If set to true, the weights will also be cloned in the next calls to the
     *  {@link #mirror} method.
     *
     *  @param status If set to true, the weights will also be cloned.
     */
    public void cloneWeight(boolean status);

    /** Create a mirror of the graph associated with this analyzer with the
     *  same runtime class.
     *
     *  @return The resulting mirror graph.
     */
    public Graph mirror();

    /** Return a mirror of this graph in the form of the argument graph type
     *  (i.e., the run-time type of the returned graph is that of the
     *  argument graph).
     *  <p>
     *
     *  @param graph The type of the graph which the graph associated with
     *  this analyzer is being mirrored to.
     *  @param cloneWeights If set true, the weights will also be cloned.
     *  @return The resulting mirror graph.
     */
    public Graph mirror(Graph graph, boolean cloneWeights);
}
