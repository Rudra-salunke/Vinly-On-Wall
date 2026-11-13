package model;

public class Album {
    private int id;
    private String title;
    private String artist;
    private int releaseYear;
    private String coverType;
    private byte[] coverData;
    public Album(int id, String title, String artist, int releaseYear, String coverType, byte[] coverData){
        this.id=id;
        this.title=title;
        this.artist=artist;
        this.releaseYear = releaseYear;
        this.coverType=coverType;
        this.coverData=coverData;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    public String getCoverType() {
        return coverType;
    }

    public void setCoverType(String coverType) {
        this.coverType = coverType;
    }

    public byte[] getCoverData() {
        return coverData;
    }

    public void setCoverData(byte[] coverData) {
        this.coverData = coverData;
    }

    public String getCoverBase64() {
        if (coverData == null) return null;
        return java.util.Base64.getEncoder().encodeToString(coverData);
    }
}
