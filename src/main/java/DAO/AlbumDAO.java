package DAO;

import model.Album;
import model.Track;
import model.TrackAudioInfo;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AlbumDAO {
    public Album getAlbum(int id) throws SQLException, ClassNotFoundException {
        String sql = "SELECT id, title, artist, release_year, cover_type, cover_data FROM albums WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
        ) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Album(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4), rs.getString(5), rs.getBytes(6));
                } else {
                    return null;
                }
            }
        }
    }

    public List<Track> getTracks(int albumId) throws SQLException, ClassNotFoundException {
        String sql = "SELECT id, album_id, track_number, title, audio_type, lyrics, lyrics_lrc, duration_seconds "
                + "FROM tracks WHERE album_id = ? ORDER BY track_number";
        List<Track> tracks = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
        ) {
            ps.setInt(1, albumId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int d = rs.getInt("duration_seconds");
                    Integer duration = rs.wasNull() ? null : d;
                    Track t = new Track(
                            rs.getInt("id"),
                            rs.getInt("album_id"),
                            rs.getInt("track_number"),
                            rs.getString("title"),
                            rs.getString("audio_type"),
                            null,
                            rs.getString("lyrics"),
                            rs.getString("lyrics_lrc"),
                            duration
                    );
                    tracks.add(t);
                }
                return tracks;
            }
        }
    }

    public Track getTrackWithAudio(int trackId) throws SQLException, ClassNotFoundException {
        String sql = "SELECT id, album_id, track_number, title, audio_type, audio_data FROM tracks WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
        ) {
            ps.setInt(1, trackId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Track(
                            rs.getInt("id"),
                            rs.getInt("album_id"),
                            rs.getInt("track_number"),
                            rs.getString("title"),
                            rs.getString("audio_type"),
                            rs.getBytes("audio_data")
                    );
                } else {
                    return null;
                }
            }
        }
    }

    public Album getAlbumByToken(String token) throws SQLException, ClassNotFoundException {
        String sql = "SELECT id, title, artist, release_year, cover_type, cover_data FROM albums WHERE token = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
        ) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Album(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4), rs.getString(5), rs.getBytes(6));
                } else {
                    return null;
                }
            }
        }
    }

    public Track getTrackWithAudioForToken(int trackId, String token) throws SQLException, ClassNotFoundException {
        String sql = "SELECT t.id, t.album_id, t.track_number, t.title, t.audio_type, t.audio_data\n" +
                "FROM tracks t\n" +
                "JOIN albums a ON a.id = t.album_id\n" +
                "WHERE t.id = ? AND a.token = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
        ) {
            ps.setInt(1, trackId);
            ps.setString(2, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Track(
                            rs.getInt("id"),
                            rs.getInt("album_id"),
                            rs.getInt("track_number"),
                            rs.getString("title"),
                            rs.getString("audio_type"),
                            rs.getBytes("audio_data")
                    );
                } else {
                    return null;
                }
            }
        }
    }

    public TrackAudioInfo getTrackInfoForToken(int trackId, String token) throws SQLException, ClassNotFoundException {
        String sql = "SELECT t.id,t.audio_type,LENGTH(t.audio_data) as audioLength\n" +
                "FROM tracks t\n" +
                "JOIN albums a ON a.id = t.album_id\n" +
                "WHERE t.id = ? AND a.token = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
        ) {
            ps.setInt(1, trackId);
            ps.setString(2, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new TrackAudioInfo(
                            rs.getInt("id"),
                            rs.getString("audio_type"),
                            rs.getInt("audioLength")
                    );
                }
                return null;
            }
        }
    }

    public byte[] getAudioBytes(int trackId) throws SQLException, ClassNotFoundException {
        String sql = "SELECT audio_data FROM tracks WHERE id = ?";
        try(
        Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);
        ) {
            ps.setInt(1, trackId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBytes("audio_data");
                }
                return null;
            }
        }
    }
}
