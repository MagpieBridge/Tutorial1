
public class Main {
  public static void main(String... args) {
    Demo demo = new Demo();
    String s1 = demo.source();
    String s2 = s1;
    String s3= "info: "+s2;
    demo.sink(s3); 
  }
}
