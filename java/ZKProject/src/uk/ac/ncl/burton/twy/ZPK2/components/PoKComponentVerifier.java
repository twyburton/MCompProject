package uk.ac.ncl.burton.twy.ZPK2.components;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

import uk.ac.ncl.burton.twy.maths.CyclicGroup;
import uk.ac.ncl.burton.twy.utils.BigIntegerUtils;

public class PoKComponentVerifier {
	
	private UUID component_id = UUID.randomUUID();
	public UUID getComponentID() { return component_id; }
	
	
	private CyclicGroup G;
	
	private BigInteger challenge;
	
	public PoKComponentVerifier( CyclicGroup G ){
		this.G = G;
		generateRandomValues();
	}
	
	private void generateRandomValues(){
		challenge = BigIntegerUtils.randomBetween( BigInteger.ONE, G.getQ() );
	}
	
	public BigInteger getChallenge(){
		return challenge;
	}
	
	public void setChallenge( BigInteger c){
		challenge = c;
	}

	public boolean verify( List<BigInteger> bases, List<BigInteger> responses, BigInteger commitment, BigInteger value ){
		//u^s1 g^s2 == t(x)^c
		
		if( bases.size() != responses.size() ) throw new IllegalArgumentException("The number of bases must match the number of responses");
		
		BigInteger leftSide = BigIntegerUtils.multiplyBaseExponents(G.getQ(), bases, responses);
		BigInteger rightSide = commitment.multiply( value.modPow(challenge, G.getQ()) ).mod(G.getQ());
		
		return leftSide.equals(rightSide);
		
	}

	
	
	
	
}