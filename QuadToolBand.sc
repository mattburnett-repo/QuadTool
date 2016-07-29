/*
QuadToolBandClass.sc

   COPYRIGHT 2016 Matthew Burnett

NOTES:
======

TO-DO:
======

*/

QuadToolBand : QuadToolPdef {
	var bandObject, band;

	*new {
		arg controllerAddr, targetPrefix, type, bandType;
		^super.new.init(controllerAddr, targetPrefix, type, bandType);
	}

	init {
		arg controllerAddr, targetPrefix, type, bandType;

		configObject = BandConfig.new(controllerAddr, targetPrefix, type, bandType);
		bandObject = BandFrequencies.new;
		band = configObject.band;

		toneObject = ToneGeneratorBand.new(configObject);
		toneSource = toneObject.getGenerator;

		this.initAzModPat;
		this.initialize(configObject);
	}

	initSource {
		arg targetPrefix;

		// sourceSelect
		OSCdef.new(("sourceSelect_" ++ targetPrefix).asSymbol,
			{
				arg msg;
				// theVal needs out-of-bounds errorhandling or rescale control (full goes to 27, wideBand goes to 7) FIXME
				// .asInteger because Lemur Knob control craps out at 15. Sends a float 15.000000953674
				var theVal = msg[1].asInteger;

				if(gestureObject.ch4_rec, {gestureObject.ch4_val = msg[1]};);

				if(configObject.bandtype == "full", {band = bandObject.getBand(theVal)});
				if(configObject.bandtype == "wide", {band = bandObject.getWideBand(theVal)});

				toneObject.setBand(band[0]);

				theController.sendMsg('/' ++ targetPrefix ++ '/source/Monitor0/value', band[0][0]);
				theController.sendMsg('/' ++ targetPrefix ++ '/source/Monitor1/value', band[0][1]);
				theController.sendMsg('/' ++ targetPrefix ++ '/source/Monitor2/value', theVal);
			},
			'/' ++ targetPrefix ++ '/source/bandSelection/x'
		);
		// respectrum
		OSCdef.new(("respectrum_" ++ targetPrefix).asSymbol,
			{
				arg msg;

				if(gestureObject.ch5_rec, {gestureObject.ch5_val = msg[1]});
				if(msg[1] == 1, {toneObject.respectrum(band)});
			},
			'/' ++ targetPrefix ++ '/source/respectrum/x'
		);
	}
} // end subclass file