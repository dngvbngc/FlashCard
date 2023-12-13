import java.sql.Connection;  
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;  

public class User {
    
    private int id;
    private int currentSet;

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
                throw new RuntimeException("Creating new user was unsuccessful. Unable to connect to database.");
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException("Creating new user was unsuccessful. Try a different username.");
            }
        } else {
            // To declare a user who has not logged in
            this.id = 0;
            this.currentSet = 0;
        }
    }

    /* To create a user who already has an account */
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
                    throw new RuntimeException("User with ID " + id + " not found");
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

    public int getID() {
        return this.id;
    }

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

    public void addSet(String name, String subject) {
        Set mySet = new Set(this.id, name, subject, true);
        this.currentSet = mySet.getID();
    }

    public boolean goToSet(int id) {
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
                    return true;
                } else {
                    // Handle case where no user with the given ID was found
                    System.out.println("Set with ID " + id + " not found");
                    return false;
                }
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

    public void exitSet() {
        this.currentSet = 0;
    }

    public int getCurrentSet() {
        return this.currentSet;
    }

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
