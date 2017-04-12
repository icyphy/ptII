package ptolemy.matlab.impl.backends.mxapi.adapters;

import ptolemy.data.ArrayToken;
import ptolemy.data.ComplexMatrixToken;
import ptolemy.data.ComplexToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.MatrixToken;
import ptolemy.data.RecordToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.matlab.impl.backends.mxapi.PtMatlab;
import ptolemy.matlab.impl.backends.mxapi.adapters.mx2pt.Mx2PtCellArrayAdapter;
import ptolemy.matlab.impl.backends.mxapi.adapters.mx2pt.Mx2PtComplexAdapter;
import ptolemy.matlab.impl.backends.mxapi.adapters.mx2pt.Mx2PtComplexMatrixAdapter;
import ptolemy.matlab.impl.backends.mxapi.adapters.mx2pt.Mx2PtScalarAdapter;
import ptolemy.matlab.impl.backends.mxapi.adapters.mx2pt.Mx2PtScalarMatrixAdapter;
import ptolemy.matlab.impl.backends.mxapi.adapters.mx2pt.Mx2PtStringAdapter;
import ptolemy.matlab.impl.backends.mxapi.adapters.mx2pt.Mx2PtStringArrayAdapter;
import ptolemy.matlab.impl.backends.mxapi.adapters.mx2pt.Mx2PtStructArrayAdapter;
import ptolemy.matlab.impl.backends.mxapi.adapters.pt2mx.Pt2MxCellMatrixAdapter;
import ptolemy.matlab.impl.backends.mxapi.adapters.pt2mx.Pt2MxComplexAdapter;
import ptolemy.matlab.impl.backends.mxapi.adapters.pt2mx.Pt2MxComplexMatrixAdapter;
import ptolemy.matlab.impl.backends.mxapi.adapters.pt2mx.Pt2MxDoubleAdapter;
import ptolemy.matlab.impl.backends.mxapi.adapters.pt2mx.Pt2MxDoubleMatrixAdapter;
import ptolemy.matlab.impl.backends.mxapi.adapters.pt2mx.Pt2MxStringAdapter;
import ptolemy.matlab.impl.backends.mxapi.adapters.pt2mx.Pt2MxStringMatrixAdapter;
import ptolemy.matlab.impl.backends.mxapi.adapters.pt2mx.Pt2MxStructAdapter;
import ptolemy.matlab.impl.engine.adaption.ObjectAdapterFactory;
import ptolemy.matlab.impl.engine.adaption.ToMatlabObjectAdapter;
import ptolemy.matlab.impl.engine.adaption.ToPtObjectAdapter;

/**
 * @author David Guardado Barcia
 *
 */
public class MxObjectAdapterFactory implements ObjectAdapterFactory {

	private ToPtObjectAdapter<Token> fallbackDelegate;
	private ToMatlabObjectAdapter<Token> fallbackDelegateToMatlab;
	private PtMatlab ptMatlab;
	
	public MxObjectAdapterFactory(PtMatlab ptMatlab) {
		super();
		this.ptMatlab = ptMatlab;
	}

	@Override
	public void setRootAdapter2Pt(ToPtObjectAdapter<Token> adapter) {
		this.fallbackDelegate = adapter;
	}

	@Override
	public ToPtObjectAdapter<Token> getCellArray2PtAdapter() {
		return new Mx2PtCellArrayAdapter(ptMatlab,fallbackDelegate);
	}

	@Override
	public ToPtObjectAdapter<ArrayToken> getStringMatrix2PtAdapter() {
		return new Mx2PtStringArrayAdapter(ptMatlab);
	}

	@Override
	public ToPtObjectAdapter<StringToken> getString2PtAdapter() {
		return new Mx2PtStringAdapter(ptMatlab);
	}

	@Override
	public ToPtObjectAdapter<Token> getStruct2PtAdapter() {
		return new Mx2PtStructArrayAdapter(ptMatlab,fallbackDelegate);
	}

	@Override
	public ToPtObjectAdapter<ComplexMatrixToken> getComplexMatrix2PtAdapter() {
		return new Mx2PtComplexMatrixAdapter(ptMatlab);
	}

	@Override
	public ToPtObjectAdapter<ComplexToken> getComplex2PtAdapter() {
		return new Mx2PtComplexAdapter(ptMatlab);
	}

	@Override
	public ToPtObjectAdapter<ScalarToken> getScalar2PtAdapter() {
		return new Mx2PtScalarAdapter(ptMatlab);
	}

	@Override
	public ToPtObjectAdapter<MatrixToken> getScalarMatrix2PtAdapter() {
		return new Mx2PtScalarMatrixAdapter(ptMatlab);
	}

	@Override
	public void setRootAdapter2Matlab(ToMatlabObjectAdapter<Token> adapter) {
		fallbackDelegateToMatlab = adapter;
	}

	@Override
	public ToMatlabObjectAdapter<ArrayToken> getPt2MlNxNMatrixAdapter() {
		return new Pt2MxStringMatrixAdapter(fallbackDelegateToMatlab, ptMatlab);
	}

	@Override
	public ToMatlabObjectAdapter<StringToken> getPt2MlStringAdapter() {
		return new Pt2MxStringAdapter(fallbackDelegateToMatlab, ptMatlab);
	}

	@Override
	public ToMatlabObjectAdapter<RecordToken> getPt2MlStructAdapter() {
		return new Pt2MxStructAdapter(fallbackDelegateToMatlab, ptMatlab);
	}

	@Override
	public ToMatlabObjectAdapter<ComplexMatrixToken> getPt2MlComplexMatrixAdapter() {
		return new Pt2MxComplexMatrixAdapter(fallbackDelegateToMatlab, ptMatlab);
	}

	@Override
	public ToMatlabObjectAdapter<ComplexToken> getPt2MlComplexAdapter() {
		return  new Pt2MxComplexAdapter(fallbackDelegateToMatlab, ptMatlab);
	}

	@Override
	public ToMatlabObjectAdapter<DoubleMatrixToken> getPt2MlDoubleMatrixAdapter() {
		return new Pt2MxDoubleMatrixAdapter(fallbackDelegateToMatlab, ptMatlab);
	}

	@Override
	public ToMatlabObjectAdapter<ScalarToken> getPt2MlDoubleAdapter() {
		return new Pt2MxDoubleAdapter(fallbackDelegateToMatlab, ptMatlab);
	}

	@Override
	public ToMatlabObjectAdapter<ArrayToken> getPt2MlCellMatrixAdapter() {
		return new Pt2MxCellMatrixAdapter(fallbackDelegateToMatlab, ptMatlab);
	}

}
