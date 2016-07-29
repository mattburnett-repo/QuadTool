/*
   FrameAutomator.sc
      Implements multi-channel, frame-based automation
      Picks up where Gesture.sc leaves off

   COPYRIGHT 2016 Matthew Burnett

DEPENDENCIES:
=============
   Called by QuadToolBase.sc

TO-DO:
======
   frameRate and framesPerSecond are just reciprocals? Don't need two vars then...
   Convert three args to two (controller and configObect)

NOTES:
======
      Each channel is dedicated to a specific controller
         ch0 = outevel
         ch1 = dur
         ch2 = attackTime
         ch3 = releaseTime
         ch4 = band
         ch5 = respectrum
         ch6 = inBus (externalAudio)
         ch7 = azModPat
         ch8 = patternType
         ch9 = outBus
         ch10 = theX
         ch11 = theY
         ch12 = loop cross fader (xyPanning)

   Some controller element vals are Integers (ie bandSelection, azModPat selection, etc.). However, Buffer wants floats between 0 and 1.
      To work with this, convert the vals in the record phase (* 0.01 for now. use floatScale var) and again on the read phase (* 100. use intScale var)
      There's some kind of odd rounding issue with SC, so use round(theVal).asInteger when returning scaled val

*/

FrameAutomator
{
	classvar numChannels = 13, frameRate = 0.06125, framesPerSecond = 16;
	classvar floatScale = 0.01, intScale = 100;
	var <theController, <theServer, <>theTargetPrefix, <>theFilepath, <theHeaderFormat;
	var <>thePlaybackRate = 1, theTempoClock;
	var <numFrames, theBuffer;

	// flags to enable channel record
	var <>ch0_rec, <>ch1_rec, <>ch2_rec, <>ch3_rec, <>ch4_rec, <>ch5_rec, <>ch6_rec, <>ch7_rec, <>ch8_rec, <>ch9_rec, <>ch10_rec, <>ch11_rec, <>ch12_rec;
	// controller data vals
	var >ch0_val, >ch1_val, >ch2_val, >ch3_val, >ch4_val, >ch5_val, >ch6_val, >ch7_val, >ch8_val, >ch9_val, >ch10_val, >ch11_val, >ch12_val;
	// flags to enable channel read
	var <>ch0_read, <>ch1_read, <>ch2_read, <>ch3_read, <>ch4_read, <>ch5_read, <>ch6_read, <>ch7_read, <>ch8_read, <>ch9_read, <>ch10_read, <>ch11_read, <>ch12_read;
	// channel indices
	var ch0, ch1, ch2, ch3, ch4, ch5, ch6, ch7, ch8, ch9, ch10, ch11, ch12;
	// channel array start index vals. decodes interleaving
	var channel0, channel1, channel2 , channel3, channel4, channel5 , channel6, channel7, channel8, channel9, channel10, channel11, channel12;

	*new {
		arg controller, configObject;
		^super.new.init(controller, configObject);
	}

	init {
		arg controller, configObject;

		theServer = Server.default;
		theController = controller;
		theTargetPrefix = configObject.targetPrefix;
		theHeaderFormat = "aiff";
		numFrames = framesPerSecond * configObject.gestureLength;
		theBuffer = Buffer.alloc(theServer, numFrames, numChannels);  // the core of this object
		theTempoClock = TempoClock.new();
		theFilepath = nil;

		// record flags
		ch0_rec = false;
		ch1_rec = false;
		ch2_rec = false;
		ch3_rec = false;
		ch4_rec = false;
		ch5_rec = false;
		ch6_rec = false;
		ch7_rec = false;
		ch8_rec = false;
		ch9_rec = false;
		ch10_rec = false;
		ch11_rec = false;
		ch12_rec = false;

		// read flags
		ch0_read = false;
		ch1_read = false;
		ch2_read = false;
		ch3_read = false;
		ch4_read = false;
		ch5_read = false;
		ch6_read = false;
		ch7_read = false;
		ch8_read = false;
		ch9_read = false;
		ch10_read = false;
		ch11_read = false;
		ch12_read = false;

		// channel indices
		ch0 = numFrames * 0;
		ch1 = numFrames * 1;
		ch2 = numFrames * 2;
		ch3 = numFrames * 3;
		ch4 = numFrames * 4;
		ch5 = numFrames * 5;
		ch6 = numFrames * 6;
		ch7 = numFrames * 7;
		ch8 = numFrames * 8;
		ch9 = numFrames * 9;
		ch10 = numFrames * 10;
		ch11 = numFrames * 11;
		ch12 = numFrames * 11;

		// channel array start index vals
		channel0 = ch0;
		channel1 = ch1;
		channel2 = ch2;
		channel3 = ch3;
		channel4 = ch4;
		channel5 = ch5;
		channel6 = ch6;
		channel7 = ch7;
		channel8 = ch8;
		channel9 = ch9;
		channel10 = ch10;
		channel11 = ch11;
		channel12 = ch12;

		// controller data vals
		ch0_val = 0.0;
		ch1_val = 0.0;
		ch2_val = 0.0;
		ch3_val = 0.0;
		ch4_val = 0.0;
		ch5_val = 0.0;
		ch6_val = 0.0;
		ch7_val = 0.0;
		ch8_val = 0.0;
		ch9_val = 0.0;
		ch10_val = 0.0;
		ch11_val = 0.0;
		ch12_val = 0.0;
	}

	/* mutator methods */
	getPlaybackRate {
		^thePlaybackRate;
	}

	setPlaybackRate {
		arg theVal;
		thePlaybackRate = theVal;
		theTempoClock.tempo = thePlaybackRate;
	}

	/* FrameAutomator methods */
	// the playback thing. returns a Task to the caller. Task iterates over Gesture list, sending OSC to controller.
	//   the Lemur controller passes-through the OSC to the SuperCollider OSCdef/s via 'OSCreceive' scripts. Other
	//   controllers should do the same.
	// returns Task to caller. Caller controls play/stop/etc in OSCdef
	theGesture {
		^Task.new({
			var theSeries = Array.series(numFrames, 0, 1);
			var theSeq = Pseq(theSeries, inf).asStream;
			var theSeqVal; // this is basically an iterator/index

			while({(theSeqVal = theSeq.next).notNil}, {
				if(theSeqVal == 0,  // reset channel indices if we're at the beginning
					{
						ch0 = numFrames * 0;
						ch1 = numFrames * 1;
						ch2 = numFrames * 2;
						ch3 = numFrames * 3;
						ch4 = numFrames * 4;
						ch5 = numFrames * 5;
						ch6 = numFrames * 6;
						ch7 = numFrames * 7;
						ch8 = numFrames * 8;
						ch9 = numFrames * 9;
						ch10 = numFrames * 10;
						ch11 = numFrames * 11;
						ch12 = numFrames * 12;

						// send loop start messages to controller
						// ("=== " + theTargetPrefix + "loop start ===").postln; // keep this for diagnostics
						theController.sendMsg("/" ++ theTargetPrefix ++ "/configMonitor/loopStartIndicator_L/x", 1); // needs a delay before switching to off. maybe script in Lemur FIXME
						theController.sendMsg("/" ++ theTargetPrefix ++ "/configMonitor/loopStartIndicator_R/x", 1);
				});

				// turn off the 'loop start' indicator. fudged here because it's totally unclear how to make Lemur do this on its own. FIXME
				if(theSeqVal == (10 * this.thePlaybackRate),
					{
						theController.sendMsg("/" ++ theTargetPrefix ++ "/configMonitor/loopStartIndicator_L/x", 0);
						theController.sendMsg("/" ++ theTargetPrefix ++ "/configMonitor/loopStartIndicator_R/x", 0)
				});

				// if channel is armed, record
				if(ch0_rec, {theBuffer.setn(ch0, ch0_val)});
				if(ch1_rec, {theBuffer.setn(ch1, ch1_val)});
				if(ch2_rec, {theBuffer.setn(ch2, ch2_val)});
				if(ch3_rec, {theBuffer.setn(ch3, ch3_val)});
				if(ch4_rec, {theBuffer.setn(ch4, ch4_val * floatScale)}); // convert val from Integer to Float. rounding error for single digit vals. rounds down one unit.
				if(ch5_rec, {theBuffer.setn(ch5, ch5_val * floatScale)});
				if(ch6_rec, {theBuffer.setn(ch6, ch6_val * floatScale)});
				if(ch7_rec, {theBuffer.setn(ch7, ch7_val * floatScale)});
				if(ch8_rec, {theBuffer.setn(ch8, ch8_val)});
				if(ch9_rec, {theBuffer.setn(ch9, ch9_val * floatScale)});
				if(ch10_rec, {theBuffer.setn(ch10, ch10_val)});
				if(ch11_rec, {theBuffer.setn(ch11, ch11_val)});
				if(ch12_rec, {theBuffer.setn(ch12, ch12_val)});

				// send to theController only if current val is different from previous val -> less network traffic.
				//    OSCreceive scripts in controller handle passthrough.
				if(ch0_read, {theBuffer.getn(channel0, numFrames, {|msg| if(this.isDelta(msg, theSeqVal), {theController.sendMsg("/" ++ theTargetPrefix ++ "/params/outLevel/fader/x", msg[theSeqVal])})})});
				if(ch1_read, {theBuffer.getn(channel1, numFrames, {|msg| if(this.isDelta(msg, theSeqVal), {theController.sendMsg("/" ++ theTargetPrefix ++ "/params/dur/fader/x", msg[theSeqVal])})})});
				if(ch2_read, {theBuffer.getn(channel2, numFrames, {|msg| if(this.isDelta(msg, theSeqVal), {theController.sendMsg("/" ++ theTargetPrefix ++ "/params/attackTime/fader/x", msg[theSeqVal])})})});
				if(ch3_read, {theBuffer.getn(channel3, numFrames, {|msg| if(this.isDelta(msg, theSeqVal), {theController.sendMsg("/" ++ theTargetPrefix ++ "/params/releaseTime/fader/x", msg[theSeqVal])})})});
				if(ch4_read, {theBuffer.getn(channel4, numFrames, {|msg| if(this.isDelta(msg, theSeqVal), {theController.sendMsg("/" ++ theTargetPrefix ++ "/source/bandSelection/x", round(msg[theSeqVal] * intScale).asInteger)})})});
				if(ch5_read, {theBuffer.getn(channel5, numFrames, {|msg| if(this.isDelta(msg, theSeqVal), {theController.sendMsg("/" ++ theTargetPrefix ++ "/source/respectrum/x", round(msg[theSeqVal] * intScale).asInteger)})})});
				if(ch6_read, {theBuffer.getn(channel6, numFrames, {|msg| if(this.isDelta(msg, theSeqVal), {theController.sendMsg("/" ++ theTargetPrefix ++ "/source/inBus/selected", round(msg[theSeqVal] * intScale).asInteger)})})});
				if(ch7_read, {theBuffer.getn(channel7, numFrames, {|msg| if(this.isDelta(msg, theSeqVal), {theController.sendMsg("/" ++ theTargetPrefix ++ "/pattern/azModPat/selected", round(msg[theSeqVal] * intScale).asInteger)})})});
				if(ch8_read, {theBuffer.getn(channel8, numFrames, {|msg| if(this.isDelta(msg, theSeqVal), {theController.sendMsg("/" ++ theTargetPrefix ++ "/pattern/type/x", msg[theSeqVal])})})});
				if(ch9_read, {theBuffer.getn(channel9, numFrames, {|msg| if(this.isDelta(msg, theSeqVal), {theController.sendMsg("/" ++ theTargetPrefix ++ "/pattern/outBus/selected", round(msg[theSeqVal] * intScale).asInteger)})})});
				if(ch10_read, {theBuffer.getn(channel10, numFrames, {|msg| if(this.isDelta(msg, theSeqVal), {theController.sendMsg("/" ++ theTargetPrefix ++ "/pattern/panner/x", msg[theSeqVal])})})});
				if(ch11_read, {theBuffer.getn(channel11, numFrames, {|msg| if(this.isDelta(msg, theSeqVal), {theController.sendMsg("/" ++ theTargetPrefix ++ "/pattern/panner/y", msg[theSeqVal])})})});
				if(ch12_read, {theBuffer.getn(channel12, numFrames, {|msg| if(this.isDelta(msg, theSeqVal), {theController.sendMsg("/" ++ theTargetPrefix ++ "/loop/crossFader/x", msg[theSeqVal])})})});

				// increment channel indices
				ch0 = ch0 + 1;
				ch1 = ch1 + 1;
				ch2 = ch2 + 1;
				ch3 = ch3 + 1;
				ch4 = ch4 + 1;
				ch5 = ch5 + 1;
				ch6 = ch6 + 1;
				ch7 = ch7 + 1;
				ch8 = ch8 + 1;
				ch9 = ch9 + 1;
				ch10 = ch10 + 1;
				ch11 = ch11 + 1;
				ch12 = ch12 + 1;

				frameRate.wait;
			}); // end while
		}, theTempoClock); // end Task
	} // end theGesture

	// check if automation channel's current val is different from previous val
	isDelta {
		arg theSequence, theIndex;
		var theVal = false;

		// if not at the beginning, and the current val not equal to the previous one
		if(theIndex != 0 && (theSequence[theIndex] != theSequence[theIndex - 1]), {theVal = true});

		^theVal;
	}
	// Gesture file methods
	writeGesture {
		var theFile = "Gesture_" ++ Date.getDate.stamp ++ ".aiff";
		var theFullFilename = this.theFilepath ++ theFile;

		theBuffer.write(theFullFilename, theHeaderFormat);
		^("Gesture file:" + theFile); // return this as message to SC IDE post window
	}
	readGesture {
		arg theFile;
		var theFullFilename;

		if(theFile.isNil,
			{"FrameAutomator.readGesture requires a filename".warn},
			{
				theFullFilename = this.theFilepath ++ theFile;
				theBuffer.allocRead(theFullFilename);
			}
		);
	}
	// display all
	dumpGestureFilePaths {
	   var thePath, folder;
		var theFullFilename = this.theFilepath;

	   if(thePath.size == 0,
		   {"FrameAutomator.dumpGestureFilePaths needs a filepath.".warn;},
	      {
				folder = PathName(thePath);
				folder.filesDo(
		         {
		            arg item, i;
		            [i, item].postln;
               });
	      });
   }
	// return list of filepath/filenames. get individual filename/s with... yourVar[x].fileName in caller
	gestureFilePaths {
		var thePath, folder;
		thePath = this.theFilepath;

	   if(thePath.size == 0,
		   {"FrameAutomator.gestureFilePaths needs a filepath.".warn;},
			{
				folder = PathName(thePath);
				^folder.files;
	      });
	}
	// display all
	dumpGestureFileNames {
	   var thePath, folder;
		thePath = this.theFilepath;

	   if(thePath.size == 0,
		   {"FrameAutomator.dumpGestureFileNames needs a filepath.".warn;},
	      {
				folder = PathName(thePath);
			   folder.filesDo(
		         {
		            arg item, i;
		            [i, item.fileName].postln;
               });
	      });
	}
	// return list of filenames
	gestureFileNames {
	   var thePath, folder, fileList;
		thePath = this.theFilepath;
		fileList = List.new();

	   if(thePath.size == 0,
		   {"FrameAutomator.gestureFileNames needs a filepath.".warn;},
	      {
				folder = PathName(thePath);
				folder.filesDo(
		         {
		            arg item, i;
						fileList.add([i, item.fileName]);
               });
	      });
		^fileList;
	}
} // end class def
