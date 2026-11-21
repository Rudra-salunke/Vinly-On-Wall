package model;

public class TrackAudioInfo {
    private final int trackId;
    private final String audioType;
    private final int audioLength;
    public TrackAudioInfo(int trackId,String audioType,int audioLength){
        this.trackId=trackId;
        this.audioType=audioType;
        this.audioLength=audioLength;
    }

    public String getAudioType() {
        return audioType;
    }

    public int getAudioLength() {
        return audioLength;
    }

    public int getTrackId() {
        return trackId;
    }
}
