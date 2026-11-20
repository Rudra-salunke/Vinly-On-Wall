package servlet;

import DAO.AlbumDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Album;
import model.Track;
import util.TokenValidator;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@WebServlet("/player")
public class AlbumServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException{
        AlbumDAO albums=new AlbumDAO();
        String token=req.getParameter("token");
        if(token==null||token.isBlank()){
            res.sendError(HttpServletResponse.SC_BAD_REQUEST,"Missing token");
            return;
        }
        token = token.trim().toLowerCase(Locale.ROOT);
        if (!TokenValidator.isValid(token)) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND, "Not found");
            return;
        }
        try {
            Album album = albums.getAlbumByToken(token);
            if (album == null) {
                res.sendError(HttpServletResponse.SC_NOT_FOUND, "Not found");
                return;
            }
            List<Track> tracks=albums.getTracks(album.getId());
            req.setAttribute("album",album);
            req.setAttribute("tracks",tracks);
            req.setAttribute("token",token);
            req.getRequestDispatcher("/index.jsp").forward(req,res);
        }catch (SQLException | ClassNotFoundException e){
            throw new ServletException("Database error", e);
        }
    }
}
