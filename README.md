# Tutorial1
In this tutorial we show you how to integrate a soot-based analysis into IDEs with MagpieBridge (Please use the [ECOOP19 release](https://github.com/MagpieBridge/MagpieBridge/releases/tag/ECOOP19) for this tutorial.) 

The tutorial project can be found on [`https://github.com/MagpieBridge/Tutorial1.git`](https://github.com/MagpieBridge/Tutorial1)

1. Create an empty maven project
2. Add dependencies as specified in this [pom.xml](https://github.com/MagpieBridge/Tutorial1/blob/master/pom.xml)
3. Create a main class [``HelloWorld.java``](https://github.com/MagpieBridge/Tutorial1/blob/master/src/main/java/HelloWorld.java) which creates an instance of `magpiebridge.core.MagpieServer`. The main method should look like this. It adds a `magpiebridge.projectservice.java.JavaProjectService` and a `SimpleServerAnalysis` to the server. `JavaProjectService` is used for resolving source code path and library code path of a Java project. 
~~~
    MagpieServer server = new MagpieServer();
    String language = "java";
    IProjectService javaProjectService = new JavaProjectService();
    server.addProjectService(language, javaProjectService);
    ServerAnalysis analysis = new SimpleServerAnalysis();
    server.addAnalysis(language, analysis);
    server.launchOnStdio();
~~~

4. Create a class [`SimpleTransformer.java`](https://github.com/MagpieBridge/Tutorial1/blob/master/src/main/java/SimpleTransformer.java) which performs a very simple intra-procedural [Taint Analysis](https://github.com/MagpieBridge/Tutorial1/blob/master/src/main/java/TaintAnalysis.java) using soot. 
The results of the analysis must implement `magpiebridge.core.AnalysisResult`, see [`Result.java`](https://github.com/MagpieBridge/Tutorial1/blob/master/src/main/java/Result.java). The source code position from wala is preserved in each corresponding Jimple Unit in form of Tag. Use the following lines to get the source code position of a Jimple Unit (see usage in [`TaintAnalyis.java`](https://github.com/MagpieBridge/Tutorial1/blob/master/src/main/java/TaintAnalysis.java)).
~~~ 
    Unit n = ...;
    PositionInfoTag tag = (PositionInfoTag) n.getTag("PositionInfoTag");
    Position stmtPos = tag.getPositionInfo().getStmtPosition();
~~~ 
You can also get precise operand position with `tag.getPositionInfo().getOperandPosition(index)`.

5. Create a class [`SimpleServerAnalysis.java`](https://github.com/MagpieBridge/Tutorial1/blob/master/src/main/java/SimpleServerAnalysis.java) which implements `magpiebridge.core.ServerAnalysis`.
Frist, you should set up soot options and use soot to load library classes. 
Second, you need the following lines to parse java source code with wala and load application classes into the soot Scene.
~~~
    WalaClassLoader loader = new WalaClassLoader(srcPath, libPath, null);
    List<SootClass> sootClasses = loader.getSootClasses();
    JimpleConverter jimpleConverter = new JimpleConverter(sootClasses);
    jimpleConverter.convertAllClasses(); 
~~~
If you don't need very precise source code position, you can also just use soot to load all classes (bytecode). However, then you can only get line numbers. 

6. In `SimpleServerAnalysis.java`, add a `SimpleTransformer` instance to the corresponding soot pack "jtp", for inter-procedural analysis add it to "wjtp". Run the corresponding soot packs. 
~~~
    Transform transform = new Transform("jtp.analysis", t);
    PackManager.v().getPack("jtp").add(transform);
    PackManager.v().runBodyPacks();
~~~

7. Use maven shade plugin to build a jar file, specify `HelloWorld` as mainClass in the [pom.xml](https://github.com/MagpieBridge/Tutorial1/blob/master/pom.xml).

8. `mvn install` in the project root. 

9. A jar file will be created in the `target` directory. If you built the tutorial project, it is `HelloWorld-1.0-SNAPSHOT.jar`. 

10. Use this jar file as langauge server in IDEs.

11. We show how to run this HelloWorld server in Eclipse and Visual Studio Code, configurations for other IDEs or editors can be found [here](https://github.com/MagpieBridge/CryptoLSPDemo)
 - Make sure you have Java (tested version: jdk-8u211-windows-x64) and Maven (tested version: apache-maven-3.6.1) installed and they are added to the PATH variable of the operating system. Maven is used to resolve project dependencies. For the DemoProject it is not important, since the project has no external dependecy.
 - Make sure you installed lsp4e [http://download.eclipse.org/lsp4e/releases/latest/](http://download.eclipse.org/lsp4e/releases/latest/) (if this server doesn't work, try  https://download.eclipse.org/lsp4e/releases/latest/) in Eclipse (tested version: eclipse-java-photon-R-win32-x86_64)
 - Create a new launch configuration called HelloWorld
![](https://github.com/MagpieBridge/MagpieBridge/blob/develop/doc/runconfig.PNG)
-  Open Eclipse->Window->Preferences->Language Servers
-  Click add -> Text -> Java Source File -> Program -> HelloWord ->OK
![](https://github.com/MagpieBridge/MagpieBridge/blob/develop/doc/setup.PNG)

12. Now you can test if it works for a demo project.

- Import [DemoProject](https://github.com/MagpieBridge/DemoProject/tree/master) as Maven project in Eclipse.

- Open a Java file in this project, this will trigger the server to run analysis.

- When the analysis finishes, server sends results to Eclipse. In the following screenshot you see a sensitive flow was detected and the sink is underlined, 
![](https://github.com/MagpieBridge/MagpieBridge/blob/develop/doc/warning.png)

13. Run the server in Visual Studio Code you need configuration files in the [vscode directory](https://github.com/MagpieBridge/Tutorial1/tree/master/vscode)
- compile and install Visual Studio Code extension from terminal:
~~~
cd PATH\\TO\\vscode
npm install
npm install -g vsce
vsce package
code --install-extension HelloWorld-0.0.1.vsix
~~~
- In Visual Studio Code it shows the data-flow path as related information in the warning messsage.
![](https://github.com/MagpieBridge/MagpieBridge/blob/develop/doc/warningvscode.png)