package ptolemy.apps.actorsTutorial09;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.StringToken;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

public class MyPtolemnizer extends TypedAtomicActor {

    public MyPtolemnizer() {
        // TODO Auto-generated constructor stub
    }

    public MyPtolemnizer(Workspace workspace) {
        super(workspace);
        // TODO Auto-generated constructor stub
    }

    public MyPtolemnizer(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        // TODO Auto-generated constructor stub

        input = new TypedIOPort(this, "input");
        output = new TypedIOPort(this, "output");
        input.setInput(true);
        output.setOutput(true);
        input.setDisplayName("stringInput");

        expression = new StringParameter(this, "expression");
        expression.setExpression("t");

        output.setTypeEquals(BaseType.STRING);
        input.setTypeEquals(BaseType.STRING);

    }

    @Override
    public void fire() throws IllegalActionException {
        StringToken inString = (StringToken) input.get(0);
        String regExp = expression.stringValue();
        StringToken outToken = new StringToken(
                inString.stringValue().replaceAll(regExp, "pt"));
        output.send(0, outToken);
    }

    public TypedIOPort input;
    public TypedIOPort output;
    public StringParameter expression;
}
