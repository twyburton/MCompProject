package uk.ac.ncl.burton.twyb.utils;

public class TimeUtils {

	public static boolean withinTolerance( long messageTime, long tolerance ){
		
		long time = System.currentTimeMillis()/1000L;
		if( messageTime > time - tolerance && messageTime < time + tolerance ) return true;
		return false;
	}
	
}
