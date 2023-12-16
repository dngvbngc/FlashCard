import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

/** A class representing a study set */
public class Set {

    /** The unique auto-generated ID of the set */
    private int id;

    /** The name of the set */
    private String name;

    /** Constructor to make a new set.
     * @param user_id The user to which the set belongs
     * @param name The name of the set
     * @param subject The subject of the set
     * @param isNew True (insert new information into the database)/ False (do not update the data)
     */
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
                System.out.println("Unable to create new set. Cannot connect to databse.");
            } catch (SQLException e) {
                e.printStackTrace();;
                System.out.println("Unable to create new set.");
            }            
        } else {
            // For default new set;
            this.id = 0;
            this.name = "";
        }
    }

    /** Constructor to make a new instance of an existing set
     * @param id The unique ID of the existing set
     * @throws RuntimeException if invalid set ID
     */
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
            System.out.println("Retrieving set was unsuccessful.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Retrieving set was unsuccessful.");
        }
    }

    /** Accessor for the set's ID 
     * @return The ID of the set
     */
    public int getID() {
        return this.id;
    }

    /** Accessor for the set's name 
     * @return The name of the set
     */
    public String getName() {
        return this.name;
    }

    /** Accessor for all the cards in the set
     * @return All cards in the set stored in a Hashtable
     * @throws RuntimeException if unable to connect to database
     */
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

    /** Get the number of cards in the set
     * @return The number of cards in the set
     */
    public int getNumberOfCards() {
        return this.getCards().size();
    }

    /** Print all cards in the set in a formatted roster */
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

    /** Add card(s) to the set
     * @param Hashtable<String,String> The terms and definitions to add, contained in a Hashtable
     */
    public void addCards(Hashtable<String, String> newCards) {

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
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
            connection.close();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        } 
    }

    /** Delete a card from the set
     * @param term The term of the card to delete
     */
    public void deleteCard(String term) {
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
