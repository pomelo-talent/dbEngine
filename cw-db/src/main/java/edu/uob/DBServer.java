package edu.uob;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;

/** This class implements the DB server. */
public final class DBServer {

  private static final char END_OF_TRANSMISSION = 4;
  private static File directory;
  private QueryHandler handler;
  private static String database;

  public static void main(String[] args) throws IOException {
    new DBServer(Paths.get(".").toAbsolutePath().toFile()).blockingListenOn(8888);
  }

  /**
   * KEEP this signature (i.e. {@code edu.uob.DBServer(File)}) otherwise we won't be able to mark
   * your submission correctly.
   *
   * <p>You MUST use the supplied {@code databaseDirectory} and only create/modify files in that
   * directory; it is an error to access files outside that directory.
   *
   * @param databaseDirectory The directory to use for storing any persistent database files such
   *     that starting a new instance of the server with the same directory will restore all
   *     databases. You may assume *exclusive* ownership of this directory for the lifetime of this
   *     server instance.
   */
  public DBServer(File databaseDirectory) {
    // TODO implement your server logic here
    /*
    if ((databaseDirectory.isDirectory())) {
      File[] documents = databaseDirectory.listFiles();
      System.out.println("Files are:");

      // Display the names of the documents
      for (int i = 0; i < documents.length; i++) {
          System.out.println(documents[i].getName());
      }
    }*/
    //File fileToOpen = new File("people.tab");
    //readFile(fileToOpen);
    //storeFile(fileToOpen);

    directory = databaseDirectory;
    if (!directory.exists()) {
      directory.mkdir();
    }

    handler = new QueryHandler();
  }

  public static File getDatabaseDirectory() {
    return directory;
  }

  /*
  public void readFile(File filename) {
    try {
      FileReader reader = new FileReader(filename);
      BufferedReader buffReader = new BufferedReader(reader);
      String line;
      while ((line = buffReader.readLine()) != null) {
        System.out.println(line);
      }
      System.out.println(line);
      buffReader.close();
    } catch (IOException e) {
      System.out.println(e);
    }
  }

  public void storeFile(File filename) {
    try {
      Table tableFromFile = new Table(filename);
      //tableFromFile.printTable();
    } catch (IOException e){
      System.out.println(e);
    }
  }

   */

  /**
   * KEEP this signature (i.e. {@code edu.uob.DBServer.handleCommand(String)}) otherwise we won't be
   * able to mark your submission correctly.
   *
   * <p>This method handles all incoming DB commands and carry out the corresponding actions.
   */
  public String handleCommand(String command) throws IOException {
    // TODO implement your server logic here

    if (!handler.handleQuery(command)) {
      database = handler.getCurrDatabase();
      //return "[ERROR] " + handler.getErrorMessage()+ handler.getCurrDatabase();
      return "[ERROR] " + handler.getErrorMessage();
    }
    database = handler.getCurrDatabase();
    //return "[OK] Thanks for your message: " + command +handler.getCurrDatabase();
    return "[OK] Thanks for your message: " + command + handler.getInformation();
  }

  public static String getDatabase() {

    return database;
  }

  public void setDatabase(String DatabaseName) {
    if (!DatabaseName.equals("")&&DatabaseName!=null) {
      database = DatabaseName;
    }
  }


  //  === Methods below are there to facilitate server related operations. ===

  /**
   * Starts a *blocking* socket server listening for new connections. This method blocks until the
   * current thread is interrupted.
   *
   * <p>This method isn't used for marking. You shouldn't have to modify this method, but you can if
   * you want to.
   *
   * @param portNumber The port to listen on.
   * @throws IOException If any IO related operation fails.
   */
  public void blockingListenOn(int portNumber) throws IOException {
    try (ServerSocket s = new ServerSocket(portNumber)) {
      System.out.println("Server listening on port " + portNumber);
      while (!Thread.interrupted()) {
        try {
          //System.out.print("SQL:> ");
          blockingHandleConnection(s);
        } catch (IOException e) {
          System.err.println("Server encountered a non-fatal IO error:");
          e.printStackTrace();
          System.err.println("Continuing...");
        }
      }
    }
  }

  /**
   * Handles an incoming connection from the socket server.
   *
   * <p>This method isn't used for marking. You shouldn't have to modify this method, but you can if
   * * you want to.
   *
   * @param serverSocket The client socket to read/write from.
   * @throws IOException If any IO related operation fails.
   */
  private void blockingHandleConnection(ServerSocket serverSocket) throws IOException {
    try (Socket s = serverSocket.accept();
        BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {
      System.out.print("SQL:> ");
      System.out.println("Connection established: " + serverSocket.getInetAddress());
      while (!Thread.interrupted()) {
        String incomingCommand = reader.readLine();
        System.out.println("Received message: " + incomingCommand);
        String result = handleCommand(incomingCommand);
        writer.write(result);
        writer.write("\n" + END_OF_TRANSMISSION + "\n");
        writer.flush();
      }
    }
  }
}
