/*
   QuadToolToneGeneration.sc
      A class to abstract out of other classes the creation of sound/s
      These are the sub/classes that actually make the sounds.
         A bunch of SynthDefs and some wrapper code for them.

   COPYRIGHT 2016 Matthew Burnett

NOTES:
======
   You can apply azModPats to inBus as well as outBus!

TO-DO:
======
   ToneGeneratorFilteredNoise.setFilter should be rewritten to present same interface as Band.setBand

   Refine Band generator scalingFactor. Right now some bands just seem louder than others.
      Adjust vals depending on bandID. Not sure yet where/how to bolt that on.

*/

QuadToolToneGeneratorBase {
	var theServer, theSynthName;
}

ToneGeneratorSynth : QuadToolToneGeneratorBase { // superclass for synth-based generators.
	getGenerator {
		^Synth.new(theSynthName);
	}
}

ToneGeneratorPdef : QuadToolToneGeneratorBase { // superclass for Pdef-based generators (band, exAud, etc.)
	var thePdefName;

	// accessor methods
	getGenerator {
		^Pdef(thePdefName);
	}
	setDur{
		arg theVal;
		Pbindef(thePdefName, \dur, theVal);
	}
	setOutLevel {
		arg theVal;
		Pbindef(thePdefName, \outLevel, theVal);
	}
	setAttackTime {
		arg theVal;
		Pbindef(thePdefName, \attackTime, theVal);
	}
	setReleaseTime {
		arg theVal;
		Pbindef(thePdefName, \releaseTime, theVal);
	}
	setInBus {
		arg theVal;
		Pbindef(thePdefName, \inBus, theVal);
	}
	setOutBus {
		arg theVal;
		Pbindef(thePdefName, \outBus, theVal);
	}
	// for Pdef version of xyPanning
	setX {
		arg theVal;
		Pbindef(thePdefName, \theX, theVal);
	}
	setY {
		arg theVal;
		Pbindef(thePdefName, \theY, theVal);
	}
} // end Pdef class

ToneGeneratorBand : ToneGeneratorPdef {
	*new {
		arg configObject;
		^super.new.init(configObject);
	}
	init {
		arg configObject;

		theSynthName = ("synth_band_" ++ configObject.targetPrefix).asString;
		thePdefName = ("pdef_band_" ++ configObject.targetPrefix).asSymbol;

		// some comm'd out code for later variBand development
		SynthDef(theSynthName,
			{
				arg band = #[1,2], amps, outBus;

				// var variBand = band * modulator;  modulator source is probably azMod rate, which is \dur param in Pdef;
				var attackTime = Control.names(\attackTime).kr(0.1);
				var releaseTime = Control.names(\releaseTime).kr(0.1);
				var outLevel = Control.names(\outLevel).kr(1.0);
				var scalingFactor = 0.1; // scalingFactor scales the output level relative to 0dB. usually signal is too hot.
				var theLevel = (scalingFactor * outLevel) * AmpCompA.kr(freq: band[0], root: band[0]); // AmpCompA looks at the lowest of the band's frequencies.

				var theTone = DynKlang.ar(`[band, amps]);
				// var theTone = DynKlang.ar(`[variBand, amps]);
				var theOutEnv = EnvGen.kr(Env.perc(attackTime, releaseTime, theLevel, \sin), doneAction: 2);

				Out.ar(outBus, Limiter.ar((theTone * theOutEnv)));
			};
		).add;

		Pdef(thePdefName,
			Pbind(
				\instrument, theSynthName.asSymbol, \dur, configObject.durMax, \outLevel, 0,
				\band, configObject.band[0], \amps, configObject.amps,
				\attackTime, configObject.attackTimeMax, \releaseTime, configObject.releaseTimeMax, \outBus, configObject.outBus
			)
		);
	}
	makeBand { // used by respectrum.
		arg bandRecord;

		var theMin = bandRecord[0][0];
		var theMax = bandRecord[0][1];
		var theMid = (theMax - theMin) / 2;
		var minfreq = rrand(theMin, (theMin + theMid)).asInteger;
		var maxfreq = rrand(theMax, (theMax - theMid)).asInteger;

		var wavecount = rrand(1, bandRecord[2]);
		var freqList = Array.rand(wavecount, minfreq, maxfreq).sort; // randomly create frequencies within band
		var ampList = Array.rand(wavecount, 0.01, 1.0).sort.reverse; // someday replace with basic envelopes.
		var theOutput = Array.new(2);

		// (configObject.targetPrefix + "makeBand:" + freqList + "/" + ampList).postln; // keep this for diagnostics
		^theOutput = [freqList, ampList]; // stuff into an array and return
	}
	// uses makeBand to create a Sequence of band frequency/amplitude arrays.
	respectrum {
		arg theBandRecord;

		var theCount = rrand(theBandRecord[1][0], theBandRecord[1][1]); // determines the "length of the line". shorter makes more shimmer.
		var theList = Array.new(theCount);
		var theFreqList = Array.new(theCount);
		var theAmpList = Array.new(theCount);

		// ("TonGeneratorBand.respectrum.theCount:" + theCount).postln; // keep this for diagnostics
		theCount.do({|i|
			theList.add(this.makeBand(theBandRecord));
			theFreqList.add(theList[i][0]);
			theAmpList.add(theList[i][1]); // change to env/s, using these vals as peak levels FIXME
		});

		Pbindef(thePdefName, \band, Pseq(theFreqList, inf), \amps, Pseq(theAmpList, inf));
	}
	setBand {
		arg theVal;
		Pbindef(thePdefName, \band, theVal);
	}
} // end ToneGeneratorBand

ToneGeneratorExternalAudio : ToneGeneratorPdef {
	*new {
		arg configObject;
		^super.new.init(configObject);
	}
	init {
		arg configObject;

		theSynthName = ("synth_externalAudio_" ++ configObject.targetPrefix).asString;
		thePdefName = ("pdef_externalAudio_" ++ configObject.targetPrefix).asSymbol;

		SynthDef(theSynthName,
			{
				arg inBus, outBus;

				var theAttackTime = Control.names(\attackTime).kr(0.1);
				var theSustainTime = 0.0; // hard-code to zero, otherwise defaults to 1?
				var theReleaseTime = Control.names(\releaseTime).kr(0.1);
				var theOutLevel = Control.names(\outLevel).kr(1.0); // level the outside world sees

				var theEnv = EnvGen.kr(Env.linen(theAttackTime, theSustainTime, theReleaseTime, theOutLevel, \sin), doneAction: 2);
				// var theTone = SoundIn.ar(inBus) * theEnv;
				var theTone = Limiter.ar(SoundIn.ar(inBus), 1.0, 0.01) * theEnv;

				Out.ar(outBus, Limiter.ar(theTone, 1.0, 0.01));
			}
		).add;

		Pdef(thePdefName,
			Pbind(
				\instrument, theSynthName.asSymbol, \dur, configObject.durMax, \inBus, configObject.inBus,
				\attackTime, configObject.attackTimeMax, \releaseTime, configObject.releaseTimeMax, \outLevel, 0,
				\outBus, configObject.outBus
			)
		);
	}
} // end getExternalAudio

ToneGeneratorXYpanningFixed : ToneGeneratorSynth {
	*new {
		arg configObject;
		^super.new.init(configObject);
	}
	init {
		arg configObject;

		theSynthName = ("synth_xyPanningFixed_" ++ configObject.targetPrefix).asString;

		SynthDef.new(theSynthName, {
			arg inBus = configObject.inBus, theX = configObject.theX, theY = configObject.theY;

			var outLevel = Control.names(\outLevel).kr(0.0); // level the outside world sees
			var outBus = configObject.outBus;
			var theTone = Limiter.ar(SoundIn.ar(inBus), 1.0, 0.01);

			Out.ar(outBus, Pan4.ar(theTone, theX, theY, outLevel)); // *** THIS VERSION OF Pan4 (Pan.sc) HAS BEEN MODIFIED ***
		}).add;
	}
} // end getXY fixed
ToneGeneratorXYpanningPulse : ToneGeneratorPdef {
	*new {
		arg configObject;
		^super.new.init(configObject);
	}
	init {
		arg configObject;

		theSynthName = ("synth_xyPanningPulse_" ++ configObject.targetPrefix).asString;
		thePdefName = ("pdef_xyPanningPulse_" ++ configObject.targetPrefix).asString;

		SynthDef.new(theSynthName, {
			arg inBus = configObject.inBus, theX = configObject.theX, theY = configObject.theY;

			var outBus = configObject.outBus;
			var theAttackTime = Control.names(\attackTime).kr(0.1);
			var theSustainTime = 0.0; // hard-code to zero, otherwise defaults to 1?
			var theReleaseTime = Control.names(\releaseTime).kr(0.1);
			var theOutLevel = Control.names(\outLevel).kr(1.0); // level the outside world sees

			var theEnv = EnvGen.kr(Env.linen(theAttackTime, theSustainTime, theReleaseTime, theOutLevel, \sin), doneAction: 2);
			var theTone = Limiter.ar(SoundIn.ar(inBus), 1, 0.01) * theEnv;

			Out.ar(outBus, Pan4.ar(theTone, theX, theY, theOutLevel)); // *** THIS VERSION OF Pan4 HAS BEEN MODIFIED ***
		}).add;
		Pdef(thePdefName,
			Pbind(
				\instrument, theSynthName.asSymbol, \dur, configObject.durMax, \inBus, configObject.inBus,
				\theX, configObject.theX, \theY, configObject.theY,
				\attackTime, configObject.attackTimeMax, \releaseTime, configObject.releaseTimeMax, \outLevel, 0
			)
		);
	}
} // end getXY

ToneGeneratorClick : ToneGeneratorSynth {
	*new {
		arg configObject;
		^super.new.init(configObject);
	}
	init {
		arg configObject;

		theSynthName = ("synth_click_" ++ configObject.type ++ "_" ++ configObject.targetPrefix).asString;

		if(configObject.type == "dust", {
			SynthDef.new(theSynthName, {
				var out = Control.names(\outBus).kr(configObject.outBus);
				var density = Control.names(\dur).kr(10);
				var level = Control.names(\outLevel).kr(0.0);
				Out.ar(out, Dust2.ar(density, level));
			}).add;
		});
		if(configObject.type == "impulse", {
			SynthDef.new(theSynthName, {
				var out = Control.names(\outBus).kr(configObject.outBus);
				var rate = Control.names(\dur).kr(10);
				var level = Control.names(\outLevel).kr(0.0);
				Out.ar(out, Impulse.ar(rate, 0.0, level));
			}).add;
		});
	}
} // end click

ToneGeneratorNoise : ToneGeneratorPdef {
	*new {
		arg configObject;
		^super.new.init(configObject);
	}
	init {
		arg configObject;

		theSynthName = ("synth_noise_" ++ configObject.type ++ "_" ++ configObject.targetPrefix).asString;
		thePdefName = ("pdef_noise_" ++ configObject.type ++ "_" ++ configObject.targetPrefix).asSymbol;


		SynthDef.new(theSynthName, {
			var attackTime = Control.names(\attackTime).kr(0.25);
			var releaseTime = Control.names(\releaseTime).kr(0.25);
			var outBus = Control.names(\outBus).kr(configObject.outBus);
			var outLevel = Control.names(\outLevel).kr(0.0);
			var scalingFactor = 0.2; // scalingFactor scales the output level relative to 0dB. usually signal is too hot.
			var theLevel = (scalingFactor * outLevel);

			var theOutEnv, theOut;
			var theTone;

			if(configObject.type == "white", {theTone = WhiteNoise.ar()});
			if(configObject.type == "gray", {theTone = GrayNoise.ar()});
			if(configObject.type == "pink", {theTone = PinkNoise.ar()});

			theOutEnv = EnvGen.kr(Env.perc(attackTime, releaseTime, theLevel, \sin), doneAction: 2);
			theOut = Limiter.ar((theTone * theOutEnv), 1.0, 0.01);

			Out.ar(outBus, theOut);
		}).add;

		Pdef(thePdefName,
			Pbind(
				\instrument, theSynthName.asSymbol, \dur, configObject.durMax, \outLevel, 0,
				\attackTime, configObject.attackTimeMax, \releaseTime, configObject.releaseTimeMax,
				\outBus, configObject.outBus
			)
		);
	}
} // end noise

ToneGeneratorFilteredNoise : ToneGeneratorPdef { // band-filtered noise. filter gets more rez the higher the freq gets FIXME
	var bandObject;

	*new {
		arg configObject;
		^super.new.init(configObject);
	}
	init {
		arg configObject;

		bandObject = BandFrequencies.new;

		theSynthName = ("synth_filteredNoise_" ++ configObject.targetPrefix).asString;
		thePdefName = ("pdef_filteredNoise_" ++ configObject.targetPrefix).asSymbol;

		SynthDef.new(theSynthName, {
			var centerFreq = Control.names(\centerFreq).kr(0);
			var theRQ = Control.names(\theRQ).kr(0);
			var attackTime = Control.names(\attackTime).kr(0.25);
			var releaseTime = Control.names(\releaseTime).kr(0.25);
			var outBus = Control.names(\outBus).kr(0);
			var theLevel = Control.names(\outLevel).kr(0.0);

			var theTone = BPF.ar(WhiteNoise.ar(), centerFreq, theRQ);
			var theOutEnv = EnvGen.kr(Env.perc(attackTime, releaseTime, theLevel, \sin), doneAction: 2);
			var theOut = Limiter.ar((theTone * theOutEnv), 1.0, 0.01);

			Out.ar(outBus, theOut * AmpCompA.kr(centerFreq));
		}).add;
		Pdef(thePdefName,
			Pbind(
				\instrument, theSynthName.asSymbol, \dur, configObject.durMax, \outLevel, 0,
				\centerFreq, configObject.centerFreq, \theRQ, configObject.theRQ,
				\attackTime, configObject.attackTimeMin, \releaseTime, configObject.releaseTimeMin, \outBus, configObject.outBus
			)
		);
	}
	setFilter { // should be just like Band.setBand FIXME
		arg theVal;
		var centerFreq = bandObject.getBandAsFilterParams(theVal)[1];
		var theRQ = bandObject.getBandAsFilterParams(theVal)[2];

		Pbindef(thePdefName, \centerFreq, centerFreq, \theRQ, theRQ);
	}
} // end filtered noise

// idea from Pousseur's Parabolic Studies
/*
- use the LFPar oscillator for this generator
But here's the original code, for future ref (uses LocalBuf. keep this for code sample):
// get band from BandFrequencies class
var size = band[0][1] - band[0][0];
var array_01 = Array.series(size, band[0][0], 1) ++ Array.series(size, band[0][0], 1).reverse;
theTone = SinOsc.ar(
   Index.kr(
      LocalBuf.newFrom(array_01),
      LFSaw.kr(dur).range(0, array_01.size)
   )
);
*/
ToneGeneratorParabolic : ToneGeneratorSynth {
	var bandObject, band;

	*new {
		arg configObject;
		^super.new.init(configObject);
	}
	init {
		arg configObject;

		bandObject = BandFrequencies.new();
		theSynthName = ("synth_parabolic_" ++ configObject.targetPrefix).asString;
		band = bandObject.getBand(configObject.bandID);

		SynthDef(theSynthName,
			{
				var outBus = Control.names(\outBus).kr(configObject.outBus);
				var dur = Control.names(\dur).kr(1);
				var outLevel = Control.names(\outLevel).kr(0.0);
				var scalingFactor = 0.2; // scalingFactor scales the output level relative to 0dB. usually signal is too hot.
				var theLevel = (scalingFactor * outLevel);
				var theTone = LFPar.ar(LFPar.kr(dur, 0, band[0][0], band[0][1])); // would be fun to respectrum these vals

				Out.ar(outBus, theTone * (theLevel * AmpCompA.kr(band[0][0])));
			};
		).add;
	}
} // end parabolic
ToneGeneratorFilePlayer : ToneGeneratorSynth {
	var theBuffer, theFile;

	*new {
		arg configObject;
		^super.new.init(configObject);
	}
	init {
		arg configObject;

		theServer = Server.default;
		theSynthName = ("synth_fileplayBack_" ++ configObject.targetPrefix).asString;

		theBuffer.free;
		theFile = nil;
		theFile = configObject.filePath ++ configObject.file;
		theBuffer = Buffer.read(theServer, theFile);

		SynthDef.new(theSynthName, {
			var outBus = Control.names(\outBus).kr(configObject.outBus);
			var playbackRate = Control.names(\playbackRate).kr(1);
			var outLevel = Control.names(\outLevel).kr(0.0);

			Out.ar(outBus, (PlayBuf.ar(1, theBuffer.bufnum, playbackRate, loop:1) * outLevel));
		}).add;
	}
} // end fileplayer
