package uk.ac.ncl.burton.twyb.PK;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import uk.ac.ncl.burton.twyb.PK.components.PKComponentVerifier;
import uk.ac.ncl.burton.twyb.crypto.CyclicGroup;
import uk.ac.ncl.burton.twyb.utils.TimeUtils;


public class PKVerifier {

	private String PK_id;
	
	private boolean proofSuccessful = false; // This is set to true if the proof is completed successfully
	public boolean isProofSuccessful(){
		return proofSuccessful;
	}
	
	private CyclicGroup G;
	public CyclicGroup getGroup(){
		return G;
	}
	
	public PKVerifier(CyclicGroup G, String PK_id, List<PKComponentVerifier> components ){
		
		this.G = G;
		this.PK_id = PK_id;
		this.components = components;
		
	}
	
	/**
	 * Create a PKVerifier instances using the initalisationJSON generated by a PKProver
	 * @param initalisationJSON
	 * @return a PKVerifier instance
	 */
	public static PKVerifier getInstance( String initalisationJSON ){ 
		
		// == JSON PROCESS ==
		try {
			JSONParser parser = new JSONParser();
			JSONObject obj = (JSONObject) parser.parse(initalisationJSON);
			
			String pk_id =  (String)obj.get("PK_id") ;
			
			
			// -- Setup cyclic group --
			JSONObject group = (JSONObject) ((JSONObject)obj).get("group");
			BigInteger g = new BigInteger( (String) ((JSONObject)group).get("generator") );
			BigInteger q = new BigInteger( (String) ((JSONObject)group).get("modulus") );
			CyclicGroup G = new CyclicGroup( null, q , g);
			
			// -- Setup components --
			JSONArray componentsArray = (JSONArray) ((JSONObject)obj).get("components");
			int nComponents = componentsArray.size();
			
			List<PKComponentVerifier> components = new ArrayList<PKComponentVerifier>();
			for( int i = 0 ; i < nComponents; i++ ){
				
				JSONObject compData = (JSONObject) componentsArray.get(i);
				String comp_id = (String) ((JSONObject)compData).get("component_id");
				int nBases = (int)(long)(Long) ((JSONObject)compData).get("nBases");
				
				components.add( new PKComponentVerifier(G,UUID.fromString(comp_id), nBases));
				
			}
			
			PKVerifier victor = new PKVerifier( G, pk_id , components );
			
			return victor;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return null;
	}
	
	/**
	 * The list of components that make up the proof of knowledge
	 */
	private List<PKComponentVerifier> components = new ArrayList<PKComponentVerifier>();
	
	/**
	 * Get the challenge value. Also ensures the challenge value is set for all components
	 * @return challenge value
	 */
	private BigInteger getChallenge(){
		
		BigInteger challenge = components.get(0).getChallenge();
		
		for( int i = 1; i < components.size(); i++ ){
			components.get(i).setChallenge(challenge);
		}
		
		return challenge;
	}
	
	
	private String JSONcommitment;
	//private String initalisationJSON; 
	
	// == JSON Text ==
	public String getJSONChallenge( String JSONcommitment ){
		
		try {
			// == JSON PROCESS ==
			JSONParser parser = new JSONParser();
			JSONObject obj = (JSONObject) parser.parse(JSONcommitment);
			
			//String PK_id =  (String)obj.get("PK_id") ;
			this.JSONcommitment = JSONcommitment;
			
			// == JSON output ==
			String json = "";
			
			json += "{\n";
				json += "\t\"PK_id\":\"" + PK_id + "\",\n";
				json += "\t\"protocol_version\":" + Arrays.toString(PKConfig.PROTOCOL_VERSION) + ",\n";
				json += "\t\"step\":\"challenge\",\n";
				json += "\t\"components\":[\n";
			
						json += "\t\t{\n";
							json += "\t\t\t\"c\":\"" +  getChallenge() + "\",\n";
							json += "\t\t\t\"component_id\":\"" + components.get(0).getComponentID() + "\"\n";
						json += "\t\t}";
						
				json += "\t],\n";
				json += "\t\"time\":" + (System.currentTimeMillis()/1000) + "\n";
			json += "}\n";
			
			return json;
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private String validationErrorMsg = "";
	public String getVerificationErrorMsg(){
		return validationErrorMsg;
	}
	
	public String getJSONOutcome( String JSONresponse, String JSONpassing ){
		
		boolean successful = true;
		
		try {
			// == JSON PROCESS ==
			JSONParser parser = new JSONParser();
			JSONObject jCommitment = (JSONObject) parser.parse(JSONcommitment);
			JSONObject jResponse = (JSONObject) parser.parse(JSONresponse);
			JSONObject jPassing = (JSONObject) parser.parse(JSONpassing);
			
			String PK_id_commitment =  (String)jCommitment.get("PK_id") ;
			String PK_id_response =  (String)jResponse.get("PK_id") ;
			String PK_id_passing =  (String)jPassing.get("PK_id") ;
			
			long time_commitment = (long)jCommitment.get("time");
			long time_response = (long)jResponse.get("time");
			long time_passing = (long)jPassing.get("time");
			
			if( !(TimeUtils.withinTolerance(time_commitment, PKConfig.PROTOCOL_TIME_TOLERANCE) 
					&& TimeUtils.withinTolerance(time_response, PKConfig.PROTOCOL_TIME_TOLERANCE) 
					&& TimeUtils.withinTolerance(time_passing, PKConfig.PROTOCOL_TIME_TOLERANCE) ) ){
				successful = false;
				validationErrorMsg += "Timestamps not within tolerance; ";
			}
			
			if( !( PK_id.equals(PK_id_commitment) && PK_id.equals(PK_id_response) && PK_id.equals(PK_id_passing)) ){
				successful = false;
				validationErrorMsg += "Proof IDs do not match; ";
			}
			
			// Go through each component and verify
			for( int i = 0 ; i < components.size() ; i++){
				
				JSONObject commitmentComps = (JSONObject) ((JSONArray)jCommitment.get("components")).get(i);
				JSONObject responseComps = (JSONObject) ((JSONArray)jResponse.get("components")).get(i);
				
				BigInteger t = new BigInteger((String)commitmentComps.get("t"));
				// Ger response list
				JSONArray sList =  (JSONArray)responseComps.get("s");
				List<BigInteger> responseList = new ArrayList<BigInteger>();
				for( int j = 0 ; j < sList.size(); j++){
					responseList.add( new BigInteger((String) sList.get(j)) );
				}
				
				// Get base list
				JSONObject passingComps =  (JSONObject) ((JSONArray)jPassing.get("components")).get(i);
				JSONArray psArr =  (JSONArray)passingComps.get("bases");
				BigInteger passingValue = new BigInteger((String)passingComps.get("value"));

				List<BigInteger> passingBasesList = new ArrayList<BigInteger>();
				for( int j = 0 ; j < psArr.size(); j++){
					passingBasesList.add( new BigInteger((String) psArr.get(j)) );
				}
				
				if (! components.get(i).verify(passingBasesList, responseList, t, passingValue ) ){
					successful = false;
					validationErrorMsg += "Proof IDs do not match; ";
				}
				
				
			}
			
			
			int outcome = 0;
			if( successful ){
				outcome = 1;
				proofSuccessful = true;
				if( PKConfig.PRINT_PK_LOG ) System.out.println("Proof Succeeded!");
			} else {
				if( PKConfig.PRINT_PK_LOG ) System.out.println("Proof Failed!");
			}
			
			// == JSON output ==
			String json = "";
			
			json += "{\n";
				json += "\t\"PK_id\":\"" + PK_id + "\",\n";
				json += "\t\"protocol_version\":" + Arrays.toString(PKConfig.PROTOCOL_VERSION) + ",\n";
				json += "\t\"step\":\"outcome\",\n";
				json += "\t\"outcome\":" + outcome + ",\n";
				json += "\t\"time\":" + (System.currentTimeMillis()/1000) + "\n";
			json += "}\n";
			
			return json;
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	/**
	 * Get the value of the component at index. Zeroth indexed.
	 * @param componentIndex
	 * @return the value
	 */
	public BigInteger getValue( int componentIndex ){
		
		if( proofSuccessful ){
			return components.get(componentIndex).getValue();
		} else return null;
		
	}
	
	/**
	 * Get a base value at index baseIndex for component at component index. Zeroth indexed.
	 * @param componentIndex
	 * @param baseIndex
	 * @return
	 */
	public BigInteger getBase( int componentIndex, int baseIndex ){
		if( proofSuccessful ){
			return components.get(componentIndex).getBases().get(baseIndex);
		} else return null;
	}
}
