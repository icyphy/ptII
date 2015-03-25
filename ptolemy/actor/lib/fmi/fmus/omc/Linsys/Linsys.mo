model Linsys
	parameter Real [2,2] A = { { -0.5, 0.0 }, { 0.0, -1.0 } };
	Real [2] x;
equation
	der(x) = A*x;
initial equation
	x[1] = 1.0;
	x[2] = 2.0;
end Linsys;
