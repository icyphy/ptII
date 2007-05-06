/* An actor that combines multiple XML files into one.

@Copyright (c) 2007 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

PT_COPYRIGHT_VERSION 2
COPYRIGHTENDKEY
*/
package ptolemy.actor.lib.xslt;

import ptolemy.actor.lib.Transformer;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.ArrayToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.XMLToken;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;

////////////////////////////////////////////////////////////////////////////////

/*
Combines multiply XMLTokens into one XMLToken.  

The actor reads in multiple arrays of XML Tokens from the <i>input</i> port.
It also takes a port parameter, <i>template</i>, that specifies how the XML 
tokens will be combined.  The template is of the form:  
<pre>
&lt;?xml version=&quot;1.0&quot; standalone=&quot;no&quot;?&gt;
 &lt;Node&gt;
   $inputi,j
 &lt;/Node&gt;
</pre>
The template is a XML Token with $input as a delimiter for where the input
XML tokens should be placed.  i specifies which array (i.e. which channel) and j
specifies which XML Token in the array.  Setting j = n will insert all XML 
tokens in that particular array into the template file.  If i or j are out of 
bounds, $inputi,j will not be replaced.
It also takes in a string parameter, <i>headerParameter</i>, which is the header
used for the output XML token.
A XML Token with the delimiters replaced with the appropriate XML Token is sent
to the <i>output</i> port.  No changes are made to the input XML Tokens besides
removing the header and DTD.

@author Christine Avanessians, Edward Lee, Thomas Feng
@version $Id$
@since Ptolemy II 6.0.2
@Pt.ProposedRating Red (cavaness)
@Pt.AcceptedRating Red (cavaness)
*/

public class XMLInclusion extends Transformer{
   
   public XMLInclusion(CompositeEntity container, String name)
             throws IllegalActionException, NameDuplicationException {
       super(container, name);
       
       // Set the type of the input port.
       // Input port is a multiport.
       input.setTypeEquals(new ArrayType(BaseType.XMLTOKEN));
       input.setMultiport(true);
       
       template = new PortParameter(this, "template");
       template.setStringMode(true);
        
       headerParameter = new StringParameter(this, "headerParameter");
       headerParameter.setExpression("<?xml version=\"1.0\" standalone=\"no\"?>");
       
       // Set the type of the output port.
       output.setTypeEquals(BaseType.XMLTOKEN);
   }

   ///////////////////////////////////////////////////////////////////
   ////                     ports and parameters                  ////

   /* WILL ADD Comments
    */
   public PortParameter template;
   public StringParameter headerParameter;

   ///////////////////////////////////////////////////////////////////
   ////                         public methods                    ////
   
   /** Takes in multiple arrays of XMLTokens from the input and combines them
    *  according to the specified template.  If the template contains invalid
    *  delimiters, it returns the template file with the valid ones replaced and
    *  the invalid ones unmodified.
    *  @exception IllegalActionException
    */
   public void fire() throws IllegalActionException{ 
       super.fire();
       template.update();
       String outputString = removeHeader(template.getToken());
       String all="";
       for (int j=0; j <input.getWidth(); j++) {
           ArrayToken a = (ArrayToken) input.get(j);
           String allInArray="";
           int i;
           for (i=0; i < a.length(); i++) {
               String elemInArray=removeHeader(a.getElement(i));
               if (i==0) {
                   allInArray=allInArray.concat(elemInArray);
               } else {
                   allInArray=allInArray.concat('\n'+ elemInArray);
               }
               String elemTag = "$input" + Integer.toString(j) + ',' 
                                                       + Integer.toString(i);
               outputString = outputString.replace(elemTag, elemInArray);
           }
               String arrayTag = "$input" + Integer.toString(j) + ",n";
               outputString = outputString.replace(arrayTag, allInArray);
          if (j==0) {
              all=all.concat(allInArray); 
          } else {
              all = all.concat('\n'+ allInArray);
          }
       }   
       outputString =outputString.replace("$inputn", all);
       String ADDheader = headerParameter.stringValue() + "\n"; 
       ADDheader = ADDheader.concat(outputString);
       try{
       XMLToken out = new XMLToken(ADDheader);
       output.broadcast(out);
       }
       catch (Exception e) {
           throw new InternalErrorException(e);
       }     
   }
   
   //Removes XML header and DTD if there is one
   private String removeHeader (Token T) {
       String str="";
       if (T instanceof StringToken){
           str = ((StringToken)T).stringValue();
       } else if (T instanceof XMLToken) {
           str = T.toString();
       } else {
           throw new InternalErrorException("The token should be either " +
                   "of type StringToken, or of type XMLToken.");
       }

       String s= str.trim();
       int i=0;
       if (s.startsWith("<?xml")) { //removes Header
           i=1;
           while (s.charAt(0)!= '\n'){
               s=s.substring(1);
           }
           s=s.substring(1); //removes the new line;
       }
       String s2= s.trim();
       if (s2.startsWith("<!DOCTYPE")) {//removes DTD
           boolean inQuote = false;
           int pos = 0;
           while (pos < s2.length() && (inQuote || s2.charAt(pos) != '>')){
               if (s2.charAt(pos) == '\"') {
                   inQuote = !inQuote;
               }
               pos++;
           }
           if (pos < s2.length()) {
               s2 = s2.substring(pos + 1);
           }
           i=2;
           /*while (s2.charAt(0)!= '\n'){
               s2=s2.substring(1);
           }*/
           if(s2.charAt(0)=='\n')  {
                s2=s2.substring(1); //removes the new line;
           }
       }
       if (i==0) { // in order to not remove the white spaces that trim removes
           return str;
       } else if (i==1) {
           return s;
       } else {
           return s2;
       }
   }

}
   