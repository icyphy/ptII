// Van der Pol model
// From OpenModelica: build/share/doc/omc/testmodels/VanDerPol.mo
model VanDerPol  "Van der Pol oscillator model"
  // Was start = 1, but FMUSDK has start = 2
  Real x(start = 2);
  // Was start = 1, but FMUSDK has start = 0
  Real y(start = 0);
  // Was lambda = 0.3, but FMUSDK has start = 1
  parameter Real lambda = 1.0;
equation
  der(x) = y;
  der(y) = - x + lambda*(1 - x*x)*y;
end VanDerPol;
