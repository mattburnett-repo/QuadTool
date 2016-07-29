/*
   QuadToolSynth.sc
      base class for Synth-based tone generators
         xyPanning, others
      All action scoped to 'toneSource', which is a Synth instance

   COPYRIGHT 2016 Matthew Burnett

NOTES:
======

*/

QuadToolSynth : QuadToolBase {
	// INIT PARAMS
	//    synth/s respond to different commands than Pdef/s. Override baseclass OSC here.
	initParams {
		arg targetPrefix;

	   // outLevel
		OSCdef.new(("outLevel_" ++ targetPrefix).asSymbol,
			{
				arg msg;
				var theVal = msg[1];

				if(gestureObject.ch0_rec, {gestureObject.ch0_val = msg[1]});
				toneSource.set(\outLevel, theVal);
			},
			'/' ++ targetPrefix ++ '/params/outLevel/fader/x'
		);
		// dur
		OSCdef.new(("dur_" ++ targetPrefix).asSymbol, // click density/rate
			{
				arg msg;
				var theVal = msg[1].linlin(0.0, 1.0, configObject.durMin, configObject.durMax).asStringPrec(5).asFloat;

				if(gestureObject.ch1_rec, {gestureObject.ch1_val = msg[1]});
				toneSource.set(\dur, theVal);
			},
			'/' ++ targetPrefix ++ '/params/dur/fader/x'
		);
		// originally for file player synths. Not the same as gesture/playbackRate!
		OSCdef.new(("filePlaybackRate_" ++ targetPrefix).asSymbol,
			{
				arg msg;
				var theVal = msg[1].linlin(0.0, 1.0, configObject.playbackMin, configObject.playbackMax).asStringPrec(2).asFloat;
				toneSource.set(\playbackRate, theVal);
				// toneSource.set(\speed, theVal);
			},
			'/' ++ targetPrefix ++ '/params/playbackRate/fader/x'
		);
		// for SimpleLoop and similar. Doesn't rescale like ^^^ does. NOT YET IMPLEMENTED IN LEMUR
		OSCdef(("loopPlaybackSpeed_" ++ configObject.targetPrefix).asSymbol,
			{
				arg msg;
				toneSource.set(\speed, msg[1]);
			},
			'/' ++ configObject.targetPrefix ++ '/loop/speed/x'
		);
		// playState
		OSCdef.new(("playState_" ++ targetPrefix).asSymbol,
			{
				arg msg;
				// from ToneGeneratorSynth. All action scoped to 'toneSource', which is a Synth instance
				if(msg[1] == 1, {toneSource = toneObject.getGenerator},{toneSource.free});
			},
			'/' ++ targetPrefix ++ '/params/playState/x'
		);
	}
	// INIT SOURCE
	initSource {
		arg targetPrefix;

		OSCdef.new(("sourceSelect_" ++ targetPrefix).asSymbol,
			{
				arg msg;

				if(gestureObject.ch6_rec, {gestureObject.ch6_val = msg[1]});
				toneSource.set(\inBus, msg[1]);
			},
			'/' ++ targetPrefix ++ '/source/inBus/selected'
		);
	}
	// INIT PATTERN
	initPattern { // initialized in base class
		arg targetPrefix;

		// outBus
		OSCdef.new(("outBus_" ++ targetPrefix).asSymbol, // click outBus
			{
				arg msg;

				if(gestureObject.ch9_rec, {gestureObject.ch9_val = msg[1]});
				toneSource.set(\outBus, msg[1]);
			},
			'/' ++ targetPrefix ++ '/pattern/outBus/selected'
		);
		// panner X
		OSCdef.new(("x_" ++ targetPrefix).asSymbol,
			{
				arg msg;

				if(gestureObject.ch10_rec, {gestureObject.ch10_val = msg[1]}); // FrameAutomator wants this format of data
				toneSource.set(\theX, msg[1].linlin(0, 1, -1, 1));  // Pan4 wants this format of data
			},
			'/' ++ targetPrefix ++ '/pattern/panner/x'
		);
		// panner Y
		OSCdef.new(("y_" ++ targetPrefix).asSymbol,
			{
				arg msg;

				if(gestureObject.ch11_rec, {gestureObject.ch11_val = msg[1]}); // FrameAutomator wants this format of data
				toneSource.set(\theY, msg[1].linlin(0, 1, -1, 1));  // Pan4 wants this format of data
			},
			'/' ++ targetPrefix ++ '/pattern/panner/y'
		);
	}
} // end class def

