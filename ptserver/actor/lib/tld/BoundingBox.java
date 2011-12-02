package ptserver.actor.lib.tld;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.MatrixToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class BoundingBox extends TypedAtomicActor {
	public TypedIOPort _input;

	public BoundingBox(CompositeEntity container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
		_input = new TypedIOPort(this, "input", true, false);
		_input.setTypeEquals(BaseType.GENERAL);
	}
	
	@Override
	public void fire() throws IllegalActionException {
		MatrixToken result = (MatrixToken)_input.get(0);
		CompositeEntity container = (CompositeEntity)this.getContainer();
		Video video = (Video)container.getEntity("Video");
		
		double[][] coords = result.doubleMatrix();
		assert(coords.length == 4);
		video.updateBoundingBox((float)coords[0][0],
								(float)coords[1][0],
								(float)coords[2][0],
								(float)coords[3][0]);
	}
}
