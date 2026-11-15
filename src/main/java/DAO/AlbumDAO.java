package DAO;

import model.Album;
import model.Track;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AlbumDAO {
    public Album getAlbum(int id) throws SQLException, ClassNotFoundException{
        String sql="SELECT id, title, artist, release_year, cover_type, cover_data FROM albums WHERE id = ?";
        try(Connection con= DBConnection.getConnection();
            PreparedStatement ps=con.prepareStatement(sql);
        ){
            ps.setInt(1,id);
            try(ResultSet rs= ps.executeQuery()){
                if (rs.next()){
                    Album a=new Album(rs.getInt(1),rs.getString(2),rs.getString(3),rs.getInt(4),rs.getString(5),rs.getBytes(6));
                    return a;
                }
                else{
                    return null;
                }
            }
        }
    }
    public List<Track> getTracks(int albumId) throws SQLException, ClassNotFoundException {
        String sql="SELECT id, album_id, track_number, title, audio_type FROM tracks WHERE album_id = ? ORDER BY track_number";
        List<Track> tracks=new ArrayList<>();
        try(Connection con= DBConnection.getConnection();
            PreparedStatement ps=con.prepareStatement(sql);
        ){
            ps.setInt(1,albumId);
            try(ResultSet rs= ps.executeQuery()) {
                while (rs.next()) {
                    Track t = new Track(
                            rs.getInt("id"),
                            rs.getInt("album_id"),
                            rs.getInt("track_number"),
                            rs.getString("title"),
                            rs.getString("audio_type")
                    );
                    tracks.add(t);
                }
                return tracks;
            }
        }
    }
    public Track getTrackWithAudio(int trackId)throws SQLException, ClassNotFoundException{
        String sql="SELECT id, album_id, track_number, title, audio_type, audio_data FROM tracks WHERE id = ?";
        try(Connection con= DBConnection.getConnection();
            PreparedStatement ps=con.prepareStatement(sql);
        ){
            ps.setInt(1,trackId);
            try(ResultSet rs= ps.executeQuery()) {
                if(rs.next()) {
                    return new Track(
                            rs.getInt("id"),
                            rs.getInt("album_id"),
                            rs.getInt("track_number"),
                            rs.getString("title"),
                            rs.getString("audio_type"),
                            rs.getBytes("audio_data")
                    );
                }else{
                    return null;
                }
            }
        }
    }
}
