import java.sql.Connection;  
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;  

/** A class representing a user */
public class User {
    
    /** The unique auto-generated ID of the user */
    private int id;

    /** Indicates whether the user is interacting with an available set */
    private int currentSet;

    /** Constructor to make a new user.
     * @param username The username of the new user
     * @param name The name of the user
     * @param password The unhashed password of the user
     * @param isNew True (insert new information into the database)/ False (do not update the data)
     */
    public User(String username, String name, String password, boolean isNew) {
        Hash hasher = new Hash();
        password = hasher.generateHash(password);
        if (isNew) {
            try {
                // Insert new user data into Users table
                Class.forName("org.sqlite.JDBC");
                String url = "jdbc:sqlite:data/flashcards.db";
                Connection connection = DriverManager.getConnection(url);
                String sql = "INSERT INTO users (username, name, password) VALUES (?, ?, ?)";
                PreparedStatement pstmt = connection.prepareStatement(sql);
                pstmt.setString(1, username);
                pstmt.setString(2, name);
                pstmt.setString(3, password);
                pstmt.executeUpdate();

                // Retrieve the unique user id
                sql = "SELECT id FROM users WHERE username = ?";
                pstmt = connection.prepareStatement(sql);
                pstmt.setString(1, username);
                ResultSet resultSet = pstmt.executeQuery();
                this.id = resultSet.getInt("id");
                this.currentSet = 0;
                System.out.println("New user created successfully.");

                // Close 
                resultSet.close();
                pstmt.close();
                connection.close();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            // To declare a user who has not logged in
            this.id = 0;
            this.currentSet = 0;
        }
    }

    /** To create a new instance of an existing user
     * @param id The user's ID
     * @throws RuntimeException if unable to access the database or invalid ID
     */
    public User(int id) {
        try {
                Class.forName("org.sqlite.JDBC");
                String url = "jdbc:sqlite:data/flashcards.db";
                Connection connection = DriverManager.getConnection(url);
                String sql = "SELECT * FROM users WHERE id = ?";
                PreparedStatement pstmt = connection.prepareStatement(sql);
                pstmt.setInt(1, id);
                ResultSet resultSet = pstmt.executeQuery();
                
                if (resultSet.next()) {
                    this.id = resultSet.getInt("id");
                    this.currentSet = 0;
                } else {
                    // Handle case where no user with the given ID was found
                    throw new RuntimeException("User with ID " + id + " not found.");
                }

                // Close 
                resultSet.close();
                pstmt.close();
                connection.close();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException("Retrieving user was unsuccessful.");
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException("Retrieving user was unsuccessful.");
            }
    }

    /** Accessor for the user's ID
     * @return The user's ID
     */
    public int getID() {
        return this.id;
    }

    /** Accessor for the user's name
     * @return The user's name
     * @throws RuntimeException if unable to access the database
     */
    public String getName() {
        try {
                Class.forName("org.sqlite.JDBC");
                String url = "jdbc:sqlite:data/flashcards.db";
                Connection connection = DriverManager.getConnection(url);
                String sql = "SELECT * FROM users WHERE id = ?";
                PreparedStatement pstmt = connection.prepareStatement(sql);
                pstmt.setInt(1, this.getID());
                ResultSet resultSet = pstmt.executeQuery();
                String name = resultSet.getString("name");

                // Close 
                resultSet.close();
                pstmt.close();
                connection.close();

                return name.substring(0,1).toUpperCase() + name.substring(1).toLowerCase();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException("Unable to connect to database.");
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException("Retrieving name was unsuccessful.");
            }
    }

    /** Retrieving the hashed password of the user from the database
     * @return The user's password (hashed)
     * @throws RuntimeException if unable to access the database
     */
    public String getPassword() {
        String url = "jdbc:sqlite:data/flashcards.db";
        String password = "";
        String sql;
        try {
                Class.forName("org.sqlite.JDBC");
                Connection connection = DriverManager.getConnection(url);
                sql = "SELECT password FROM users WHERE id = ?";
                PreparedStatement pstmt = connection.prepareStatement(sql);
                pstmt.setInt(1, this.id);
                ResultSet resultSet = pstmt.executeQuery();

                if (resultSet.next()) {
                    password = resultSet.getString("password");
                } else {
                    throw new RuntimeException("User not found.");
                }

                // Close 
                resultSet.close();
                pstmt.close();
                connection.close();
                return password;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException("Cannot connect to database.");
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException("Problems retrieving old password from database.");
            }
    }

    /** Updating the user's password
     * @param newPassword The user's new password (unhashed)
     * @throws RuntimeException if unable to update the database
     */
    public void changePassword(String newPassword) {
        Hash hasher = new Hash();
        String hashedPassword = hasher.generateHash(newPassword);
        String url = "jdbc:sqlite:data/flashcards.db";

        try (Connection connection = DriverManager.getConnection(url)) {
            String sql = "UPDATE users SET password = ? WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, hashedPassword);
                pstmt.setInt(2, this.id);
                int rowsUpdated = pstmt.executeUpdate();
                
                if (rowsUpdated > 0) {
                    System.out.println("Password changed successfully. Your new password is " + newPassword + ".");
                } else {
                    System.out.println("Password update failed. User not found.");
                }

                // Close 
                pstmt.close();
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace(); 
            throw new RuntimeException("Problems updating password.");
        }
    }

    /** Print all of the user's available sets in a formatted roster 
     * @throws RuntimeException if unable to access the database
     */
    public void printSets() {
        try {
                Class.forName("org.sqlite.JDBC");
                String url = "jdbc:sqlite:data/flashcards.db";
                Connection connection = DriverManager.getConnection(url);
                String sql = "SELECT * FROM sets WHERE user_id = ?";
                PreparedStatement pstmt = connection.prepareStatement(sql);
                pstmt.setInt(1, this.getID());
                ResultSet resultSet = pstmt.executeQuery();
                
                String header = "--------------- YOUR SETS: ---------------";
                System.out.println(header);
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String name = resultSet.getString("name");
                    String subject = resultSet.getString("subject");
                    System.out.println("ID: " + id + ", Name: " + name + ", Subject: " + subject);
                }
                int i = 0;
                while (i < header.length()) {
                    System.out.print("-");
                    i++;
                }
                System.out.println();

                // Close 
                resultSet.close();
                pstmt.close();
                connection.close();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException("Retrieving user was unsuccessful.");
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException("Retrieving user was unsuccessful.");
            }
    }

    /** Add a new set to the user's account
     * @param name The name of the new set
     * @param subject The subject of the new set
     */
    public void addSet(String name, String subject) {
        Set mySet = new Set(this.id, name, subject, true);
        this.currentSet = mySet.getID();
    }

    /** Go to an existing set to perform actions such as study and test
     * @param id The ID of the set to go to
     * @return True (successfully accessed the set) / False (unable to access the set)
     */
    public boolean goToSet(int id) {
        boolean success;
        try {
                Class.forName("org.sqlite.JDBC");
                String url = "jdbc:sqlite:data/flashcards.db";
                Connection connection = DriverManager.getConnection(url);
                String sql = "SELECT * FROM sets WHERE id = ? AND user_id = ?";
                PreparedStatement pstmt = connection.prepareStatement(sql);
                pstmt.setInt(1, id);
                pstmt.setInt(2, this.getID());
                ResultSet resultSet = pstmt.executeQuery();
                
                if (resultSet.next()) {
                    this.currentSet = id;
                    success = true;
                } else {
                    // Handle case where no user with the given ID was found
                    System.out.println("Set with ID " + id + " not found.");
                    success = false;
                }

                // Close 
                resultSet.close();
                pstmt.close();
                connection.close();
                return success;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                System.out.println("Retrieving user was unsuccessful.");
                return false;
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Retrieving user was unsuccessful.");
                return false;
            }
    }

    /** Exit the current set */
    public void exitSet() {
        this.currentSet = 0;
    }

    /** Retrieve the current set's ID
     * @return The current set's ID
     */
    public int getCurrentSet() {
        return this.currentSet;
    }

    /** Delete a set from the user's account
     * @param id The ID of the set to delete
     * @return True (successfully deleted set)/ False (unable to delete set)
     */
    public boolean deleteSet(int id) {

        // Update database
        Connection connection = null;

        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:data/flashcards.db";
            connection = DriverManager.getConnection(url);
            String deleteSql = "DELETE FROM sets WHERE id = ?";

            try (PreparedStatement pstmt = connection.prepareStatement(deleteSql)) {
                pstmt.setInt(1, id);
                int rowsAffected = pstmt.executeUpdate();
                System.out.println(rowsAffected + " set deleted: ID = " + id);
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            System.out.println("Unable to connect to/update the database.");
            return false;
        } finally {
            try {
                if (connection != null) {
                    Statement stmt = connection.createStatement();
                    String tableName = "set" + id;
                    String dropTableSql = "DROP TABLE " + tableName;
                    stmt.execute(dropTableSql);
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Unable to delete set (Set ID not found).");
                return false;
            }
        }
        return true;
    } 
}
