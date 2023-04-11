package bftsmart.dti;

public class nft {
    private long id;
    private int owner;
    private String name;
    private String uri;
    
    public nft(long id, int owner, String name, String uri) {
        this.id = id;
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

    
}
