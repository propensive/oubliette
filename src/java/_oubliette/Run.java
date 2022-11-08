package _oubliette;

import java.io.*;
import java.util.ArrayList;
import java.net.*;
import java.lang.reflect.*;

public class Run {
  private static ClassLoader classLoader = ClassLoader.getSystemClassLoader();
  private static final ArrayList<URL> pending = new ArrayList<URL>();
  private static ArrayList<String> mainArgs = new ArrayList<String>();
  private static String mainClass = null;

  private static void update() {
    if (!pending.isEmpty()) {
      URL[] urls = new URL[pending.size()];
      pending.toArray(urls);
      classLoader = new URLClassLoader(urls, classLoader);
    }
  }

  public static void main(String[] args) throws IOException, ReflectiveOperationException {
    File file = new File(args[0]);
    file.deleteOnExit();

    BufferedReader reader = new BufferedReader(new FileReader(file));
    String line = reader.readLine();
    
    while (line != null) {
      String[] data = line.split("\t");

      switch (data[0]) {
        case "path" -> {
          pending.add(new File(data[1]).toURI().toURL());
        }
	      case "load" -> {
	        update();
	        classLoader.loadClass(data[1]);
        }
        case "exit" -> {
          System.exit(Integer.parseInt(data[1]));
        }
        case "main" -> {
          mainClass = data[1];
        }
        case "arg" -> {
          mainArgs.add(line.substring(4));
        }
        default -> {
        }
      }
      
      line = reader.readLine();
    }

    if (mainClass == null) System.exit(1);
    update();
    file.delete();
    
    Class<?> cls = classLoader.loadClass(mainClass);
    Method method = cls.getMethod("main", String[].class);
    String[] params = new String[mainArgs.size()];
    mainArgs.toArray(params);
    method.invoke(null, (Object) params);
  }
}