/*
   QuadTestTones.sc

   COPYRIGHT 2016 Matthew Burnett

NOTES:
======
   Current outlevels are 0.375. SC server meter reads dBFS, not dB; 0.375 gets close enough to 0dB.

*/

QuadTestTones
{
	var theServer;

	*new {
		^super.new.init();
	}

	init {
		theServer = Server.default;

		SynthDef("testTone_WhiteNoise", { arg theOutBus=0;
			Out.ar(theOutBus, WhiteNoise.ar(0.375));
		}).add;

		SynthDef("testTone_1k_Sinewave", { arg theOutBus;
			Out.ar(theOutBus, SinOsc.ar(1000, 0, 0.375));
		}).add;

		^Task.new({
			var theVal, theSeq;
			var theTone, theWaitTime = 5;

			theSeq = Pseq([0,1,2,3], inf).asStream;

			theServer.volume = 0;

			"testTones_Task begins.".postln;

			while {(theVal = theSeq.next).notNil}
			{
				("outbus: " + theVal).postln;
				"1k sine".postln;
				theTone = Synth("testTone_1k_Sinewave", ["theOutBus", theVal]);
				theWaitTime.wait;
				theTone.free;

				"White noise".postln;
				theTone = Synth("testTone_WhiteNoise", ["theOutBus", theVal]);
				theWaitTime.wait;
				theTone.free;
			};	// end while theVal
		});
	}
} // end classfile
