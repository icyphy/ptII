/*
 Parse a string into a type.

 Copyright (c) 2011-2013 The Regents of the University of California.
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

package ptserver.util;

import java.util.Scanner;

import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// TypeParser

/**
 * This is a helper class that parses string into a type. The rationale
 * for this class is to have an ability to send type information over the network
 * as a string since Type instances are not serializable or their equals method (BaseType primarily)
 * checks for equality by reference which breaks after a new instance is deserialized.
 *
 * @author Anar Huseynov
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public class TypeParser {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /**
     * Return a Type instance by parsing the provided type string.
     *
     * <p> The type string must be created by calling toString() method on the Type.
     * @param type the type string
     * @return the Type instance identified by its type string.
     * @exception IllegalActionException if it was not possible to parse the type since it's not supported.
     */
    public static Type parse(String type) throws IllegalActionException {
        if (type == null) {
            return null;
        }
        Scanner scanner = new Scanner(type);
        scanner.useDelimiter("[^a-zA-Z0-9]+");
        String token = scanner.next();
        Type baseType = BaseType.forName(type);
        if (baseType != null) {
            return baseType;
        } else if (token.equals("arrayType")) {
            String innerTypeName = scanner.next();
            Type innerType = parse(innerTypeName);
            if (scanner.hasNextInt()) {
                return new ArrayType(innerType, scanner.nextInt());
            } else {
                return new ArrayType(innerType);
            }
        }
        // TODO add support for other types
        throw new IllegalActionException(type + " type is not supported");
    }
}
