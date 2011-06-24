/*
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

package ptserver.util;

import java.util.HashMap;
import java.util.Scanner;

import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// TypeParser

public class TypeParser {

    public static Type parse(String type) throws IllegalActionException {
        if (type == null) {
            return null;
        }
        Scanner scanner = new Scanner(type);
        scanner.useDelimiter("[^a-zA-Z0-9]+");
        String token = scanner.next();
        Type baseType = typeNames.get(token);
        if (baseType != null) {
            return baseType;
        }
        if (token.equals("arrayType")) {
            String innerTypeName = scanner.next();
            Type innerType = parse(innerTypeName);
            if (scanner.hasNextInt()) {
                return new ArrayType(innerType, scanner.nextInt());
            } else {
                return new ArrayType(innerType);
            }
        }
        throw new IllegalActionException(type + " type is not supported");
    }

    private static HashMap<String, Type> typeNames = new HashMap<String, Type>();

    static {
        typeNames.put("arrayBottom", BaseType.ARRAY_BOTTOM);
        typeNames.put("booolean", BaseType.BOOLEAN);
        typeNames.put("complex", BaseType.COMPLEX);
        typeNames.put("double", BaseType.DOUBLE);
        typeNames.put("event", BaseType.EVENT);
        typeNames.put("float", BaseType.FLOAT);
        typeNames.put("general", BaseType.GENERAL);
        typeNames.put("int", BaseType.INT);
        typeNames.put("long", BaseType.LONG);
        typeNames.put("niltype", BaseType.NIL);
        typeNames.put("petite", BaseType.PETITE);
        typeNames.put("scalar", BaseType.SCALAR);
        typeNames.put("short", BaseType.SHORT);
        typeNames.put("string", BaseType.STRING);
        typeNames.put("unknown", BaseType.UNKNOWN);
        typeNames.put("unsignedByte", BaseType.UNSIGNED_BYTE);
        typeNames.put("fixedpoint", BaseType.UNSIZED_FIX);
        typeNames.put("xmltoken", BaseType.XMLTOKEN);
    }
}
