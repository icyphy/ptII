package ptolemy.domains.fsm.kernel.fmv;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

public class FmvState extends State {
    public FmvState(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        isRiskAnalysisState = new Parameter(this, "isRiskAnalysisState");
        isRiskAnalysisState.setTypeEquals(BaseType.BOOLEAN);
        isRiskAnalysisState.setExpression("false");
        isSafetyAnalysisState = new Parameter(this, "isSafetyAnalysisState");
        isSafetyAnalysisState.setTypeEquals(BaseType.BOOLEAN);
        isSafetyAnalysisState.setExpression("false");

    }

    /** 
     */
    public Parameter isRiskAnalysisState;

    /** 
     */
    public Parameter isSafetyAnalysisState;

    /**
     * React to a change in an attribute. If the changed attribute is the
     * <i>refinementName</i> attribute, record the change but do not check
     * whether there is a TypedActor with the specified name and having the same
     * container as the FSMActor containing this state.
     * 
     * @param attribute
     *                The attribute that changed.
     * @exception IllegalActionException
     *                    If thrown by the superclass attributeChanged() method.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        super.attributeChanged(attribute);

        if (attribute == isRiskAnalysisState) {
            NamedObj container = getContainer();
            // Container might not be an FSMActor if, for example,
            // the state is in a library.
            if (container instanceof FSMActor) {
                if (((BooleanToken) isRiskAnalysisState.getToken())
                        .booleanValue()) {
                    if (((BooleanToken) isSafetyAnalysisState.getToken())
                            .booleanValue()) {
                        _attachText(
                                "_iconDescription",
                                "<svg>\n"
                                        + "<circle cx=\"0\" cy=\"0\" r=\"25\" style=\"fill:green\"/>\n"
                                        + "<circle cx=\"0\" cy=\"0\" r=\"20\" style=\"fill:red\"/>\n"
                                        + "</svg>\n");
                    } else {
                        _attachText(
                                "_iconDescription",
                                "<svg>\n"
                                        + "<circle cx=\"0\" cy=\"0\" r=\"20\" style=\"fill:red\"/>\n"
                                        + "</svg>\n");
                    }
                }  else{
                    if (((BooleanToken) isSafetyAnalysisState.getToken())
                            .booleanValue()) {
                        _attachText(
                                "_iconDescription",
                                "<svg>\n"
                                        + "<circle cx=\"0\" cy=\"0\" r=\"20\" style=\"fill:green\"/>\n"
                                        + "</svg>\n");
                    } else {
                        _attachText(
                                "_iconDescription",
                                "<svg>\n"
                                        + "<circle cx=\"0\" cy=\"0\" r=\"20\" style=\"fill:white\"/>\n"
                                        + "</svg>\n");
                    }
                }
            }
        } else if (attribute == isSafetyAnalysisState) {
            NamedObj container = getContainer();
            // Container might not be an FSMActor if, for example,
            // the state is in a library.
            if (container instanceof FSMActor) {
                if (((BooleanToken) isSafetyAnalysisState.getToken())
                        .booleanValue()) {
                    if (((BooleanToken) isRiskAnalysisState.getToken())
                            .booleanValue()) {
                        _attachText(
                                "_iconDescription",
                                "<svg>\n"
                                        + "<circle cx=\"0\" cy=\"0\" r=\"25\" style=\"fill:green\"/>\n"
                                        + "<circle cx=\"0\" cy=\"0\" r=\"20\" style=\"fill:red\"/>\n"
                                        + "</svg>\n");
                    } else {
                        _attachText(
                                "_iconDescription",
                                "<svg>\n"
                                        + "<circle cx=\"0\" cy=\"0\" r=\"20\" style=\"fill:green\"/>\n"
                                        + "</svg>\n");
                    }

                } else{
                    if (((BooleanToken) isRiskAnalysisState.getToken())
                            .booleanValue()) {
                        _attachText(
                                "_iconDescription",
                                "<svg>\n"
                                        
                                        + "<circle cx=\"0\" cy=\"0\" r=\"20\" style=\"fill:red\"/>\n"
                                        + "</svg>\n");
                    } else {
                        _attachText(
                                "_iconDescription",
                                "<svg>\n"
                                        + "<circle cx=\"0\" cy=\"0\" r=\"20\" style=\"fill:white\"/>\n"
                                        + "</svg>\n");
                    }
                }
            }
        }
    }

}
