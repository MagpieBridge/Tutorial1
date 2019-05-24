import magpiebridge.core.IProjectService;
import magpiebridge.core.MagpieServer;
import magpiebridge.core.ServerAnalysis;
import magpiebridge.projectservice.java.JavaProjectService;

/**
 * 
 * @author Linghui Luo
 *
 */
public class HelloWorld {

  public static void main(String... args) {
    MagpieServer server = new MagpieServer();
    String language = "java";
    IProjectService javaProjectService = new JavaProjectService();
    server.addProjectService(language, javaProjectService);
    ServerAnalysis analysis = new SimpleServerAnalysis();
    server.addAnalysis(language, analysis);
    server.launchOnStdio();
  }
}
