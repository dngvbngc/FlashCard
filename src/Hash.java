import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash { 

  String hash;

  public Hash() {
    this.hash = "";
  } 

  public String generateHash(String password) {
    try {
      // Create MessageDigest instance for MD5
      MessageDigest md = MessageDigest.getInstance("MD5");

      // Add password bytes to digest
      md.update(password.getBytes());

      // Get the hash's bytes
      byte[] bytes = md.digest();

      // This bytes[] has bytes in decimal format. Convert it to hexadecimal format
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < bytes.length; i++) {
        sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
      }

      // Get complete hashed password in hex format
      this.hash = sb.toString();
      return this.hash;

    } catch (NoSuchAlgorithmException e) {
      System.out.println("No such algorithm");
      return this.hash;
    }
  }

  public static void main(String[] args) {
    Hash hasher = new Hash();
    System.out.println(hasher.generateHash("hello"));
  }
}

