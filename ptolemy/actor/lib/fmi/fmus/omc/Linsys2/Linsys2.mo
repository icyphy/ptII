model Linsys2
	parameter Real [2,2] A = { { -2, 0.0 }, { 0.0, -1.0 } };
	Real [2] x;
equation
	A*der(x) = x;
initial equation
	x[1] = 1.0;
	x[2] = 2.0;
end Linsys2;
