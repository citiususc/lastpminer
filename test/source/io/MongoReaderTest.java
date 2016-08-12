package source.io;

import org.junit.Assert;
import org.junit.Test;



public class MongoReaderTest {


   @Test
   public void testUri(){


      MongoReader reader = new MongoReader();
      reader.authSource = "db1";
      reader.username = "user1";
      reader.password = "pwd1".toCharArray();
      reader.hostname = "host1";
      reader.withAuth = true;

      Assert.assertEquals("mongodb://user1:pwd1@host1/?authSource=db1", reader.getMongoURI());
   }
}
