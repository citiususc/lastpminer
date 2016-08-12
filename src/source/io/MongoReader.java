package source.io;


public class MongoReader {
   protected String hostname = "localhost";
   protected Integer port = null; // 27017;
   protected String dbname;
   protected boolean withAuth = false;
   protected String username = "";
   protected char[] password = {};
   protected String authSource = "";
   protected String database;

   public MongoReader() {
      //
   }

   public MongoReader(String username, char[] password) {
      withAuth = true;
      this.username = username;
      this.password = password;
   }

   public String getMongoURI(){
      //"mongodb://user1:pwd1@host1/?authSource=db1"
      return "mongodb://"
            + (withAuth? username + ":" + String.copyValueOf(password) + "@" : "")
            + (hostname == null ? "localhost" : hostname)
            + (port != null? ":" + Integer.toString(port)  : "")
            + "/"
            + (database != null? database : "")
            + (withAuth? "?authSource=" + authSource : "" );
   }

   public String getHostname() {
      return hostname;
   }

   public void setHostname(String hostname) {
      this.hostname = hostname;
   }

   public int getPort() {
      return port;
   }

   public void setPort(int port) {
      this.port = port;
   }

   public String getDbname() {
      return dbname;
   }

   public void setDbname(String dbname) {
      this.dbname = dbname;
   }

   public boolean isWithAuth() {
      return withAuth;
   }

   public void setWithAuth(boolean withAuth) {
      this.withAuth = withAuth;
   }

   public String getUsername() {
      return username;
   }

   public void setUsername(String username) {
      this.username = username;
   }

   public char[] getPassword() {
      return password;
   }

   public void setPassword(char[] password) {
      this.password = password;
   }

   public String getAuthSource() {
      return authSource;
   }

   public void setAuthSource(String authSource) {
      this.authSource = authSource;
   }




}
