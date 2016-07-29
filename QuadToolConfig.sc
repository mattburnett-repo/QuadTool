/*
   QuadToolConfig.sc

   COPYRIGHT 2016 Matthew Burnett

NOTES:
======
   There are durFromFreq and freqFromDur conversion methods in QuadToolUtility
      ~asdf = QuadToolUtility.new;
      ~asdf.durFromFreq(120);
      ~asdf.freqFromDur(1.5);

   Each gestureFilepath should point to a different folder.
      Each 'instrument' should have its own gesture folder, since the gestures aren't really 'translatable' between instrument types
      For now, partition according to type band/exAud/XY, etc.
   durRange/s for ExAud probably don't match those for Band, but there is likely some overlap around 05 or 06. need to test this.

   Room size/type (ie. large/small, reverberant/dry). This should affect range of param vals (shorter for reverby, longer for dry, etc).

TO-DO:
======
   Sort attackTimeMin/Max/s for each durRange

   Come up with meaningful names for band/exAud durRange vals

*/

QuadToolConfig { // inherits 'new' from Object
	classvar lemurDefaultPort = 8000; // 8000 is the default Lemur receive port. Set here, assign to all the instances. Use 57120 for localhost.
	classvar frameAutomatorLength = 10; // override in subclasses via gestureLength var. different instrument types need different lengths.
	classvar rootPath = "/Volumes/Micronet HD/project/Berlin/quadTool/version_01.1/files/"; // default/dev

	// declare vars here regardless of whether each configObject uses them. this makes QuadToolBase.showInfo() simpler/work better.
	var <hostname, <port, <targetPrefix, <type;
	var <gestureFilepath, <gestureLength;
	var <>loopFilepath, <>filePath, <file;
	var <band;
	var <dur, <durMin, <durMax, <attackTimeMin, <attackTimeMax, <releaseTimeMin, <releaseTimeMax;
	var <inBus = 0, <outBus = 0;
	var <ampSeqName, <ampSeqDesc, <azModPat_list, <outBus_list, <loopLength, <theX, <theY;
	var <playbackMin, <playbackMax;
}

BandConfig : QuadToolConfig {
	var <amps, <bandtype, <bandIDmax;

	*new {
		arg theHostname, theTargetPrefix, durRange, bandType;
		^super.new.init(theHostname, theTargetPrefix, durRange, bandType);
	}
	init {
		arg theHostname, theTargetPrefix, durRange, bandType;

		hostname = theHostname;
		port = lemurDefaultPort;
		gestureFilepath = rootPath ++ "gesture/band/";
		targetPrefix = theTargetPrefix;
		type = durRange;
		bandtype = bandType;

		if(bandtype == "full", {bandIDmax = 27});
		if(bandtype == "wide", {bandIDmax = 7});

		// some defaults
		band = [[120,320], [1, 12], 3]; // bandID 3 (Iii)
		amps = [1, 0.5];

		/* line between (binaural) synthesis and panning is around 0.075? */
		if(durRange == "00", {
			gestureLength = frameAutomatorLength;
			durMin = 0.004;  // 250 Hz. Smaller than this causes SC to overflow/crash
			durMax = 0.008;  // 120 Hz
			attackTimeMin = 0.002;
			attackTimeMax = 0.05;
			releaseTimeMin = 0.002;
			releaseTimeMax = 0.5;
		});
		if(durRange == "01", {
			gestureLength = frameAutomatorLength;
			durMin = 0.008;  // 120 Hz
			durMax = 0.0125; // 80 Hz
			attackTimeMin = 0.01;
			attackTimeMax = 0.05;
			releaseTimeMin = 0.1;
			releaseTimeMax = 0.5;
		});
		if(durRange == "02", {
			gestureLength = frameAutomatorLength;
			durMin = 0.0125;  // 80 Hz
			durMax = 0.025;   // 40 Hz
			attackTimeMin = 0.01;
			attackTimeMax = 0.05;
			releaseTimeMin = 0.1;
			releaseTimeMax = 0.5;
		});
		if(durRange == "03", {
			gestureLength = frameAutomatorLength;
			durMin = 0.025; // 40 Hz
			durMax = 0.05;  // 20 Hz
			attackTimeMin = 0.01;
			attackTimeMax = 0.022;
			releaseTimeMin = 0.1;
			releaseTimeMax = 0.22;
		});
		if(durRange == "04", { // border between synthesis and panning? TEST
			gestureLength = frameAutomatorLength;
			durMin = 0.05;  // 20 Hz
			durMax = 0.083; // 12 Hz
			attackTimeMin = 0.01;
			attackTimeMax = 0.0375;
			releaseTimeMin = 0.1;
			releaseTimeMax = 0.15;
		});
		if(durRange == "05", {
			gestureLength = frameAutomatorLength;
			durMin = 0.083; // 12 Hz
			durMax = 0.16;  // 6 Hz
			attackTimeMin = 0.01;
			attackTimeMax = 0.075;
			releaseTimeMin = 0.1;
			releaseTimeMax = 0.15;
		});
		if(durRange == "06", {
			gestureLength = frameAutomatorLength;
			durMin = 0.16;  // 6 Hz
			durMax = 0.5;   // 2 Hz
			attackTimeMin = 0.01;
			attackTimeMax = 0.25;
			releaseTimeMin = 0.1;
			releaseTimeMax = 0.25;
		});
		if(durRange == "07", {
			gestureLength = frameAutomatorLength;
			durMin = 0.5;   // 2 Hz
			durMax = 1.5;   // 1.5 seconds
			attackTimeMin = 0.01;
			attackTimeMax = 0.75;
			releaseTimeMin = 0.1;
			releaseTimeMax = 0.75;
		});
	}
} // end BandConfig

ExternalAudioConfig : QuadToolConfig {
	*new {
		arg theHostname, theTargetPrefix, durRange;
		^super.new.init(theHostname, theTargetPrefix, durRange);
	}
	init {
		arg theHostname, theTargetPrefix, durRange;

		hostname = theHostname;
		port = lemurDefaultPort;
		gestureFilepath = rootPath ++ "gesture/externalAudio/";
		targetPrefix = theTargetPrefix;
		type = durRange;
		inBus = 0;

		if(durRange == "00", { // border between synthesis and panning? TEST
			gestureLength = frameAutomatorLength;
			durMin = 0.05;  // 20 Hz
			durMax = 0.083; // 12 Hz
			attackTimeMin = 0.01;
			attackTimeMax = 0.0375;
			releaseTimeMin = 0.1;
			releaseTimeMax = 0.15;
		});
		if(durRange == "01", {
			gestureLength = frameAutomatorLength;
			durMin = 0.083; // 12 Hz
			durMax = 0.16;  // 6 Hz
			attackTimeMin = 0.01;
			attackTimeMax = 0.075;
			releaseTimeMin = 0.1;
			releaseTimeMax = 0.15;
		});
		if(durRange == "02", {
			gestureLength = frameAutomatorLength;
			durMin = 0.16;  // 6 Hz
			durMax = 0.5;   // 2 Hz
			attackTimeMin = 0.01;
			attackTimeMax = 0.25;
			releaseTimeMin = 0.1;
			releaseTimeMax = 0.25;
		});
		if(durRange == "03", {
			gestureLength = frameAutomatorLength;
			durMin = 0.5;   // 2 Hz
			durMax = 1.5;   // 1.5 seconds
			attackTimeMin = 0.01;
			attackTimeMax = 0.75;
			releaseTimeMin = 0.1;
			releaseTimeMax = 0.75;
		});
		if(durRange == "04", {
			gestureLength = frameAutomatorLength;
			durMin = 1.5;
			durMax = 3;
			attackTimeMin = 0.01;
			attackTimeMax = 0.75;
			releaseTimeMin = 0.1;
			releaseTimeMax = 0.75;
		});
		if(durRange == "05", {
			gestureLength = frameAutomatorLength;
			durMin = 3;
			durMax = 8;
			attackTimeMin = 0.01;
			attackTimeMax = 0.75;
			releaseTimeMin = 0.1;
			releaseTimeMax = 0.75;
		});
		// does it make sense to go any longer than this?
	}
} // ExternalAudioConfig

XYpanningLoopConfig : QuadToolConfig {
	*new {
		arg theHostname, theTargetPrefix;
		^super.new.init(theHostname, theTargetPrefix);
	}
	init {
		arg theHostname, theTargetPrefix;

		hostname = theHostname;
		port = lemurDefaultPort;
		loopFilepath = rootPath ++ "audio/loop/xyPanning/";
		loopLength = 4;
		inBus = 0;
		theX = 0;
		theY = 0;
		gestureFilepath = rootPath ++ "gesture/xyPanning/";
		gestureLength = frameAutomatorLength;
		targetPrefix = theTargetPrefix;
	}
}

XYpanningFixedConfig : QuadToolConfig {
	*new {
		arg theHostname, theTargetPrefix;
		^super.new.init(theHostname, theTargetPrefix);
	}
	init {
		arg theHostname, theTargetPrefix;

		hostname = theHostname;
		port = lemurDefaultPort;
		type = "fixed";
		inBus = 0;
		theX = 0;
		theY = 0;
		gestureFilepath = rootPath ++ "gesture/xyPanning/";
		gestureLength = frameAutomatorLength;
		targetPrefix = theTargetPrefix;
	}
} // XYpanningFixed

XYpanningPulseConfig : QuadToolConfig {
	*new {
		arg theHostname, theTargetPrefix;
		^super.new.init(theHostname, theTargetPrefix);
	}
	init {
		arg theHostname, theTargetPrefix;

		hostname = theHostname;
		port = lemurDefaultPort;
		targetPrefix = theTargetPrefix;
		type = "pulse";
		inBus = 0;
		theX = 0;
		theY = 0;
		gestureFilepath = rootPath ++ "gesture/xyPanning/";
		gestureLength = frameAutomatorLength;

		durMin = 0.125;
		durMax = 1.5;
		attackTimeMin = 0.01;
		attackTimeMax = 0.75;
		releaseTimeMin = 0.098;
		releaseTimeMax = 0.75;
	}
} // XYpanningPulse

ClickConfig : QuadToolConfig {
	*new {
		arg theHostname, theTargetPrefix, theType;
		^super.new.init(theHostname, theTargetPrefix, theType);
	}
	init {
		arg theHostname, theTargetPrefix, theType;

		hostname = theHostname;
		port = lemurDefaultPort;
		targetPrefix = theTargetPrefix;
		type = theType;
		gestureFilepath = rootPath ++ "gesture/misc/";
		gestureLength = frameAutomatorLength;

		if(type == "dust", {
			durMin = 1;
			durMax = 2500;
		});
		if(type == "impulse", {
			durMin = 1;
			durMax = 40;
		});
	}
}

NoiseConfig : QuadToolConfig {
	*new {
		arg theHostname, theTargetPrefix, theType;
		^super.new.init(theHostname, theTargetPrefix, theType);
	}
	init {
		arg theHostname, theTargetPrefix, theType;

		hostname = theHostname;
		port = lemurDefaultPort;
		type = theType;
		gestureFilepath = rootPath ++ "gesture/misc/";
		durMin = 0.06125;
		durMax = 1.5;
		attackTimeMin = 0.01;
		attackTimeMax = 0.75;
		releaseTimeMin = 0.098;
		releaseTimeMax = 0.75;
		gestureLength = frameAutomatorLength;
		targetPrefix = theTargetPrefix;
	}
}

FilteredNoiseConfig : QuadToolConfig {
	var <centerFreq, <theRQ;

	*new {
		arg theHostname, theTargetPrefix;
		^super.new.init(theHostname, theTargetPrefix);
	}
	init {
		arg theHostname, theTargetPrefix;

		hostname = theHostname;
		port = lemurDefaultPort;
		targetPrefix = theTargetPrefix;
		type = "filteredNoise";
		band = [[120,320], [1, 12], 3];

		gestureFilepath = rootPath ++ "gesture/misc/";
		durMin = 0.01;
		durMax = 1.5;
		attackTimeMin = 0.01;
		attackTimeMax = 0.75;
		releaseTimeMin = 0.098;
		releaseTimeMax = 0.75;
		centerFreq = 875;          // band 4
		theRQ = 0.17142857142857;
		gestureLength = frameAutomatorLength;
	}
}

ParabolicConfig : QuadToolConfig {
	var <bandID;

	*new {
		arg theHostname, theTargetPrefix, theBandID;
		^super.new.init(theHostname, theTargetPrefix, theBandID);
	}
	init {
		arg theHostname, theTargetPrefix, theBandID;

		hostname = theHostname;
		port = lemurDefaultPort;
		type = "parabolic";
		bandID = theBandID;
		durMin = 1.0;
		durMax = 20;
		gestureFilepath = rootPath ++ "gesture/misc/";
		gestureLength = frameAutomatorLength;
		targetPrefix = theTargetPrefix;
	}
}

FilePlayerConfig : QuadToolConfig { // for starters, use the mono NASA file/s
	*new {
		arg theHostname, theTargetPrefix, theFile;
		^super.new.init(theHostname, theTargetPrefix, theFile);
	}
	init {
		arg theHostname, theTargetPrefix, theFile;

		hostname = theHostname;
		port = lemurDefaultPort;
		targetPrefix = theTargetPrefix;
		type = "filePlayer";
		playbackMin = 0.125;
		playbackMax = 4.0;
		filePath =  "/Volumes/Micronet HD/project/Berlin/webmixes/NASA/plasmaWaveAntenna/individualTracks_MONO/";
		file = theFile;
		gestureFilepath = rootPath ++ "gesture/misc/";
		gestureLength = frameAutomatorLength;
	}
	showFiles { // display filePath's files in SC post window
	   var folder, fileList;
		fileList = List.new();

	   if(this.filePath.size == 0,
		   {"FilePlayerConfig.showFiles needs a filePath.".warn;},
	      {
				folder = PathName(this.filePath);
			   folder.filesDo(
		         {
		            arg item, i;
		            [i, item.fileName].postln;
               });
	      });
	}
}

TGrainsConfig : QuadToolConfig {
	*new {
		arg theHostname, theTargetPrefix, theFile;
		^super.new.init(theHostname, theTargetPrefix, theFile);
	}
	init {
		arg theHostname, theTargetPrefix, theFile;

		hostname = theHostname;
		port = lemurDefaultPort;
		theX = 0;
		theY = 0;
		filePath = Platform.resourceDir; // has the SC test audio, ie 'columbia...' files
		file = theFile;
		gestureFilepath = rootPath ++ "gesture/tGrains/"; // FIXME
		gestureLength = frameAutomatorLength;
		targetPrefix = theTargetPrefix;
	}
}

SimpleLoopConfig : QuadToolConfig {
	*new {
		arg theHostname, theTargetPrefix, theLength;
		^super.new.init(theHostname, theTargetPrefix, theLength);
	}
	init {
		arg theHostname, theTargetPrefix, theLength;

		hostname = theHostname;
		port = lemurDefaultPort;
		targetPrefix = theTargetPrefix;
		loopLength = theLength;
		theX = 0;
		theY = 0;
		loopFilepath = rootPath ++ "audio/loop/xyPanning/";
		playbackMin = 0.125;
		playbackMax = 4.0;
		gestureFilepath = rootPath ++ "gesture/xyPanningLoop/";
		gestureLength = frameAutomatorLength;
	}
}