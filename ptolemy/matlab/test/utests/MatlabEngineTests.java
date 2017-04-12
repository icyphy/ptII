package ptolemy.matlab.test.utests;

import java.util.HashMap;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import ptolemy.data.ArrayToken;
import ptolemy.data.ComplexMatrixToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntMatrixToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.math.Complex;
import ptolemy.matlab.impl.engine.MatlabEngine;
import ptolemy.matlab.impl.engine.MatlabEngine.MatlabEngineInstance;

/**
 * @author david
 *
 */
public abstract class MatlabEngineTests {

	protected static final String ANS = "ans";
	
	protected  MatlabEngine testedEngine;


	/**
	 * Test method for {@link ptolemy.matlab.impl.backends.jmathlib.JMLEngine#putMatlabObject(ptolemy.matlab.impl.engine.MatlabEngine.MatlabEngineInstance, java.lang.String, ptolemy.matlab.impl.adapters.MatlabObject)}.
	 * @throws IllegalActionException 
	 */
	@Test
	public void testGetAndPutMatlabObject_throwsExceptionForUnnamedObject() throws IllegalActionException {
		
		// With

		final MatlabEngineInstance engInstance = testedEngine.open();
		final String objectValue = "nothing matters anymore";
		
		// When
		try {
			testedEngine.put(engInstance, "", new StringToken(objectValue));			
		} catch (final IllegalActionException e) {
			// Then
			Assert.assertTrue(true);
			return;
		} finally {
			// Test disposal.
			testedEngine.close(engInstance);
		}
		
		Assert.fail("expected exception for variable's empty name");
		
	}

	/**
	 * Test method for {@link ptolemy.matlab.impl.backends.jmathlib.JMLEngine#putMatlabObject(ptolemy.matlab.impl.engine.MatlabEngine.MatlabEngineInstance, java.lang.String, ptolemy.matlab.impl.adapters.MatlabObject)}.
	 * @throws IllegalActionException 
	 */
	@Test
	public void testGetAndPutMatlabObject_putSingleStringGoesOK() throws IllegalActionException {
		
		// With
		final MatlabEngineInstance engInstance = testedEngine.open();
		final String objectName = "spiderman_identity";
		final String objectString = "Peter Paker";
		final StringToken objectValue = new StringToken(objectString);
	
		// When
		testedEngine.put(engInstance, objectName, objectValue);
		testedEngine.evalString(engInstance, objectName);
		final Token ans = testedEngine.get(engInstance, ANS);
		
		// Then...
		Assert.assertTrue(ans instanceof StringToken);
		Assert.assertEquals(objectValue,ans);
		
		// Test disposal.
		testedEngine.close(engInstance);
		
	}

	/**
	 * Test method for {@link ptolemy.matlab.impl.backends.jmathlib.JMLEngine#putMatlabObject(ptolemy.matlab.impl.engine.MatlabEngine.MatlabEngineInstance, java.lang.String, ptolemy.matlab.impl.adapters.MatlabObject)}.
	 * @throws IllegalActionException 
	 */
	@Test
	public void testGetAndPutMatlabObject_putArrayOfStringsGoesOK() throws IllegalActionException {
		
		// With
		final MatlabEngineInstance engInstance = testedEngine.open();
		final String objectName = "girlfriends";
		final StringToken[] objectElements = { 
				new StringToken("Alice"), 
				new StringToken("Lisa"), 
				new StringToken("Sally")
		};
		final ArrayToken objectValue = new ArrayToken(objectElements);
		
		// When
		testedEngine.put(engInstance, objectName, objectValue);
		testedEngine.evalString(engInstance, objectName);
		final Token ans = testedEngine.get(engInstance, ANS);
		
		// Then...
		Assert.assertTrue(ans instanceof ArrayToken);
		Assert.assertEquals(objectValue, ans);
		
		// Test disposal.
		testedEngine.close(engInstance);
	
	}

	/** Test method for {@link ptolemy.matlab.impl.backends.jmathlib.JMLEngine#putMatlabObject(ptolemy.matlab.impl.engine.MatlabEngine.MatlabEngineInstance, java.lang.String, ptolemy.matlab.impl.adapters.MatlabObject)}.
	 * @throws IllegalActionException 
	 */
	@Test
	public void testGetAndPutMatlabObject_putArrayOfNonStringsGoesOK() throws IllegalActionException {
		
		// With
		final MatlabEngineInstance engInstance = testedEngine.open();
		final String objectName = "alice_fact";
		final String ageString = "23";
		final Token[] objectValue = { 
				new StringToken("Alice"), 
				new StringToken("is"), 
				new IntToken(Integer.valueOf(ageString))
		};
		
		// When
		testedEngine.put(engInstance, objectName, new ArrayToken(objectValue));
		testedEngine.evalString(engInstance, objectName);
		final Token ans = testedEngine.get(engInstance, ANS);
		
		// Then...
		// ... check that the variable is "truly inside" the Matlab instance,
		Assert.assertTrue(ans instanceof ArrayToken);
		// Non-string elements are converted by PT into strings, hence this expectation.
		final StringToken actualThirdElement = (StringToken) ((ArrayToken) ans).getElement(2);
		Assert.assertEquals(ageString, actualThirdElement.stringValue());
		
		// Test disposal.
		testedEngine.close(engInstance);
		
	}

	/** Test method for {@link ptolemy.matlab.impl.backends.jmathlib.JMLEngine#putMatlabObject(ptolemy.matlab.impl.engine.MatlabEngine.MatlabEngineInstance, java.lang.String, ptolemy.matlab.impl.adapters.MatlabObject)}.
	 * @throws IllegalActionException 
	 */
	@Test
	public void testGetAndPutMatlabObject_putDoubleGoesOK() throws IllegalActionException {
		
		// With
		final MatlabEngineInstance engInstance = testedEngine.open();
		final String objectName = "four";
		final double objectValue = 4.0;
		
		// When
		testedEngine.put(engInstance, objectName, new DoubleToken(objectValue));
		testedEngine.evalString(engInstance, objectName);
		final Token ans = testedEngine.get(engInstance, ANS);
		
		// Then...
		// ... check that the variable is "truly inside" the Matlab instance,
		Assert.assertTrue(ans instanceof DoubleToken);
		Assert.assertTrue(objectValue == ((DoubleToken) ans).doubleValue());
		
		// Test disposal.
		testedEngine.close(engInstance);
		
	}

	/** Test method for {@link ptolemy.matlab.impl.backends.jmathlib.JMLEngine#putMatlabObject(ptolemy.matlab.impl.engine.MatlabEngine.MatlabEngineInstance, java.lang.String, ptolemy.matlab.impl.adapters.MatlabObject)}.
	 * @throws IllegalActionException 
	 */
	@Test
	public void testGetAndPutMatlabObject_putDoubleAndGetIntGoesOK() throws IllegalActionException {
		
		// With
		final MatlabEngineInstance engInstance = testedEngine.open();
		final String objectName = "four";
		final double objectValue = 4.0;
		MatlabEngine.ConversionParameters forceInts = new MatlabEngine.ConversionParameters();
		forceInts.getIntMatrices = true;
		
		// When
		testedEngine.put(engInstance, objectName, new DoubleToken(objectValue));
		testedEngine.evalString(engInstance, objectName);
		final Token ans = testedEngine.get(engInstance, ANS,forceInts);
		
		// Then...
		// ... check that the variable is "truly inside" the Matlab instance,
		Assert.assertTrue(ans instanceof IntToken);
		Assert.assertTrue(Math.floor(objectValue) == ((IntToken) ans).intValue());
		
		// Test disposal.
		testedEngine.close(engInstance);
		
	}

	/** Test method for {@link ptolemy.matlab.impl.backends.jmathlib.JMLEngine#putMatlabObject(ptolemy.matlab.impl.engine.MatlabEngine.MatlabEngineInstance, java.lang.String, ptolemy.matlab.impl.adapters.MatlabObject)}.
	 * @throws IllegalActionException 
	 */
	@Test
	public void testGetAndPutMatlabObject_putIntAndGetIntGoesOK() throws IllegalActionException {
		
		// With
		final MatlabEngineInstance engInstance = testedEngine.open();
		final String objectName = "four";
		final int objectValue = 4;
		MatlabEngine.ConversionParameters forceInts = new MatlabEngine.ConversionParameters();
		forceInts.getIntMatrices = true;
		
		// When
		testedEngine.put(engInstance, objectName, new IntToken(objectValue));
		testedEngine.evalString(engInstance, objectName);
		final Token ans = testedEngine.get(engInstance, ANS,forceInts);
		
		// Then...
		// ... check that the variable is "truly inside" the Matlab instance,
		Assert.assertTrue(ans instanceof IntToken);
		Assert.assertTrue(Math.floor(objectValue) == ((IntToken) ans).intValue());
		
		// Test disposal.
		testedEngine.close(engInstance);
		
	}

	/** Test method for {@link ptolemy.matlab.impl.backends.jmathlib.JMLEngine#putMatlabObject(ptolemy.matlab.impl.engine.MatlabEngine.MatlabEngineInstance, java.lang.String, ptolemy.matlab.impl.adapters.MatlabObject)}.
	 * @throws IllegalActionException 
	 */
	@Test
	public void testGetAndPutMatlabObject_putIntAndGetDoubleGoesOK() throws IllegalActionException {
		
		// With
		final MatlabEngineInstance engInstance = testedEngine.open();
		final String objectName = "four";
		final int objectValue = 4;
		MatlabEngine.ConversionParameters forceInts = new MatlabEngine.ConversionParameters();
		forceInts.getIntMatrices = false;
		
		// When
		testedEngine.put(engInstance, objectName, new IntToken(objectValue));
		testedEngine.evalString(engInstance, objectName);
		final Token ans = testedEngine.get(engInstance, ANS,forceInts);
		
		// Then...
		// ... check that the variable is "truly inside" the Matlab instance,
		Assert.assertTrue(ans instanceof DoubleToken);
		Assert.assertTrue(objectValue == ((DoubleToken) ans).doubleValue());
		
		// Test disposal.
		testedEngine.close(engInstance);
		
	}

	/** Test method for {@link ptolemy.matlab.impl.backends.jmathlib.JMLEngine#putMatlabObject(ptolemy.matlab.impl.engine.MatlabEngine.MatlabEngineInstance, java.lang.String, ptolemy.matlab.impl.adapters.MatlabObject)}.
	 * @throws IllegalActionException 
	 */
	@Test
	public void testGetAndPutMatlabObject_putDoubleMatrixGoesOK() throws IllegalActionException {
		
		// With
		final MatlabEngineInstance engInstance = testedEngine.open();
		final String objectName = "some_matrix";
		final double[][] objectValue = new double[][] { { 5,6} };
		final DoubleMatrixToken doubleMatrixToken = new DoubleMatrixToken(objectValue);
		MatlabEngine.ConversionParameters dontForceInts = new MatlabEngine.ConversionParameters();
		dontForceInts.getIntMatrices = false;
		
		// When
		testedEngine.put(engInstance, objectName, doubleMatrixToken);
		testedEngine.evalString(engInstance, objectName);
		final Token ans = testedEngine.get(engInstance, ANS,dontForceInts);
		
		// Then...
		// ... check that the variable is "truly inside" the Matlab instance,
		Assert.assertTrue(ans instanceof DoubleMatrixToken);
		Assert.assertEquals(doubleMatrixToken, ans);
		
		// Test disposal.
		testedEngine.close(engInstance);
		
	}

	/** Test method for {@link ptolemy.matlab.impl.backends.jmathlib.JMLEngine#putMatlabObject(ptolemy.matlab.impl.engine.MatlabEngine.MatlabEngineInstance, java.lang.String, ptolemy.matlab.impl.adapters.MatlabObject)}.
	 * @throws IllegalActionException 
	 */
	@Test
	public void testGetAndPutMatlabObject_putDoubleMatrixAndGetIntMatrixGoesOK() throws IllegalActionException {
		
		// With
		final MatlabEngineInstance engInstance = testedEngine.open();
		final String objectName = "some_matrix";
		final double[][] objectValue = new double[][] { { 5.0,6.0} };
		final DoubleMatrixToken doubleMatrixToken = new DoubleMatrixToken(objectValue);
		MatlabEngine.ConversionParameters dontForceInts = new MatlabEngine.ConversionParameters();
		dontForceInts.getIntMatrices = true;
		
		// When
		testedEngine.put(engInstance, objectName, doubleMatrixToken);
		testedEngine.evalString(engInstance, objectName);
		final Token ans = testedEngine.get(engInstance, ANS,dontForceInts);
		
		// Then...
		// ... check that the variable is "truly inside" the Matlab instance,
		Assert.assertTrue(ans instanceof IntMatrixToken);
		final IntMatrixToken intMatrix = (IntMatrixToken) ans;
		Assert.assertEquals(Math.round(objectValue[0][0]), intMatrix.getElementAt(0, 0));
		Assert.assertEquals(Math.round(objectValue[0][1]), intMatrix.getElementAt(0, 1));
		
		// Test disposal.
		testedEngine.close(engInstance);
		
	}

	/** Test method for {@link ptolemy.matlab.impl.backends.jmathlib.JMLEngine#putMatlabObject(ptolemy.matlab.impl.engine.MatlabEngine.MatlabEngineInstance, java.lang.String, ptolemy.matlab.impl.adapters.MatlabObject)}.
	 * @throws IllegalActionException 
	 */
	@Test
	public void testGetAndPutMatlabObject_putComplexMatrixGoesOK() throws IllegalActionException {
		
		// With
		final MatlabEngineInstance engInstance = testedEngine.open();
		final String objectName = "some_matrix";
		final Complex firstComplex = new Complex(5.0,1.0);
		final Complex secondComplex = new Complex(6.0, 2.0);
		final Complex[][] complexMatrix = new Complex[][] { { firstComplex, secondComplex } };
		final ComplexMatrixToken expectedObjectValue = new ComplexMatrixToken(complexMatrix);
		
		// When
		testedEngine.put(engInstance, objectName, expectedObjectValue);
		testedEngine.evalString(engInstance, objectName);
		final Token ans = testedEngine.get(engInstance, ANS);
		
		// Then...
		Assert.assertTrue(ans instanceof ComplexMatrixToken);
		final ComplexMatrixToken actualObjectValue = (ComplexMatrixToken) ans;
		Assert.assertEquals(firstComplex, actualObjectValue.getElementAt(0, 0));
		Assert.assertEquals(secondComplex, actualObjectValue.getElementAt(0, 1));
		
		// Test disposal.
		testedEngine.close(engInstance);
		
	}

	/**
	 * Test method for {@link ptolemy.matlab.impl.backends.jmathlib.JMLEngine#putMatlabObject(ptolemy.matlab.impl.engine.MatlabEngine.MatlabEngineInstance, java.lang.String, ptolemy.matlab.impl.adapters.MatlabObject)}.
	 * @throws IllegalActionException 
	 */
	@Test
	public void testGetAndPutMatlabObject_putCellArrayOfDoublesGoesOK() throws IllegalActionException {
		
		// With
		final MatlabEngineInstance engInstance = testedEngine.open();
		final String objectName = "generations";
		final DoubleToken[] objectElements = { 
				new DoubleToken(1.0), 
				new DoubleToken(2.0), 
				new DoubleToken(3.0)
		};
		final ArrayToken objectValue = new ArrayToken(objectElements);
		
		// When
		testedEngine.put(engInstance, objectName, objectValue);
		testedEngine.evalString(engInstance, objectName);
		final Token ans = testedEngine.get(engInstance, ANS);
		
		// Then...
		Assert.assertTrue(ans instanceof ArrayToken);
		Assert.assertEquals(objectValue, ans);
		
		// Test disposal.
		testedEngine.close(engInstance);
	
	}

	/**
	 * Test method for {@link ptolemy.matlab.impl.backends.jmathlib.JMLEngine#putMatlabObject(ptolemy.matlab.impl.engine.MatlabEngine.MatlabEngineInstance, java.lang.String, ptolemy.matlab.impl.adapters.MatlabObject)}.
	 * @throws IllegalActionException 
	 */
	@SuppressWarnings("serial")
	@Test
	public void testGetAndPutMatlabObject_putRecordGoesOK() throws IllegalActionException {
		
		// With
		final MatlabEngineInstance engInstance = testedEngine.open();
		final RecordToken expectedObject = new RecordToken(new HashMap<String,Token>(){{
			put("a_field",new StringToken("a_value"));
			put("b_field",new StringToken("b_value"));
			put("c_field",new StringToken("c_value"));
		}});
		final String objectName = "some_record";
		
		// When
		testedEngine.put(engInstance, objectName, expectedObject);
		testedEngine.evalString(engInstance, objectName);
		final Token ans = testedEngine.get(engInstance, ANS);
		
		// Then...
		Assert.assertTrue(ans instanceof RecordToken);
		Assert.assertEquals(expectedObject, ans);
		
		// Test disposal.
		testedEngine.close(engInstance);
	
	}

	/**
	 * Test method for {@link ptolemy.matlab.impl.backends.jmathlib.JMLEngine#open(java.lang.String, boolean)}.
	 * @throws IllegalActionException 
	 */
	@Test
	public void testOpen_afterwardsEvalAndCloseGoOK() throws IllegalActionException {

		// When
		final MatlabEngineInstance engInstance = testedEngine.open();
	
		// Then...
		testedEngine.evalString(engInstance, "2 + 2");
		
		// Test disposal.
		testedEngine.close(engInstance);
		
	}

	/**
	 * Test method for {@link ptolemy.matlab.impl.backends.jmathlib.JMLEngine#close(ptolemy.matlab.impl.engine.MatlabEngine.MatlabEngineInstance)}.
	 * @throws IllegalActionException 
	 */
	@Test
	public void testClose_goesOKandMakesInstanceInvalid() throws IllegalActionException {
		
		// When
		final MatlabEngineInstance engInstance = testedEngine.open();
		testedEngine.close(engInstance);
	
		// Then...
		try {
			testedEngine.evalString(engInstance, "2 + 2");
			Assert.fail("exception expected, but missing");
		} catch (IllegalActionException e) {
			Assert.assertTrue(true);
		}
	}

	/**
	 * Test method for {@link ptolemy.matlab.impl.backends.jmathlib.JMLEngine#close(ptolemy.matlab.impl.engine.MatlabEngine.MatlabEngineInstance)}.
	 * @throws IllegalActionException 
	 */
	@Test
	public void testClose_onInvalidInstanceThrowsException() throws IllegalActionException {
		// With
		final MatlabEngineInstance engInstance = new MatlabEngineInstance() { /*invalid instance*/};
		engInstance.id = UUID.randomUUID().toString();
		
		try {
			// When
			testedEngine.close(engInstance);
			// Then...
			Assert.fail("exception expected, but missing");
		} catch (IllegalActionException e) {
			// Then...
			Assert.assertTrue(true);
		}
	}

	/**
	 * Test method for {@link ptolemy.matlab.impl.backends.jmathlib.JMLEngine#evalString(ptolemy.matlab.impl.engine.MatlabEngine.MatlabEngineInstance, java.lang.String)}.
	 * @throws IllegalActionException 
	 */
	@Test
	public void testEvalString_aSimpleExpressionWithoutVariablesGoesOK() throws IllegalActionException {
		
		// With
		final MatlabEngineInstance engInstance = testedEngine.open();
		
		// When
		testedEngine.evalString(engInstance, "2 + 2");
	
		// Then
		Assert.assertTrue(true); // no exception means OK.
		
		// Test disposal.
		testedEngine.close(engInstance);
		
	}

	/**
	 * Test method for {@link ptolemy.matlab.impl.backends.jmathlib.JMLEngine#evalString(ptolemy.matlab.impl.engine.MatlabEngine.MatlabEngineInstance, java.lang.String)}.
	 * @throws IllegalActionException 
	 */
	@Test
	public void testEvalString_aSimpleExpressionWithVariablesGoesOK() throws IllegalActionException {
		
		// With
		final MatlabEngineInstance engInstance = testedEngine.open();
		final String resultName = "sum";
		testedEngine.put(engInstance, resultName, new IntToken(0));
		
		// When
		testedEngine.evalString(engInstance, resultName + " = 2 + 2");
	
		// Then
		MatlabEngine.ConversionParameters forceInts = new MatlabEngine.ConversionParameters();
		forceInts.getIntMatrices = true;
		forceInts.getScalarMatrices = true;
		Token actualResultValue = testedEngine.get(engInstance, resultName,forceInts);
		Assert.assertEquals(4, ((IntToken)actualResultValue).intValue());
		
		// Test disposal.
		testedEngine.close(engInstance);
		
	}

	/**
	 * Test method for {@link ptolemy.matlab.impl.backends.jmathlib.JMLEngine#evalString(ptolemy.matlab.impl.engine.MatlabEngine.MatlabEngineInstance, java.lang.String)}.
	 * @throws IllegalActionException 
	 */
	@Test
	public void testEvalString_invalidExpressionThrowsException() throws IllegalActionException {
		
		// With
		final MatlabEngineInstance engInstance = testedEngine.open();
		
		// When
		try {
			testedEngine.evalString(engInstance, "is this... something special, maybe...?");
			// Then
			Assert.fail("expected exception, but see none");
		} catch (final IllegalActionException e) {
			// Then
			Assert.assertTrue(true);
		} finally {
			// Test disposal.
			testedEngine.close(engInstance);			
		}
		
	}

	/**
	 * Test method for {@link ptolemy.matlab.impl.backends.jmathlib.JMLEngine#getOutput(ptolemy.matlab.impl.engine.MatlabEngine.MatlabEngineInstance)}.
	 * @throws IllegalActionException 
	 */
	@Test
	public void testGetOutput() throws IllegalActionException {
		
		// With
		final MatlabEngineInstance engInstance = testedEngine.open();
		final String expectedChunkInOutput = "expected_variable_name_23";
		testedEngine.put(engInstance, expectedChunkInOutput, new IntToken(23));
		
		// When
		testedEngine.evalString(engInstance, "who");
	
		// Then
		final StringToken outputString = testedEngine.getOutput(engInstance);
		Assert.assertTrue(outputString.stringValue().contains(expectedChunkInOutput));
		
		// Test disposal.
		testedEngine.close(engInstance);
		
	}

}