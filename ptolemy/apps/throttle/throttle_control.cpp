#include <iostream.h>
#include <nidaq.h>
#include <windows.h>
#include <fstream.h>
#include <math.h>

#include "ThrottlePlant.h"

LONGLONG PrefFreq;

// Read throttle position sensor
void GetPlantStates( struct ThrottlePlantStates *states, int IsInitial );
// Output throttle actuator (dc motor)
void OutputControl( double ea );
// The controller function (runs in a high priority thread)
DWORD WINAPI Controller(LPVOID pData);

int main(void)
{
	DWORD CtrlThreadId, ExitCode;
	HANDLE hCtrlThread;

	hCtrlThread = CreateThread(NULL, 0 /*default stack size*/, Controller, 
		NULL /*no arguments*/, CREATE_SUSPENDED /*run now*/, &CtrlThreadId);
	
	// make this thread into a high priority thread
	SetPriorityClass(hCtrlThread, HIGH_PRIORITY_CLASS );
	SetThreadPriority(hCtrlThread, THREAD_PRIORITY_HIGHEST);

	// run
	ResumeThread(hCtrlThread);

	// check for the thread to exit
	do
	{
		Sleep(500);
		GetExitCodeThread(hCtrlThread, &ExitCode);
	} while(ExitCode == STILL_ACTIVE);

	return 0;
}

DWORD WINAPI Controller(LPVOID pData)
{
	short int	status, board, boardCode;
	int ctr;

	// MIO_Calibrate();
	// AI_Configure() - call if board settings vary from factory settings

	// ************** Check for timing ability **************
	LARGE_INTEGER li;
	if( !QueryPerformanceFrequency(&li) )
		return -1;

	PrefFreq = li.QuadPart;
	if (PrefFreq < 100)
		return -1;

	// ************* Setup NI-DAQ ***************************
	board		= 3;
	boardCode	= 0;
	status = Init_DA_Brds(board, &boardCode);
	cout << "DA Initialization: " << status << endl;

	ctr = 1;
	status = CTR_Config(board, ctr, 0, 0, 0, 0);
	cout << "Counter configuration : " << status << endl;

	AI_Clear(board);	// clear any current DAQ operations

	// turn the motor off initially
	CTR_Reset(board, ctr, 0);
	
	// *******************************************************

	int t;				// 
	LONGLONG start_t, real_t;
	double dt;		// ms
	ThrottlePlant plant;
	struct ThrottlePlantParams params;
	struct ThrottlePlantStates states;
	double c1, c2, c3, c4, c5, v, u, l;			// uncertainty constants
	double lambda, n;							// controller constants
	double ea;
	double Kf_hat, gamma_f;
	fstream f;

	params.J = 5e-5;							//  (kg-m^2)
//	params.Ks = 390.0 * params.J;				//  (N-m/rad)
	params.Ks = 10.0 * params.J;				//  (N-m/rad)
	params.Kd = 0.1 * params.J;					//  (N-m/rad/s)
	params.Kf = 140.0 * params.J;				//  (N-m)
	params.Ra = 1.7;							//  (Ohms)
	params.Kt = 140.0 * params.Ra * params.J;	// (N-m/A)
	params.theta_eq = -0.25;
	plant.SetPlantParameters( &params );
	
	states.theta = 0;
	states.omega = 0;
	plant.SetPlantStates( &states );
	f.open("throttle_sim.dat", ios::out);

	// ************* Uncertainty Constants ************
	v = 0.005;		// +/- 2%
	u = 1.0 + v;
	l = 1.0 - v;

	c1 = fabs( params.Kt*u - params.Kt*l );
	c2 = fabs( params.Ra*u*params.Kd*u/params.Kt/l - params.Ra*l*params.Kd*l/params.Kt/u );
	c3 = fabs( params.Ra*u/**params.Kf*u*//params.Kt/l - params.Ra*l/**params.Kf*l*//params.Kt/u );
	c4 = fabs( params.Ra*u*params.Ks*u/params.Kt/l - params.Ra*l*params.Ks*l/params.Kt/u );
	c5 = fabs( params.Ra*u*params.J*u/params.Kt/l - params.Ra*l*params.J*l/params.Kt/u );
	
	// ************* Control Law Constants ***************
	lambda = 30;
	n = 1.5;	// volts
	ea = 0.0;	// volts

	// adaption stuff
	Kf_hat = params.Kf;
	gamma_f = 1e-6;

	// initialize the function 
	GetPlantStates( &states, 1 );
	GetPlantStates( &states, 0 );
	GetPlantStates( &states, 0 );
	GetPlantStates( &states, 0 );

	QueryPerformanceCounter(&li);
	start_t = li.QuadPart;
	real_t = 0;

	dt = 0.002;	// update rate for PWM
	t = 0;		// int time counter, the current sampling time = t*dt

	//for( t = 0; t < 40000; t++ ) // simulation
	while(real_t/((double)PrefFreq) < 12.0)		// run for 12 seconds
	{
		double theta_des, omega_des, omega_d_des;
		double s;

		//plant.GetPlantStates( &states );
		GetPlantStates( &states, 0 );
		if(dt*t <= real_t)
		{

			// desired angle and derivatives
			theta_des = 0.7+0.6*sin(3.0*real_t/((double)PrefFreq));		// rad
			omega_des = 3.0*0.6*cos(3*real_t/((double)PrefFreq));;		// rad/s
			omega_d_des = -9.0*0.6*sin(3.0*real_t/((double)PrefFreq));;		// rad/s^2

			f << states.theta << "  " << states.omega;
			f << " " << theta_des << " " << omega_des;
			f << " " << ea << " " << Kf_hat << " " << real_t/((double)PrefFreq) << endl;


			// ************* The control law *******************
			s = (states.omega - omega_des) + lambda * (states.theta - theta_des);
			ea = ( (params.Kt + params.Ra*params.Kd/params.Kt)*states.omega 
					+ (params.Ra*Kf_hat/params.Kt)*sign(states.omega)
					+ (params.Ra*params.Ks/params.Kt)*(states.theta-params.theta_eq)
					+ (params.Ra*params.J/params.Kt)*(omega_d_des - lambda*(states.omega-omega_des)) )
				-( (c1+c2)*fabs(states.omega) + c3*fabs(Kf_hat) + c4*fabs(states.omega-params.theta_eq)
					+ c5*fabs(omega_d_des-lambda*(states.omega-omega_des)) + n ) * sign(s);

			// saturate at 0 and 7.5 volts
			if (ea < 0)
				ea = 0;
			if (ea > 7.5)
				ea = 7.5;

			//RungeKutta4(&plant, t*dt, dt, ea);
			OutputControl( ea );

			// update the adaption law
			Kf_hat += -gamma_f / params.J * sign(states.omega)*s *dt;

			t++;
		}

		QueryPerformanceCounter(&li);
		real_t = li.QuadPart - start_t;
	}
	// end of the controller loop

	// turn the motor off 
	CTR_Reset(board, ctr, 0);
	// close up the output file
	f.close();

	return 0;
}

void GetPlantStates( struct ThrottlePlantStates *states, int IsInitial )
{
	short int status, board, chan, gain, reading;
	static struct ThrottlePlantStates prev = { 0.0, 0.0 };
	static LONGLONG t_prev = 0;
	LARGE_INTEGER li;
	LONGLONG  t;

	board	= 3;
	chan	= 2;
	gain	= 1;

	status = AI_Read(board, chan, gain, &reading);
	// Retrieve the time of this sample
	QueryPerformanceCounter(&li);
	t = li.QuadPart;
	states->theta = Raw2Rad(reading);

	if(!IsInitial)
	{
		double dt = (t-t_prev)/((double)PrefFreq);
		double c = 200 * dt;

		// calculate the derivative of theta
		states->omega = prev.omega + ((states->theta - prev.theta)/dt - prev.omega)*c;
	}
	t_prev = t;
	prev.theta = states->theta;
	prev.omega = states->omega;
}

void OutputControl( double ea )
{
	short int timeBase, board;
	unsigned short int period1, period2;
	int ctr;
	double freq, duty;

	board	= 3;
	ctr = 1;
	freq = 5e2;
	duty = 1.0 - ea/7.5;   // 1 is off and 0 is on

	CTR_Rate(freq, duty, &timeBase, &period1, &period2);
	//cout << ea << ", " << duty << endl;
	CTR_Square(board, ctr, timeBase, period1, period2);
}

