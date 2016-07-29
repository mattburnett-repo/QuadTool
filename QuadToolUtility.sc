/*
   QuadToolUtility.sc
      Miscellaneous methods

   COPYRIGHT 2016 Matthew Burnett

TO-DO:
======

*/

QuadToolUtility
{
	*new {
		^super.new.init();
	}

	init {
		^this;
	}
	// MISCELLANEOUS METHODS
	// utility function to get duration value (x.xxxxx) from a frequency (Hz)
	durFromFreq {
		arg theFreq;
		var theDur;
		^theDur = ((60 / theFreq) / 60);
	}

	// utility function to get a frequency (Hz) from a duration value (x.xxxxx)
	freqFromDur {
		arg theDur;
		var theFreq;
		^theFreq = (60 / (60 * theDur));
	}
} // end class file