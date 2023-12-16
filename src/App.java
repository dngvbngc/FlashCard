import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;
import java.util.Scanner;
import java.sql.Connection;  
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;  

/** The main application */
public class App {
    public static void main(String[] args) {

        User user = new User("", "", "", false);
        boolean loggedIn = false;
        String sql = "";
        String answer= "";
        boolean exit = false;

        try (Scanner scanner = new Scanner(System.in).useDelimiter("\n")) {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            /* Ensure user log in before proceeding */
            while (!exit) {
                while (!loggedIn) {

                    // Print menu
                    System.out.println("** Welcome to FLASHCARDS! Log in or create new user to begin. **");
                    System.out.println("A. Log in\nB. Create new user\nC. Exit");
                    
                    // Take user's input
                    while (!answer.equals("a") && !answer.equals("b") && !answer.equals("c")) {
                        System.out.print("Your choice (A/B/C): ");
                        answer = scanner.next().toLowerCase();
                        scanner.nextLine();
                    }

                    // Make new user
                    if (answer.equals("b")) {

                        System.out.print("Enter your first name: ");
                        String name = scanner.next().toLowerCase();
                        scanner.nextLine();

                        System.out.print("Enter your new username: ");
                        String username = scanner.next().toLowerCase();
                        scanner.nextLine();
                        
                        System.out.print("Enter your new password: ");
                        String password = scanner.next().toLowerCase();
                        scanner.nextLine();
                        System.out.println("Remember! Your password is " + password + ".");

                        try {
                            user = new User(username, name, password, true);
                            loggedIn = true;
                        } catch (RuntimeException e) {
                            System.out.println(e.getMessage());
                        }

                        answer = "";
                    } else if (answer.equals("a")) {
                        // Log in existing user
                        System.out.print("Enter your username: ");
                        String username = scanner.next().toLowerCase().trim();
                        scanner.nextLine();

                        Hash hasher = new Hash();
                        System.out.print("Enter your password: ");
                        String password = scanner.next().toLowerCase().trim();
                        scanner.nextLine();

                        sql = "SELECT id FROM users WHERE username = ? AND password = ?";
                        try {
                            Connection connection = DriverManager.getConnection("jdbc:sqlite:data/flashcards.db");
                            PreparedStatement pstmt = connection.prepareStatement(sql);
                            pstmt.setString(1, username);
                            pstmt.setString(2, hasher.generateHash(password)); 
                            ResultSet resultSet = pstmt.executeQuery();

                            if (resultSet.next()) {
                                // If the query returns a row, it means the user exists and credentials are correct
                                user = new User(resultSet.getInt("id"));
                                System.out.println("You are logged in.");
                                loggedIn = true;
                            } else {
                                System.out.println("** Invalid username or password. **");
                            }
                            
                            // Close resources
                            answer = "";
                            resultSet.close();
                            pstmt.close();
                            connection.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    } else if (answer.equals("c")) {
                        // Terminate the program
                        exit = true;
                        break;
                    } 
                }

                // For logged in user not interacting with a set
                while (user.getCurrentSet() == 0 & loggedIn) {
                    int mode = 0;

                    // Print options
                    System.out.println("Hello " + user.getName() + ". What would you like to do?");
                    System.out.println("1. Change password");
                    System.out.println("2. View your sets");
                    System.out.println("3. Make new set");
                    System.out.println("4. Go to set (Add/Delete/Print/Study/Test)");
                    System.out.println("5. Delete set");
                    System.out.println("6. Log out");
                    System.out.println("7. Exit");
                
                    // Take user's input
                    boolean correctAnswer = false;
                    while (!correctAnswer) {
                        System.out.print("Enter your action (1-7): ");
                        try {
                            mode = scanner.nextInt();
                            scanner.nextLine();
                            correctAnswer = (mode >= 1 && mode <= 7);;
                            if (!correctAnswer) {
                                System.out.println("Invalid command. Please enter a number from 1 to 7.");
                            }
                        } catch (Exception e) {
                            System.out.println("Invalid command. Please enter a number from 1 to 7.");
                            scanner.nextLine();
                        }
                    }
                
                    // Change password
                    if (mode == 1) {
                        Hash hasher = new Hash();
                        String oldPassword = user.getPassword();

                        answer = "";
                        int counter = 0;

                        while (counter < 3 && !answer.equals(oldPassword)) {
                            System.out.print("Enter old password: ");
                            answer = scanner.next().toLowerCase();
                            answer = hasher.generateHash(answer);
                            counter++;
                            scanner.nextLine();
                        }

                        if (counter == 3 && !answer.equals(oldPassword)) {
                            System.out.println("You have entered the wrong password too many times. Try again later.");
                        } else {
                            System.out.print("Enter new password: ");
                            answer = scanner.nextLine().toLowerCase();
                            user.changePassword(answer);
                        }
                        mode = 0;
                    } else if (mode == 6) {
                        // Log out 
                        user = new User("", "", "", false);
                        mode = 0;
                        loggedIn = false;
                    } else if (mode == 7) {
                        // Terminate the program
                        exit = true;
                        break;
                    } else if (mode == 2) {
                        // Print available sets
                        user.printSets();
                        mode = 0;
                    } else if (mode == 3) {
                        // Make new set
                        try {
                            System.out.print("Subject: ");
                            String subject = scanner.next();
                            scanner.nextLine();

                            System.out.print("Name of new set: ");
                            String name = scanner.next();
                            scanner.nextLine();
                            
                            user.addSet(name, subject);
                        } catch (RuntimeException e) {
                            e.getMessage();
                        }
                        mode = 0;
                    } else if (mode == 4) {
                        // Go to set
                        user.printSets();
                        int id = 0;
                        
                        boolean success = false;
                        while(!success) {
                            while (id <= 0) {
                                System.out.print("Access set with ID: ");
                                try {
                                    id = scanner.nextInt();
                                    scanner.nextLine();
                                    success = user.goToSet(id);
                                } catch (Exception e) {
                                    System.out.println("Please enter a valid numeric set ID.");
                                    scanner.nextLine();
                                }
                            }
                            id = 0;
                        }

                    } else if (mode == 5) {
                        // Delete a set
                        int set_id = 0;
                        boolean success = false;

                        while (!success) {
                            // Print available sets to user
                            user.printSets();

                            while (set_id <= 0) {
                                System.out.print("Enter the SET ID to delete (numeric): ");
                                set_id = scanner.nextInt();
                                scanner.nextLine();
                            }

                            // Delete set & Update database
                            success = user.deleteSet(set_id);
                            set_id = 0;
                        }
                    }
                }  
                
                // For logged in user interacting with a set
                while (user.getCurrentSet() > 0 & loggedIn) {
                    int mode = 0;
                    Set currentSet = new Set(user.getCurrentSet());

                    // Print options
                    System.out.println("Hello " + user.getName() + ". What would you like to do with " + currentSet.getName() + "?");
                    System.out.println("1. View all cards in this set");
                    System.out.println("2. Study cards in set");
                    System.out.println("3. Test cards in set");
                    System.out.println("4. Add cards");
                    System.out.println("5. Delete card");
                    System.out.println("6. Go back to main menu");
                    System.out.println("7. Exit");

                    // Take user's input
                    boolean correctAnswer = false;
                    while (!correctAnswer) {
                        System.out.print("Enter your action (1-7): ");
                        try {
                            mode = scanner.nextInt();
                            scanner.nextLine();
                            correctAnswer = (mode >= 1 && mode <= 7);;
                            if (!correctAnswer) {
                                System.out.println("Invalid command. Please enter a number from 1 to 7.");
                            }
                        } catch (Exception e) {
                            System.out.println("Invalid command. Please enter a number from 1 to 7.");
                            scanner.nextLine();
                        }
                    }

                    // Print all cards
                    if (mode == 1) {
                        currentSet.printCards();
                    } else if (mode == 2) {
                        // Study cards
                        // Setting study mode (case sensitivity) 
                        System.out.println("It's study time! Choose mode:\nA. Study definitions\nB. Study terms");
                        answer = "";
                        while (!answer.equals("a") && !answer.equals("b")) {
                            System.out.print("Mode (A/B): ");
                            answer = scanner.next().toLowerCase();
                            scanner.nextLine();
                        }
                        boolean studyDefinitions = answer.toLowerCase().equals("a");
                        answer = "";


                        // Setting test mode (term or definition) 
                        System.out.println("Choose mode:\nA. Case sensitive\nB. Case insensitive");
                        while (!answer.equals("a") && !answer.equals("b")) {
                            System.out.print("Mode (A/B): ");
                            answer = scanner.next().toLowerCase();
                            scanner.nextLine();
                        }
                        boolean isCaseSensitive = answer.toLowerCase().equals("a");

                        // Run the study session 
                        Hashtable<String, String> cards = currentSet.getCards();
                        int nCards = currentSet.getNumberOfCards();
                        String reply = "";
                        ArrayList<String> terms = new ArrayList<>();
                        ArrayList<String> definitions = new ArrayList<>();
                        ArrayList<Integer> testedIndex = new ArrayList<>(); 
                        int currentIndex = nCards;
                        String currentTerm = "";
                        String currentDefinition = "";
                        Random ran = new Random(); 

                        cards.forEach((term, definition) -> {
                            terms.add(term);
                            definitions.add(definition);
                        });

                        int counter;
                        while (testedIndex.size() < nCards) {
                            currentIndex = ran.nextInt(nCards);
                            counter = 0;
                        
                            // Checking if currentIndex has already been tested
                            if (!testedIndex.contains(currentIndex)) {
                                currentTerm = terms.get(currentIndex);
                                currentDefinition = definitions.get(currentIndex);
                        
                                if (studyDefinitions) {
                                    while (!reply.trim().equals(currentDefinition) && counter < 3) {
                                        System.out.println("Term: " + currentTerm);
                                        System.out.print("Definition: ");
                                        reply = scanner.next();
                                        if (!isCaseSensitive) {
                                            reply = reply.toLowerCase();
                                            currentDefinition = currentDefinition.toLowerCase();
                                        }
                                        scanner.nextLine();
                                        counter++;
                                    }
                                    if (counter == 3) {
                                        while (!reply.trim().equals(currentDefinition)) {
                                            System.out.println("Term: " + currentTerm);
                                            System.out.println("Definition: " + currentDefinition);
                                            System.out.print("Enter the correct definition above: ");
                                            reply = scanner.next();
                                            if (!isCaseSensitive) {
                                                reply = reply.toLowerCase();
                                                currentDefinition = currentDefinition.toLowerCase();
                                            }
                                            scanner.nextLine();
                                        }
                                        reply = "";
                                    } else {
                                        testedIndex.add(currentIndex);
                                    }
                                } else {
                                    while (!reply.trim().equals(currentTerm) && counter < 3) {
                                        System.out.println("Definition: " + currentDefinition);
                                        System.out.print("Term: ");
                                        reply = scanner.next();
                                        if (!isCaseSensitive) {
                                            reply = reply.toLowerCase();
                                            currentTerm = currentTerm.toLowerCase();
                                        }
                                        scanner.nextLine();
                                        counter++;
                                    }
                                    if (counter == 3) {
                                        while (!reply.trim().equals(currentTerm)) {
                                            System.out.println("Definition: " + currentDefinition);
                                            System.out.println("Term: " + currentTerm);
                                            System.out.print("Enter the correct term above: ");
                                            reply = scanner.next();
                                            if (!isCaseSensitive) {
                                                reply = reply.toLowerCase();
                                                currentDefinition = currentDefinition.toLowerCase();
                                            }
                                            scanner.nextLine();
                                        }
                                        reply = "";
                                    } else {
                                        testedIndex.add(currentIndex);
                                    }
                                }
                            }
                        }
                        System.out.println("Congratulations on finishing studying this test!");

                    } else if (mode == 3) {
                        // Test cards
                        // Setting test mode (case sensitivity) 
                        System.out.println("It's test time! Choose mode:\nA. Test with definitions\nB. Test with terms");
                        answer = "";
                        while (!answer.toLowerCase().equals("a") && !answer.toLowerCase().equals("b")) {
                            System.out.print("Mode (A/B): ");
                            answer = scanner.next();
                            scanner.nextLine();
                        }
                        boolean testWithDefinitions = answer.toLowerCase().equals("a");
                        answer = "";


                        // Setting test mode (term or definition) 
                        System.out.println("Choose mode:\nA. Case sensitive\nB. Case insensitive");
                        while (!answer.toLowerCase().equals("a") && !answer.toLowerCase().equals("b")) {
                            System.out.print("Mode (A/B): ");
                            answer = scanner.next();
                            scanner.nextLine();
                        }
                        boolean isCaseSensitive = answer.toLowerCase().equals("a");

                        // Run the test 
                        Hashtable<String, String> cards = currentSet.getCards();
                        int currentScore = 0;
                        int maxScore = currentSet.getNumberOfCards();
                        String reply;
                        ArrayList<String> terms = new ArrayList<>();
                        ArrayList<String> definitions = new ArrayList<>();
                        ArrayList<Integer> testedIndex = new ArrayList<>(); 
                        int currentIndex = maxScore;
                        String currentTerm = "";
                        String currentDefinition = "";
                        Random ran = new Random(); 

                        cards.forEach((term, definition) -> {
                            terms.add(term);
                            definitions.add(definition);
                        });

                        int i = 0;
                        while (testedIndex.size() < maxScore && i < maxScore) {
                            currentIndex = ran.nextInt(maxScore);
                        
                            // Checking if currentIndex has already been tested
                            if (!testedIndex.contains(currentIndex)) {
                                currentTerm = terms.get(currentIndex);
                                currentDefinition = definitions.get(currentIndex);
                        
                                if (testWithDefinitions) {
                                    System.out.println("Term: " + currentTerm);
                                    System.out.print("Definition: ");
                                    reply = scanner.next();
                                    if (!isCaseSensitive) {
                                        reply = reply.toLowerCase();
                                        currentDefinition = currentDefinition.toLowerCase();
                                    }
                                    if (reply.trim().equals(currentDefinition)) {
                                        currentScore++;
                                    }
                                    scanner.nextLine();
                                } else {
                                    System.out.println("Definition: " + currentDefinition);
                                    System.out.print("Term: ");
                                    reply = scanner.next();
                                    if (!isCaseSensitive) {
                                        reply = reply.toLowerCase();
                                        currentTerm = currentTerm.toLowerCase();
                                    }
                                    if (reply.trim().equals(currentTerm)) {
                                        currentScore++;
                                    }
                                    scanner.nextLine();
                                }
                        
                                testedIndex.add(currentIndex);
                                i++;
                            }
                        }

                        float score = (float) currentScore / maxScore * 100;
                        System.out.println("Congratulations on finishing this test! Your score is " + score + "/100.");

                    } else if (mode == 4) {
                        // Add cards
                        int nTerms = 0;
                        String term = new String();
                        String definition= new String();
                        Hashtable<String, String> oldCards = currentSet.getCards();
                        Hashtable<String, String> newCards = new Hashtable<>();

                        correctAnswer = false;
                        while (!correctAnswer) {
                            System.out.print("Number of cards to add: ");
                            try {
                                nTerms = scanner.nextInt();
                                scanner.nextLine();
                                correctAnswer = (nTerms > 0);;
                                if (!correctAnswer) {
                                    System.out.println("Invalid command. Please enter a positive number.");
                                }
                            } catch (Exception e) {
                                System.out.println("Invalid command. Please enter a positive number.");
                                scanner.nextLine();
                            }
                        }

                        // Add new cards with user's input
                        int i = 0;
                        while (i < nTerms) { 
                            System.out.print("Enter a term: ");
                            term = scanner.next(); 
                            scanner.nextLine();

                            System.out.print("Enter the definition: ");
                            definition = scanner.next(); 
                            scanner.nextLine();

                            if (!oldCards.containsKey(term) && !newCards.containsKey(term)) {
                                newCards.put(term, definition);      
                                i++;
                            } else {
                                System.out.println("This term has been previously added. Add a different term instead.");
                            }

                            term = "";
                        }
                        
                        currentSet.addCards(newCards);

                    } else if (mode == 5) {
                        // Delete a card
                        // Print available cards to user
                        currentSet.printCards();

                        // Prompt user for card to delete
                        answer = "";
                        Hashtable<String, String> cards = currentSet.getCards();

                        while (!cards.containsKey(answer)) {
                            System.out.print("Enter the TERM to delete (case sensitive): ");
                            answer = scanner.next();
                            scanner.nextLine();
                        }

                        currentSet.deleteCard(answer);
                        answer = "";
                    } else if (mode == 6) {
                        // Go to main menu
                        user.exitSet();
                    } else if (mode == 7) {
                        // Terminate the program
                        exit = true;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }      
}
