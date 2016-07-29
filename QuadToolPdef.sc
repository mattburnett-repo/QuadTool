/*
   QuadToolPdef.sc
      base class for Pdef-based tone generators
         band, exAud
      Add Pdef-specific methods to base class. Subclasses add tone generator-specific functionality later.

   COPYRIGHT 2016 Matthew Burnett

NOTES:
======
   Pdef drives patterns, so we need initPattern implementation here

TO-DO:
======

*/

QuadToolPdef : QuadToolBase {
	var patternObject, azModPats, azModPat, azModPatName, patternType;

	initAzModPat {
		patternObject = AzModPats.new;
		azModPats = patternObject.getAzModPats;
		azModPat = azModPats[0][1];
		azModPatName = azModPats[0][0];
		patternType = "Array";
	}

	// INIT PATTERN
	// Pdef drives patterns/azModPats, so we need initPattern implementation here. Synths (ie XYpanning) are different.
	initPattern { // initialized in base class
		arg targetPrefix;

		OSCdef(("azModPat_" ++ targetPrefix).asSymbol,
			{
				arg msg;
				var theMsg;

				if(azModPats[msg[1]].isNil,
					{theMsg = ("No pattern for " ++ msg[1])},
					{
						azModPatName = azModPats[msg[1]][0];
						azModPat = azModPats[msg[1]][1];
						theMsg = azModPatName;

						if(gestureObject.ch7_rec, {gestureObject.ch7_val = msg[1];});
						if(patternType == "Array", {toneObject.setOutBus(patternObject.transAMP(outBus, azModPat))});
						if(patternType == "Pseq", {toneObject.setOutBus(patternObject.transAMP(outBus, Pseq(azModPat, 1)))});
				});
				theController.sendMsg('/' ++ targetPrefix ++ '/pattern/messageMonitor/value', theMsg);
			},
			'/' ++ targetPrefix ++ '/pattern/azModPat/selected'
		);
		OSCdef.new(("patternType_" ++ targetPrefix).asSymbol,
			{
				arg msg;
				var theVal, thePattern;
				theVal = msg[1];

				if(gestureObject.ch8_rec, {gestureObject.ch8_val = theVal});

				if(theVal == 0,
					{
						patternType = "Array";
						toneObject.setOutBus(patternObject.transAMP(outBus, azModPat));
					},
					{
						patternType = "Pseq";
						toneObject.setOutBus(patternObject.transAMP(outBus, Pseq(azModPat, 1)));
					};
				);
			},
			'/' ++ targetPrefix ++ '/pattern/type/x'
		);
		OSCdef.new(("outBus_" ++ targetPrefix).asSymbol,
			{
				arg msg;
				outBus = msg[1];

				if(gestureObject.ch9_rec, {gestureObject.ch9_val = outBus});

				if(patternType == "Array",
					{toneObject.setOutBus(patternObject.transAMP(outBus, azModPat))},
					{toneObject.setOutBus(patternObject.transAMP(outBus, Pseq(azModPat, 1)))}
				);
			},
			'/' ++ targetPrefix ++ '/pattern/outBus/selected'
		);
	}
} // end class def

