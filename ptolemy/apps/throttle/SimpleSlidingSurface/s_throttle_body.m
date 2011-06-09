%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% Equations of Motion:
% --------------------------------- 
% theta_dot = omega; 
% omega_dot = -b/J*omega + m*g*r/J*sin(theta) + u;
%
% theta_hat_dot = omega + L1*(theta-theta_hat);
% omega_hat_dot = -b/J*omega_hat + m*g*r/J*sin(theta_hat) 
%                 + u + L2*(theta-theta_hat);
%
% The control goal is to make the link position, theta, track some desired
% position profile.  An I/O linearizing control is used and a nonlinear 
% observer is used to calculate the unknown states.
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%


function [sys,x0] = s_throttle_body(t,x,u,flag)
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% t =	Current simulation time
% x = 	States internal to this function.    States are brought in from the 
%	outside because they are integrated by one of simulink's integration 
%	routines (rk45, for example) and then used by this s-file to calculate
%	the next state derivs which are then passed back out to be integrated.
% u = 	Vector of inputs.
% flag= Flag indicating which information Simulink wants from this routine.
%	Possible flag values are as follows:
%		0	Return the sizes of parameters and initial conditions.
%		1	Return the state derivs dx/dt for integration.
%		2	Return the discrete state x(n+1)
%		3	Return the output vector, y.
%		4	Return the next time interval for a discrete state
%			update (for mixed discrete/continuous time systems)
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%


% discrete sample time for controller
ts = 0.02;

% measured from physical system  (each is normalized by J)
Ks_J = 390;   % (rad/s^2) / rad
Kd_J = 0.1;   % (rad/s^2) / (rad/s)
Kf_J = 140;   % (rad/s^2)
Ra = 1.7;		% ohms
theta_eq = -0.25;	% rad
Kt_Ra_J = 140;

% educated guesses and assumptions
J = 5e-5; 	% kg-m^2
La = 0;		% H

% derived constants
Ks = Ks_J*J;	% N-m/rad
Kf = Kf_J*J; 	% N-m
Kd = Kd_J*J;   % N-m/rad/s
Kt = Ra*J*Kt_Ra_J;  % N-m/A

% Observer and controller gains


% Give states and inputs nice names so that they match diffQs in header
if abs(flag) ~= 0  % so that state is not accessed when 
                   % simulink queries for IC's
	theta = x(1);
	omega = x(2);
	Ea = x(3);
end



%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% STATE DERIVATIVES:  Calculate them according to difQs in header.
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

if abs(flag)==1		

  % diffQ's for continuous state	
  % plant
  theta_dot = omega;
  
  omega_dot = -(Ks/J)*(theta-theta_eq)-(Kt^2/Ra/J+Kd/J)*omega...
     				-Kf/J*sign(omega) + Ea*Kt/Ra/J;
  
  %  output state derivs 
  sys = [theta_dot omega_dot];

	
	
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% DISCRETE STATE EQUATIONS:
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

elseif flag == 2
        
   % Discrete state equations
   
   %%%%%% CONTROL LAW %%%%%%%%%%
   lambda = 30;
   n = 1.5; %volts
   theta_des = pi/4+pi/4*sin(t*2*pi);
   omega_des = pi^2/2*cos(t*2*pi);
   omega_des_dot = -pi^3*sin(t*2*pi);
   s = (omega - omega_des) + lambda*(theta-theta_des);
   Ea_next = ( (Kt+Ra*Kd/Kt)*omega + (Ra*Kf/Kt)*sign(omega) ...
      + (Ra*Ks/Kt)*(theta-theta_eq) ...
      + (Ra*J/Kt)*(omega_des_dot - lambda*(omega-omega_des)) ) ...
      - n*sign(s); % (volts) 
   
   % saturate at 10 Volts and 0 Volts
   if Ea_next > 10
      Ea_next = 10;
   end
   
      
   % Return next discrete states
   sys = [Ea_next, theta_des];


	
	
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% OUTPUT: Calculate the outputs--the full state, and a few extras
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

elseif flag==3
       
  % Calculate extra outputs
  
	
  % Output full state
  sys = [x];


  
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% NEXT SAMPLE HIT FOR DISCRETE STATES:
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  
elseif flag == 4
  
  % ns temporarily stores the number of samples
  ns = (t/ts);
  sys = (1 + floor(ns + 1e-13*(1+ns)))*ts;  
  
  
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%  
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% INITIAL CONDITIONS AND STATE VECTOR SIZES: Initial conditions of all state
% estimates are zero.  State vector size vector is as follows:
% 	sizes(1) =	number of continuous states internal to this file
%	sizes(2) = 	number of discrete states.
%	sizes(3) = 	number of outputs
%	sizes(4) = 	number of inputs
%	sizes(5) = 	number of discontinuous roots
%	sizes(6) = 	flag for direct feedthrough (used for finding algebraic
%			loops)
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

elseif flag==0	 
  
  % sys returns vector sizes; x0 returns ICs 
  sys = [2,2,4,0,0,0]; x0=[ 0; 0; 0; 0];   %%%% EXPERIMENT WITH ICS %%%
	
	
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% OTHER FLAG OPTIONS: not used
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

else			
 	sys = [];
end




























