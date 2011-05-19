%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% run_s_observer.m: runs the simulation of inverted pendulum.
%   Runs the similink block diagram s_observer_diagram.mdl
%   which in turn calls the s-function s_observer.m
%
%
% ME 237 Spring 2000
%
% Mike Uchanski
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
clear all;
close all;



%%%%%%%%%%%%%%%%%%%% SOLVER PARAMETERS %%%%%%%%%%%%%%%%%%%%%%%
tf = 2.0;  		% total sim time
options = simset('Solver', 'ode23');
options = simset('MaxStep', 0.002);

%%%%%%%%%%%%%%%%%%% SOLVER CALL %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
[ junk, junk, junk] = sim('throttle_body', tf, options);



%%%%%%%%%%%%%%%%% NICE NAMES FOR COLS OF SIMOUT VECTOR %%%%%%%%
theta = simout(:,1);
omega = simout(:,2);
u = simout(:,3);
theta_des = simout(:,4);


%%%%%%%%%%%%%%%%%%%%%% FIGURE 1 %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
figure(1);

subplot(3,1,1), plot(t,theta);
hold
subplot(3,1,1), plot(t,theta_des);
subplot(3,1,2), plot(t,omega);
subplot(3,1,3), plot(t,u);

zoom on;



