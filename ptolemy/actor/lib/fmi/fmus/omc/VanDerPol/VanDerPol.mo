// Van der Pol model
// From OpenModelica: build/share/doc/omc/testmodels/VanDerPol.mo
model VanDerPol  "Van der Pol oscillator model"
  Real x(start = 1);
  Real y(start = 1);
  parameter Real lambda = 0.3;
equation
  der(x) = y;
  der(y) = - x + lambda*(1 - x*x)*y;
end VanDerPol;
