/* Edges of a graph in a Dijkstra Algorithm.
   Copyright (c) 2015-2016 The Regents of the University of California.
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

package ptolemy.domains.atc.kernel.policy2;

/** Edges of a graph in DijkstraAlgorithm.
 *  @author Maryam Bagheri
 *  @version $Id$
 *  @since Ptolemy II 11.0
 */
public class Edge  {

    /** Create an edge.
     * @param id an int specifying an identifier
     * @param source a source
     * @param destination  a destination
     * @param weight a weight.
     */
    public Edge(int id, Vertex source, Vertex destination, int weight) {
        _id = id;
        _source = source;
        _destination = destination;
        _weight = weight;
    }

    /** Get the id.
     *  @return The id.
     */
    public int getId() {
        return _id;
    }

    /** Get the destination.
     *  @return The destination.
     */
    public Vertex getDestination() {
        return _destination;
    }

    /** Get the source.
     *  @return The source
     */
    public Vertex getSource() {
        return _source;
    }

    /** Get the weight.
     * @return the weight.
     */
    public int getWeight() {
        return _weight;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private fields                        ////

    private final int _id;
    private final Vertex _source;
    private final Vertex _destination;
    private final int _weight;
}
