/*
   MiscGeneratorClass.sc
      clicks, noise, etc.
      not sure how useful these are.

   COPYRIGHT 2016 Matthew Burnett

NOTES:
======

TO-DO:
======

*/

/* SYNTH BASED */
// click
QuadToolClickGenerator : QuadToolSynth {
   *new {
		arg controllerAddr, targetPrefix, type;
		^super.new.init(controllerAddr, targetPrefix, type);
	}

	init {
		arg controllerAddr, targetPrefix, type;

		configObject = ClickConfig.new(controllerAddr, targetPrefix, type);
		toneObject = ToneGeneratorClick.new(configObject);

		this.initialize(configObject);
	}
}
// file player
QuadToolFilePlayerGenerator : QuadToolSynth {
	*new {
		arg controllerAddr, targetPrefix, file;
		^super.new.init(controllerAddr, targetPrefix, file);
	}

	init {
		arg controllerAddr, targetPrefix, file;

		// should be able to set filePath here also. FIXME
		configObject = FilePlayerConfig.new(controllerAddr, targetPrefix, file);
		toneObject = ToneGeneratorFilePlayer.new(configObject);

		this.initialize(configObject);
	}
	showFiles {
		configObject.showFiles;
	}
	/*setFilepath {
		arg theVal;
		// do stuff
	}*/
}

// put SimpleLoop here
//    not sure that this is useful for anything, but here it is
//    look at XYpanning stuff for usefulness
QuadToolSimpleLoopGenerator : QuadToolSynth {
	*new {
		arg controllerAddr, targetPrefix, theLength;
		^super.new.init(controllerAddr, targetPrefix, theLength);
	}
	init {
		arg controllerAddr, targetPrefix, theLength;

		configObject = SimpleLoopConfig.new(controllerAddr, targetPrefix, theLength);
		toneObject = SimpleLoop.new(configObject);
		// toneSource???

		this.initialize(configObject);
	}
}

// parabolic. Inspired by Pousseur's Parabolic Studies
QuadToolParabolicGenerator : QuadToolSynth {
   *new {
		arg controllerAddr, targetPrefix, bandID;
		^super.new.init(controllerAddr, targetPrefix, bandID);
	}

	init {
		arg controllerAddr, targetPrefix, bandID;

		configObject = ParabolicConfig.new(controllerAddr, targetPrefix, bandID);
		toneObject = ToneGeneratorParabolic.new(configObject);

		this.initialize(configObject);
	}
}

/* PDEF BASED */
// noise
QuadToolNoiseGenerator : QuadToolPdef { // theType: white, gray, pink
   *new {
		arg controllerAddr, targetPrefix, type;
		^super.new.init(controllerAddr, targetPrefix, type);
	}

	init {
		arg controllerAddr, targetPrefix, type;

		configObject = NoiseConfig.new(controllerAddr, targetPrefix, type);
		toneObject = ToneGeneratorNoise.new(configObject);
		toneSource = toneObject.getGenerator;

		this.initAzModPat;
		this.initialize(configObject);
	}
} // end subclass file

// filtered noise
QuadToolFilteredNoiseGenerator : QuadToolPdef {
	var bandObject, band; // borrows these and some OSC from QuadToolBandClass

   *new {
		arg controllerAddr, targetPrefix;
		^super.new.init(controllerAddr, targetPrefix);
	}

	init {
		arg controllerAddr, targetPrefix;

		bandObject = BandFrequencies.new;
		configObject = FilteredNoiseConfig.new(controllerAddr, targetPrefix);
		toneObject = ToneGeneratorFilteredNoise.new(configObject);
		toneSource = toneObject.getGenerator;
		band = configObject.band;

		this.initAzModPat;
		this.initialize(configObject);
	}
	initSource {
		arg targetPrefix;

		OSCdef.new(("bandSelection_" ++ targetPrefix).asSymbol, // sloppy, but it works. should be setBand, just like BandClass FIXME
			{
				arg msg;
				// .asInteger because Lemur Knob control craps out at 15. Sends a float 15.000000953674
				var theVal = msg[1].asInteger;

				if(gestureObject.ch4_rec, {gestureObject.ch4_val = msg[1]};);
				band = bandObject.getBand(theVal);
				toneObject.setFilter(theVal); // <--

				theController.sendMsg('/' ++ targetPrefix ++ '/source/Monitor0/value', band[0][0]);
				theController.sendMsg('/' ++ targetPrefix ++ '/source/Monitor1/value', band[0][1]);
				theController.sendMsg('/' ++ targetPrefix ++ '/source/Monitor2/value', theVal);
			},
			'/' ++ targetPrefix ++ '/source/bandSelection/x'
		);
	}
	setFilter { // should be just like Band.setBand FIXME
		arg theVal;
		toneObject.setFilter(theVal);
	}
}



