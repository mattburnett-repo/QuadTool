/*
QuadToolExternalAudioClass.sc

   COPYRIGHT 2016 Matthew Burnett

NOTES:
======

*/

QuadToolExternalAudio : QuadToolPdef {
	*new {
		arg controllerAddr, targetPrefix, type;
		^super.new.init(controllerAddr, targetPrefix, type);
	}
	init {
		arg controllerAddr, targetPrefix, type;

		configObject = ExternalAudioConfig.new(controllerAddr, targetPrefix, type);
		toneObject = ToneGeneratorExternalAudio.new(configObject);
		// toneSource = toneObject.getGenerator; // ??? FIXME

		this.initAzModPat;
		this.initialize(configObject);
	}

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
} // end subclass file