<%@ page import="javax.ws.rs.core.Response"
%><%@ page import="java.io.BufferedReader"
%><%@ page import="java.io.FileReader"
%><%@ page import="java.nio.file.Files"
%><%@ page import="java.nio.file.Path"
%><%@ page import="java.nio.file.Paths"
%><%
    String version = request.getParameter("version");
    if(version == null) {
        response.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
        response.setHeader("X-Message", "Version required");
        return;
    }
    int indexOfDot = version.indexOf('.');
    if(indexOfDot <= 0) {
        response.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
        response.setHeader("X-Message", "Invalid version: " + version);
        return;
    }
    String majorVersion = version.substring(0, indexOfDot);
    String pathOfThisJspFile = request.getServletContext().getRealPath(request.getServletPath());
    Path versionFile = Paths.get(pathOfThisJspFile).getParent().resolve(majorVersion + ".x");
    if(!Files.isRegularFile(versionFile)) {
        response.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
        response.setHeader("X-Message", "Invalid major version: " + majorVersion);
        return;
    }
    try (BufferedReader fileReader = new BufferedReader(new FileReader(versionFile.toFile()))) {
        response.getWriter().write(fileReader.readLine());
    }
%>
