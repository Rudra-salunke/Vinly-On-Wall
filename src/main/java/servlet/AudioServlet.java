package servlet;

import DAO.AlbumDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Track;
import util.TokenValidator;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;
import java.util.regex.Pattern;

@WebServlet("/audio")
public class AudioServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        String id = req.getParameter("id");
        String token = req.getParameter("token");
        if (id == null || id.isBlank() || token == null || token.isBlank()) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND, "Not found");
            return;
        }

        token = token.trim().toLowerCase(Locale.ROOT);
        if (!TokenValidator.isValid(token)) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND, "Not found");
            return;
        }

        int trackId;
        try {
            trackId = Integer.parseInt(id.trim());
        } catch (NumberFormatException e) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND, "Not found");
            return;
        }

        Track track;
        try {
            track = new AlbumDAO().getTrackWithAudioForToken(trackId, token);
        } catch (SQLException | ClassNotFoundException e) {
            throw new ServletException("Database error", e);
        }
        if (track == null) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND, "Not found");
            return;
        }

        byte[] data = track.getAudioData();
        int total = data.length;
        int start = 0;
        int end = total - 1;
        boolean isRange = false;
        String range = req.getHeader("Range");
        if (range != null && range.startsWith("bytes=")) {
            isRange = true;
            String[] parts = range.substring(6).split("-", -1);
            try {
                if (parts.length != 2) {
                    throw new NumberFormatException("bad range");
                }
                if (parts[0].isEmpty()) {
                    int lastN = Integer.parseInt(parts[1]);
                    start = Math.max(0, total - lastN);
                } else {
                    start = Integer.parseInt(parts[0]);
                    if (!parts[1].isEmpty()) {
                        end = Integer.parseInt(parts[1]);
                    }
                }
            } catch (NumberFormatException e) {
                res.setStatus(416);
                res.setHeader("Content-Range", "bytes */" + total);
                return;
            }
            if (end > total - 1) end = total - 1;
            if (start > end) {
                res.setStatus(416);
                res.setHeader("Content-Range", "bytes */" + total);
                return;
            }
        }
        int length = end - start + 1;

        res.setContentType(track.getAudioType());
        res.setHeader("Accept-Ranges", "bytes");
        res.setContentLength(length);
        if (isRange) {
            res.setStatus(206);
            res.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + total);
        }
        res.getOutputStream().write(data, start, length);
    }
}
