import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class TestServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        ServletContext context = getServletContext();
        PrintWriter out = response.getWriter();
        out.write("REMOTE_USER header: " + request.getHeader("REMOTE_USER"));
        out.write("REMOTE_USER getRemoteUser: " + request.getRemoteUser());
        out.write("REMOTE_USER getAttribute: " + request.getAttribute("REMOTE_USER"));


    }
}
