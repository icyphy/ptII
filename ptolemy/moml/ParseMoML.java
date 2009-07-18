/* Provider of MoML parser for the expression language.

 Copyright (c) 1998-2009 The Regents of the University of California.
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
package ptolemy.moml;

import ptolemy.data.ActorToken;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// ParserMoML

/** 
This class provides a static method that is registered with the expression
language that will return an ActorToken given a string argument that
contains MoML defining an Entity or subclass of Entity.
@author Edward A. Lee
@version $Id$
@since Ptolemy II 8.0
@Pt.ProposedRating Yellow (eal)
@Pt.AcceptedRating Red (johnr)
*/
public final class ParseMoML {
    /** Parse the string provided and return the result wrapped in a token.
     *  @param moml The MoML string.
     *  @return The result of parsing the MoML.
     *  @throws Exception If the MoML is invalid or the results is not an
     *   instance of Entity.
     */
    public static ActorToken parseMoML(String moml) throws Exception {
        _parser.reset();
        NamedObj parseResult = _parser.parse(moml);
        if (!(parseResult instanceof Entity)) {
            throw new IllegalActionException("MoML does not specify an Entity: " + moml);
        }
        return new ActorToken((Entity)parseResult);
    }
    
    private static MoMLParser _parser = new MoMLParser();
}
