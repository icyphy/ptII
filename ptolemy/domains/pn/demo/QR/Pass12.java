package ptolemy.domains.pn.demo.QR;

import java.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import ptolemy.kernel.util.*;

import ptolemy.data.type.BaseType;

public class Pass12 extends TypedAtomicActor {

    public TypedIOPort in0;
    public TypedIOPort out0;
    public TypedIOPort out1;

    public Pass12(TypedCompositeActor aContainer, String aName)
	throws IllegalActionException, NameDuplicationException   
    {
	super(aContainer, aName);
	in0 = new TypedIOPort(this,"in0",true,false);

	out0 = new TypedIOPort(this,"out0",false,true);
	out1 = new TypedIOPort(this,"out1",false,true);

        in0.setTypeEquals(BaseType.DOUBLE);

        out0.setTypeEquals(BaseType.DOUBLE);
        out1.setTypeEquals(BaseType.DOUBLE);

 	System.out.println(" -- PASS12 Created -- ");
    }
 
    public void fire() throws IllegalActionException {	
	System.out.println(" -- DUMMY Firing Pass12 -- ");

	Token _at = in0.get(0);
	out0.broadcast( _at );  

	double x = ((DoubleToken) _at).doubleValue();
	out1.broadcast( new DoubleToken( x ));    

    }

    public boolean prefire() {	
	System.out.println(" -- DUMMY PRE Firing Pass12 -- ");
	return true;
    }

    // The State Update
    public boolean postfire() {	
	System.out.println(" -- DUMMY POST Firing Pass12 -- ");
	return true;
    }
    
}

