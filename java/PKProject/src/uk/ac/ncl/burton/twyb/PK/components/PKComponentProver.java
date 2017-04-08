package uk.ac.ncl.burton.twyb.PK.components;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import uk.ac.ncl.burton.twyb.crypto.CyclicGroup;
import uk.ac.ncl.burton.twyb.utils.BigIntegerUtils;


public class PKComponentProver {

	private UUID component_id = UUID.randomUUID();
	public UUID getComponentID() { return component_id; }
	
	
	/** The cyclic group for the proof */
	private final CyclicGroup G;
	
	/** The list of bases */
	private final List<BigInteger> bases;
	
	/** The list of exponents */
	private final List<BigInteger> exponents;
	
	/** The list of random exponents generated by the class */
	private List<BigInteger> randomExponents;
	
	/** The value of the equation */
	private final BigInteger value;


	private PKComponentProver(final CyclicGroup G, final List<BigInteger> bases, final List<BigInteger> exponents ){
		
		if( bases.size() != exponents.size()){
			throw new IllegalArgumentException("The number of bases must match the number of exponents");
		}
		
		this.bases = bases;
		this.exponents = exponents;
		this.G = G;
		
		this.value =  BigIntegerUtils.multiplyBaseExponents(G.getQ(), bases, exponents);
		
		generateRandomValues();
	}

	
	
	public static PKComponentProver generateProver( final CyclicGroup G, final List<BigInteger> bases, final List<BigInteger> exponents){
		return new PKComponentProver(G,bases,exponents);
	}
	
	public static PKComponentProver generateProver( final CyclicGroup G, final BigInteger base, final BigInteger exponent){
		
		List<BigInteger> bases = new ArrayList<BigInteger>();
		List<BigInteger> exponents = new ArrayList<BigInteger>();
		
		bases.add(base);
		exponents.add(exponent);
		
		return new PKComponentProver(G,bases,exponents);
		
	}

	/**
	 * Get a list of the bases
	 * @return the list of bases
	 */
	public List<BigInteger> getBases(){
		return bases;
	}
	
	/**
	 * Get the value of the equation
	 * @return the value of the equation
	 */
	public BigInteger getValue(){
		return value;
	}
	
	/**
	 * Generate the required random values
	 */
	private void generateRandomValues(){
		randomExponents = new ArrayList<BigInteger>();
		for( int i = 0 ; i < exponents.size(); i++ ) randomExponents.add(BigIntegerUtils.randomBetween( BigInteger.ONE, G.getQ() ));
	}
	
	/**
	 * Get the commitment value
	 * @return the commitment value
	 */
	public BigInteger getCommitment(){
		
		return BigIntegerUtils.multiplyBaseExponents(G.getQ(), bases, randomExponents);
		
	}
	
	/**
	 * Get a list of the response values
	 * @param c the challenge
	 * @return the list of response values
	 */
	public List<BigInteger> getResponse( final BigInteger c ){
		// s1 = rd + rc			s2 = td + tc
		
		List<BigInteger> s = new ArrayList<BigInteger>();
		
		for( int i = 0 ; i < bases.size(); i++ ){
			
			BigInteger rd = randomExponents.get(i);
			BigInteger r = exponents.get(i);
			
			BigInteger rc = r.multiply(c);
			BigInteger x = rd.add( rc ); 
			
			s.add( x );
		}
		
		return s;
	}
	
	/**
	 * Get the random exponent value for base i.
	 * This method should only be used for setting the random exponent of another component. This value should never be sent to the other party.
	 * @param index the base index
	 * @return the random exponent
	 */
	public BigInteger getRandomExponent( final int index ){
		if( index < 0 || index >= randomExponents.size() ){
			throw new IllegalArgumentException("Index is out of range");
		}
		
		return randomExponents.get(index);
	}
	
	/**
	 * Set the random exponent to value for base i
	 * @param index the base index 
	 * @param value the value which is being set
	 * @return the previous value at the position
	 */
	public BigInteger setRandomExponent( final int index, final BigInteger value ){
		if( index < 0 || index >= randomExponents.size() ){
			throw new IllegalArgumentException("Index is out of range");
		}
		
		return randomExponents.set(index, value);
	}
}