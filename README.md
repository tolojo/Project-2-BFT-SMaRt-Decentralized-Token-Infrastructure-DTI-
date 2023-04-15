# Project 2 – BFT-SMaRt Decentralized Token Infrastructure (DTI - 2022/2023)

Tomás Ferreira - Nº 59449; João Rolo - Nº 59450; Catarina Sousa - Nº 54966

#Install 
Inside the project folder execute the following command in a Linux terminal to build:

```
./gradlew installDist
```

#Run
1) Open five terminals (or tabs in a terminal)

2) On the first four, run the following commands (one on each):

```
./smartrun.sh intol.bftmap.BFTMapServer 0
./smartrun.sh intol.bftmap.BFTMapServer 1
./smartrun.sh intol.bftmap.BFTMapServer 2
./smartrun.sh intol.bftmap.BFTMapServer 3
```
3) Wait until first four terminals print “Ready to process operations”

4) On the last terminal, run the following:

```
./smartrun.sh intol.bftmap.BFTMapInteractiveClient <uniqueId>
```
  
## Commands
**MY_COINS**: List of all Coins.

**MINT: ** Create new coin. 
Only user with **id=0** has permission to MINT.

**SPEND: ** Mint a Coin.

**MY_NFTS: ** List of all NFTS.

**MINT_NFT: ** Mint an NFT.

**REQUEST_NFT_TRANSFER: ** Request for NFT transfer.

**CANCEL_REQUEST_NFT_TRANSFER: ** Cancel a transfer request of an NFT.

**MY_NFT_REQUESTS: ** List of all requests for a NFT.

**PROCESS_NFT_TRANSFER: **

