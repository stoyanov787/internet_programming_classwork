package com.example.personal_profile_showcase;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.logging.Logger;

@MultipartConfig
@WebServlet(name = "profileServlet", value = "/profile")
public class ProfileServlet extends HttpServlet {
    private Logger logger;

    public void init() {
        logger = Logger.getLogger(ProfileServlet.class.getName());
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String theme = "null"; // by default
        for (Cookie cookie : request.getCookies()) {
            if(cookie.getName().equals("theme")) {
                theme = cookie.getValue();
            }
        }
        logger.info("Theme: " + theme);

        HttpSession session = request.getSession();
        String name = (String)session.getAttribute("name");
        logger.info("Name: " + name);

        if(name == null) {
            response.setContentType("text/html");
            RequestDispatcher view = request.getRequestDispatcher("profile.html");
            view.forward(request, response);
        } else {
            PrintWriter out = response.getWriter();
            if(!theme.equals("on")) out.println("<html><body>");
            else out.println("<html><head>\n" +
                    "    <title>Profile In</title>\n" +
                    "    <style>\n" +
                    "        body {\n" +
                    "            background-color: black;\n" +
                    "            color: white;\n" +
                    "        }\n" +
                    "    </style>\n" +
                    "</head><body>");
            out.println("<h1> Hello " + name + "</h1>");
            out.println("<h1> Email " + session.getAttribute("email") + "</h1>");
            String profilePicture = ((String) session.getAttribute("profile_picture"));
            logger.info("Profile picture: " + profilePicture);
            out.println("<img src=" + profilePicture + ">");
            String video = ((String) session.getAttribute("video"));
            logger.info("Video: " + video);
            out.println("<video controls>\n" +
                    "<source src=" + video + "type=\"video/mp4\">\n" +
                    "Your browser does not support the video tag.\n" +
                    "</video>");
            String song = ((String) session.getAttribute("song"));
            logger.info("Song: " + song);
            out.println("<audio controls>\n" +
                    " <source src=" + song + "type=\"audio/mp3\">\n" +
                    "Your browser does not support the audio element.\n" +
                    "</audio>");
            out.println("</body></html>");
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String applicationPath = getServletContext().getRealPath("");
        String uploadFilePath = applicationPath + "uploaded_files";
        File uploadDir = new File(uploadFilePath);
        if (!uploadDir.exists()) uploadDir.mkdir();
        logger.info("Location of uploaded files: " + uploadFilePath);

        String name = request.getParameter("name");
        logger.info("Name: " + name);
        String email = request.getParameter("email");
        logger.info("Email: " + email);
        String theme = request.getParameter("theme"); // "null" or "on"
        logger.info("Theme: " + theme);

        Cookie themeCookie = new Cookie("theme", theme);
        response.addCookie(themeCookie);

        HttpSession session = request.getSession();
        session.setAttribute("name", name);
        session.setAttribute("email", email);

        String[] fileTypes = {"profile_picture", "song", "video"};
        HashMap<String, String> fileTypesAccept = new HashMap<>();
        fileTypesAccept.put("profile_picture", "image");
        fileTypesAccept.put("song", "audio");
        fileTypesAccept.put("video", "video");

        for (String fileType : fileTypes) {
            Part filePart = request.getPart(fileType);
            String contentType = filePart.getContentType();
            if (!contentType.startsWith(fileTypesAccept.get(fileType))) {
                logger.warning("The uploaded file is not a " + fileTypesAccept.get(fileType));
                printMessage(response, "Unsupported file type: " + contentType);
            } else {
                String fileName = filePart.getSubmittedFileName();
                String filePath = uploadFilePath + File.separator + fileName;
                filePart.write(filePath);
                logger.info("The file has been saved to " + filePath);
                session.setAttribute(fileType, "\"uploaded_files" + File.separator + fileName + "\"");
            }
        }
    }

    private void printMessage(HttpServletResponse response, String message) throws IOException {
        PrintWriter out = response.getWriter();
        out.println("<html><body>");
        out.println("<h1>" + message + "</h1>");
        out.println("</body></html>");
    }

    public void destroy() {
    }
}
