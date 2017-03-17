package uk.ac.ncl.burton.twy.ZPK.components.testing;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.util.List;

import org.junit.Test;

import uk.ac.ncl.burton.twy.ZPK.PKProver;
import uk.ac.ncl.burton.twy.ZPK.PKVerifier;
import uk.ac.ncl.burton.twy.ZPK.components.prover.PKComponentProver;
import uk.ac.ncl.burton.twy.ZPK.components.prover.PKComponentProverAlpha;
import uk.ac.ncl.burton.twy.ZPK.components.prover.PKComponentProverBeta;
import uk.ac.ncl.burton.twy.ZPK.components.verifier.PKComponentVerifier;
import uk.ac.ncl.burton.twy.ZPK.components.verifier.PKComponentVerifierAlpha;
import uk.ac.ncl.burton.twy.ZPK.components.verifier.PKComponentVerifierBeta;
import uk.ac.ncl.burton.twy.maths.CyclicGroup;

public class PKTesting {

	
	@Test
	public void PKTest() {
		
		CyclicGroup G = CyclicGroup.generateGroup(256);
		
		BigInteger g = G.getG();
		BigInteger r = BigInteger.TEN;
		
		BigInteger h = G.generateGenerator();
		
		BigInteger x1 = BigInteger.valueOf( 873 );
		BigInteger x2 = BigInteger.valueOf( 2945 );
		BigInteger d = g.modPow(x1, G.getP()).multiply( h.modPow(x2, G.getP()) ).mod(G.getP());
		
		BigInteger a = BigInteger.valueOf(100);
	
		
		PKProver peggy = new PKProver();
		PKVerifier victor = new PKVerifier();
		
		// == Component Setup
		PKComponentProver PAlpha1 = new PKComponentProverAlpha( G, g, r);	
		PKComponentProver PAlpha2 = new PKComponentProverAlpha( G, h, r);
		PKComponentProver PBeta = new PKComponentProverBeta( G, (PKComponentProverAlpha) PAlpha1, a, d);
		
		peggy.addPKComponent(PAlpha1);
		peggy.addPKComponent(PAlpha2);
		peggy.addPKComponent(PBeta);
	
		PKComponentVerifier VAlpha1 = new PKComponentVerifierAlpha( G );
		PKComponentVerifier VAlpha2 = new PKComponentVerifierAlpha( G );
		PKComponentVerifier VBeta = new PKComponentVerifierBeta( G, (PKComponentVerifierAlpha) VAlpha1);
		
		victor.addPKComponent(VAlpha1);
		victor.addPKComponent(VAlpha2);
		victor.addPKComponent(VBeta);
		
		// == Commitment ==
		List<BigInteger> commitmentList = peggy.getCommitmentList();
		
		// == Challenge ==
		BigInteger c = victor.getChallenge();
		
		// == Response ==
		List<BigInteger> responseList = peggy.getResponseList(c);
		
		// == Verify ==
		List<List<BigInteger>> passingVariablesList = peggy.getPassingVariablesList();
		
		assertTrue(victor.verify(commitmentList, responseList, passingVariablesList));
			
	}
	
	
	//@Test
	public void ComponentAlphaAndBetaSetupTest() {
		
		CyclicGroup G = CyclicGroup.generateGroup(256);
		
		BigInteger g = G.getG();
		BigInteger r = BigInteger.TEN;
		
		BigInteger h = G.generateGenerator();
		
		BigInteger x1 = BigInteger.valueOf( 873 );
		BigInteger x2 = BigInteger.valueOf( 2945 );
		BigInteger d = g.modPow(x1, G.getP()).multiply( h.modPow(x2, G.getP()) ).mod(G.getP());
		
		BigInteger a = BigInteger.valueOf(100);
		
		// == Component Setup
		PKComponentProver PAlpha1 = new PKComponentProverAlpha( G, g, r);
		PKComponentVerifier VAlpha1 = new PKComponentVerifierAlpha( G );
		
		PKComponentProver PAlpha2 = new PKComponentProverAlpha( G, h, r);
		PKComponentVerifier VAlpha2 = new PKComponentVerifierAlpha( G );
		
		PKComponentProver PBeta = new PKComponentProverBeta( G, (PKComponentProverAlpha) PAlpha1, a, d);
		PKComponentVerifier VBeta = new PKComponentVerifierBeta( G, (PKComponentVerifierAlpha) VAlpha1);
		
		// == Commitment ==
		BigInteger t1 = PAlpha1.getCommitment();
		System.out.println("t1: " + t1);
		
		BigInteger t2 = PAlpha2.getCommitment();
		System.out.println("t2: " + t2);
		
		BigInteger t3 = PBeta.getCommitment();
		System.out.println("t3: " + t3);
		
		// == Challenge ==
		BigInteger c = VAlpha1.getChallenge();
		System.out.println("c: " + c);
		
		VAlpha2.setChallenge(c);
		VBeta.setChallenge(c);
		
		// == Response ==
		BigInteger s1 = PAlpha1.getResponse(c);
		System.out.println("s1: " + s1);
		
		BigInteger s2 = PAlpha2.getResponse(c);
		System.out.println("s2: " + s2);
		
		BigInteger s3 = PBeta.getResponse(c);
		System.out.println("s3: " + s3);
		
		// == Verify ==	
		List<BigInteger> passingVariables = PAlpha1.getPassingVariables();
		assertTrue(VAlpha1.verify(t1, s1, passingVariables));
		
		passingVariables = PAlpha2.getPassingVariables();
		assertTrue(VAlpha2.verify(t2, s2, passingVariables));
		
		passingVariables = PBeta.getPassingVariables();
		assertTrue(VBeta.verify(t3, s3, passingVariables));
		
		
	}

}
