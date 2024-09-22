package Library;

import javafx.scene.control.ChoiceDialog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DatabaseMan {
    private static final String INSERT_REVIEW_SQL = "INSERT INTO BookReviews (isbn, rating, note) VALUES (?, ?, ?)";
    private static final String SELECT_REVIEWS_SQL = "SELECT * FROM BookReviews WHERE isbn = ?";

    private static final String DELETE_REVIEW_SQL = "DELETE FROM BookReviews WHERE id = ?";
    private static final String CREATE_REVIEWS_TABLE_SQL = "CREATE TABLE BookReviews (" +
            "id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY," +
            "isbn VARCHAR(20)," +
            "rating INT," +
            "note VARCHAR(255)" +
            ")";

    /**
     * Uloží hodnocení a poznámku knihy do databáze.
     * @param connection připojení k databázovému serveru
     * @param isbn ISBN knihy
     * @param rating hodnocení knihy
     * @param note poznámka k knize
     * @throws SQLException SQL výjimka
     */
    public static void saveReview(Connection connection, String isbn, int rating, String note) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_REVIEW_SQL)) {
            statement.setString(1, isbn);
            statement.setInt(2, rating);
            statement.setString(3, note);
            statement.executeUpdate();
        }
    }

    /**
     * Načte všechna hodnocení a poznámky pro dané ISBN knihy z databáze.
     * @param connection připojení k databázovému serveru
     * @param isbn ISBN knihy
     * @return seznam hodnocení a poznámek
     * @throws SQLException SQL výjimka
     */
    public static List<Review> getReviews(Connection connection, String isbn) throws SQLException {
        List<Review> reviews = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SELECT_REVIEWS_SQL)) {
            statement.setString(1, isbn);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                int rating = resultSet.getInt("rating");
                String note = resultSet.getString("note");
                Review review = new Review(isbn, rating, note);
                review.setId(id);
                reviews.add(review);
            }
        }
        return reviews;
    }

    /**
     * Vytvoří řetěžec všech hodnocení knihy
     * @param connection připojení k databázi
     * @param isbn ISBN knihy které hledáme hodnocení
     * @return řetězec hodnocení knihy
     */
    public static String createReviewsString(Connection connection, String isbn) {
        if (!isbn.isEmpty()) {
            List<Review> reviews;
            try {
                reviews = DatabaseMan.getReviews(connection, isbn);
                StringBuilder sb = new StringBuilder();
                for (Review review : reviews) {
                    sb.append("Rating: ").append(review.getRating()).append("/10, ID: ")
                            .append(review.getId()).append(" Note: ")
                            .append(review.getNote()).append("\n");
                }
                return sb.toString();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return ""; // Pokud není zadané ISBN nebo dojde k chybě, vrátí prázdný řetězec
    }

    /**
     * Získá IDčka všech recenzí knihy podle ISBN
     * @param connection připojení k databázi
     * @param isbn ISBN knihy od které hledáme recenze
     * @return list IDček recenzí
     */
    public static List<Integer> getReviewIds(Connection connection, String isbn) {
        List<Integer> ids = new ArrayList<>();
        if (!isbn.isEmpty()) {
            try {
                List<Review> reviews = DatabaseMan.getReviews(connection, isbn);
                for (Review review : reviews) {
                    ids.add(review.getId());
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return ids;
    }

    /**
     * Smazání hodnocení nebo poznámky z databáze podle ID.
     * @param connection připojení k databázovému serveru
     * @param id ID hodnocení nebo poznámky
     * @throws SQLException SQL výjimka
     */
    public static void deleteReview(Connection connection, int id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_REVIEW_SQL)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    /**
     * vytvoří tabulku pro hodnocení pokuď už neexistuje
     * @param connection připojení k databázi
     * @throws SQLException error u SQL
     */
    public static void createReviewsTable(Connection connection) throws SQLException {
        if (!tableExist(connection)){
            try (Statement statement = connection.createStatement()) {
                statement.execute(CREATE_REVIEWS_TABLE_SQL);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    /**
     * zjistí zda v databázi je tabulka pro recenze
     * @param connection připojení k databázi
     * @return true/false podle toho zda existuje tabulka
     * @throws SQLException SQL error
     */
    public static boolean tableExist(Connection connection) throws SQLException {
        boolean bookTableExists = false;

        try {
            DatabaseMetaData metaData = connection.getMetaData();

            ResultSet tables = metaData.getTables(null, null, null, new String[]{"TABLE"});

            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                if (tableName.equalsIgnoreCase("BookReviews")) {
                    bookTableExists = true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }

        return bookTableExists;
    }
}