package uk.ac.ncl.burton.twy.ZPK.components.prover;

import java.math.BigInteger;

import uk.ac.ncl.burton.twy.ZPK.components.PKComponentType;
import uk.ac.ncl.burton.twy.maths.CyclicGroup;
import uk.ac.ncl.burton.twy.utils.BigIntegerUtils;

public class PKComponentProverBeta extends PKComponentProverBasic {

	/*
	 * This component is used for proving knowledge of r  and a  in the form u = g^r # e = g^a . d^r
	 * {PK(r): u = g^r # e = g^a . d^r}
	 *
	 *	!!!!! This requires a component of type alpha in order to prove knowledge of r first
	 * 
	 * 
	 * 
	 *  (Capital values refer to random generated values. Instead of the lower case with a tilde above.)
	 *  (# replaces the conjunction  symbol)
	 *  (. represents multiplication)
	 * 
	 * 
	 *	Proving PK{(a,r): u = g^r # e = g^a . d^r }
	 * 	
	 *  Commitment: t1 = g^R		t2 = g^A . d^R
	 *  Challenge: 	c = random
	 *  Response: 	s1 = R + c.r	s2 = A + c.a
	 *  
	 *  Verification:	g^s1 == t1.(u)^c 	g^s2 . d^s1 == t2.(e)^c
	 * 
	 * 
	 */
	
	private PKComponentProverAlpha alpha;
	
	// Values for proof
	private BigInteger e,g,a,d,r;
	
	// Random values
	private BigInteger rd,ad;
	
	public PKComponentProverBeta(CyclicGroup G, PKComponentProverAlpha alpha, BigInteger a, BigInteger d ) {
		super(G);
		this.alpha = alpha;
		
		// Get g and r from alpha
		this.g = alpha.getValueg();
		this.r = alpha.getValuer();
		
		this.a = a;
		this.d = d;
		
		e = g.modPow(a, G.getP()).multiply( d.modPow(r, G.getP())).mod(G.getP());
	}

	@Override
	protected BigInteger generateTValue() {
		
		rd = alpha.getDValuer();
		ad = BigIntegerUtils.randomBetween( BigInteger.ONE, G.getQ() );
		
		BigInteger t = g.modPow(ad, G.getP()).multiply( d.modPow(rd, G.getP())).mod(G.getP());
		
		return t;
	}

	@Override
	protected BigInteger generateSValue(BigInteger c) {
		
		BigInteger s = ad.add( c.multiply(a).mod( G.getQ()) ).mod(  G.getQ() );
		
		return s;
		
	}

	
	@Override
	protected void generatePassingVariables() {
		// ORDER: g,d,e
		passingVariables.add( g );
		passingVariables.add( d );
		passingVariables.add( e );
	}
	
	@Override
	protected void setComponentType() {
		type = PKComponentType.BETA;
	}
}
