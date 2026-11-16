package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Arrays;

/**
 * ONE-TIME LOADER. Run its main() from IntelliJ; it is not part of the web app.
 *
 * Expects a folder like:
 *   C:\vinyl-media\animals\
 *       cover.jpg   (or cover.png)
 *       01-Pigs On The Wing (Part 1).mp3
 *       02-Dogs.mp3
 *       ...
 *
 * File name pattern for tracks:  NN-Title.mp3
 * Re-running it is safe: it replaces the tracks of the album each time.
 */
public class MediaLoader {

    // ===== EDIT THESE =====
    private static final String MEDIA_FOLDER = "C:\\vinyl-media\\RubberSoul";
    private static final int    ALBUM_ID     = 2;
    private static final String ALBUM_TITLE  = "RubberSoul";
    private static final String ALBUM_ARTIST = "Beatles";
    private static final int    ALBUM_YEAR   = 1965;

    public static void main(String[] args) throws Exception {
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

                con.commit();
                System.out.println("Done. Loaded " + mp3s.length + " tracks.");
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
        String insert = "INSERT INTO albums (id, title, artist, release_year, cover_type, cover_data) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(insert)) {
            ps.setInt(1, ALBUM_ID);
            ps.setString(2, ALBUM_TITLE);
            ps.setString(3, ALBUM_ARTIST);
            ps.setInt(4, ALBUM_YEAR);
            ps.setString(5, coverType);
            ps.setBytes(6, coverBytes);
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

        String sql = "INSERT INTO tracks (album_id, track_number, title, audio_type, audio_data) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql);
             InputStream in = new FileInputStream(mp3)) {

            ps.setInt(1, ALBUM_ID);
            ps.setInt(2, trackNumber);
            ps.setString(3, title);
            ps.setString(4, "audio/mpeg");
            ps.setBinaryStream(5, in, mp3.length());
            ps.executeUpdate();

            System.out.println("Inserted track " + trackNumber + ": " + title
                    + " (" + (mp3.length() / 1024) + " KB)");
        }
    }
}