model Influenza
  // Parameters
  parameter Real MortalityProb = 0.01;
  parameter Real RecoveryTime = 3.0;
  // In days
  parameter Real MortalityTime = 1.0;
  // In days
  parameter Real TransmissionProb = 0.15;
  parameter Real EncounterRate = 4;
  // In days
  // Start variables
  Real Deceased(start = 0);
  Real Recovered(start = 0);
  Real Removed(start = 0);
  Real Infectious(start = 0);
  Real Susceptible(start = 499000);
  Real Population;
  Real R;
equation
  Population = Recovered + Infectious + Susceptible;
  R = (TransmissionProb * EncounterRate * Susceptible) / Population;
  der(Removed) = (MortalityProb / MortalityTime + (1 - MortalityProb) / RecoveryTime) * Infectious;
  der(Deceased) = MortalityProb * der(Removed);
  der(Recovered) = (1 - MortalityProb) * der(Removed);
  der(Susceptible) = -R * Infectious;
  der(Infectious) = -der(Removed) + R * Infectious + (1+sin(4*time/365));
end Influenza;

