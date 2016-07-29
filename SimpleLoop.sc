/*
   SimpleLoop.sc
      Pass theBuffer around to different Synths.
         Buffers are globally available per help file.
         Pass this.theBufnum around instead.
      Designed to work in XYpanning context

   COPYRIGHT 2016 Matthew Burnett

TO-DO:
======
Maybe tie loop playback speed to Gesture speed?
   Maybe not. Still like the idea of varying the playback speed, though.

Add trigger to playback Synth PlayBuf?
Or one-shot functionality (just turn theLoop -> loop: val to 0)

*/

SimpleLoop : ToneGeneratorSynth {
	var theBuffer, <theBufnum, <>loopFilepath;

	*new {
		arg configObject;
		^super.new.init(configObject);
	}

	init {
		arg configObject;

		theSynthName = ("SimpleLoop_" ++ configObject.targetPrefix).asString; // from ToneGeneratorSynth
		loopFilepath = configObject.loopFilepath;

		theServer = Server.default; // should use QuadToolBase class var instead but can't get to it
	   theBuffer = Buffer.alloc(theServer, theServer.sampleRate * configObject.loopLength, 1); // this is the center of it all
		theBufnum = theBuffer.bufnum; // pass this around to reference theBuffer

		SynthDef(theSynthName, {
			arg inBus=0, outBus=0, speed=1, record=0, crossFade=0, theX=configObject.theX, theY=configObject.theY, outLevel=0;

			var theLoop = PlayBuf.ar(1, this.theBufnum, speed * BufRateScale.kr(this.theBufnum), loop: 1);
			var toneSource = Limiter.ar(SoundIn.ar(inBus));
			var theOutput = XFade2.ar(theLoop, toneSource, crossFade);

			RecordBuf.ar(toneSource, this.theBufnum, recLevel: 1.0, preLevel: 1.0, run: record, loop: 1);

			Out.ar(outBus, Pan4.ar(theOutput, theX, theY, outLevel));
		}).add;
	}

	clear {
		theBuffer.zero; // clear buffer while retaining allocation (I hope)
	}

	// convenience method. ToneGeneratorSynth.getGenerator() handles 'synth getting' in the QT class
	getLoop {
		arg inBus=0, outBus=0;
		^Synth.new(this.theSynthName, [\inBus, inBus, \outBus, outBus]);
	}

	writeLoopFile {
		var theFile;

		if(this.loopFilepath.size == 0,
			{"SimpleLoop.writeLoopFile needs a filepath.".warn;},
	      {
				theFile = this.loopFilepath +/+ "loop_" ++ Date.localtime.stamp ++ ".wav";
				theBuffer.write(theFile, headerFormat: "wav");
	      });
	}

	readLoopFile{ // starts playing as soon as it's done loading.
		arg theVal = "loop_default.wav";
		var theFullFilename = this.loopFilepath ++ theVal;

		theBuffer.allocRead(theServer, theFullFilename); // might be fun to do a completionMessage here. look at helpfile.
		theBuffer.normalize;
	}

	loopFileNames { // return list of filenames
	   var folder, fileList;
		fileList = List.new();

	   if(this.loopFilepath.size == 0,
		   {"SimpleLoop.loopFileNames needs a filepath.".warn;},
	      {
				folder = PathName(this.loopFilepath);
				folder.filesDo(
		         {
		            arg item, i;
						fileList.add([i, item.fileName]);
               });
	      });
		^fileList;
	} // end method
} // end class file
