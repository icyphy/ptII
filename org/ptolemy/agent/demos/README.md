# Standard demos

Three end-to-end scenarios exercising the auto-modeling agent. Each is
designed to be runnable in well under a minute and to make a different
capability of the system visible.

## Goal cheat sheet

Paste these prompts into the chat panel after starting the server with
an `OPENAI_API_KEY` configured.

### 1. BuildRCLowPass — natural language to running simulation

> Build a continuous-time RC low-pass filter. Drive it with a sinewave
> source. Use ContinuousDirector with stopTime 1.0. Connect a Recorder
> to the filter output. Then run the simulation.

What the agent should do, step by step:

1. `list_library` to discover `Sinewave`, `Integrator`, `Recorder`.
2. Set top-level director via `set_parameter` (or add a fresh director
   if needed).
3. `add_entity` Sinewave, Integrator-cascade implementing the RC
   transfer function, Recorder.
4. `connect` them in order.
5. `validate` then `run`.

Acceptable result: probes panel shows a low-pass-filtered sinusoid.

### 2. TunePID — multi-turn closed loop

Start from `tune-pid-seed.xml` (see this folder). The seed model
contains a PID controller around a first-order plant but with
deliberately bad gains (`Kp=0.1`, `Ki=0.0`, `Kd=0.0`).

> Tune the PID controller so the plant output reaches the setpoint in
> under 2 seconds with no more than 10% overshoot. Run the simulation
> after each adjustment.

The agent should call `run`, observe the signals, call
`set_parameter` on `Kp`, `Ki`, `Kd`, and iterate.

### 3. RepairModel — fix a broken model

Load `repair-seed.xml`. It is a deliberately-broken SDF chain
(`Ramp -> Scale -> Discard`) where the Scale's input port has been
disconnected.

> Validate this model and reconnect anything that is dangling. Then
> run it.

The agent should call `validate`, notice the missing relation, call
`connect Ramp.output -> Scale.input`, then `run`.

## How to load a demo seed

```sh
SID=$(curl -s -X POST http://localhost:7777/api/v1/sessions \
        | python -c "import sys,json;print(json.load(sys.stdin)['id'])")
curl -s -X POST http://localhost:7777/api/v1/sessions/$SID/load \
     -H 'Content-Type: application/json' \
     -d "{\"path\":\"$PTII/org/ptolemy/agent/demos/tune-pid-seed.xml\"}"
```

Or simply paste the path in the UI's "Load" field.
