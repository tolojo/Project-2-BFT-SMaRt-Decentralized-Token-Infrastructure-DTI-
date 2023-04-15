/**
 * BFT Map implementation (interactive client).
 *
 */
package intol.bftmap;

import java.io.Console;
import java.io.IOException;
import java.util.Random;
import java.util.Set;

public class BFTMapInteractiveClient {

    public static void main(String[] args) throws IOException {
        Random random = new Random();
        int local_key = random.nextInt(50000);
        int clientId = (args.length > 0) ? Integer.parseInt(args[0]) : 1001;
        BFTMap<Integer, Object> bftMap = new BFTMap<>(clientId);

        Console console = System.console();

        System.out.println("\nCommands:\n");
        System.out.println("\tPUT: Insert value into the map");
        System.out.println("\tGET: Retrieve value from the map");
        System.out.println("\tMY_COINS: List of all coins");
        System.out.println("\tMINT: Mint a Coin");
        System.out.println("\tSPEND: Transfer Coins");
        System.out.println("\tMY_NFTS: List of all NFTS");
        System.out.println("\tMY_NFT_REQUESTS: List of all requests for a NFT");
        System.out.println("\tCANCEL_REQUEST_NFT_TRANSFER: Cancel a transfer request of an NFT if you are the one that placed it");
        System.out.println("\tMINT_NFT: Mint an NFT");
        System.out.println("\tREQUEST_NFT_TRANSFER: Request for NFT transfer");
        System.out.println("\tSIZE: Retrieve the size of the map");
        System.out.println("\tREMOVE: Removes the value associated with the supplied key");
        System.out.println("\tEXIT: Terminate this client\n");

        while (true) {
            String cmd = console.readLine("\n  > ");

            if (cmd.equalsIgnoreCase("PUT")) {

                int key;
                try {
                    key = Integer.parseInt(console.readLine("Enter a numeric key: "));
                } catch (NumberFormatException e) {
                    System.out.println("\tThe key is supposed to be an integer!\n");
                    continue;
                }
                String value = console.readLine("Enter an alpha-numeric value: ");

                //invokes the op on the servers
                bftMap.put(key, value);

                System.out.println("\nkey-value pair added to the map\n");
                
            }else if (cmd.equalsIgnoreCase("MY_COINS")) {
            	Set<Integer> keys = bftMap.keySet();
                System.out.println("\nKeys in the map:");
            	for (int key : keys) {
            		String coin = (String) bftMap.get(key);
                	String[] coinTokens = coin.split("\\|");
                	if(coinTokens[0].equals("coin") && coinTokens[1].equals(String.valueOf(clientId))) {
                		String value = coinTokens[2];
                		System.out.println("Key " + key + " -> value: " + value );
                	}
               	}
            	
            }else if (cmd.equalsIgnoreCase("MINT")) {
            	int value;
                try {
                	value = Integer.parseInt(console.readLine("Enter the value of the coin: "));
                	if(value <= 0) {
                		System.out.println("Invalid input: Please enter an integer value greater than 0!"); 
                		continue;
                	}
                } catch (NumberFormatException e) {
                	System.out.println("Invalid input: The value is supposed to be an integer!"); 
                	continue;
                }
                String coin = "coin"+ "|" + clientId + "|" + value; 

                //invokes the op on the servers
                String resp_value = bftMap.put(local_key, coin).toString();

                System.out.println("\ncoin id: " + resp_value + " created");
                local_key=random.nextInt(50000);;

            }else if (cmd.equalsIgnoreCase("SPEND")) {
            	int receiver;
                try {
                	receiver = Integer.parseInt(console.readLine("Enter the receiver's ID: "));
                } catch (NumberFormatException e) {
                	System.out.println("Invalid input: The ID is supposed to be an integer!"); 
                	continue;
                }
                int value;
                try {
                	value = Integer.parseInt(console.readLine("Enter the value to be transfered: "));
                	if(value <= 0) {
                		System.out.println("Invalid input: Please enter an integer value greater than 0!"); 
                		continue;
                	}
                } catch (NumberFormatException e) {
                	System.out.println("Invalid input: The value is supposed to be an integer!"); 
                	continue;
                }
                String coins;
                try {
                	coins = console.readLine("Enter the ids of the coins (separated by a comma): ");
                } catch (NumberFormatException e) {
                	System.out.println("Invalid input: The value is supposed to be the ids of the coins (separated by a comma)!"); 
                	continue;
                }
                String[] coins_list = coins.split(",");
                for (String coin : coins_list) {
            		// Check that each value is numeric
            		try {
                        Integer.parseInt(coin);
                    } catch (NumberFormatException e) {
                    	System.out.println("Invalid input: The ID is supposed to be an integer!");
                    	continue;
                    }             	
            	}
                String spend = "spend" + "|" + clientId + "|" + coins + "|" + receiver + "|" + value; 
                //invokes the op on the servers
                String resp = bftMap.put(local_key, spend).toString();
                
                if(resp.equals("0")) {
                    System.out.println("Invalid operation");
                }else {
                	System.out.println("coin ID: " + resp + " created");
                	local_key=random.nextInt(50000);
                }
            	
            }else if (cmd.equalsIgnoreCase("MINT_NFT")){
                
                String name = console.readLine("Enter the name of the nft: ");

                String uri = console.readLine("Enter the URI of the nft: ");
                String nft = "nft"+ "|" + clientId + "|" + name +"|" + uri; 

                //invokes the op on the servers
                bftMap.put(local_key, nft);

                System.out.println("\nkey-value pair added to the map\n");
                local_key=random.nextInt(50000);

            } 
            else if (cmd.equalsIgnoreCase("REQUEST_NFT_TRANSFER")){
                
                String nftID = console.readLine("Enter the id of the nft: ");
                Boolean _coins = true;
                String coins ="";
                while(_coins){
                    String coin = console.readLine("Enter the ids of the coin you want to use: \n You can type 'DONE' to finish inputing coins ");
                    if (coin.equals("DONE")){
                        _coins= false;
                    }else{
                        coins += coin + ",";
                    }
                }
                String validity = console.readLine("Enter the validity of the transfer request");
                
                String request = "nft_request" + "|" + clientId + "|" + nftID + "|" + coins + "|" + validity; 
                //invokes the op on the servers
                bftMap.put(local_key, request);

                System.out.println("\nkey-value pair added to the map\n");
                local_key=random.nextInt(50000);

            } 
            else if (cmd.equalsIgnoreCase("GET")) {

                int key;
                try {
                    key = Integer.parseInt(console.readLine("Enter a numeric key: "));
                } catch (NumberFormatException e) {
                    System.out.println("\tThe key is supposed to be an integer!\n");
                    continue;
                }

                //invokes the op on the servers
                //String value = bftMap.get(key);

                //System.out.println("\nValue associated with " + key + ": " + value + "\n");

            } else if (cmd.equalsIgnoreCase("MY_NFTS")) {
                Set<Integer> keys = bftMap.keySet();
                System.out.println("\nKeys in the map:");

                for (int key : keys) {
            		String nft = (String) bftMap.get(key);
                	String[] nftTokens = nft.split("\\|");
                	if(nftTokens[0].equals("nft")) {
                		String name = nftTokens[2];
                		String uri = nftTokens[3];
                		System.out.println("Key " + key + " -> name: " + name + " URI: " + uri );
                	}
                    
                }
                                
            } else if (cmd.equalsIgnoreCase("MY_NFT_REQUESTS")) {
                String nft = console.readLine("Enter the id of the nft: ");
                Set<Integer> keys = bftMap.keySet();
                boolean found = false;
                System.out.println("\nNFT requests in the map:");

                for (int key : keys){
                    String request = (String) bftMap.get(key);
                    String[] requestTokens = request.split("\\|");

                    if (requestTokens[0].equals("nft_request") && requestTokens[2].equals(nft)){
                        String coins = requestTokens[3];
                        String validity = requestTokens[4];
                        System.out.println("Key " + key + " -> NFT ID: " + nft + ", Coins: " + coins + ", Validity: " + validity);
                        found = true;
                    }
                }

                if (!found) {
                    System.out.println("NFT " + nft + " not found.");
                }

            } else if (cmd.equalsIgnoreCase("PROCESS_NFT_TRANSFER")) {

                System.out.println("\tYou are supposed to implement this command :)\n");

            } else if (cmd.equalsIgnoreCase("CANCEL_REQUEST_NFT_TRANSFER")){
                String nftID = console.readLine("Enter the id of the nft: ");
                String cancelRequest = "cancel_request"+ "|" + clientId + "|" + nftID; 
                //invokes the op on the servers
                bftMap.remove(cancelRequest);

            } else if (cmd.equalsIgnoreCase("SIZE")) {

                System.out.println("\tYou are supposed to implement this command :)\n");

            } else if (cmd.equalsIgnoreCase("EXIT")) {

                System.out.println("\tEXIT: Bye bye!\n");
                System.exit(0);

            } else {
                System.out.println("\tInvalid command :P\n");
            }
        }
    }

}
