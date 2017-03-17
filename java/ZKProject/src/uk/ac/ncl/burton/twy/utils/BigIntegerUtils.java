package uk.ac.ncl.burton.twy.utils;

import java.math.BigInteger;
import java.security.SecureRandom;

public class BigIntegerUtils {

	public static BigInteger randomBetween(BigInteger lower, BigInteger upper, SecureRandom ran ){
		
		BigInteger x = null;
		
		while( true ){
			x = new BigInteger( upper.bitLength() , ran );
			x = x.mod(upper);
			
			if( x.compareTo(lower) != -1 && x.compareTo(upper) != 1 ) break;
		}
		
		return x;
		
	}
	
	public static BigInteger randomBetween(BigInteger lower, BigInteger upper ){
		
		SecureRandom ran = new SecureRandom();
		
		BigInteger x = null;
		
		while( true ){
			x = new BigInteger( upper.bitLength() , ran );
			x = x.mod(upper);
			
			if( x.compareTo(lower) != -1 && x.compareTo(upper) != 1 ) break;
		}
		
		return x;
		
	}

	
	
}
