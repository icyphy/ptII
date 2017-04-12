package ptolemy.matlab.impl.engine.adaption;

import ptolemy.data.ArrayToken;
import ptolemy.data.ComplexMatrixToken;
import ptolemy.data.ComplexToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.MatrixToken;
import ptolemy.data.RecordToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;

/**
 * @author david
 *
 */
public interface ObjectAdapterFactory {
	
	void setRootAdapter2Pt(ToPtObjectAdapter<Token> adapter);
	void setRootAdapter2Matlab(ToMatlabObjectAdapter<Token> adapter);

	ToPtObjectAdapter<Token> getCellArray2PtAdapter();
	ToPtObjectAdapter<ArrayToken> getStringMatrix2PtAdapter();
	ToPtObjectAdapter<StringToken> getString2PtAdapter();
	ToPtObjectAdapter<Token> getStruct2PtAdapter();
	ToPtObjectAdapter<ComplexMatrixToken> getComplexMatrix2PtAdapter();
	ToPtObjectAdapter<ComplexToken> getComplex2PtAdapter();
	ToPtObjectAdapter<ScalarToken> getScalar2PtAdapter();
	ToPtObjectAdapter<MatrixToken> getScalarMatrix2PtAdapter();
	
	ToMatlabObjectAdapter<ArrayToken> getPt2MlNxNMatrixAdapter();
	ToMatlabObjectAdapter<StringToken> getPt2MlStringAdapter();
	ToMatlabObjectAdapter<RecordToken> getPt2MlStructAdapter();
	ToMatlabObjectAdapter<ComplexMatrixToken> getPt2MlComplexMatrixAdapter();
	ToMatlabObjectAdapter<ComplexToken> getPt2MlComplexAdapter();
	ToMatlabObjectAdapter<DoubleMatrixToken> getPt2MlDoubleMatrixAdapter();
	ToMatlabObjectAdapter<ScalarToken> getPt2MlDoubleAdapter();
	ToMatlabObjectAdapter<ArrayToken> getPt2MlCellMatrixAdapter();
}
