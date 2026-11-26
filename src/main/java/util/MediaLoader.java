package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.*;
import java.util.Arrays;
import java.util.regex.Pattern;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioFile;

public class MediaLoader {

    // ===== EDIT THESE =====
    private static final String MEDIA_FOLDER = "C:\\vinyl-media\\animals";
    private static final int    ALBUM_ID     = 1;
    private static final String ALBUM_TITLE  = "Animals";
    private static final String ALBUM_ARTIST = "Pink Floyd";
    private static final int    ALBUM_YEAR   = 1977;
    private static final java.util.logging.Logger TAG_LOGGER =
            java.util.logging.Logger.getLogger("org.jaudiotagger");

    public static void main(String[] args) throws Exception {
        TAG_LOGGER.setLevel(java.util.logging.Level.OFF);
        File folder = new File(MEDIA_FOLDER);
        if (!folder.isDirectory()) {
            System.out.println("Folder not found: " + MEDIA_FOLDER);
            return;
        }

        File[] mp3s = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3"));
        if (mp3s == null || mp3s.length == 0) {
            System.out.println("No .mp3 files found in " + MEDIA_FOLDER);
            return;
        }
        Arrays.sort(mp3s);

        File cover = findCover(folder);

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            try {
                saveAlbum(con, cover);
                deleteOldTracks(con);

                for (File mp3 : mp3s) {
                    insertTrack(con, mp3);
                }
                String token=ensureToken(ALBUM_ID,con);
                con.commit();
                System.out.println("Done. Loaded " + mp3s.length + " tracks.");
                System.out.println("The token for this album is /player?token="+token);
            } catch (Exception e) {
                con.rollback();
                System.out.println("FAILED, nothing was saved. Reason: " + e.getMessage());
                throw e;
            }
        }
    }

    private static File findCover(File folder) {
        String[] names = {"cover.jpg", "cover.jpeg", "cover.png"};
        for (String n : names) {
            File f = new File(folder, n);
            if (f.isFile()) return f;
        }
        System.out.println("No cover image found (cover.jpg / cover.png). Album will have no cover.");
        return null;
    }

    private static void saveAlbum(Connection con, File cover) throws Exception {
        byte[] coverBytes = null;
        String coverType = null;

        if (cover != null) {
            coverBytes = Files.readAllBytes(cover.toPath());
            coverType = cover.getName().toLowerCase().endsWith(".png") ? "image/png" : "image/jpeg";
        }

        String update = "UPDATE albums SET title = ?, artist = ?, release_year = ?, "
                + "cover_type = ?, cover_data = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(update)) {
            ps.setString(1, ALBUM_TITLE);
            ps.setString(2, ALBUM_ARTIST);
            ps.setInt(3, ALBUM_YEAR);
            ps.setString(4, coverType);
            ps.setBytes(5, coverBytes);
            ps.setInt(6, ALBUM_ID);

            if (ps.executeUpdate() > 0) {
                System.out.println("Updated album " + ALBUM_ID + ": " + ALBUM_TITLE);
                return;
            }
        }
        String insert = "INSERT INTO albums (id, title, artist, release_year, cover_type, cover_data,token) "
                + "VALUES (?, ?, ?, ?, ?, ?,?)";
        try (PreparedStatement ps = con.prepareStatement(insert)) {
            ps.setInt(1, ALBUM_ID);
            ps.setString(2, ALBUM_TITLE);
            ps.setString(3, ALBUM_ARTIST);
            ps.setInt(4, ALBUM_YEAR);
            ps.setString(5, coverType);
            ps.setBytes(6, coverBytes);
            ps.setString(7,TokenGenerator.generateToken());
            ps.executeUpdate();
            System.out.println("Created album " + ALBUM_ID + ": " + ALBUM_TITLE);
        }
    }

    private static void deleteOldTracks(Connection con) throws Exception {
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM tracks WHERE album_id = ?")) {
            ps.setInt(1, ALBUM_ID);
            int removed = ps.executeUpdate();
            System.out.println("Removed " + removed + " old track(s).");
        }
    }

    private static Integer readDurationSeconds(File mp3) {
        try {
            AudioFile audioFile = AudioFileIO.read(mp3);
            return audioFile.getAudioHeader().getTrackLength();
        } catch (Exception e) {
            System.out.println("WARNING: could not read duration of " + mp3.getName());
            return null;
        }
    }

    private static void insertTrack(Connection con, File mp3) throws Exception {
        String name = mp3.getName();
        name = name.substring(0, name.length() - 4); // drop ".mp3"

        int dash = name.indexOf('-');
        if (dash < 1) {
            throw new IllegalArgumentException(
                    "File name must look like NN-Title.mp3 but was: " + mp3.getName());
        }

        int trackNumber = Integer.parseInt(name.substring(0, dash).trim());
        String title = name.substring(dash + 1).trim();
        String lyrics = readLyrics(mp3);
        String lrc = readLrc(mp3);
        Integer duration = readDurationSeconds(mp3);

        String sql = "INSERT INTO tracks "
                + "(album_id, track_number, title, audio_type, audio_data, lyrics, lyrics_lrc, duration_seconds) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql);
             InputStream in = new FileInputStream(mp3)) {

            ps.setInt(1, ALBUM_ID);
            ps.setInt(2, trackNumber);
            ps.setString(3, title);
            ps.setString(4, "audio/mpeg");
            ps.setBinaryStream(5, in, mp3.length());
            if (lyrics != null){
                ps.setString(6, lyrics);
            } else{
                ps.setNull(6, Types.LONGVARCHAR);
            }
            if (lrc != null){
                ps.setString(7, lrc);
            } else {
                ps.setNull(7, Types.LONGVARCHAR);
            }
            if (duration != null) {
                ps.setInt(8, duration);
            } else {
                ps.setNull(8, Types.INTEGER);
            }
            ps.executeUpdate();

            System.out.println("Inserted track " + trackNumber + ": " + title
                    + " (" + (mp3.length() / 1024) + " KB), lyrics: "
                    + (lyrics != null ? lyrics.length() + " chars" : "none")
                    + ", synced: " + (lrc != null ? lrc.length() + " chars" : "none"));
        }
    }
    private static String ensureToken(int id,Connection con) throws Exception{
        String SQL="UPDATE albums SET token = ? WHERE id = ? AND (token IS NULL OR token = '')";
        try (PreparedStatement ps = con.prepareStatement(SQL)){
            ps.setString(1,TokenGenerator.generateToken());
            ps.setInt(2,id);
            ps.executeUpdate();
        }
        String getSql = "SELECT token FROM albums WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(getSql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("token");
                }
                throw new SQLException("Album " + id + " not found while reading token");
            }
        }
    }
    private static final int MAX_LYRICS_BYTES = 60_000;
    private static final Pattern LRC_LINE = Pattern.compile("^\\[\\d{1,3}:\\d{2}([.:]\\d{1,3})?\\].*");

    private static String readSidecar(File mp3, String ext) {
        String mp3Name = mp3.getName();
        String sideName = mp3Name.substring(0, mp3Name.length() - 4) + ext;
        File side = new File(mp3.getParentFile(), sideName);

        if (!side.isFile()) {
            return null;
        }

        try {
            String text = Files.readString(side.toPath(), StandardCharsets.UTF_8);

            if (text.startsWith("\uFEFF")) {
                text = text.substring(1);
            }
            text = text.replace("\r\n", "\n").trim();

            if (text.isEmpty()) {
                return null;
            }

            int bytes = text.getBytes(StandardCharsets.UTF_8).length;
            if (bytes > MAX_LYRICS_BYTES) {
                System.out.println("WARNING: " + sideName + " is " + bytes
                        + " bytes, close to the TEXT column limit (65535).");
            }
            return text;
        } catch (IOException e) {
            System.out.println("WARNING: could not read " + sideName + " (" + e.getMessage()
                    + "). Storing nothing for this file.");
            return null;
        }
    }
    private static String readLyrics(File mp3) {
        return readSidecar(mp3, ".txt");
    }
    private static String readLrc(File mp3) {
        String text = readSidecar(mp3, ".lrc");
        if (text == null) {
            return null;
        }

        int valid = 0;
        int skipped = 0;
        for (String line : text.split("\n")) {
            String l = line.trim();
            if (l.isEmpty()) {
                continue;
            }
            if (LRC_LINE.matcher(l).matches()) {
                valid++;
            } else {
                skipped++;
            }
        }

        if (valid == 0) {
            System.out.println("WARNING: " + mp3.getName().replace(".mp3", ".lrc")
                    + " has no [mm:ss] lines. It is probably plain text. Storing nothing.");
            return null;
        }
        if (skipped > 0) {
            System.out.println("NOTE: " + skipped + " line(s) in the .lrc do not look like"
                    + " [mm:ss]text. They are stored, and the page will skip them.");
        }
        return text;
    }
}