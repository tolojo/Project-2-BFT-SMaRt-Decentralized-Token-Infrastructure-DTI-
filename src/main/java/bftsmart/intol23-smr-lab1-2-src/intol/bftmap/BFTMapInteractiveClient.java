/**
 * BFT Map implementation (interactive client).
 *
 */
package intol.bftmap;

import java.io.Console;
import java.io.IOException;
import java.util.Set;

import bftsmart.dti.nft;

public class BFTMapInteractiveClient {

    public static void main(String[] args) throws IOException {
        int local_key = 0;
        int clientId = (args.length > 0) ? Integer.parseInt(args[0]) : 1001;
        BFTMap<Integer, Object> bftMap = new BFTMap<>(clientId);

        Console console = System.console();

        System.out.println("\nCommands:\n");
        System.out.println("\tPUT: Insert value into the map");
        System.out.println("\tGET: Retrieve value from the map");
        System.out.println("\tMY_COINS: List of all coins");
        System.out.println("\tMINT: Mint a Coin");
        System.out.println("\tSPEND: Transfer Coins");
        System.out.println("\tMINT_NFT: Mint an NFT");
        System.out.println("\tSIZE: Retrieve the size of the map");
        System.out.println("\tREMOVE: Removes the value associated with the supplied key");
        System.out.println("\tMY_NFTS: List of all NFTS");
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
                	if(coinTokens[0].equals("coin")) {
                		String value = coinTokens[2];
                		System.out.println("Key " + key + " -> value: " + value );
                	}
               	}
            	
            }else if (cmd.equalsIgnoreCase("MINT")) {
            	String value = console.readLine("Enter the value of the coin: ");
                String coin = "coin"+ "|" + clientId + "|" + value; 

                //invokes the op on the servers
                String resp_value = bftMap.put(local_key, coin).toString();

                System.out.println("\ncoin id: " + resp_value + " created");
                local_key+=1;

            }else if (cmd.equalsIgnoreCase("SPEND")) {
            	int sum_coins = 0;
            	String[] coins = console.readLine("Enter the ids of the coins (separated by a comma): ").split(",");
            	int receiver = Integer.parseInt(console.readLine("Enter the receiver's ID: "));
            	int value = Integer.parseInt(console.readLine("Enter the value to be transfered: "));
            	System.out.println(coins[0] + coins[1]);
            	for (String coin : coins) {
                	String coin_value = (String) bftMap.get(Integer.parseInt(coin));
                	sum_coins += Integer.parseInt(coin_value.split("\\|")[2]);
            	}
            	if(sum_coins >= value) {
            		String coin = "coin"+ "|" + receiver + "|" + value; 
            		String resp_value = bftMap.put(local_key, coin).toString();
                    System.out.println("\ncoin id: " + resp_value + " created in user: " + receiver);
                    local_key+=1;
                    
                    //new_coin = sum_coins - value;
            	}else {
                	System.out.println("Not enough coins");
            	}
            	
            }else if (cmd.equalsIgnoreCase("MINT_NFT")){
                
                String name = console.readLine("Enter the name of the nft: ");

                String uri = console.readLine("Enter the URI of the nft: ");
                String nft = "nft"+ "|" + clientId + "|" + name +"|" + uri; 

                //invokes the op on the servers
                bftMap.put(local_key, nft);

                System.out.println("\nkey-value pair added to the map\n");
                local_key+=1;

            } else if (cmd.equalsIgnoreCase("GET")) {

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

                System.out.println("\tYou are supposed to implement this command :)\n");

            } else if (cmd.equalsIgnoreCase("PROCESS_NFT_TRANSFER")) {

                System.out.println("\tYou are supposed to implement this command :)\n");

            } else if (cmd.equalsIgnoreCase("REMOVE")) {

                System.out.println("\tYou are supposed to implement this command :)\n");

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
