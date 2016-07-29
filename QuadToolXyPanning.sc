/*

QuadToolXyPanningClass.sc
   Free-form quad panning
      fixed/pulse panning
      fixed loop panning

   COPYRIGHT 2016 Matthew Burnett

DEPENDENCIES:
=============

NOTES:
=====
   Fixed extends Synth, Pulse extends Pdef
      Because of this we can't create superclass for these two

TO-DO:
======

*/

// Fixed/static tone version
QuadToolXyPanningFixed : QuadToolSynth {
	*new {
		arg controllerAddr, targetPrefix;
		^super.new.init(controllerAddr, targetPrefix);
	}
	init {
		arg controllerAddr, targetPrefix;

		configObject = XYpanningFixedConfig.new(controllerAddr, targetPrefix);
		toneObject = ToneGeneratorXYpanningFixed.new(configObject);

		this.initialize(configObject);
	}
} // end class def

// Fixed looping version
QuadToolXyPanningFixedLoop : QuadToolSynth {
	*new {
		arg controllerAddr, targetPrefix, theLength;
		^super.new.init(controllerAddr, targetPrefix, theLength);
	}
	init {
		arg controllerAddr, targetPrefix, theLength;

		configObject = SimpleLoopConfig.new(controllerAddr, targetPrefix, theLength);
		toneObject = SimpleLoop.new(configObject);

		this.initialize(configObject);
		this.initLoopOSC(configObject);
	}

	initLoopOSC {
		arg configObject;
		var loopFile, loopFiles;

		OSCdef(("loopAction_" ++ configObject.targetPrefix).asSymbol,
			{
				arg msg;
				var theMsg;

				// keep record turned off unless explicitly recording ( == 3). otherwise have to put in every if branch.
				toneSource.set(\record, 0);

				if(msg[1] == 0,{
					toneSource.notNil.if({
						loopFile = nil;
						toneObject.clear;  // zeroes out Buffer, keeps allocation
						theMsg = (configObject.targetPrefix + "loop cleared");
					},{theMsg = "No Loop to clear. Record one or load a file."});
				});
				if(msg[1] == 1,{
					toneSource.notNil.if({
						toneSource.set(\speed, 0); // this controls the buffer, not the synth. allows passthough to continue.
						theMsg = (configObject.targetPrefix + "loop stopped");
					},{theMsg = "No Loop to stop. Record one or load a file."});
				});
				if(msg[1] == 2,{
					toneSource.notNil.if({
						toneSource.set(\speed, 1); // this controls the buffer, not the synth. allows passthough to continue.
						if(loopFile.notNil, {theMsg = loopFile + "playing"},{theMsg = configObject.targetPrefix + "loop playing"});
					},{
						toneSource = toneObject.play(out: configObject.outBus);
						theMsg = (configObject.targetPrefix + "loop initialized");
					});
				});
				if(msg[1] == 3, {
					toneSource.set(\speed, 1); // try this for passthough?
					toneSource.set(\record, 1); // doesn't always have passthrough. maybe passthrough isn't possible? doesn't make sense.
					theMsg = (configObject.targetPrefix + "loop recording");
				});

				theMsg.notNil.if({
					theMsg.postln;
					theController.sendMsg('/' ++ configObject.targetPrefix ++ '/loop/messageMonitor/value', theMsg);
				});
			},
			'/' ++ configObject.targetPrefix ++ '/loop/action/x'
		);
		OSCdef(("crossFader_" ++ configObject.targetPrefix).asSymbol,
			{
				arg msg;
				var theVal, theMsg, theOutLevel=1;

				theVal = msg[1].linlin(0, 1, -1, 1);
				toneSource.set(\crossFade, theVal);
			},
			'/' ++ configObject.targetPrefix ++ '/loop/crossFader/x'
		);
		OSCdef(("writeLoopFile_" ++ configObject.targetPrefix).asSymbol,
			{
				arg msg;
				var theMsg;

				if(msg[1] == 1, {
					theMsg = (configObject.targetPrefix + "loop file written");
					toneObject.writeLoopFile;
				});

				theMsg.notNil.if({
					theMsg.postln;
					theController.sendMsg('/' ++ configObject.targetPrefix ++ '/loop/messageMonitor/value', theMsg);
				});
			},
			'/' ++ configObject.targetPrefix ++ '/loop/write/x'
		);
		OSCdef(("selectLoopFile_" ++ configObject.targetPrefix).asSymbol,
			{
				arg msg;
				var theMsg;

				loopFiles = toneObject.loopFileNames;

				if(loopFiles[msg[1]].isNil,
					{theMsg = ("No loop file for " ++ msg[1])},
					{
						loopFile = loopFiles[msg[1]][1];
						toneObject.readLoopFile(loopFile); // auto-play after load; should set control on Lemur to 'play'
						theMsg = loopFile;
				});

				(theMsg + "selected").postln;
				theController.sendMsg('/' ++ configObject.targetPrefix ++ '/loopFiles/theMonitor/value', theMsg);
			},
			'/' ++ configObject.targetPrefix ++ '/loopFiles/Switches/selected'
		);
	} // end initLoopOSC
} // end class def

// Pulse version
QuadToolXyPanningPulse : QuadToolPdef {
	*new {
		arg controllerAddr, targetPrefix;
		^super.new.init(controllerAddr, targetPrefix);
	}

	init {
		arg controllerAddr, targetPrefix;

		configObject = XYpanningPulseConfig.new(controllerAddr, targetPrefix);
		toneObject = ToneGeneratorXYpanningPulse.new(configObject);
		toneSource = toneObject.getGenerator;

		this.initialize(configObject);
	}

	// INIT SOURCE
	initSource { // OSC targets changed from band -> source
		arg targetPrefix;

		// inBus
		OSCdef.new(("sourceSelect_" ++ targetPrefix).asSymbol,
			{
				arg msg;

				if(gestureObject.ch6_rec, {gestureObject.ch6_val = msg[1]});
				toneObject.setInBus(msg[1]);
			},
			'/' ++ targetPrefix ++ '/source/inBus/selected'
		);
	}
	// INIT PATTERN
	initPattern {
		arg targetPrefix;

		OSCdef.new(("x_" ++ targetPrefix).asSymbol,
			{
				arg msg;
				if(gestureObject.ch10_rec, {gestureObject.ch10_val = msg[1]}); // FrameAutomator wants this format of data
				toneObject.setX(msg[1].linlin(0, 1, -1, 1));
			},
			'/' ++ targetPrefix ++ '/pattern/panner/x'
		);
		OSCdef.new(("y_" ++ targetPrefix).asSymbol,
			{
				arg msg;

				if(gestureObject.ch11_rec, {gestureObject.ch11_val = msg[1]}); // FrameAutomator wants this format of data
				toneObject.setY(msg[1].linlin(0, 1, -1, 1));
			},
			'/' ++ targetPrefix ++ '/pattern/panner/y'
		);
	}
} // end class def



