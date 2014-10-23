/* Concept function definition attribute for any unary operation.

 Copyright (c) 2003-2014 The Regents of the University of California.
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
package ptolemy.data.ontologies.lattice;

import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.ontologies.ExpressionConceptFunctionDefinitionAttribute;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

///////////////////////////////////////////////////////////////////
//// UnaryConceptFunctionDefinition

/** Concept function definition attribute for any unary operation.
 *
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class UnaryOperationMonotonicFunctionDefinition extends
ExpressionConceptFunctionDefinitionAttribute {

    /** Construct the UnaryOperationConceptFunctionDefinition attribute
     *  with the given container and name.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public UnaryOperationMonotonicFunctionDefinition(CompositeEntity container,
            String name) throws IllegalActionException,
            NameDuplicationException {
        super(container, name);

        // Since a binary function always has 2 arguments, its
        // number of arguments is fixed.
        numberOfArgumentsIsFixed.setToken(BooleanToken.TRUE);
        numberOfArgumentsIsFixed.setVisibility(Settable.NONE);

        // This attribute must define a monotonic concept function.
        constrainFunctionToBeMonotonic.setToken(BooleanToken.TRUE);
        constrainFunctionToBeMonotonic.setVisibility(Settable.NOT_EDITABLE);

        functionOntologyName = new StringParameter(this, "functionOntologyName");
        functionOntologyName.setExpression("");

        // Constrain argument list to have only 1 argument.
        argumentNames.setTypeEquals(new ArrayType(BaseType.STRING, 1));
        argumentDomainOntologies
        .setTypeEquals(new ArrayType(BaseType.STRING, 1));
        argumentDomainOntologies.setVisibility(Settable.NONE);

        Token[] argNamesArray = new Token[] { new StringToken("arg") };
        argumentNames.setToken(new ArrayToken(argNamesArray));
        argumentNames.setVisibility(Settable.NOT_EDITABLE);

        outputRangeOntologyName.setVisibility(Settable.NONE);

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-50\" y=\"-20\" width=\"40\" height=\"20\" "
                + "style=\"fill:white\"/>" + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:black\">"
                + "f(c0)</text></svg>");
    }

    ///////////////////////////////////////////////////////////////////
    ////                  ports and parameters                     ////

    /** The name of the ontology that specifies the domain and range of concepts
     *  for the defined binary operation concept function.
     */
    public StringParameter functionOntologyName;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the attributeChanged method so that the output range and all
     *  argument domain ontology names are set to the functionOntologyName.
     *  @param attribute The attribute that has been changed.
     *  @exception IllegalActionException If there is a problem changing the attribute.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == functionOntologyName) {
            StringToken ontologyNameToken = (StringToken) functionOntologyName
                    .getToken();
            outputRangeOntologyName.setToken(ontologyNameToken);

            ArrayToken domainOntologiesToken = new ArrayToken(
                    new Token[] { ontologyNameToken });
            argumentDomainOntologies.setToken(domainOntologiesToken);
        }

        super.attributeChanged(attribute);
    }
}
