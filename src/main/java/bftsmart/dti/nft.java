package bftsmart.dti;

public class nft {

    private int owner;
    private String name;
    private String uri;
    
    public nft(int owner, String name, String uri) {
        this.owner = owner;
        this.name = name;
        this.uri = uri;
    }

    public int getOwner() {
        return owner;
    }

    public void setOwner(int owner) {
        this.owner = owner;
    }

    public String getName(){
        return name;
    }

    
}
