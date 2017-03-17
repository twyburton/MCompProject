package uk.ac.ncl.burton.twy.ZPK.components.verifier;

import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.List;

import uk.ac.ncl.burton.twy.ZPK.components.PKComponentType;
import uk.ac.ncl.burton.twy.maths.CyclicGroup;

public class PKComponentVerifierAlpha extends PKComponentVerifierBasic {
	
	public PKComponentVerifierAlpha(CyclicGroup G) {
		super(G);
	}

	@Override
	public boolean verify(BigInteger commitment, BigInteger response, List<BigInteger> passingVariables) {
		this.saveTS(commitment, response);
		
		BigInteger g = passingVariables.get(0);
		BigInteger u = passingVariables.get(1);
		
		BigInteger gs = g.modPow( response, G.getP());
		BigInteger tuc = commitment.multiply( u.modPow( this.getChallenge() , G.getP())).mod(G.getP());
		
		return gs.equals(tuc);
	}

	
	@Override
	protected void setComponentType() {
		type = PKComponentType.ALPHA;
	}
}
