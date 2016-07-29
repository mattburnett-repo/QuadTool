/*
   BandFrequencies.sc
      Class file for band frequencies
      Also returns BPF-friendly data for using bands as filters/filters as bands

   COPYRIGHT 2016 Matthew Burnett

NOTES:
======
Still looking for 'good' val/s for respectrum.count var (respectrumMaxVal)
   12 seems ok; provides minimal Delta, particularly in upper bands

A band/wideBand record: band.add(\index -> [[lowerFreq, upperFreq], [lowerCount, upperCount], waveCountMax);

TO-DO:
======
Tweak respectrum, wave count vals for each band
I think if you force a value of 1 or 2 into theCount you can make a shimmer band. Try it.
   intercept theBandRecord and change record[1][0] = 1, record[1][1] = 2 before passing it into this method

*/

BandFrequencies
{
	var <band, <wideBand;

	*new {
		^super.new.init();
	}

	init {
		band = Dictionary.new;
		wideBand = Dictionary.new;

		// original, baseline band division
		band.add(\0 -> [[20,40], [1,8], 3]);            // 0i
		band.add(\1 -> [[40,80], [1,8], 3]);            // 0ii
		band.add(\2 -> [[80,120], [1,12], 3]);          // Ii
		band.add(\3 -> [[120,320], [1,12], 3]);         // Iii
		band.add(\4 -> [[320,485], [1,24], 5]);         // IIi
		band.add(\5 -> [[485,650], [1,24], 5]);         // IIii
		band.add(\6 -> [[650,800], [1,24], 5]);         // IIiii
		band.add(\7 -> [[800,950], [1,24], 5]);         // IIiv
		band.add(\8 -> [[950,1100], [1,24], 8]);        // IIv
		band.add(\9 -> [[1100,1250], [1,24], 8]);       // IIvi
		band.add(\10 -> [[1250,1400], [1,24], 8]);      // IIvii
		band.add(\11 -> [[1400,1550], [1,24], 8]);      // IIviii
		band.add(\12 -> [[1550,1643.75], [1,36], 5]);   // IIIi
		band.add(\13 -> [[1643.75,1737.5], [1,36], 5]); // IIIii   <-- this starts the 'zone' at 1700
		band.add(\14 -> [[1737.5,1856.25], [1,36], 3]); // IIIiii
		band.add(\15 -> [[1856.25,1975], [1,36], 3]);   // IIIiv
		band.add(\16 -> [[1975,2093.75], [1,36], 3]);   // IIIv
		band.add(\17 -> [[2093.75,2212.5], [1,36], 3]); // IIIvi   <-- this starts the most sensitive band
		band.add(\18 -> [[2212.5,2356.25], [1,36], 3]); // IIIvii  <-- this ends the most sensitive band
		band.add(\19 -> [[2356.25,2500], [1,36], 3]);   // IIIviii <-- this ends the 'zone' at 2500
		band.add(\20 -> [[2500,2812.5], [1,48], 2]);    // IVi
		band.add(\21 -> [[2812.5,3125], [1,48], 2]);    // IVii
		band.add(\22 -> [[3125,3487.5], [1,48], 2]);    // IViii
		band.add(\23 -> [[3487.5,3850], [1,48], 2]);    // IViv
		band.add(\24 -> [[3850,4112.5], [1,48], 2]);    // IVv
		band.add(\25 -> [[4112.5,4375], [1,48], 2]);    // IVvi
		band.add(\26 -> [[4375,4687.5], [1,48], 2]);    // IVvii
		band.add(\27 -> [[4687.5,5000], [1,48], 2]);    // IVviii

		//    has nice gamelan sound
		wideBand.add(\0 -> [[20,80], [4,12], 3]);       // 0
		wideBand.add(\1 -> [[80,120], [4,12], 3]);      // I
		wideBand.add(\2 -> [[120,320], [6,24], 3]);     // II
		wideBand.add(\3 -> [[320,640], [8,32], 5]);     // III starts getting ganky here
		wideBand.add(\4 -> [[640,1280], [8,32], 5]);    // IV
		wideBand.add(\5 -> [[1280,1700], [16,32], 5]);  // V
		wideBand.add(\6 -> [[1700,2500], [16,32], 5]);  // VI
		wideBand.add(\7 -> [[2500,5000], [16,32], 5]);  // VII
		wideBand.add(\8 -> [[5000,10000], [16,48], 5]); // VIII
	}

	// ACCESSOR METHODS
	getBands {
		^band;
	}
	getWideBands {
		^wideBand;
	}
	getBand {
		arg theID;
		var theVal = theID.asSymbol;

		^band[theVal];
	}
	getWideBand {
		arg theID;
		var theVal = theID.asSymbol;

		^wideBand[theVal];
	}
	getBandAsFilterParams { // for use with BPF
		arg theID;
		var theBand = this.getBand(theID);
		var theBandwidth = theBand[0][1] - theBand[0][0];
		var theMid = (theBandwidth / 2) + theBand[0][0];
		var theRQ = theBandwidth / theBand[0][1]; // not sure if this is accurate enough. is 'cutoff frequency' theMid or theBand[1]? FIXME
		// var theRQ = theBandwidth / theMid;
		var theOut = [theBand[0], theMid, theRQ];

		^theOut;
	}
} // end classfile
