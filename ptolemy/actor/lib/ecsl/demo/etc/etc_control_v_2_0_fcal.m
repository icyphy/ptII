%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%  ETC Throttle Control Problem
%  Variable and State Initialization
%
%   Created: 07/23/01 JSS
%   Parameters and calcs given by PG
%   Updated: 10/01 JSS, PG
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
'etc_control'
clear all;

% Add the libraries
addpath('drivers', 'actuators', 'plants', 'sensors', 'controllers');
decimation = 1;

% Human-Driver Inputs
alpha_desired = 0.7;
alpha_gain = 1.0;

% State Initialization
w_throttle_init = 0;
alpha_init = 0;

% Drivers parameters
	pwm_period = 25.0;

% Plant parameters
	% Constants, measured from physical system  (each is normalized by J)
	Ks_J = 390;             % (rad/s^2) / rad
	Kd_J = 0.1;             % (rad/s^2) / (rad/s)
	Kf_J = 140;             % (rad/s^2)
	Ra = 1.7;		        % ohms
	theta_eq = -0.25;	    % rad
	Kt_Ra_J = 140;
	
	% Assumptions
	J = 5e-5; 	            % kg-m^2
	
	% Derived constants
	Ks = Ks_J*J;	        % N-m/rad
	Kf = Kf_J*J; 	        % N-m
	Kd = Kd_J*J;            % N-m/rad/s

    we_max = 6000;

% Actuators parameters
	volts_batt = 12.0;      % battery voltage
	min_motor_amps = 0.0;
	max_motor_amps = 7.4;
    alpha_max = 89.8*pi/180;
    alpha_min = 0.2*pi/180;
  
	% Power Electronics
	Ra = 1.7;               % resistance of motor windings (Ohms)
	Rc = 1.5;               % resistance of RC filter (Ohms)
	Rbat = 0.5;             % internal resistance of battery (Ohms)
	L = 1.5e-3;             % motor winding inductance (Henrys)
	C = 1.5e-3;             % capacitance of RC filter (Farads)
	
	Kt = Ra*J*Kt_Ra_J;      % N-m/A, derived constant
	motor_on_Ax = [ -1/C/(Rbat + Rc),  -Rbat/C/(Rbat + Rc); ...
                 +Rbat/L/(Rbat + Rc),  (-Ra - Rbat +Rbat^2/(Rbat + Rc))/L ];
	motor_on_B_Vbat = [  1/C/(Rbat + Rc), (1 - Rbat/(Rbat + Rc))/L ]';
	motor_on_B_Vbemf = [ 0, -1/L ]';
	motor_off_Ax = [ 0, -(1/L);
                     (1/L), -(Rc+Ra)/L];
	motor_off_B_Vbemf = [ 0, -1/L ]';


% Sensor parameters
	tps_max_volts = 12.0;
	tps_min_volts = 5.0;
	tps1_resist_gain = (1200-500)/(1.57-0);
	tps2_resist_gain = (1200-500)/(1.57-0);
	accelpos2rads = 1.0;
	tps1_volt_offset = 5.0;
	tps2_volt_offset = 5.0;
	tps1_volts2rad = 1.57/7.0;
	tps2_volts2rad = 1.57/7.0;
	current_source = 0.010;

% Controller - Manager parameters
	% Timing Information
	ControllerPeriod = 1;           % 1 ms period for controller
	EndToEndDelay = 3;              % 3 ms delay

    % Controller constants for flow control (note that the value must 
	%  match the ordering of the lines for multiport switching in the controller)

    % correspond to which_mode variable 
	SelectStartup = 1
	SelectDriving = 2
	SelectLimiting = 3
	SelectLimpHome = 4
	SelectShutdown = 5

    % correspond to which_driving_cruise variable 
	SelectDrivingCruise = 1
	SelectDrivingCruiseMin = 2

	% correspond to which_limiting_rev variable 
	SelectLimitingRev = 1
	SelectLimitingRevMax = 2
	
	% correspond to which_limiting_traction variable 
	SelectLimitingTraction = 1
	SelectLimitingTractionMax = 2


% Controller - Servo Control parameters
	% Observer and controller parameters
	lambda = 60;
	n = 2.0;                %Amps
 	% third order filter with a char. poly.
	p_f = poly([-80, -80, -90, -90, -100]);
    alpha_F = tf(p_f(length(p_f)), p_f);
    alpha_DF = c2d(alpha_F, ControllerPeriod/1000);
   	alpha_num = alpha_DF.num{1};
	alpha_den = alpha_DF.den{1};
    MaxMotorVolt = 7.5;     % Volts - approximation of PWM on voltage

	% Sliding mode controller parameters
	v = 0.005;		        % +/- 2%
	u = 1.0 + v;
	l = 1.0 - v;
	c1 = abs( Kt*u - Kt*l );
	c2 = abs( Ra*u*Kd*u/Kt/l - Ra*l*Kd*l/Kt/u );
	c3 = abs( Ra*u/Kt/l - Ra*l/Kt/u );
	c4 = abs( Ra*u*Ks*u/Kt/l - Ra*l*Ks*l/Kt/u );
	c5 = abs( Ra*u*J*u/Kt/l - Ra*l*J*l/Kt/u );
	Int_Gain = 40;
		
	% Other controller modes
	Ktracking = 0.2;
	TPS_error_threshold = 0.10;       % radians of error between TPS readings to detect a fault
    max_alpha_limphome = 40*pi/180;   % maximum allowed throttle angle during limp-home mode
	motor_amps_cruise = 1.0;
	motor_amps_traction = 0.5;

% Controller - Monitor parameters
	% correspond to which_faults variable. Note that more than one fault
    % can occur at the same time. In that case, the which_faults variable
    % is set to the sum of the individual fault values (each fault value
    % should toggle a bit in the which_faults variable).
    FaultNoFault = 0;
    FaultHardwareTPS = 1;
    FaultHardwareActuator = 2;
	FaultSoftwareManagerTask = 4;
    FaultSoftwareServoTask = 8;    % the filter on the input is a simple 
