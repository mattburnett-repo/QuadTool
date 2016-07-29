/*
QuadToolBase.sc
   Mostly to keep the common code (OSC, Gesture automation, etc.) in one place

   COPYRIGHT 2016 Matthew Burnett

DEPENDENCIES:
=============

NOTES:
=====
   'toneObject' is whatever generator is instantiated in subclasses

TO-DO:
=====
   Start figuring out how to create sequences of Gestures for each instrument/panel
      Related to AzModPatSequencer project

*/

QuadToolBase {
	var theController;
	var outBus;
	var configObject, toneObject, toneSource;
   var gestureObject, gestureFiles, gestureFile, theGesture;
	// vvv this is gesture/automation on/off stuff vvv
	var ch0_auto, ch1_auto, ch2_auto, ch3_auto, ch4_auto, ch5_auto, ch6_auto;
	var ch7_auto, ch8_auto, ch9_auto, ch10_auto, ch11_auto, ch12_auto;

	initialize {
		arg configObject;

		this.initDefaults;
		this.initGestureAutomation;
		this.initOSC;
		this.showInfo(configObject); // use configObject as arg to make method code easier to deal with
	}

	initDefaults {
		theController = NetAddr.new(configObject.hostname, configObject.port);
		outBus = configObject.outBus;
	}

	initGestureAutomation {
		gestureObject = FrameAutomator.new(theController, configObject);
		gestureObject.theFilepath = configObject.gestureFilepath;
		gestureFiles = gestureObject.gestureFileNames;
		gestureFile = gestureFiles[0]; // default to the first one.
		theGesture = gestureObject.theGesture;
	}

	initOSC {
		this.initParams(configObject.targetPrefix);     // all inherit from here
		this.initSource(configObject.targetPrefix);	   // must implement in subclasses
		this.initPattern(configObject.targetPrefix);    // must implement in subclasses
		this.initGesture(configObject.targetPrefix);    // all inherit from here
		this.initAutomation(configObject.targetPrefix); // all inherit from here

		^OSCdef;  // return the entire OSCdef IdentityDictionary. Makes all OSCdefs visible to caller. YAY :)
	}

	// INIT PARAMS
	initParams {
		arg targetPrefix;

		// outLevel
		OSCdef.new(("outLevel_" ++ targetPrefix).asSymbol,
			{
				arg msg;
				var theVal = msg[1];

				if(gestureObject.ch0_rec, {gestureObject.ch0_val = msg[1]});
				toneObject.setOutLevel(theVal);
			},
			'/' ++ targetPrefix ++ '/params/outLevel/fader/x'
		);
		// dur
		OSCdef.new(("dur_" ++ targetPrefix).asSymbol,
			{
				arg msg;
				var theVal = msg[1].linlin(0.0, 1.0, configObject.durMin, configObject.durMax).asStringPrec(7).asFloat;

				if(gestureObject.ch1_rec, {gestureObject.ch1_val = msg[1]});
				toneObject.setDur(theVal);
			},
			'/' ++ targetPrefix ++ '/params/dur/fader/x'
		);
		// attackTime
		OSCdef.new(("attackTime_" ++ targetPrefix).asSymbol,
			{
				arg msg;
				var theVal = msg[1].linlin(0.0, 1.0, configObject.attackTimeMin, configObject.attackTimeMax).asStringPrec(3).asFloat;

				if(gestureObject.ch2_rec, {gestureObject.ch2_val = msg[1]});
				toneObject.setAttackTime(theVal);
			},
			'/' ++ targetPrefix ++ '/params/attackTime/fader/x'
		);
		// releaseTime
		OSCdef.new(("releaseTime_" ++ targetPrefix).asSymbol,
			{
				arg msg;
				var theVal = msg[1].linlin(0.0, 1.0, configObject.releaseTimeMin, configObject.releaseTimeMax).asStringPrec(3).asFloat;

				if(gestureObject.ch3_rec, {gestureObject.ch3_val = msg[1]});
				toneObject.setReleaseTime(theVal);
			},
			'/' ++ targetPrefix ++ '/params/releaseTime/fader/x'
		);
		// playState
		OSCdef.new(("playState_" ++ targetPrefix).asSymbol,
			{
				arg msg;
				if(msg[1] == 1, {toneSource.play},{toneSource.stop});
			},
			'/' ++ targetPrefix ++ '/params/playState/x'
		);
	}

	// INIT GESTURE
	//    everything uses this
	initGesture {
		arg targetPrefix;

		OSCdef(("playbackRate_" ++ targetPrefix).asSymbol,
			{
				arg msg;
				var theVal = this.rescalePlayback(msg[1]);

				if(gestureObject.isNil,
					{"No Gesture to control yet. Record one or load a Gesture File.".postln},
					{
						gestureObject.setPlaybackRate(theVal);
						theController.sendMsg('/' ++ targetPrefix ++ '/gesture/playbackRateMonitor/value', theVal);
				});
			},
			'/' ++ targetPrefix ++ '/gesture/playbackRate/x'
		);
		OSCdef(("gestureAction_" ++ targetPrefix).asSymbol,
			{
				arg msg;
				var theMsg;

				if(msg[1] == 0, { // CLEAR
					if(theGesture.isPlaying, {theGesture.stop;});
					theGesture = nil;
					gestureObject = nil;
					gestureFile = nil;
					// theController.sendMsg('/' ++ targetPrefix ++ '/gesture/action/x', 1); // set control to 'stop'
					theMsg = (configObject.targetPrefix + "gesture cleared");
				});
				if(msg[1] == 1 && theGesture.isPlaying, { // STOP
					theGesture.stop;
					theMsg = (configObject.targetPrefix + "gesture stopped");
				});
				if(msg[1] == 2, { // PLAY. Use this with chX_auto to toggle automation record/play/off
					if(theGesture.isPlaying,
						{theMsg = "Gesture already playing"},
						{
							if(gestureObject.isNil, { // if it's been cleared start a new one
								gestureObject = FrameAutomator.new(theController, configObject);
								gestureObject.theFilepath = configObject.gestureFilepath;
								theGesture = gestureObject.theGesture;
							});

							theGesture.play;
							if(gestureFile.notNil,
								{theMsg = (gestureFile + "playing")},
								{theMsg = (configObject.targetPrefix + "gesture playing")}
								);
					});
				});

				if(theMsg.notNil, {
					theMsg.postln;
					theController.sendMsg('/' ++ targetPrefix ++ '/gesture/messageMonitor/value', theMsg);
				});
			},
			'/' ++ targetPrefix ++ '/gesture/action/x'
		);
		OSCdef(("selectGesture_" ++ targetPrefix).asSymbol,
			{
				arg msg;
				var theMsg;

				// always read files in case new one/s written. could be done better, but this works. FIXME
				gestureFiles = gestureObject.gestureFileNames;

				if(gestureFiles[msg[1]].isNil,
					{theMsg = ("No gesture file for " ++ msg[1])},
					{  // if gestureObject has been cleared start a new one
						if(gestureObject.isNil, {gestureObject = FrameAutomator.new(theController, configObject)});
						gestureFile = gestureFiles[msg[1]][1];
						gestureObject.readGesture(gestureFile);
						theMsg = gestureFile;
				});

				theController.sendMsg('/' ++ targetPrefix ++ '/gestureFiles/theMonitor/value', theMsg);
			},
			'/' ++ targetPrefix ++ '/gestureFiles/Switches/selected'
		);
		OSCdef(("writeGesture_" ++ targetPrefix).asSymbol,
			{
				arg msg;
				var theMsg = "writeGesture";

				if(msg[1] == 1,
					{
						if(gestureObject.isNil,
							{theMsg = "GestureObject doesn't exist yet. Record a Gesture first."},
							{gestureObject.writeGesture; theMsg = (configObject.targetPrefix + "gesture file written")}
						); // end ... isNil

						theMsg.postln;
						theController.sendMsg('/' ++ targetPrefix ++ '/gesture/messageMonitor/value', theMsg);
				});
			},
			'/' ++ targetPrefix ++ '/gesture/writeGesture/x'
		);
	}

	// INIT AUTOMATION
	//    these are used in all subclasses, but not every one of these
	initAutomation {
		arg targetPrefix;

		ch0_auto = "off";
		ch1_auto = "off";
		ch2_auto = "off";
		ch3_auto = "off";
		ch4_auto = "off";
		ch5_auto = "off";
		ch6_auto = "off";
		ch7_auto = "off";
		ch8_auto = "off";
		ch9_auto = "off";
		ch10_auto = "off";
		ch11_auto = "off";
		ch12_auto = "off";

		OSCdef.new(("outLevel_auto_" ++ targetPrefix).asSymbol,
			{
				arg msg;

				switch(msg[1].asString,
					"0", {ch0_auto = "off"; gestureObject.ch0_rec = false; gestureObject.ch0_read = false;},
					"1", {ch0_auto = "read"; gestureObject.ch0_rec = false; gestureObject.ch0_read = true;},
					"2", {ch0_auto = "record"; gestureObject.ch0_rec = true; gestureObject.ch0_read = false;}
				);
			},
			'/' ++ targetPrefix ++ '/params/outLevel/autoAction/selection'
		);
		OSCdef.new(("dur_auto_" ++ targetPrefix).asSymbol,
			{
				arg msg;

				switch(msg[1].asString,
					"0", {ch1_auto = "off"; gestureObject.ch1_rec = false; gestureObject.ch1_read = false;},
					"1", {ch1_auto = "read"; gestureObject.ch1_rec = false; gestureObject.ch1_read = true;},
					"2", {ch1_auto = "record"; gestureObject.ch1_rec = true; gestureObject.ch1_read = false;}
				);
			},
			'/' ++ targetPrefix ++ '/params/dur/autoAction/selection'
		);
		OSCdef.new(("attackTime_auto_" ++ targetPrefix).asSymbol,
			{
				arg msg;

				switch(msg[1].asString,
					"0", {ch2_auto = "off"; gestureObject.ch2_rec = false; gestureObject.ch2_read = false;},
					"1", {ch2_auto = "read"; gestureObject.ch2_rec = false; gestureObject.ch2_read = true;},
					"2", {ch2_auto = "record"; gestureObject.ch2_rec = true; gestureObject.ch2_read = false;}
				);
			},
			'/' ++ targetPrefix ++ '/params/attackTime/autoAction/selection'
		);
		OSCdef.new(("releaseTime_auto_" ++ targetPrefix).asSymbol,
			{
				arg msg;

				switch(msg[1].asString,
					"0", {ch3_auto = "off"; gestureObject.ch3_rec = false; gestureObject.ch3_read = false;},
					"1", {ch3_auto = "read"; gestureObject.ch3_rec = false; gestureObject.ch3_read = true;},
					"2", {ch3_auto = "record"; gestureObject.ch3_rec = true; gestureObject.ch3_read = false;}
				);
			},
			'/' ++ targetPrefix ++ '/params/releaseTime/autoAction/selection'
		);
		OSCdef.new(("bandSelect_auto_" ++ targetPrefix).asSymbol,
			{
				arg msg;

				switch(msg[1].asString,
					"0", {ch4_auto = "off"; gestureObject.ch4_rec = false; gestureObject.ch4_read = false;},
					"1", {ch4_auto = "read"; gestureObject.ch4_rec = false; gestureObject.ch4_read = true;},
					"2", {ch4_auto = "record"; gestureObject.ch4_rec = true; gestureObject.ch4_read = false;}
				);
			},
			'/' ++ targetPrefix ++ '/source/bandSelection/autoAction/selection'
		);
		OSCdef.new(("respectrum_auto_" ++ targetPrefix).asSymbol,
			{
				arg msg;

				switch(msg[1].asString,
					"0", {ch5_auto = "off"; gestureObject.ch5_rec = false; gestureObject.ch5_read = false;},
					"1", {ch5_auto = "read"; gestureObject.ch5_rec = false; gestureObject.ch5_read = true;},
					"2", {ch5_auto = "record"; gestureObject.ch5_rec = true; gestureObject.ch5_read = false;}
				);
			},
			'/' ++ targetPrefix ++ '/source/respectrum/autoAction/selection'
		);
		OSCdef.new(("inBus_auto_" ++ targetPrefix).asSymbol,
			{
				arg msg;

				switch(msg[1].asString,
					"0", {ch6_auto = "off"; gestureObject.ch6_rec = false; gestureObject.ch6_read = false;},
					"1", {ch6_auto = "read"; gestureObject.ch6_rec = false; gestureObject.ch6_read = true;},
					"2", {ch6_auto = "record"; gestureObject.ch6_rec = true; gestureObject.ch6_read = false;}
				);
			},
			'/' ++ targetPrefix ++ '/source/inBus/autoAction/selection'
		);
		OSCdef.new(("azModPat_auto_" ++ targetPrefix).asSymbol,
			{
				arg msg;

				switch(msg[1].asString,
					"0", {ch7_auto = "off"; gestureObject.ch7_rec = false; gestureObject.ch7_read = false;},
					"1", {ch7_auto = "read"; gestureObject.ch7_rec = false; gestureObject.ch7_read = true;},
					"2", {ch7_auto = "record"; gestureObject.ch7_rec = true; gestureObject.ch7_read = false;}
				);
			},
			'/' ++ targetPrefix ++ '/pattern/azModPat/autoAction/selection'
		);
		OSCdef.new(("patternType_auto_" ++ targetPrefix).asSymbol,
			{
				arg msg;

				switch(msg[1].asString,
					"0", {ch8_auto = "off"; gestureObject.ch8_rec = false; gestureObject.ch8_read = false;},
					"1", {ch8_auto = "read"; gestureObject.ch8_rec = false; gestureObject.ch8_read = true;},
					"2", {ch8_auto = "record"; gestureObject.ch8_rec = true; gestureObject.ch8_read = false;}
				);
			},
			'/' ++ targetPrefix ++ '/pattern/type/autoAction/selection'
		);
		OSCdef.new(("outBus_auto_" ++ targetPrefix).asSymbol,
			{
				arg msg;

				switch(msg[1].asString,
					"0", {ch9_auto = "off"; gestureObject.ch9_rec = false; gestureObject.ch9_read = false;},
					"1", {ch9_auto = "read"; gestureObject.ch9_rec = false; gestureObject.ch9_read = true;},
					"2", {ch9_auto = "record"; gestureObject.ch9_rec = true; gestureObject.ch9_read = false;}
				);
			},
			'/' ++ targetPrefix ++ '/pattern/outBus/autoAction/selection'
		);
		OSCdef.new(("panner_auto_" ++ targetPrefix).asSymbol,
			{
				arg msg;

				switch(msg[1].asString,
					"0", {
						   ch10_auto = "off"; gestureObject.ch10_rec = false; gestureObject.ch10_read = false;
						   ch11_auto = "off"; gestureObject.ch11_rec = false; gestureObject.ch11_read = false;
						  },
					"1", {
						   ch10_auto = "read"; gestureObject.ch10_rec = false; gestureObject.ch10_read = true;
						   ch11_auto = "read"; gestureObject.ch11_rec = false; gestureObject.ch11_read = true;
					},
					"2", {
						   ch10_auto = "record"; gestureObject.ch10_rec = true; gestureObject.ch10_read = false;
						   ch11_auto = "record"; gestureObject.ch11_rec = true; gestureObject.ch11_read = false;
					});
			},
			'/' ++ targetPrefix ++ '/pattern/autoAction/selection'
		);
		OSCdef.new(("crossFader_auto_" ++ targetPrefix).asSymbol,
			{
				arg msg;

				switch(msg[1].asString,
					"0", {ch12_auto = "off"; gestureObject.ch12_rec = false; gestureObject.ch12_read = false;},
					"1", {ch12_auto = "read"; gestureObject.ch12_rec = false; gestureObject.ch12_read = true;},
					"2", {ch12_auto = "record"; gestureObject.ch12_rec = true; gestureObject.ch12_read = false;}
				);
			},
			'/' ++ targetPrefix ++ '/loop/crossFader/autoAction/selection'
		);
	}

	// MISC METHODS
	rescalePlayback { // Turns gesture playbackRate controller vals into something useful.
		arg theInVal;
		var theOutVal;

		switch(theInVal.asStringPrec(1),
			"0", {theOutVal = 0.125},
			"0.2", {theOutVal = 0.25},
			"0.4", {theOutVal = 0.5},
			"0.6", {theOutVal = 1},
			"0.8", {theOutVal = 2},
			"1", {theOutVal = 4}
		);

		^theOutVal;
	}

	showInfo {
		arg in; // <-- configObject. keep this arg because it makes this method's code shorter/easier to read
		var string_0 = "", string_1 = "";
		var theMsg = Array.new(2);

		in.isNil.if({^"showInfo needs an 'in' argument. It should be a configObject.".warn});

		("========= vvv" + in.targetPrefix + "vvv ========").postln;
		("hostname:" + in.hostname).postln;
		("port:" + in.port).postln;
		("targetPrefix:" + in.targetPrefix).postln;
		in.type.notNil.if({("type:" + in.type).postln});
		in.durMin.notNil.if({("durMin:" + in.durMin).postln});
		in.durMax.notNil.if({("durMax:" + in.durMax).postln});
		in.attackTimeMin.notNil.if({("attackTimeMin:" + in.attackTimeMin).postln});
		in.attackTimeMax.notNil.if({("attackTimeMax:" + in.attackTimeMax).postln});
		in.releaseTimeMin.notNil.if({("releaseTimeMin:" + in.releaseTimeMin).postln});
		in.releaseTimeMax.notNil.if({("releaseTimeMax:" + in.releaseTimeMax).postln});
		in.gestureFilepath.notNil.if({("gestureFilepath:" + in.gestureFilepath).postln});
		in.gestureLength.notNil.if({("gestureLength:" + in.gestureLength + "seconds").postln});
		in.loopFilepath.notNil.if({("loopFilepath:" + in.loopFilepath).postln});

		in.ampSeqName.notNil.if({("ampSeqName:" + in.ampSeqName).postln});
		in.ampSeqDesc.notNil.if({("ampSeqDesc:" + in.ampSeqDesc).postln});
		in.azModPat_list.notNil.if({("azModPat_list:" + in.azModPat_list).postln});
		in.outBus_list.notNil.if({("outBus_list:" + in.outBus_list).postln});
		in.loopLength.notNil.if({("loopLength:" + in.loopLength + "seconds").postln});
		// ("========= ^^^" + in.targetPrefix + "INFO ^^^ ========").postln;

		// send to header/s on controller
		// in.hostname.notNil.if({string_0 = (string_0 + "HOSTNAME:" + in.hostname).asString});
		in.type.notNil.if({string_0 = (string_0 + "TYPE:" + in.type).asString});
		in.targetPrefix.notNil.if({string_0 = (string_0 + "PREFIX:" + in.targetPrefix).asString});
		in.gestureLength.notNil.if({string_0 = (string_0 + "GESTURE LENGTH:" + in.gestureLength).asString});
		in.loopLength.notNil.if({string_1 = (string_1 + "LOOP LENGTH:" + in.loopLength).asString});

		in.durMin.notNil.if({string_1 = (string_1 + "durMin:" + in.durMin).asString});
		in.durMax.notNil.if({string_1 = (string_1 + "durMax:" + in.durMax).asString});
		in.attackTimeMin.notNil.if({string_1 = (string_1 + "attkMin:" + in.attackTimeMin).asString});
		in.attackTimeMax.notNil.if({string_1 = (string_1 + "attkMax:" + in.attackTimeMax).asString});
		in.releaseTimeMin.notNil.if({string_1 = (string_1 + "relMin:" + in.releaseTimeMin).asString});
		in.releaseTimeMax.notNil.if({string_1 = (string_1 + "relMax:" + in.releaseTimeMax).asString});

		theMsg = [string_0, string_1];
		theController.sendMsg('/' ++ in.targetPrefix ++ '/configMonitor/_0/value', theMsg[0]);
		theController.sendMsg('/' ++ in.targetPrefix ++ '/configMonitor/_1/value', theMsg[1]);
	}
} // end class def

