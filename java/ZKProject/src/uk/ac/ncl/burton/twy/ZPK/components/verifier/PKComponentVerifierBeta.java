package uk.ac.ncl.burton.twy.ZPK.components.verifier;

import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.List;

import uk.ac.ncl.burton.twy.ZPK.components.PKComponentType;
import uk.ac.ncl.burton.twy.maths.CyclicGroup;

public class PKComponentVerifierBeta extends PKComponentVerifierBasic {

	private PKComponentVerifierAlpha alpha;
	
	public PKComponentVerifierBeta(CyclicGroup G,  PKComponentVerifierAlpha alpha) {
		super(G);
		
		this.alpha = alpha;
	}

	@Override
	public boolean verify(BigInteger commitment, BigInteger response, List<BigInteger> passingVariables) {
		this.saveTS(commitment, response);
		// g^s2 . d^s1 == t2.(e)^c
		
		BigInteger g = passingVariables.get(0);
		BigInteger d = passingVariables.get(1);
		BigInteger e = passingVariables.get(2);
		
		BigInteger s1 = alpha.getSavedS();
		
		
		BigInteger ds1 = d.modPow(s1, G.getP());
		BigInteger gs2 = g.modPow(response, G.getP());
		BigInteger ds1gs2 = ds1.multiply(gs2).mod(G.getP());
		BigInteger t2ec = commitment.multiply(e.modPow(this.getChallenge(), G.getP())).mod(G.getP());
		
		return ds1gs2.equals(t2ec);
		
	}

	
	@Override
	protected void setComponentType() {
		type = PKComponentType.BETA;
	}
}
