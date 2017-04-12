package ptolemy.matlab.impl.backends.jmathlib.adapters;

import ptolemy.data.ArrayToken;
import ptolemy.data.ComplexMatrixToken;
import ptolemy.data.ComplexToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.MatrixToken;
import ptolemy.data.RecordToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.matlab.impl.backends.jmathlib.adapters.jml2pt.Jml2PtCellArrayAdapter;
import ptolemy.matlab.impl.backends.jmathlib.adapters.jml2pt.Jml2PtComplexAdapter;
import ptolemy.matlab.impl.backends.jmathlib.adapters.jml2pt.Jml2PtComplexMatrixAdapter;
import ptolemy.matlab.impl.backends.jmathlib.adapters.jml2pt.Jml2PtScalarAdapter;
import ptolemy.matlab.impl.backends.jmathlib.adapters.jml2pt.Jml2PtMathLibObjectAdapter;
import ptolemy.matlab.impl.backends.jmathlib.adapters.jml2pt.Jml2PtScalarMatrixAdapter;
import ptolemy.matlab.impl.backends.jmathlib.adapters.jml2pt.Jml2PtStringAdapter;
import ptolemy.matlab.impl.backends.jmathlib.adapters.jml2pt.Jml2PtStringMatrixAdapter;
import ptolemy.matlab.impl.backends.jmathlib.adapters.pt2jml.Pt2JmlArray2CellArrayAdapter;
import ptolemy.matlab.impl.backends.jmathlib.adapters.pt2jml.Pt2JmlComplexMatrixAdapter;
import ptolemy.matlab.impl.backends.jmathlib.adapters.pt2jml.Pt2JmlComplexAdapter;
import ptolemy.matlab.impl.backends.jmathlib.adapters.pt2jml.Pt2JmlDoubleMatrixAdapter;
import ptolemy.matlab.impl.backends.jmathlib.adapters.pt2jml.Pt2JmlStructAdapter;
import ptolemy.matlab.impl.backends.jmathlib.adapters.pt2jml.Pt2JmlScalarAdapter;
import ptolemy.matlab.impl.backends.jmathlib.adapters.pt2jml.Pt2JmlStringAdapter;
import ptolemy.matlab.impl.backends.jmathlib.adapters.pt2jml.Pt2JmlStringArrayAdapter;
import ptolemy.matlab.impl.engine.adaption.ObjectAdapterFactory;
import ptolemy.matlab.impl.engine.adaption.ToMatlabObjectAdapter;
import ptolemy.matlab.impl.engine.adaption.ToPtObjectAdapter;

/**
 * @author David Guardado Barcia
 *
 */
public class JMLObjectAdapterFactory implements ObjectAdapterFactory {

	private ToPtObjectAdapter<Token> fallbackDelegate2Pt;
	private ToMatlabObjectAdapter<Token> fallbackDelegateToMatlab;

	@Override
	public void setRootAdapter2Pt(ToPtObjectAdapter<Token> adapter) {
		this.fallbackDelegate2Pt = adapter;
	}
	
	@Override
	public void setRootAdapter2Matlab(ToMatlabObjectAdapter<Token> adapter) {
		this.fallbackDelegateToMatlab = adapter;
	}
	
	@Override
	public ToPtObjectAdapter<Token> getCellArray2PtAdapter() {
		return new Jml2PtCellArrayAdapter(fallbackDelegate2Pt);
	}

	@Override
	public ToPtObjectAdapter<ArrayToken> getStringMatrix2PtAdapter() {
		return new Jml2PtStringMatrixAdapter();
	}

	@Override
	public ToPtObjectAdapter<StringToken> getString2PtAdapter() {
		return new Jml2PtStringAdapter();
	}

	@Override
	public ToPtObjectAdapter<Token> getStruct2PtAdapter() {
		return new Jml2PtMathLibObjectAdapter(fallbackDelegate2Pt);
	}

	@Override
	public ToPtObjectAdapter<ComplexMatrixToken> getComplexMatrix2PtAdapter() {
		return new Jml2PtComplexMatrixAdapter();
	}

	@Override
	public ToPtObjectAdapter<ComplexToken> getComplex2PtAdapter() {
		return new Jml2PtComplexAdapter();
	}

	@Override
	public ToPtObjectAdapter<ScalarToken> getScalar2PtAdapter() {
		return new Jml2PtScalarAdapter();
	}

	@Override
	public ToPtObjectAdapter<MatrixToken> getScalarMatrix2PtAdapter() {
		return new Jml2PtScalarMatrixAdapter();
	}

	@Override
	public ToMatlabObjectAdapter<ArrayToken> getPt2MlNxNMatrixAdapter() {
		return new Pt2JmlStringArrayAdapter(fallbackDelegateToMatlab);
	}

	@Override
	public ToMatlabObjectAdapter<StringToken> getPt2MlStringAdapter() {
		return new Pt2JmlStringAdapter(fallbackDelegateToMatlab);
	}

	@Override
	public ToMatlabObjectAdapter<RecordToken> getPt2MlStructAdapter() {
		return new Pt2JmlStructAdapter(fallbackDelegateToMatlab);
	}

	@Override
	public ToMatlabObjectAdapter<ComplexMatrixToken> getPt2MlComplexMatrixAdapter() {
		return new Pt2JmlComplexMatrixAdapter(fallbackDelegateToMatlab);
	}

	@Override
	public ToMatlabObjectAdapter<ComplexToken> getPt2MlComplexAdapter() {
		return new Pt2JmlComplexAdapter(fallbackDelegateToMatlab);
	}

	@Override
	public ToMatlabObjectAdapter<DoubleMatrixToken> getPt2MlDoubleMatrixAdapter() {
		return new Pt2JmlDoubleMatrixAdapter(fallbackDelegateToMatlab);
	}

	@Override
	public ToMatlabObjectAdapter<ScalarToken> getPt2MlDoubleAdapter() {
		return new Pt2JmlScalarAdapter(fallbackDelegateToMatlab);
	}

	@Override
	public ToMatlabObjectAdapter<ArrayToken> getPt2MlCellMatrixAdapter() {
		return new Pt2JmlArray2CellArrayAdapter(fallbackDelegateToMatlab);
	}

}
