package ptolemy.domains.pn.demo.QR;

import java.lang.*;
import java.util.*;

public class ArrayIndex extends TreeMap {

    SourceMatrix  x_0;

    //    private Map _map = Collections.synchronizedMap(new HashMap());
    private Map _map = new HashMap();

    public ArrayIndex() {
	super();
    }

    public String atKey(int i_1 ) {
	String key =  "(" + i_1 + ")";
	return key;
    }

    public String atKey(int i_1, int i_2 ) {
	String key =  "(" + i_1 + "," + i_2 + ")";
	return key;
    }

    public String atKey(int i_1, int i_2, int i_3 ) {
	String key =  "(" + i_1 + "," + i_2 + "," + i_3 +  ")";
	return key;
    }

    public String atKey(int i_1, int i_2, int i_3, int i_4 ) {
	String key =  "(" + i_1 + "," + i_2 + "," + i_3 + "," + i_4
	    +")";
	return key;
    }

    public String atKey(int i_1, int i_2, int i_3, int i_4, int i_5 ) {
	String key =  "(" + i_1 + "," + i_2 + "," + i_3 + "," + i_4
	    + "," + i_5 + ")";
	return key;
    }

    public String atKey(int i_1, int i_2, int i_3, int i_4, int i_5,
			int i_6 ) {
	String key =  "(" + i_1 + "," + i_2 + "," + i_3 + "," + i_4
	    + "," + i_5 + "," + i_6  + ")";
	return key;
    }

    public double retrieve(String aKey ) {
	Double value = (Double) _map.get( aKey );
	if ( value == null ) {
	    throw new Error(" --- NULL Value retrieved for key " + aKey );
	}
	return value.doubleValue();
    }

    public void store(double aValue, String aKey) {
	_map.put( aKey, new Double(aValue) );
    }

    public void ReadMatrix(String filename ) {
	// System.out.println(" --- ReadMatrix: " + filename);

	if ( filename == "U_1000x16" ) {

	    // System.out.println(" ---- CREATE U_1000x16 ----- ");
	    for (int i=0;i<500;i++) {
		for (int j=0;j<16;j++) {
		    String key = atKey(i+1,j+1);
		    // System.out.println("\n Key: " + key );
		    _map.put(key, new Double( x_0.sourcematrix_0[i][j] ));
		}
	    }
	} else {
	    // System.out.println(" ---- CREATE Zeros64x64 ----- ");
	    for (int i=0;i<64;i++) {
		for (int j=0;j<64;j++) {
		    String key = atKey(i,j);
		    // System.out.println("\n Key: " + key );
		    _map.put(key, new Double(0.0 ));
		}
	    }

	}
    }

    public void WriteMatrix(String filename ) {
	// System.out.println(" --- WriteMatrix: " + filename);
    }
}
