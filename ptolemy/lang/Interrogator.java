/*
Object interrogator for debugging purposes.

Copyright (c) 2000 The Regents of the University of California.
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

@ProposedRating Red (ctsay@eecs.berkeley.edu)
@AcceptedRating Red (ctsay@eecs.berkeley.edu)
*/


package ptolemy.lang;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Interrogator {

    /** Allow the user to query methods properties from the object. Then query
     *  the method return value or property value. Use the standard input stream 
     *  as the source for property keys to use. 
     */   
    public static void interrogate(Object obj) {
        interrogate(obj, new BufferedReader(new InputStreamReader(System.in)));    
    }
   
    /** Allow the user to query methods properties from the object. Then query 
     *  the method return value or property value. The user input BufferedReader is 
     *  taken from the argument.
     */
    public static void interrogate(Object obj, BufferedReader reader) {
        if (obj == null) {
           System.out.println("null");
           return;
        }
    
        int propertyNumber = -1;
        String lineString;
       
        PropertyMap propertyMap = (obj instanceof PropertyMap) ? (PropertyMap) obj : null;                          
        Class myClass = obj.getClass();
        String className = myClass.getName();
                 
        System.out.println(obj);
       
        do {
                                 
           System.out.print("[" + className + "] " +
            "Enter method name or property number to inspect (0 to quit) : ");   
      
           try {
             lineString = reader.readLine();
           } catch (IOException e) {
             throw new RuntimeException("I/O error reading method name or property number.");
           }
           
           try {                           
             propertyNumber = Integer.parseInt(lineString);              
       
             if (propertyNumber != 0) {
                // user input was a property number 
                if (propertyMap != null) {                                           
                   Object childObj = propertyMap.getProperty(new Integer(propertyNumber));                
          
                   if (childObj != null) {       
                      interrogate(childObj, reader);
                   } 
                } else {
                   System.err.println("Cannot retrieve a property of an object that is not a PropertyMap.");   
                }
             }
                        
           } catch (NumberFormatException nfe) {
             
             // user input was a method name
             
             Method method = null;
             
             try {             
               method = myClass.getMethod(lineString, null);
             } catch (NoSuchMethodException nsme) {
               System.err.println("No such method '" + lineString + "'.");                                       
               method = null;
             }
       
             if (method != null) {
                // can we use null instead of new Object[0] here? It doesn't say
                // in the spec for Method.invoke()
                try {                
                  Object childObj = method.invoke(obj, new Object[0]);                            
                  interrogate(childObj, reader);
                } catch (IllegalAccessException iae) {
                  System.err.println("Illegal access exception invoking method "
                   + lineString);
                } catch (InvocationTargetException ite) {
                  System.err.println("Invocation target exception invoking method "
                   + lineString + " : target = " + ite.getTargetException().toString());
                }                                                
             }
           
             propertyNumber = -1; // dummy number to ensure we don't exit the loop
           } 
        } while (propertyNumber != 0);                 
    }
}    
