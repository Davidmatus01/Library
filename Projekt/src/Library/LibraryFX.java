package Library;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LibraryFX extends Application {
    private Library library;
    private TextArea textArea;
    private Label titleLabel;

    private Connection connection;

    @Override
    public void stop() throws Exception {
        XmlHandler.saveLibrary(library.getAllBooks());
        try{
            connection.close();
        }catch (Exception e){
            System.out.println("No connection to close.");
        }

    }

    @Override
    public void start(Stage primaryStage) {
        library = new Library();
        List<Book> loadedBooks = XmlHandler.loadLibrary();
        library.addAllBooks(loadedBooks);

        try {
            connection = DriverManager.getConnection("jdbc:derby://localhost:1527/libraries;create=true");
            DatabaseMan.createReviewsTable(connection);
        } catch (SQLException e) {
            System.out.println("couldn't connect to database");
        }

        primaryStage.setTitle("Library");

        // Vytvoření menu
        Menu fileMenu = new Menu("Library");
        MenuItem addItem = new MenuItem("Add Book");
        addItem.setOnAction(e -> showAddBookDialog());

        MenuItem markBookItem = new MenuItem("Read Book");
        markBookItem.setOnAction(e -> showMarkBookDialog());

        MenuItem removeBookItem = new MenuItem("Remove Book");
        removeBookItem.setOnAction(e -> showRemoveBookDialog());

        MenuItem reviewBookItem = new MenuItem("Review Book");
        reviewBookItem.setOnAction(e -> showAddReviewDialog(null));

        fileMenu.getItems().addAll(addItem, markBookItem, removeBookItem, reviewBookItem);
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(fileMenu);

        // Vytvoření čudlíků
        Button showAllButton = new Button("Show All");
        showAllButton.setOnAction(e -> displayBooks(false, false));

        Button showUnreadButton = new Button("Show Unread");
        showUnreadButton.setOnAction(e -> displayBooks(true, false));

        Button showReadButton = new Button("Show Read");
        showReadButton.setOnAction(e -> displayBooks(true, true));


        Button showReviewsButton = new Button("Show Reviews");
        showReviewsButton.setOnAction(e -> showReviewsStage());

        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(10));
        buttonBox.getChildren().addAll(showAllButton, showUnreadButton, showReadButton, showReviewsButton);

        // Vytvoření místa pro text
        textArea = new TextArea();
        textArea.setEditable(false);
        VBox.setVgrow(textArea, javafx.scene.layout.Priority.ALWAYS);

        // Popisek místa pro text
        titleLabel = new Label("All Books");
        titleLabel.setStyle("-fx-font-weight: bold");

        displayBooks(false, false);

        BorderPane layout = new BorderPane();
        layout.setTop(menuBar);
        layout.setCenter(new VBox(5, titleLabel, textArea));
        layout.setBottom(buttonBox);

        Scene scene = new Scene(layout, 700, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Dialog pro přidání knihy do knihovny
     */
    private void showAddBookDialog() {
        Dialog<Book> dialog = new Dialog<>();
        dialog.setTitle("Add Book");
        dialog.setHeaderText("Please enter book details:");

        ButtonType addButton = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButton, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        TextField titleField = new TextField();
        TextField authorField = new TextField();
        TextField genreField = new TextField();
        TextField pagesField = new TextField();
        TextField isbnField = new TextField();

        content.getChildren().addAll(
                new Label("Title:"), titleField,
                new Label("Author:"), authorField,
                new Label("Genre:"), genreField,
                new Label("Pages:"), pagesField,
                new Label("ISBN:"), isbnField
        );

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButton) {
                try {
                    String title = titleField.getText().trim();
                    String author = authorField.getText().trim();
                    String genre = genreField.getText().trim();
                    String pagesText = pagesField.getText().trim();
                    String isbn = isbnField.getText().trim();

                    if (title.isEmpty() || author.isEmpty() || genre.isEmpty() || pagesText.isEmpty() || isbn.isEmpty()) {
                        throw new IllegalArgumentException("All fields must be filled out");
                    }

                    int pages = Integer.parseInt(pagesText);
                    return new Book(title, author, genre, pages, isbn);
                } catch (IllegalArgumentException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Input Error");
                    alert.setHeaderText(null);
                    alert.setContentText("All fields must be filled out and pages must be a valid number.");
                    alert.showAndWait();
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(book -> {
            library.addBook(book);
            displayBooks(false, true);
        });
    }

    /**
     * Zobrazí knihy podle vstupů
     * @param filtered jestli se bude filtrovat(false vypíše všechny knihy)
     * @param read jestli filtrované knihy jsou přečtené nebo ne
     */
    private void displayBooks(boolean filtered, boolean read) {
        List<Book> booksToDisplay;
        String title;

        if (filtered) {
            booksToDisplay = library.getFilteredBooks(read);
            title = read ? "Read Books" : "Unread Books";
        } else {
            booksToDisplay = library.getAllBooks();
            title = "All Books";
        }

        StringBuilder sb = new StringBuilder();
        for (Book book : booksToDisplay) {
            sb.append(book.toString()).append("\n");
        }
        textArea.setText(sb.toString());
        titleLabel.setText(title);
    }

    /**
     * Dialog pro označení knihy za přečtenou, vyvolá otázku zda chceme i dát recenzi
     */
    private void showMarkBookDialog() {
        List<Book> unreadBooks = library.getFilteredBooks(false);
        List<String> names = new ArrayList<>();
        for (Book book : unreadBooks) {
            names.add(book.getTitle());
        }
        if (unreadBooks.isEmpty()) {
            showAlert("No unread books", "There are no unread books in the library.");
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(names.get(0), names);
        dialog.setTitle("Mark Book");
        dialog.setHeaderText("Select a book to mark as read:");
        dialog.setContentText("Book:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            boolean wantsReview = showConfirmationDialog("Leave a review", "Do you want to leave a review for this book?");
            library.markBookAsRead(name);
            displayBooks(true, false);
            if (wantsReview) {
                showAddReviewDialog(name);
            }
        });
    }

    /**
     * Ukáže dialog pro odstranění knihy, vybere se kniha podle názvu a je vymazána
     */
    private void showRemoveBookDialog() {
        List<Book> books = library.getAllBooks();
        List<String> names = library.getNames();

        if (books.isEmpty()) {
            showAlert("No books", "There are no books in the library.");
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(names.get(0), names);
        dialog.setTitle("Remove Book");
        dialog.setHeaderText("Select a book to remove:");
        dialog.setContentText("Book:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            library.removeBook(name);
            displayBooks(false, false);
        });
    }

    /**
     * Upozornění
     * @param title nadpis pro upozornění
     * @param message samotná zpráva
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    /**
     * Dialog pro přidání recenze ke knize
     */
    private void showAddReviewDialog(String title) {
        List<Book> books = library.getAllBooks();
        List<String> titles;
        String selectedTitle;
        if(title != null){
            selectedTitle = title;
        }else{
            titles = library.getNames();
            if (books.isEmpty()) {
                showAlert("No books", "There are no books in the library.");
                return;
            }
            ChoiceDialog<String> choiceDialog = new ChoiceDialog<>(titles.get(0), titles);
            choiceDialog.setTitle("Select Book to review");
            choiceDialog.setHeaderText("Please select a book you wish to review:");

            Optional<String> result = choiceDialog.showAndWait();
            if (result.isEmpty()) {
                return;
            }
            selectedTitle = result.get();
        }

        String finalTitle = selectedTitle;
        String selectedIsbn = books.stream()
                .filter(book -> book.getTitle().equals(finalTitle))
                .map(Book::getIsbn)
                .findFirst()
                .orElse(null);

        Dialog<Review> dialog = new Dialog<>();
        dialog.setTitle("Add Review");
        dialog.setHeaderText("Please enter review details:");

        ButtonType addButton = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButton, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        TextField ratingField = new TextField();
        TextField noteField = new TextField();

        content.getChildren().addAll(
                new Label("ISBN:"), new Label(selectedIsbn),
                new Label("Rating x/10:"), ratingField,
                new Label("Note:"), noteField
        );

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            int rating = 0;
            if (dialogButton == addButton) {
                try{
                    rating = Integer.parseInt(ratingField.getText());
                    if(0 > rating || rating > 10){
                        throw new IllegalArgumentException("inserted input in Rating field, input must be number in range 0-10");
                    }
                }catch (IllegalArgumentException e){
                    showAlert("Wrong Input", e.getMessage());
                    showAddReviewDialog(finalTitle);
                    return null;
                }
                return new Review(selectedIsbn, rating, noteField.getText());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(review -> {
            try {
                DatabaseMan.saveReview(connection, review.getIsbn(), review.getRating(), review.getNote());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Potvrzení pro akci
     * @param title nadpis
     * @param message samotná zpráva
     * @return True/False podle potvrzení nebo ne
     */
    private boolean showConfirmationDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Vyvolá okno pro zobrazování recenzí
     */
    private void showReviewsStage() {
        Stage reviewsStage = new Stage();
        reviewsStage.setTitle("Reviews");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        TextField isbnField = new TextField();
        isbnField.setPromptText("Enter ISBN");

        TextArea reviewsArea = new TextArea();
        reviewsArea.setEditable(false);

        Button showReviewsButton = new Button("Show Reviews");
        showReviewsButton.setOnAction(e -> {
            String isbn = isbnField.getText();
            String reviewsString = DatabaseMan.createReviewsString(connection, isbn);
            reviewsArea.setText(reviewsString);
        });
        Button deleteReviewButton = new Button("Delete Review");
        deleteReviewButton.setOnAction(e -> {
            String isbn = isbnField.getText();
            List<Integer> reviewIds = DatabaseMan.getReviewIds(connection, isbn);

            if (!reviewIds.isEmpty()) {
                ChoiceDialog<Integer> choiceDialog = new ChoiceDialog<>(reviewIds.get(0), reviewIds);
                choiceDialog.setTitle("Select Review to Delete");
                choiceDialog.setHeaderText("Please select a review you wish to delete:");

                Optional<Integer> result = choiceDialog.showAndWait();
                result.ifPresent(reviewId -> {
                    try {
                        DatabaseMan.deleteReview(connection, reviewId);
                        String reviewsString = DatabaseMan.createReviewsString(connection, isbn);
                        reviewsArea.setText(reviewsString);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                });
            }
        });
        HBox buttonLayout = new HBox(10);
        buttonLayout.getChildren().addAll(showReviewsButton, deleteReviewButton);

        layout.getChildren().addAll(isbnField, buttonLayout, reviewsArea);
        Scene scene = new Scene(layout, 500, 300);
        reviewsStage.setScene(scene);
        reviewsStage.show();
    }



    public static void main(String[] args) {
        launch(args);
    }
}
