/* A base interface for transformers.

 Copyright (c) 2003-2005 The University of Maryland.
 All rights reserved.
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

//////////////////////////////////////////////////////////////////////////
//// Transformer

/**
 A base interface for transformers. Transformers are graph analyzers which
 transform a graph into another graph and they provide a bilateral way to
 retrieve the original elements of the graph from the new (transformed) ones
 and vice versa. If only unilateral relation is being considered,
 this can be communicated to the client through the {@link #hasBackwardMapping()}
 and {@link #hasForwardMapping()} methods.
 <p>
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (shahrooz)
 @Pt.AcceptedRating Red (ssb)
 @author Shahrooz Shahparnia, Shuvra S. Bhattacharyya
 @version $Id$
 */
public interface Transformer extends GraphAnalyzer {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Specify if this transformer has a mapping from the transformed
     *  version to the original version or not.
     *
     *  @return True  if the implementation of the transformer supports backward
     *  mapping.
     */
    public boolean hasBackwardMapping();

    /** Specify if this transformer has a mapping from the original
     *  version to the transformed version or not.
     *
     *  @return True if the implementation of the transformer supports forward
     *  mapping.
     */
    public boolean hasForwardMapping();

    /** Return the original version of given object in the transformed graph.
     *  The method should be defined in implementing classes to return the
     *  original version of the given object. The transformed objects and a
     *  mapping between the original and the transformed objects are
     *  created during the transformation process in a specific algorithm
     *  (strategy).
     *
     *  @param transformedObject The given object in the transformed graph.
     *  @return Return the original version the given object.
     */
    public Object originalVersionOf(Object transformedObject);

    /** Return the transformed version of a given object in the original graph.
     *  The method should be defined in implementing classes to return the
     *  transformed version of the given object. The transformed objects and a
     *  mapping between the original and the transformed objects are
     *  created during the transformation process in a specific algorithm
     *  (strategy).
     *
     *  @param originalObject The given object in the original graph.
     *  @return Return the transformed version of the given object.
     */
    public Object transformedVersionOf(Object originalObject);
}
