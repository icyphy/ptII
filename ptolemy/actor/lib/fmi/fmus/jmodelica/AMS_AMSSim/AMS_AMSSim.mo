package AMS
  import SI = Modelica.SIunits;
  type Pressure = SI.Pressure;
  type MassFlowRate = SI.MassFlowRate;
  type Temperature = SI.Temperature;
  type Mass = SI.Mass;
  type Volume = SI.Volume;
  type Area = SI.Area;
  type Energy = SI.Energy;
  type Density = SI.Density;
  type Power = SI.Power;
  final constant Real R(final unit = "J/(mol.K)") = 8.314472;
  final constant Real M(final unit = "kg/mol") = 0.02897;
  final constant Real Cair(final unit = "J/(kg.K)") = 716.75;
  function LinearMap "2D linear map used by the HX"
    input MassFlowRate w_grid[:];
    input Temperature t_grid[:];
    input Real h_grid[:,:];
    input MassFlowRate w;
    input Temperature t;
    output Real h;
  protected
    Integer n "size of mass flow grid";
    Integer m "size of temperature grid";
  algorithm
    n:=size(w_grid, 1);
    m:=size(t_grid, 1);
    assert(n == size(h_grid, 1), "mass flow size mismatch");
    assert(m == size(h_grid, 2), "temperature size mismatch");
    assert(w >= w_grid[1] and w <= w_grid[n], "mass flow out of range");
    assert(t >= t_grid[1] and t <= t_grid[m], "temperature out of range");
    for i in 1:n - 1 loop
          if w >= w_grid[i] and w <= w_grid[i + 1] then 
        for j in 1:m - 1 loop
                  if t >= t_grid[j] and t <= t_grid[j + 1] then 
            h:=((w - w_grid[i]) * (t - t_grid[j]) * h_grid[i + 1,j + 1] + (w - w_grid[i]) * (t_grid[j + 1] - t) * h_grid[i + 1,j] + (w_grid[i + 1] - w) * (t - t_grid[j]) * h_grid[i,j + 1] + (w_grid[i + 1] - w) * (t_grid[j + 1] - t) * h_grid[i,j]) / (t_grid[j + 1] - t_grid[j]) / (w_grid[i + 1] - w_grid[i]);
          else

          end if;
        end for;
      else

      end if;
    end for;
  end LinearMap;
  /*  model testLinearMap
    parameter Temperature t_vals[4] = {0,2,4,6};
    parameter MassFlowRate w_vals[3] = {0,3,6};
    parameter Real hcoef_vals[3,4] = {{0,1,2,3},{0,2,4,6},{0,3,6,9}};
    Real hcoef;
    equation
    hcoef = LinearMap(t_grid = t_vals, w_grid = w_vals, hcoef_grid = hcoef_vals, t = time, w = time);
    annotation(experiment(StartTime = 0, StopTime = 6, Tolerance = 0.000001));
    end testLinearMap;*/
  model Valve "Valve for air flow, accounts for choked flow conditions"
    input Port inport;
    output Port outport;
    Temperature t;
    MassFlowRate w;
    Pressure pi "inlet pressure";
    Pressure po "outlet pressure";
    Real C "valve coefficient";
  equation
    inport.t = t;
    inport.w = w;
    inport.p = pi;
    outport.t = t;
    outport.w + inport.w = 0;
    outport.p = po;
    if noEvent(po > 0.5 * pi) then
      w = 0.000472 * C * (pi + 2 * po) * sqrt((1 - po / pi) / t);
    else
      w = 0.000667 * C * pi * sqrt(1 / t);
    end if;
  end Valve;
  model HX
    parameter Mass m = 13.61 "mass of HX";
    parameter MassFlowRate wc_grid[11] = {0.76,1.51,2.27,3.02,3.78,4.54,5.29,6.05,6.8,7.56,8.32};
    parameter MassFlowRate wh_grid[11] = {0.19,0.38,0.57,0.76,0.95,1.13,1.32,1.51,1.7,1.89,2.08};
    parameter Temperature th_grid[10] = {200.0,216.67,233.33,250.0,266.67,283.33,300.0,316.67,333.33,366.67};
    parameter Temperature tc_grid[10] = {200.0,216.67,233.33,250.0,266.67,283.33,300.0,316.67,333.33,366.67};
    parameter Real hc_grid[11,10] = {{5.08,5.12,5.2,5.28,5.37,5.45,5.53,5.61,5.65,5.81},{8.17,8.25,8.37,8.54,8.66,8.82,8.94,9.06,9.15,9.35},{10.61,10.69,10.85,11.06,11.22,11.38,11.59,11.75,11.91,12.15},{12.52,12.68,12.89,13.13,13.37,13.62,13.82,14.02,14.19,14.51},{14.15,14.35,14.59,14.88,15.12,15.41,15.65,15.89,16.14,16.54},{15.65,15.85,16.1,16.42,16.71,16.99,17.28,17.52,17.8,18.25},{16.95,17.19,17.48,17.8,18.13,18.41,18.74,19.02,19.27,19.76},{18.13,18.37,18.7,19.06,19.43,19.76,20.08,20.37,20.65,21.18},{19.23,19.47,19.84,20.2,20.57,20.93,21.3,21.63,21.95,22.48},{20.28,20.53,20.89,21.3,21.67,22.03,22.4,22.76,23.09,23.7},{20.28,20.53,20.89,21.3,21.67,22.03,22.4,22.76,23.09,23.7}};
    parameter Real hh_grid[11,10] = {{3.25,3.66,3.66,3.66,3.66,3.66,3.66,3.66,3.66,4.07},{5.69,5.69,5.69,6.1,6.1,6.1,6.1,6.5,6.5,6.5},{7.72,7.72,7.72,7.72,8.13,8.13,8.13,8.54,8.54,8.94},{8.94,9.35,9.35,9.76,9.76,9.76,10.16,10.16,10.57,10.57},{10.57,10.57,10.57,10.98,11.38,11.38,11.79,11.79,12.2,12.2},{11.79,11.79,12.2,12.2,12.6,12.6,13.01,13.41,13.41,13.82},{12.6,13.01,13.01,13.41,13.82,13.82,14.23,14.63,14.63,15.04},{13.82,13.82,14.23,14.63,15.04,15.04,15.45,15.85,15.85,16.26},{14.63,15.04,15.04,15.45,15.85,16.26,16.67,16.67,17.07,17.48},{15.45,15.85,16.26,16.67,16.67,17.07,17.48,17.89,18.29,18.7},{15.45,15.85,16.26,16.67,16.67,17.07,17.48,17.89,18.29,18.7}};
    parameter Area Ahx = 0.00161 "hot pipe cross section area";
    parameter Real Cmetal(final unit = "J/(kg.K)") = 837.3 "heat capacity of hx metal";
    Real hc "cold flow heat transfer coefficient";
    Real hh "hot flow heat transfer coefficient";
    MassFlowRate wc "cold flow rate";
    MassFlowRate wh "hot flow rate";
    Temperature t "hx metal temperature";
    Energy Q "hx metal thermal energy";
    Density rhoh "density of hot flow air";
    Power qh "per sec energy transfered from hot flow to the hx metal";
    Power qc "per sec energy transfered from hx metal to cold flow";
    input Port hin "hot flow inlet";
    output Port hout "hot flow outlet";
    input Port cin "cold flow inlet";
    //Port cout;
  equation
    //hot flow equations
    hin.w + hout.w = 0;
    wh = hin.w;
    Cair * (hout.w * hout.t + hin.w * hin.t) - qh = 0;
    rhoh = M * hin.p / (R * hin.t);
    //cold flow equations
    cin.w = wc;
    //HX equations
    Q = m * t * Cmetal;
    der(Q) = qh + qc;
    //equations need to be modified
    hc = LinearMap(t_grid = tc_grid, w_grid = wc_grid, h_grid = hc_grid, t = cin.t, w = wc);
    hh = LinearMap(t_grid = th_grid, w_grid = wh_grid, h_grid = hh_grid, t = hin.t, w = hin.w);
    qh = hh * (hin.t - t);
    qc = hc * (cin.t - t);
    hin.w = sqrt(2 * (hin.p - hout.p) * rhoh * Ahx ^ 2 / 0.009895);
  end HX;
  model Container "constant volume container"
    parameter Volume v = 0.004916 "container volume";
    input Port inport;
    output Port outport;
    Pressure p;
    Temperature t;
    Mass m;
    Energy Q "thermal energy of the fluid contained in the container";
  equation
    inport.p = p;
    outport.p = p;
    outport.t = t;
    //algebraic equations
    Q = m * t * Cair;
    p = m * t * R / (v * M);
    //dynamical equations
    der(m) = inport.w + outport.w;
    der(Q) = inport.w * inport.t * Cair + outport.w * outport.t * Cair;
  end Container;
  model Cabin "constant pressure and volume"
    parameter Volume v = 141.58;
    parameter Pressure p = 101325;
    parameter Real Qpass = 90 "per sec heat generated per passenger";
    parameter Integer passenger = 200 "number of passengers";
    parameter Real dQ = -8792 "per sec heat gain from the environment";
    input Port inport;
    output Port outport;
    Temperature t;
    Mass m;
    Energy Q "thermal energy of air";
    MassFlowRate wa "air lost to the environment to maintain cabin pressure constant";
  equation
    inport.p = p;
    outport.p = p;
    outport.t = t;
    //algebraic equations
    Q = m * t * Cair;
    p = m * t * R / (v * M);
    0 = (inport.w * inport.t + outport.w * outport.t + wa * t) * Cair + Qpass * passenger + dQ;
    //dynamical equations
    der(m) = inport.w + outport.w + wa;
  end Cabin;
  model Fan "constant flow fan"
    input Port inport;
    output Port outport;
    parameter MassFlowRate wf = 0.3024;
  equation
    inport.w = wf;
    outport.w + inport.w = 0;
    inport.t = outport.t;
  end Fan;
  model Env "constant pressure and temperature"
    parameter Pressure p_env = 200000;
    parameter Temperature t_env = 480;
    Port port;
  equation
    port.p = p_env;
    port.t = t_env;
  end Env;
  model Mixer
    input Port inport[3];
    output Port outport;
  equation
    inport[2].p = inport[1].p;
    inport[3].p = inport[1].p;
    outport.p = inport[1].p;
    inport[1].w + inport[2].w + inport[3].w + outport.w = 0;
    inport[1].w * inport[1].t + inport[2].w * inport[2].t + inport[3].w * inport[3].t + outport.w * outport.t = 0;
  end Mixer;
  connector Port
    Pressure p;
    flow MassFlowRate w;
    Temperature t;
  end Port;
  model AMSSim
    Env inlet(t_env = 350, p_env = 259932);
    Valve valve1;
    Container fork(t(start = 350, fixed = true), p(start = 80000, fixed = true));
    Valve valve2;
    Env ambientair(t_env = 220, p_env = 50000);
    HX hx(t(start = 250, fixed = true));
    Mixer mixer;
    Cabin cabin(t(start = 297.2, fixed = true), p = 75152);
    Fan fan;
    //Variables defined on the simple AMS diagram
    Temperature T1;
    Pressure P1;
    MassFlowRate W1;
    Pressure P2;
    Temperature T3;
    MassFlowRate W31;
    MassFlowRate W32;
    MassFlowRate W33;
    Temperature T4;
    MassFlowRate W4;
    Pressure P5;
    Temperature T6;
    Temperature T7;
    MassFlowRate W7;
    Temperature T8;
    MassFlowRate W8;
    Temperature Thx;
  equation
    //control variables
    valve1.C = 0.155;
    valve2.C = 0.1;
    hx.wc = 4;
    //topology
    connect(inlet.port,valve1.inport);
    connect(valve1.outport,fork.inport);
    connect(fork.outport,hx.hin);
    connect(fork.outport,valve2.inport);
    connect(hx.hout,mixer.inport[1]);
    connect(hx.cin,ambientair.port);
    connect(valve2.outport,mixer.inport[3]);
    connect(mixer.outport,cabin.inport);
    connect(mixer.inport[2],fan.outport);
    connect(cabin.outport,fan.inport);
    //variables defined on the ams diagram after model reduction
    T1 = inlet.t_env;
    P1 = inlet.p_env;
    W1 = valve1.w;
    T3 = fork.t;
    P2 = fork.p;
    W31=W32+W33;
    W32 = valve2.w;
    W33 = hx.wh;
    W4 = hx.wc;
    T4 = ambientair.t_env;
    T6 = hx.hout.t;
    T8 = mixer.outport.t;
    W8 = -mixer.outport.w;
    T7 = cabin.t;
    Thx = hx.t;
    P5 = cabin.p;
    W7 = fan.wf;
    annotation(experiment(StartTime = 0, StopTime = 10000, Tolerance = 0.000001));
  end AMSSim;
end AMS;

