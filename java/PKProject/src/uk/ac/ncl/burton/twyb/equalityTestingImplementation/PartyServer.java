package uk.ac.ncl.burton.twyb.equalityTestingImplementation;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import uk.ac.ncl.burton.twyb.PK.PKProver;
import uk.ac.ncl.burton.twyb.PK.PKVerifier;
import uk.ac.ncl.burton.twyb.PK.components.PKComponentProver;
import uk.ac.ncl.burton.twyb.PK.network.NetworkConnectionServer;
import uk.ac.ncl.burton.twyb.crypto.CyclicGroup;
import uk.ac.ncl.burton.twyb.crypto.EEA;
import uk.ac.ncl.burton.twyb.crypto.EEAResult;
import uk.ac.ncl.burton.twyb.utils.BigIntegerUtils;

public class PartyServer {

	public static void main(String[] args) {
		
		
		NetworkConnectionServer server = new NetworkConnectionServer();
		Thread serverThread = new Thread(server);
		serverThread.start();

		server.setBlockingMode( true );
		
		
		// ====== PKS ======
		SecureRandom ran = new SecureRandom();
		
		CyclicGroup G = null;
		BigInteger g = null;
		
		
		BigInteger b = BigInteger.valueOf(8213); // Secret value
		
		
		// === STEP 1 ===
		String init = server.receiveMessage();
		PKVerifier victor = PKVerifier.getInstance(init);
		G = victor.getGroup();
		g = G.getG();
		BigInteger mod = G.getQ();
		
		String commitment = server.receiveMessage();
		
		String challenge = victor.getJSONChallenge(commitment);
		server.sendMessage(challenge);
		
		String response = server.receiveMessage();
		String passing = server.receiveMessage();
		
		String outcome = victor.getJSONOutcome(commitment, response, passing);
		server.sendMessage(outcome);
		
		boolean success = victor.isProofSuccessful();
		System.out.println(success);
		
		BigInteger u1 = victor.getValue(0);
		BigInteger u2 = victor.getValue(1);
		BigInteger e = victor.getValue(2);
		
		BigInteger h = victor.getBase(1, 0);
		BigInteger c = victor.getBase(2, 1);
		
		
		
		
		// === STEP 2 ===
		
		// -- Calculations --
		BigInteger s = RandomZQ(G.getQ());//BigIntegerUtils.randomBetween(BigInteger.ONE, G.getQ(), ran);
		BigInteger t = RandomZQ(G.getQ());//BigIntegerUtils.randomBetween(BigInteger.ONE, G.getQ(), ran);
		
		
		BigInteger u1d = u1.modPow(s, mod).multiply( g.modPow(t, mod) );
		BigInteger u2d = u2.modPow(s, mod).multiply( h.modPow(t, mod) );
		
			BigInteger es = e.modPow(s, mod);
				BigInteger bs = b.multiply(s);
			BigInteger gbs = g.modPow( bs ,mod).modInverse(mod);
			BigInteger ct = c.modPow(t, mod);
		
		BigInteger ed = es.multiply(gbs).multiply(ct);	
		
		// GCD PROOF:   PK{(z,q,r1,r2,a,b) : C = g^z h^r1 AND D = g^q h^r2 AND g = C^a D^b
		BigInteger r1 = RandomZQ(G.getQ());
		BigInteger r2 = RandomZQ(G.getQ());
		
		EEAResult eea = EEA.eea(s, G.getQ());
		
		List<BigInteger> baseseea = new ArrayList<BigInteger>();
		List<BigInteger> exponentseea = new ArrayList<BigInteger>();
		baseseea.add(g);
		baseseea.add(h);
		exponentseea.add(s);
		exponentseea.add(r1);
		BigInteger C = BigIntegerUtils.multiplyBaseExponents(mod, baseseea, exponentseea);
		
		baseseea = new ArrayList<BigInteger>();
		exponentseea = new ArrayList<BigInteger>();
		baseseea.add(g);
		baseseea.add(h);
		exponentseea.add(mod);
		exponentseea.add(r2);
		BigInteger D = BigIntegerUtils.multiplyBaseExponents(mod, baseseea, exponentseea);
		
		
		// -- PoK --
		PKProver peggy = new PKProver(G);
		
		// (1)
		List<BigInteger> bases = new ArrayList<BigInteger>();
		List<BigInteger> exponents = new ArrayList<BigInteger>();
		bases.add(u1);
		exponents.add(s);
		bases.add(g);
		exponents.add(t);
		PKComponentProver c2X1 = PKComponentProver.generateProver(G, bases, exponents);
		
		// (2)
		bases = new ArrayList<BigInteger>();
		exponents = new ArrayList<BigInteger>();
		bases.add(u2);
		exponents.add(s);
		bases.add(h);
		exponents.add(t);
		PKComponentProver c2X2 = PKComponentProver.generateProver(G, bases, exponents);
		
		// (3)
		bases = new ArrayList<BigInteger>();
		exponents = new ArrayList<BigInteger>();
		bases.add(e);
		exponents.add(s);
		bases.add(g);
			List<BigInteger> nbsList = new ArrayList<BigInteger>(); nbsList.add(BigInteger.valueOf(-1)); nbsList.add(b); nbsList.add(s);
		exponents.add(BigIntegerUtils.multiplyList(nbsList));
		bases.add(c);
		exponents.add(t);
		PKComponentProver c2X3 = PKComponentProver.generateProver(G, bases, exponents);
		
		// (4)
		bases = new ArrayList<BigInteger>();
		exponents = new ArrayList<BigInteger>();
		bases.add(g);
		exponents.add(s);
		bases.add(h);
		exponents.add(r1);
		PKComponentProver c2X4 = PKComponentProver.generateProver(G, bases, exponents);
		
		// (5)
		bases = new ArrayList<BigInteger>();
		exponents = new ArrayList<BigInteger>();
		bases.add(g);
		exponents.add(mod);
		bases.add(h);
		exponents.add(r2);
		PKComponentProver c2X5 = PKComponentProver.generateProver(G, bases, exponents);
		
		// (6)
		bases = new ArrayList<BigInteger>();
		exponents = new ArrayList<BigInteger>();
		bases.add(C);
		exponents.add(eea.getS());
		bases.add(D);
		exponents.add(eea.getT());
		PKComponentProver c2X6 = PKComponentProver.generateProver(G, bases, exponents);
		
		// -- ADD --
		peggy.addComponent(c2X1);
		peggy.addComponent(c2X2);
		peggy.addComponent(c2X3);
		peggy.addComponent(c2X4);
		peggy.addComponent(c2X5);
		peggy.addComponent(c2X6);
		
		// -- Proof --
		init = peggy.getJSONInitialise();
		server.sendMessage(init);
		
		commitment = peggy.getJSONCommitment();
		server.sendMessage(commitment);
		
		challenge = server.receiveMessage();
		
		response = peggy.getJSONResponse(challenge);
		server.sendMessage(response);
		
		passing = peggy.getJSONPassingVariables();
		server.sendMessage(passing);
		
		outcome = server.receiveMessage();
		System.out.println(outcome);
		
		
		
		// === STEP 3 === 
		init = server.receiveMessage();
		victor = PKVerifier.getInstance(init);
		
		commitment = server.receiveMessage();
		
		challenge = victor.getJSONChallenge(commitment);
		server.sendMessage(challenge);
		
		response = server.receiveMessage();
		passing = server.receiveMessage();
		
		outcome = victor.getJSONOutcome(commitment, response, passing);
		server.sendMessage(outcome);
		
		success = victor.isProofSuccessful();
		System.out.println(success);
		
		System.out.println("d: " + victor.getValue(1) );
		
		//server.stop();
		
	}

	
	private static BigInteger RandomZQ(BigInteger q){
		return BigIntegerUtils.randomBetween(BigInteger.ONE, q);
	}
	
}
