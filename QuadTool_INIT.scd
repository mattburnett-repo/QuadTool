/*
   QuadTool_INIT.scd
      Instrument configurations/initialization

      *** THIS FILE DOES NOT BELONG IN THE SC COMPILE PATH ***

   COPYRIGHT 2016 Matthew Burnett

NOTES:
======
   "multi" -> multi-panel
   multi_1: two bands, two exAud, xyFixed, xyPulse
   multi_2: five bands
   multi_3: 8 exAud
   multi_4: four exAud, two xyFixed, two xyPulse
   multi_5: misc generators
   multi_6: appears to have gone missing. a file player thing?
   multi_7: xy panning plus loop functionality
   multi_8: four bands, four exAud, four XYloop

TO-DO:
======
   *** Test gesture functionality ***
      Gesture filepath/s (configObject/s)
   Fine-tune config object vals
   Fileplayer/xyPanning instrument

   Test circumstances where FrameAutomator length is different from recorded gesture file length
      Longer and shorter

   The Pan4 channel assigns for outBus 2 and 3 are reversed. Mod 3.7.0 class file like we did for 3.6.6. or figure out another way to
      remap the back two channels.

   Some sort of Lemur playback speed control for SimpleLoop stuff would be nice.

   Look at UGens/Generators
     NTube
     TwoTube
     OteyPiano
     Demand things (Dbrown, etc).
     TGrains (look at 4 channels examples) <-- !!!
        might be fun with Accelerometer and RT recorded audio from iPad
        even better would be getting the iPhone accelerometer to work with this
        'freeze' state and gesture recording
        Also look at GrainIn, Warp1
     Ball
     Spring (last example)
     MarkovSynth

     Tclutch or something like that

     Buffer loadCollection <-- ?

   Look at Buffer.fill(...), especially the sine/cheby things. Alternative to band/s

   Look at PitchShift also. Combine with \speed to maintain pitch when changing playback rate

*/

// Setup
(  // test connection between SC and Lemur device
// ~theIP = "172.16.42.4"; // the IP of the control device
~theIP = "169.254.165.19"; // ad hoc
OSCconnectionTest.new(~theIP);
)

(  // calibration tone/noise for quad output
a = QuadTestTones.new;
a.play;
// cmd + . --> all stop
)

// Instrument/s
(  // multi_1: two bands, two exAud, xyFixed, xyPulse
~theIP = "172.16.42.4"; // the IP of the control device (Lemur iPad)
~instName = "multi_1";
QuadToolBand.new(~theIP, ~instName ++ "/bandPlayer_1", "00", "full");
QuadToolBand.new(~theIP, ~instName ++ "/bandPlayer_2", "03", "full");
QuadToolExternalAudio.new(~theIP, ~instName ++ "/externalAudioPlayer_1", "02");
QuadToolExternalAudio.new(~theIP, ~instName ++ "/externalAudioPlayer_2", "02");
QuadToolXyPanningFixed.new(~theIP, ~instName ++ "/xyPanning_1");
QuadToolXyPanningPulse.new(~theIP, ~instName ++ "/xyPanning_2");
)

(  // multi_2: six bands
// ~theIP = "172.16.42.4"; // the IP of the control device (Lemur iPad)
~theIP = "169.254.88.20"; // ad hoc IP
~instName = "multi_2";
QuadToolBand.new(~theIP, ~instName ++ "/bandPlayer_1", "00", "full");
QuadToolBand.new(~theIP, ~instName ++ "/bandPlayer_2", "01", "full");
QuadToolBand.new(~theIP, ~instName ++ "/bandPlayer_3", "02", "full");
QuadToolBand.new(~theIP, ~instName ++ "/bandPlayer_4", "03", "full");
QuadToolBand.new(~theIP, ~instName ++ "/bandPlayer_5", "04", "full");
QuadToolBand.new(~theIP, ~instName ++ "/bandPlayer_6", "05", "full");
/*QuadToolBand.new(~theIP, ~instName ++ "/bandPlayer_1", "ultraShort", "full");
QuadToolBand.new(~theIP, ~instName ++ "/bandPlayer_2", "veryShort", "full");
QuadToolBand.new(~theIP, ~instName ++ "/bandPlayer_3", "short", "full");
QuadToolBand.new(~theIP, ~instName ++ "/bandPlayer_4", "long", "full");
QuadToolBand.new(~theIP, ~instName ++ "/bandPlayer_5", "veryLong", "full");
QuadToolBand.new(~theIP, ~instName ++ "/bandPlayer_6", "ultraLong", "full");*/
/*QuadToolBand.new(~theIP, ~instName ++ "/bandPlayer_1", "ultraShort", "wide"); // should also use full
QuadToolBand.new(~theIP, ~instName ++ "/bandPlayer_2", "veryShort", "wide");
QuadToolBand.new(~theIP, ~instName ++ "/bandPlayer_3", "short", "wide");
QuadToolBand.new(~theIP, ~instName ++ "/bandPlayer_4", "long", "wide");
QuadToolBand.new(~theIP, ~instName ++ "/bandPlayer_5", "veryLong", "wide");
QuadToolBand.new(~theIP, ~instName ++ "/bandPlayer_6", "ultraLong", "wide");*/
)

(  // multi_3: 8 exAud
~theIP = "172.16.42.4"; // the IP of the control device (Lemur iPad)
~instName = "multi_3";
QuadToolExternalAudio.new(~theIP, ~instName ++ "/externalAudioPlayer_1", "02");
QuadToolExternalAudio.new(~theIP, ~instName ++ "/externalAudioPlayer_2", "01");
QuadToolExternalAudio.new(~theIP, ~instName ++ "/externalAudioPlayer_3", "02");
QuadToolExternalAudio.new(~theIP, ~instName ++ "/externalAudioPlayer_4", "01");
QuadToolExternalAudio.new(~theIP, ~instName ++ "/externalAudioPlayer_5", "02");
QuadToolExternalAudio.new(~theIP, ~instName ++ "/externalAudioPlayer_6", "01");
QuadToolExternalAudio.new(~theIP, ~instName ++ "/externalAudioPlayer_7", "02");
QuadToolExternalAudio.new(~theIP, ~instName ++ "/externalAudioPlayer_8", "01");
)

(  // multi_4: four exAud, two xyFixed, two xyPulse
~theIP = "172.16.42.4"; // the IP of the control device (Lemur iPad)
~instName = "multi_4";
QuadToolExternalAudio.new(~theIP, ~instName ++ "/externalAudioPlayer_1", "02");
QuadToolExternalAudio.new(~theIP, ~instName ++ "/externalAudioPlayer_2", "01");
QuadToolExternalAudio.new(~theIP, ~instName ++ "/externalAudioPlayer_3", "02");
QuadToolExternalAudio.new(~theIP, ~instName ++ "/externalAudioPlayer_4", "01");
QuadToolXyPanningFixed.new(~theIP, ~instName ++ "/xyPanning_1");
QuadToolXyPanningFixed.new(~theIP, ~instName ++ "/xyPanning_2");
QuadToolXyPanningPulse.new(~theIP, ~instName ++ "/xyPanning_3");
QuadToolXyPanningPulse.new(~theIP, ~instName ++ "/xyPanning_4");
)

(  // multi_5: misc generators
~theIP = "172.16.42.4"; // the IP of the control device (Lemur iPad)
~instName = "multi_5";

QuadToolClickGenerator.new(~theIP, ~instName ++ "/synthPlayer_1", "dust");
QuadToolClickGenerator.new(~theIP, ~instName ++ "/synthPlayer_2", "impulse");
~theFile_1 = "saturn_MONO.wav";
QuadToolFilePlayerGenerator.new(~theIP, ~instName ++ "/synthPlayer_3", ~theFile_1);
~theFile_2 = "neptune_MONO.wav";
QuadToolFilePlayerGenerator.new(~theIP, ~instName ++ "/synthPlayer_4", ~theFile_2);
QuadToolNoiseGenerator.new(~theIP, ~instName ++ "/noise_1", "white");
QuadToolFilteredNoiseGenerator.new(~theIP, ~instName ++ "/filteredNoise_1");
QuadToolParabolicGenerator.new(~theIP, ~instName ++ "/synthPlayer_5", 3);
QuadToolParabolicGenerator.new(~theIP, ~instName ++ "/synthPlayer_6", 4);
)

(  // multi_6: file players w/ xyPanning. did we make a Lemur thing for this yet? FIXME
~theIP = "172.16.42.4"; // the IP of the control device (Lemur iPad)
~instName = "multi_6";

~theFile_1 = "jupiter_MONO.wav";
~theFile_2 = "miranda_MONO.wav";
~theFile_3 = "neptune_MONO.wav";
~theFile_4 = "ringsOfUranus_MONO.wav";
~theFile_5 = "saturn_MONO.wav";
~theFile_6 = "saturnsRings_MONO.wav";
~theFile_7 = "songOfEarth_MONO.wav";
~theFile_8 = "sphereOfIo_MONO.wav";
~theFile_9 = "uranus_MONO.wav";
~theFile_10 = "voiceOfEarth_MONO.wav";

QuadToolFilePlayerGenerator.new(~theIP, ~instName ++ "/synthPlayer_1", ~theFile_1);
QuadToolFilePlayerGenerator.new(~theIP, ~instName ++ "/synthPlayer_2", ~theFile_2);
QuadToolFilePlayerGenerator.new(~theIP, ~instName ++ "/synthPlayer_3", ~theFile_3);
QuadToolFilePlayerGenerator.new(~theIP, ~instName ++ "/synthPlayer_4", ~theFile_4);
QuadToolFilePlayerGenerator.new(~theIP, ~instName ++ "/synthPlayer_5", ~theFile_5);
QuadToolFilePlayerGenerator.new(~theIP, ~instName ++ "/synthPlayer_6", ~theFile_6);
QuadToolFilePlayerGenerator.new(~theIP, ~instName ++ "/synthPlayer_7", ~theFile_7);
QuadToolFilePlayerGenerator.new(~theIP, ~instName ++ "/synthPlayer_8", ~theFile_8);
QuadToolFilePlayerGenerator.new(~theIP, ~instName ++ "/synthPlayer_9", ~theFile_9);
QuadToolFilePlayerGenerator.new(~theIP, ~instName ++ "/synthPlayer_10", ~theFile_10);
)

( // multi_7: fixed xy panning with looping
~theIP = "172.16.42.4"; // the IP of the control device (Lemur iPad)
~instName = "multi_7";
~theLength =  4;

QuadToolXyPanningFixedLoop.new(~theIP, ~instName ++ "/XY_1", ~theLength);
QuadToolXyPanningFixedLoop.new(~theIP, ~instName ++ "/XY_2", ~theLength);
QuadToolXyPanningFixedLoop.new(~theIP, ~instName ++ "/XY_3", ~theLength);
QuadToolXyPanningFixedLoop.new(~theIP, ~instName ++ "/XY_4", ~theLength);
)

( // multi_8: four bands, four exAud, four XYloop
// ~theIP = "172.16.42.4"; // the IP of the control device (Lemur iPad)
~theIP = "169.254.165.19"; // ad hoc
~instName = "multi_8";
// ~loopLength = 4; // maybe change to 8 or 10. 10 is gestureLength...
~loopLength = 8;

QuadToolBand.new(~theIP, ~instName ++ "/bandPlayer_1", "00", "full");
QuadToolBand.new(~theIP, ~instName ++ "/bandPlayer_2", "03", "full");
QuadToolBand.new(~theIP, ~instName ++ "/bandPlayer_3", "04", "full");
QuadToolBand.new(~theIP, ~instName ++ "/bandPlayer_4", "06", "full");

QuadToolExternalAudio.new(~theIP, ~instName ++ "/externalAudioPlayer_1", "02");
QuadToolExternalAudio.new(~theIP, ~instName ++ "/externalAudioPlayer_2", "02");
QuadToolExternalAudio.new(~theIP, ~instName ++ "/externalAudioPlayer_3", "02");
QuadToolExternalAudio.new(~theIP, ~instName ++ "/externalAudioPlayer_4", "02");

QuadToolXyPanningFixedLoop.new(~theIP, ~instName ++ "/xyPanning_1", ~loopLength);
QuadToolXyPanningFixedLoop.new(~theIP, ~instName ++ "/xyPanning_2", ~loopLength);
QuadToolXyPanningFixedLoop.new(~theIP, ~instName ++ "/xyPanning_3", ~loopLength);
QuadToolXyPanningFixedLoop.new(~theIP, ~instName ++ "/xyPanning_4", ~loopLength);
)



