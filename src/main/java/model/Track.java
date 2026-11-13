package model;

public class Track {
    private int id;
    private int albumId;
    private int trackNumber;
    private String title;
    private String audioType;
    private byte[] audioData;
    public Track(int id,int albumId,int trackNumber,String title,String audioType,byte[] audioData){
        this.id=id;
        this.albumId=albumId;
        this.trackNumber=trackNumber;
        this.title=title;
        this.audioType=audioType;
        this.audioData=audioData;
    }
    public Track(int id, int albumId, int trackNumber, String title, String audioType) {
        this(id, albumId, trackNumber, title, audioType, null);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAlbumId() {
        return albumId;
    }

    public void setAlbumId(int albumId) {
        this.albumId = albumId;
    }

    public int getTrackNumber() {
        return trackNumber;
    }

    public void setTrackNumber(int trackNumber) {
        this.trackNumber = trackNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAudioType() {
        return audioType;
    }

    public void setAudioType(String audioType) {
        this.audioType = audioType;
    }

    public byte[] getAudioData() {
        return audioData;
    }

    public void setAudioData(byte[] audioData) {
        this.audioData = audioData;
    }
}

