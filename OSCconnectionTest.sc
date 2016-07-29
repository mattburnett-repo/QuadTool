/*
   OSCconnectionTest.sc
      Use this with the Lemur connectivityTest project

   COPYRIGHT 2016 Matthew Burnett

*/

OSCconnectionTest
{
	*new {
		arg theIP;
		^super.new.init(theIP);
	}

	init {
		arg theIP;
		var theControllerAddress, thePort;

		thePort = 8000; // this is usually the right port for Lemur.

		if(theIP.isNil,
			{"OSCconnectionTest needs an IP address of the controller as an arg.".warn},
			{
				theControllerAddress = NetAddr(theIP, thePort);

				^OSCdef(\connectionTest,
					{
						arg msg;
						msg.postln;
						theControllerAddress.sendMsg('/Monitor/value', msg[1].asInteger);
					},
					'/control/x'
				);
		});
	}
} // end classfile





