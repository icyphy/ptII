/* Ptolemy II Version identifiers

 Copyright (c) 2001 The Regents of the University of California.
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

package ptolemy.kernel.util;

//////////////////////////////////////////////////////////////////////////
//// Version
/**
See page 53 of the JNLP specification at
<a href="http://jcp.org/jsr/detail/056.jsp"><code>http://jcp.org/jsr/detail/056.jsp</code></a>
 */
public class Version extends List {
    
    /** Construct a new Version ID from a String.
     *  The versionString argument consists of a string containing
     *  any ASCII characters except a space.  Version ID elements
     *  are strings separated by one of '.', '-' or '_'. 
     *  Version IDs may optionally have a trailing '+' or '*'
     *  character.  '+' indicates a greater-than-or-equal-to match.
     * '*' indicates a prefix match.
     *
     *  @exception Exception If the argument contains a space, which violates
     *  the JNLP Version format specification
     */
    public Version(String versionString) throws Exception {
        if (versionString.contains(" ")) {
            throw new Exception("Versions cannot contain spaces: '"
                    + versionString + "'");
        }
        StringTokenizer tokenizer = new StringTokenizer(versionString, ".-_");
        while (tokenizer.hasMoreTokens()) {
            add(nextToken());
        }
      
    }

    ///////////////////////////////////////////////////////////////
    ////                     public methods                    ////

    /** Test that the value of this token is greater than the argument
     *  according to the Version conversion and padding rules.  
     *  <p> "1.2.2-005" is greater than "1.2.2.4", 
     *  <br> "1.3.1" is an greater than "1.3"
     *  @param object The Version to compare against. 
     *  @return 0 if the argument is an exact match according to
     *  the Version padding rules, a number less than 0 if the argument is less
     *  than this Version, a number greater than 0 if the argument is
     *  greater than this Version
     */ 
    public boolean compareTo(Object object) {
        // Similar to the String.compareTo()
        Version version = (Version) object
        Iterator versionTuples = version.iterator();
        Iterator tuples = iterator();
        while (versionTuples.hasNext() || tuples.hasNext()){
            String versionTuple, tuple;

            // FIXME: deal with * and + 
            // Normalize the shortest tuple by padding with 0
            if (versionTuples.hasNext()) {
                versionTuple = versionTuples.next();
            } else {
                versionTuple = "0";
            }
            if (tuples.hasNext()) {
                tuple = tuples.next();
            } else {
                tuple = "0";
            }
            if (tuple.compareTo(version) < 0) {
                return true;
            }
        }
            
        }
    }


    ///////////////////////////////////////////////////////////////
    ////                     public variables                  ////

    private List _tupleList;

}
