package ptolemy.domains.metroII.kernel;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Builder;

public class OverridingMappingContraintSolver extends MappingConstraintSolver {

    public OverridingMappingContraintSolver() {
        super(); 
    }

    @Override
    public void resolve(Iterable<Builder> metroIIEventList) {
        // TODO Auto-generated method stub

    }
    
    @Override
    public void readMapping(String filename) throws IOException {
        
    }
}
