package servlet;

import DAO.AlbumDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Album;
import model.Track;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/player")
public class AlbumServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException{
        AlbumDAO albums=new AlbumDAO();
        String id=req.getParameter("id");
        if(id==null||id.trim().isEmpty()){
            res.sendError(HttpServletResponse.SC_BAD_REQUEST,"Missing or invalid album id");
            return;
        }
        int albumId;
        try {
            albumId = Integer.parseInt(id.trim());
        } catch (NumberFormatException e) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or invalid album id");
            return;
        }
        try {
            Album album=albums.getAlbum(albumId);
            if (album==null){
                res.sendError(HttpServletResponse.SC_NOT_FOUND, "Album not found");
                return;
            }
            List<Track> tracks=albums.getTracks(album.getId());
            req.setAttribute("album",album);
            req.setAttribute("tracks",tracks);
            req.getRequestDispatcher("/index.jsp").forward(req,res);
        }catch (SQLException | ClassNotFoundException e){
            throw new ServletException("Database error", e);
        }
    }
}
