/*
   AzModPats.sc
      basic collection of azimuth modulation patterns

   COPYRIGHT 2016 Matthew Burnett

NOTES:
======

*/

AzModPats {
	var <azModPats;

	*new {
		^super.new.init();
	}

	init {
		azModPats = (
			amp_0: [0],

			amp_0_1: [0,1],

			amp_0_0_1:   [0,0,1],
			amp_0_1_0:   [0,1,0],
			amp_0_1_1:   [0,1,1],
			amp_0_1_2:   [0,1,2],
			amp_0_1_3:   [0,1,3],

			amp_0_0_0_0:   [0,0,0,0],
			amp_0_0_0_1:   [0,0,0,1],
			amp_0_0_1_1:   [0,0,1,1],
			amp_0_1_1_1:   [0,1,1,1],
			amp_0_1_1_0:   [0,1,1,0],
			amp_0_1_0_0:   [0,1,0,0],

			amp_0_1_2_0:   [0,1,2,0],		// triangle
			amp_0_1_2_1:   [0,1,2,1],
			amp_0_1_2_2:   [0,1,2,2],
			amp_0_1_2_3:   [0,1,2,3],

			amp_0_2:   [0,2],

			amp_0_0_2:   [0,0,2],
			amp_0_2_0:   [0,2,0],
			amp_0_2_1:   [0,2,1],
			amp_0_2_2:   [0,2,2],
			amp_0_2_3:   [0,2,3],

			amp_0_0_0_2:   [0,0,0,2],
			amp_0_0_2_2:   [0,0,2,2],
			amp_0_2_2_2:   [0,2,2,2],
			amp_0_2_2_0:   [0,2,2,0],
			amp_0_2_0_0:   [0,2,0,0],

			amp_0_2_3_0:   [0,2,3,0],		// triangle
			amp_0_2_3_1:   [0,2,3,1],
			amp_0_2_3_2:   [0,2,3,2],
			amp_0_2_3_3:   [0,2,3,3],

			amp_0_3:   [0,3],

			amp_0_0_3:   [0,0,3],
			amp_0_3_0:   [0,3,0],
			amp_0_3_1:   [0,3,1],
			amp_0_3_2:   [0,3,2],
			amp_0_3_3:   [0,3,3],

			amp_0_0_0_3:   [0,0,0,3],
			amp_0_0_3_3:   [0,0,3,3],
			amp_0_3_3_3:   [0,3,3,3],
			amp_0_3_3_0:   [0,3,3,0],
			amp_0_3_0_0:   [0,3,0,0],

			amp_0_3_1_0:   [0,3,1,0],		// triangle
			amp_0_3_2_0:   [0,3,2,0],
			amp_0_3_2_1:   [0,3,2,1],
			amp_0_3_3_0:   [0,3,3,0],
			amp_0_3_3_1:   [0,3,3,1],
			amp_0_3_3_2:   [0,3,3,2],

			amp_0_1_2_3_0:   [0,1,2,3,0],	// "closes the loop"

			amp_0_2_1_3: [0,2,1,3],		// the "square graph" shapes
			amp_0_2_3_1: [0,2,3,1],

			// for fun. make symmetry in a square
			// amp_0_1_2_3:   [0,1,2,3],	// already have this upstairs
			amp_1_1_3_0:   [1,1,3,0],
			amp_2_3_2_1:   [2,3,2,1],
			amp_3_0_1_3:   [3,0,1,3]
		);

		// ^azModPats.asSortedArray; FIXME
	}
	getAzModPats {
		^azModPats.asSortedArray;
	}

	/*displayAzModPats { // not understood... error FIXME
		var theVal = this.getAzModPats;
		theVal.do (
			{
				arg i, item;
				(item + ":" + theVal[item]).postln;
		});
	}*/

	transAMP {
		arg theOriginBus = 0, thePattern;
		var maxNumChannels = 4;

		if(thePattern.isNil,
			{"Pattern fail in transAMP. the pattern probably doesn't exist in azModPat Dictionary.".warn;},
			{if(thePattern.isKindOf(Pseq),
				{
					var thePatternLength = thePattern.list.size;
					^Pseq((thePattern + theOriginBus % maxNumChannels).asStream.nextN(thePatternLength), inf);
				}, {
					^(thePattern + theOriginBus % maxNumChannels);
			}); // end isKindOf
		}); // end isNil
	}
} // end class file

