import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

public class Set {

    private int id;
    private String name;

    public Set(int user_id, String name, String subject, boolean isNew) {
        if (isNew) {
            try {
                // Insert new set into Sets table
                Class.forName("org.sqlite.JDBC");
                String url = "jdbc:sqlite:data/flashcards.db";
                Connection connection = DriverManager.getConnection(url);
                String sql = "INSERT INTO sets (user_id, name, subject) VALUES (?, ?, ?)";
                PreparedStatement pstmt = connection.prepareStatement(sql);
                pstmt.setInt(1, user_id);
                pstmt.setString(2, name);
                pstmt.setString(3, subject);
                pstmt.executeUpdate();

                // Retrieve the unique set id
                sql = "SELECT * FROM sets WHERE user_id = ? AND name = ?";
                pstmt = connection.prepareStatement(sql);
                pstmt.setInt(1, user_id);
                pstmt.setString(2, name);
                ResultSet resultSet = pstmt.executeQuery();
                this.id = resultSet.getInt("id");
                this.name = resultSet.getString("name");

                // Make new SQL table for the set
                String tablename = "set" + this.id;
                sql = "CREATE TABLE IF NOT EXISTS " + tablename + " (\n"
                        + "	id integer PRIMARY KEY AUTOINCREMENT,\n"
                        + "	term TEXT NOT NULL,\n"
                        + "	definition TEXT NOT NULL\n"
                        + ");";
                Statement stmt = connection.createStatement();
                stmt.execute(sql);             

                // Close 
                pstmt.close();
                connection.close();
                System.out.println("New set created successfully.");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();;
                throw new RuntimeException("Unable to create new set. Cannot connect to databse.");
            } catch (SQLException e) {
                e.printStackTrace();;
                throw new RuntimeException("Unable to create new set.");
            }            
        } else {
            // For default new set;
            this.id = 0;
            this.name = "";
        }
    }

    // To retrieve old  via id
    public Set(int id) {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:data/flashcards.db";
            Connection connection = DriverManager.getConnection(url);
            String sql = "SELECT * FROM sets WHERE id = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, id);
            ResultSet resultSet = pstmt.executeQuery();
            
            if (resultSet.next()) {
                this.id = resultSet.getInt("id");
                this.name = resultSet.getString("name");
            } else {
                // Handle case where no user with the given ID was found
                throw new RuntimeException("Set with ID " + id + " not found");
            }

            // Close 
            resultSet.close();
            pstmt.close();
            connection.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Retrieving set was unsuccessful.");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Retrieving set was unsuccessful.");
        }
    }

    public int getID() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Hashtable<String, String> getCards() {
        try {
            Hashtable<String, String> cards = new Hashtable<>();
            String term;
            String definition;

            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:data/flashcards.db";
            Connection connection = DriverManager.getConnection(url);

            // Retrieve the unique set id
            String sql = "SELECT term, definition FROM set" + this.id +";";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            ResultSet resultSet = pstmt.executeQuery();

            // Iterate through resultSet
            while (resultSet.next()) {
                term = resultSet.getString("term");
                definition = resultSet.getString("definition");
                cards.put(term, definition);
            }

            connection.close();
            return cards;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot connect to database.");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Problem retrieving cards from set.");
        }
    }

    public int getNumberOfCards() {
        return this.getCards().size();
    }

    public void printCards() {
        Hashtable<String, String> cards = this.getCards();
        String header = "----- ALL " + this.getNumberOfCards() + " CARDS IN " + this.getName().toUpperCase()+ " -----";
        System.out.println(header);
        cards.forEach((term, definition) -> {
            System.out.println("Term: " + term + ", Definition: " + definition);
        });
          
        int i = 0;
        while (i < header.length()) {
            System.out.print("-");
            i++;
        }
        System.out.println();
    }

    public void addTerms(Hashtable<String, String> newCards) {

        // Update database with new cards
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:data/flashcards.db";
            Connection connection = DriverManager.getConnection(url);
            
            newCards.forEach((key, value) -> {
                String tableName = "set" + this.getID();
                String sql = "INSERT INTO " + tableName + " (term, definition) VALUES (?, ?)";
                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    pstmt.setString(1, key);
                    pstmt.setString(2, value);
                    int rowsAffected = pstmt.executeUpdate();
                    System.out.println(rowsAffected + " row(s) inserted: Term: " + key + ", Definition: " + value);
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        } 
    }

    public void deleteTerm(String term) {
        // Update database
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:data/flashcards.db";
            String tableName = "set" + this.getID();
            Connection connection = DriverManager.getConnection(url);
            String sql = "DELETE FROM " + tableName + " WHERE term = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    pstmt.setString(1, term);
                    int rowsAffected = pstmt.executeUpdate();
                    System.out.println(rowsAffected + " row(s) deleted: Term: " + term + ".");
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            }
    }
}
