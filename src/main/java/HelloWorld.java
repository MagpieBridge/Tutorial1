import org.eclipse.lsp4j.jsonrpc.messages.Either;

import magpiebridge.core.IProjectService;
import magpiebridge.core.MagpieServer;
import magpiebridge.core.ServerAnalysis;
import magpiebridge.core.ServerConfiguration;
import magpiebridge.core.ToolAnalysis;
import magpiebridge.projectservice.java.JavaProjectService;

/**
 * 
 * @author Linghui Luo
 *
 */
public class HelloWorld {

  public static void main(String... args) {
    MagpieServer server = new MagpieServer(new ServerConfiguration());
    String language = "java";
    IProjectService javaProjectService = new JavaProjectService();
    server.addProjectService(language, javaProjectService);
    ServerAnalysis myAnalysis = new SimpleServerAnalysis();
    Either<ServerAnalysis, ToolAnalysis> analysis=Either.forLeft(myAnalysis);
	server.addAnalysis(analysis,language);
    server.launchOnStdio();
  }
}
