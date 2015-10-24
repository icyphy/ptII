package ptolemy.domains.atc.lib;


import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

////////////////////////////////////////////////////////////////

public class StormHandling extends TypedAtomicActor {
        public StormHandling(CompositeEntity container, String name)
                        throws NameDuplicationException,
                        IllegalActionException {
                super(container, name);
                selectedTrack = new TypedIOPort(this, "selectedTrack", true, false);
                selectedTrack.setTypeEquals(BaseType.INT);
                selectedValue = new TypedIOPort(this, "selectedValue", true, false);
                selectedValue.setTypeEquals(BaseType.BOOLEAN);
                numberOfTracks = new Parameter(this, "numberOfTracks", new IntToken(0));
                numberOfTracks.setTypeEquals(BaseType.INT);
                trackStatus=new TypedIOPort(this,"trackStatus",false,true);
                trackStatus.setTypeEquals(new ArrayType(BaseType.BOOLEAN));
        }
         /** Ports and parameters. */
         public TypedIOPort selectedTrack, selectedValue,trackStatus;
         public Parameter numberOfTracks;
        
         /** Action methods. */
         public void initialize() throws IllegalActionException {
                 super.initialize();
                 _count = ((IntToken)numberOfTracks.getToken()).intValue();
                 _temp=new Token[_count];
                 for(int i=0;i<_count;i++)
                     _temp[i]=(Token) new BooleanToken(false);
         }
         public void fire() throws IllegalActionException {
                 super.fire();
                 int trackNumber=0;
                 Token value;
                 if (selectedValue.hasToken(0)) {
                     value=selectedValue.get(0);
                     if (selectedTrack.hasToken(0)) {
                         trackNumber=((IntToken)selectedTrack.get(0)).intValue();
                         _temp[trackNumber-1]=value;
                     }
                    
                 } 
                 trackStatus.send(0, (Token)(new ArrayToken(BaseType.BOOLEAN, _temp)));
                }
        public boolean postfire() throws IllegalActionException {
                return super.postfire();
        }
        private int _count = 0; /** Local variable. */
        private Token[] _temp;
         }
