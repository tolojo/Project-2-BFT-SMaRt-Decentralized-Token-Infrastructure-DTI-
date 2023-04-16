/**
 * BFT Map implementation (server side).
 *
 */
package intol.bftmap;

import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;

import org.bouncycastle.asn1.ocsp.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.util.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.plaf.TreeUI;

public class BFTMapServer<K, V> extends DefaultSingleRecoverable {
    private final Logger logger = LoggerFactory.getLogger("bftsmart");
    private final ServiceReplica replica;
    private TreeMap<K, V> replicaMap;
    private TreeMap<K,V> replicaRequestMap;
    //private TreeMap<K,V> replicaSpendMap;
    private long coinID = 1L;
    private long nftID = 1L;

    //The constructor passes the id of the server to the super class
    public BFTMapServer(int id) {
        replicaMap = new TreeMap<>();
        replicaRequestMap = new TreeMap<>();
        replica = new ServiceReplica(id, this, this);
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Use: java BFTMapServer <server id>");
            System.exit(-1);
        }
        new BFTMapServer<Integer, String>(Integer.parseInt(args[0]));
    }

    @Override
    public byte[] appExecuteOrdered(byte[] command, MessageContext msgCtx) {
        try {
            BFTMapMessage<K,V> response = new BFTMapMessage<>();
            BFTMapMessage<K,V> request = BFTMapMessage.fromBytes(command);
            BFTMapRequestType cmd = request.getType();

            logger.info("Ordered execution of a {} request from {}", cmd, msgCtx.getSender());
            System.out.println(cmd);
            switch (cmd) {
                //write operations on the map
                case PUT:
                    V oldValue = replicaMap.put(request.getKey(), request.getValue());

                    if(oldValue != null) {
                        response.setValue(oldValue);
                    }
                    return BFTMapMessage.toBytes(response);
                case SIZE:
                    
                case REMOVE:
                	
                case SPEND:
                	int sum_coins = 0;
                	int remaining_value = 0;
                	String[] spend = request.getValue().toString().split("\\|");

                	for (String coin : spend[2].split(",")) {
                		try {
                			String[] ret = replicaMap.get(Integer.parseInt(coin)).toString().split("\\|");
                    		// Check if the coin belongs to this client
                            if (Integer.parseInt(spend[1]) != Integer.parseInt(ret[1])) {
                            	response.setValue(0); 
                            	return BFTMapMessage.toBytes(response);
                            }else {
                            	sum_coins += Integer.parseInt(ret[2]);
                            }
                		}
                		// Check if the coin exists
                		catch(NumberFormatException | NullPointerException e) {
                			response.setValue(0); 
                        	return BFTMapMessage.toBytes(response);
                		}
                	}
                	if(sum_coins >= Integer.parseInt(spend[4])) {

                		//create new coin for the receiver 
                		V receiver_coin = (V) ("coin"+ "|" + spend[3] + "|" + spend[4]);
                		replicaMap.put(request.getKey(), receiver_coin);

                        //remove all sender coins used in the transaction
                		for (String used_coin : spend[2].split(",")) {
                			V removedValue = replicaMap.remove(Integer.parseInt(used_coin));
                        }

                        //create new coin for the sender with the remaining value
                		remaining_value = sum_coins - Integer.parseInt(spend[4]);
                		V sender_coin = (V) ("coin"+ "|" + spend[1] + "|" + remaining_value); 
                		K key = (K) Integer.valueOf(Integer.valueOf(request.getKey().toString())+1);
                		V oldVal = replicaMap.put(key, sender_coin);

                		if(oldVal != null) {
                            response.setValue(oldVal);
                        }else {
                        	response.setValue(key);
                        }
                        return BFTMapMessage.toBytes(response);
                		
                	}
                	
                    response.setValue(request.getValue()); //TODO change this to new coin
                    return BFTMapMessage.toBytes(response);
                	
                case MINT:
                	String[] coinTokens = request.getValue().toString().split("\\|");
                	String userId = coinTokens[1];
                	String value = coinTokens[2];
                	
                	//only user with id=0 has permission to MINT coins and value contains only digits
                	if(userId.equals("0") && value.matches("\\d+")) {
                		V oldV = replicaMap.put(request.getKey(), request.getValue());
                        if(oldV != null) {
                            response.setValue(oldV);
                        }else {
                        	response.setValue(request.getKey());
                        }

                        return BFTMapMessage.toBytes(response);
                	}
                case MINT_NFT:
                    String[] nftTokens = request.getValue().toString().split("\\|");
                    String user_ID = nftTokens[1];
                    String nftName = nftTokens[2];
                    
                        boolean _nftExists = false;
                        for(Map.Entry<K,V> nftEntry : replicaMap.entrySet()){
                            V nftValue = nftEntry.getValue();
                            String[] nft = ((String) nftValue).split("\\|");
                            if(nft[0].equals("nft")){
                                if(nftName.equals(nft[2])){
                                    _nftExists = true;
                                }
                            }
                        }
                        if(!_nftExists){
                            V oldV = replicaMap.put(request.getKey(), request.getValue());
                            if(oldV != null) {
                                response.setValue(oldV);
                            }else {
                                response.setValue(request.getKey());
                            }
    
                            return BFTMapMessage.toBytes(response);
                        }
                        else {
                            request.setValue("Already exists a NFT with that name");
                            return BFTMapMessage.toBytes(request);
                        }
                    
                case CANCEL_REQUEST_NFT_TRANSFER:
                    Boolean userRequest = false;
                    Boolean nftRequest = false;
                    int requestID = 0;
                    String[] requestTransferCancel = request.getValue().toString().split("\\|");
                    for(Map.Entry<K,V> nft : replicaMap.entrySet()){
                        String nftID = nft.getKey().toString();
                        if (requestTransferCancel[2].equals(nftID)){ 
                            nftRequest= true;
                        }
                    }
                    for(Map.Entry<K,V> req : replicaMap.entrySet()){
                        String[] userID = req.getValue().toString().split("\\|");
                        if (requestTransferCancel[1].equals(userID[1])){
                            if(requestTransferCancel[2].equals(userID[2])){
                            requestID = Integer.parseInt(req.getKey().toString());
                            userRequest= true;
                            }
                        }
                    }
                    if(userRequest && nftRequest){
                    V valueToRemove = replicaMap.get(requestID);
                    replicaMap.remove(requestID);                
                    return BFTMapMessage.toBytes(request);
                    }
                    else return BFTMapMessage.toBytes(request);
                
                case REQUEST_NFT_TRANSFER:
                    Boolean exists = true;
                    String[] requestTransfer = request.getValue().toString().split("\\|");
                    String userID = requestTransfer[1];
                    V entry = replicaMap.get(Integer.parseInt(requestTransfer[2]));
                    String nftUserId = entry.toString().split("\\|")[1];
                    if (userID.equals(nftUserId)){
                        response.setValue("You are the onwer of the nft");
                        return BFTMapMessage.toBytes(response);
                    }
                    DateValidatorUsingDateFormat validator = new DateValidatorUsingDateFormat("MM/dd/yyyy");
                    boolean isDateValid = validator.isValid(requestTransfer[4]);
                    //ver monedas 
                    String[] requestCoins = requestTransfer[3].split(",");
                    boolean areCoinsFromUser = false;
                    int coinsSumValue = 0;
                    for(int i = 0; i<requestCoins.length;i++){
                        areCoinsFromUser = isCoinFromUser(requestCoins[i], msgCtx.getSender());
                  		coinsSumValue += Integer.parseInt(replicaMap.get(Integer.parseInt(requestCoins[i])).toString().split("\\|")[2]);
                    }

                    for (Map.Entry<K,V> _entry : replicaMap.entrySet()){
                        String[] requestAux = _entry.getValue().toString().split("\\|");
                        String userAux = requestAux[1];
                        if(requestAux[0].equals("nft_request")){
                        if(userID.equals(userAux)) exists = false;}
                    }
                    System.out.println(exists);
                    System.out.println(isDateValid);
                    System.out.println(areCoinsFromUser);
                    if (exists && isDateValid && areCoinsFromUser && coinsSumValue >= Integer.parseInt(requestTransfer[5])){
                        V oldV = replicaMap.put(request.getKey(), request.getValue());
                        System.out.println(replicaMap.get(request.getKey()));
                        if(oldV != null) {
                            response.setValue(oldV);
                        }else {
                        	response.setValue(request.getKey());
                        }
                        
                        return BFTMapMessage.toBytes(response);
                    }
                    System.out.println("erro a colocar proposta na lista");
                    return BFTMapMessage.toBytes(request);
                
                case PROCESS_NFT_TRANSFER:
                    Set<K> keys = replicaMap.keySet();
                    //String transfer = "nft_process" + "|" + nftID + "|" + buyerID + "|" + acceptStr;
                    String[] requestProcess = request.getValue().toString().split("\\|");
                    boolean isNFTowner = false;   
                    boolean buyerHasOffer = false;
                    String nftId = requestProcess[1];
                    String buyerId = requestProcess[2];
                    boolean accept = Boolean.parseBoolean(requestProcess[3]);
                    //String nft = "nft"+ "|" + clientId + "|" + name +"|" + uri; 
                    String[] nft = replicaMap.get(Integer.parseInt(nftId)).toString().split("\\|");
                    boolean isValid = false;
                    String offerValue = "";
                    String[] coins = new String[100];

                    if (msgCtx.equals(nft[1])) isNFTowner = true;
                

                    //String request = "nft_request" + "|" + clientId + "|" + nftID + "|" + coins + "|" + validity + "|" + value; 
                    for (K key : keys){
                        String[] offer = replicaMap.get(key).toString().split("\\|");

                        if(offer[0].equals("nft_request") && offer[1].equals(requestProcess[2]) && offer[2].equals(nftId)){
                            coins = offer[3].split(",");
                            offerValue = offer[5]; 
                            buyerHasOffer = true;
                            SimpleDateFormat sdformat = new SimpleDateFormat("MM/dd/yyyy");
                            Date d1;
                            try {
                                d1 = sdformat.parse(offer[4]);
                                Date d2 = new Date(); 
                                if(d1.compareTo(d2)>0) isValid = true; 
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            
                        }
                    }

                    if(isNFTowner && buyerHasOffer && isValid && requestProcess[3].equals("true")){
                        int sumCoins = 0;
                        for (int i = 0; i < coins.length; i ++){ // falta verificar a existencia delas
                            sumCoins += Integer.parseInt(replicaMap.get(Integer.parseInt(coins[i])).toString().split("\\|")[2]);
                        }

                        int nftRequestValue = Integer.parseInt(offerValue);

                        if (nftRequestValue == sumCoins){
                            nftRequestValue = nftRequestValue - sumCoins;


                            //create new coin for the receiver 
                		    V receiver_coin = (V) ("coin"+ "|" + requestProcess[2] + "|" + offerValue);
                		    replicaMap.put(request.getKey(), receiver_coin);


                            for (String usedCoins : coins) {
                                replicaMap.remove(Integer.parseInt(usedCoins));
                            }

                            V newVal = (V) ("nft"+ "|" + requestProcess[2] + "|" + nft[2] + "|" + nft[3]); 
                            V oldV = replicaMap.put((K) nftId , newVal);

                            if(oldV != null) {
                                response.setValue(oldV);
                            }else {
                                response.setValue(request.getKey());
                            }
                            
                            return BFTMapMessage.toBytes(response);


                        } else if(nftRequestValue < sumCoins){
                            int rest = nftRequestValue - sumCoins;

                            //create new coin for the receiver 
                		    V receiver_coin = (V) ("coin"+ "|" + requestProcess[2] + "|" + offerValue);
                		    replicaMap.put(request.getKey(), receiver_coin);

                            //remove all sender coins used in the transaction
                		    for (String usedCoins : coins) {
                			    replicaMap.remove(Integer.parseInt(usedCoins));
                            }

                            //create new coin for the sender with the remaining value
                		    V sender_coin = (V) ("coin"+ "|" + buyerId + "|" + rest); 
                		    K key = (K) Integer.valueOf(Integer.valueOf(request.getKey().toString()) + 1);
                		    V oldVal = replicaMap.put(key, sender_coin);

                            V newVal = (V) ("nft"+ "|" + requestProcess[2] + "|" + nft[2] + "|" + nft[3]); 
                            replicaMap.put((K) nftId , newVal);

                            if(oldVal != null) {
                                response.setValue(oldVal);
                            }else {
                                response.setValue(request.getKey());
                            }
                            
                            return BFTMapMessage.toBytes(response);

                        }

                    }

                    return BFTMapMessage.toBytes(request);    
            }
            
            return BFTMapMessage.toBytes(request);
        }catch (IOException | ClassNotFoundException ex) {
            logger.error("Failed to process ordered request", ex);
            return new byte[0];
        }
    }

    @Override
    public byte[] appExecuteUnordered(byte[] command, MessageContext msgCtx) {
        try {
            BFTMapMessage<K,V> response = new BFTMapMessage<>();
            BFTMapMessage<K,V> request = BFTMapMessage.fromBytes(command);
            BFTMapRequestType cmd = request.getType();

            logger.info("Unordered execution of a {} request from {}", cmd, msgCtx.getSender());

            switch (cmd) {
                //read operations on the map
                case GET:
                    V ret = replicaMap.get(request.getKey());

                    if (ret != null) {
                        response.setValue(ret);
                    }
                    return BFTMapMessage.toBytes(response);
                
                case KEYSET:
                    Set<K> keySet = replicaMap.keySet();
                    response.setKeySet(keySet);

                    return BFTMapMessage.toBytes(response);
                
                case MY_NFT_REQUESTS:
                    K nft = request.getKey();
                    V nftOwner = replicaMap.get(nft);

                    if (nftOwner.equals(msgCtx.getSender())){
                        Map<K, V> purchaseOffers = new HashMap<>();

                        for (Map.Entry<K,V> entry : replicaMap.entrySet()) {
                            K key = entry.getKey();
                            V value = entry.getValue();
                            System.out.println(key);
                            System.out.println(value);

                            if (key instanceof String && ((String) key).startsWith("nft_request") && value instanceof String){
                                String[] offerTokens = ((String) value).split("\\|");

                                if (offerTokens.length == 3 && offerTokens[0].equals(nft.toString())){
                                    BFTMapMessage<K, V>  offer = new BFTMapMessage<>();
                                    offer.setKey((K) offerTokens[1]);
                                    offer.setValue((V) (offerTokens[2] + "|" + offerTokens[3]));
                                    purchaseOffers.put(offer.getKey(), offer.getValue());

                                }

                            }
                        }

                        response.setValue(purchaseOffers);
                        return BFTMapMessage.toBytes(response);
                    } else {
                        logger.info("Caller {} is not the owner of NFT {}", msgCtx.getSender(), nft);
                        return null;
                    }
            }
        } catch (IOException | ClassNotFoundException ex) {
            logger.error("Failed to process unordered request", ex);
            return new byte[0];
        }
        return null;
    }

    @Override
    public byte[] getSnapshot() {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutput out = new ObjectOutputStream(bos)) {
            out.writeObject(replicaMap);
            out.flush();
            bos.flush();
            return bos.toByteArray();
        } catch (IOException ex) {
            ex.printStackTrace(); //debug instruction
            return new byte[0];
        }
    }

    @Override
    public void installSnapshot(byte[] state) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(state);
             ObjectInput in = new ObjectInputStream(bis)) {
            replicaMap = (TreeMap<K, V>) in.readObject();
        } catch (ClassNotFoundException | IOException ex) {
            ex.printStackTrace(); //debug instruction
        }
    }

    boolean isCoinFromUser(String coinID, int userID){
        if(coinID.equals("") || coinID.equals(" ")) return true;
        String[] coin = replicaMap.get(Integer.parseInt(coinID)).toString().split("\\|");
        if(Integer.parseInt(coin[1]) == userID) return true;
    return false;
    }
}
