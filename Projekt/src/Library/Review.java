package Library;

public class Review {

    private int id;

    private final String isbn;
    private final int rating;
    private final String note;

    public Review(String isbn, int rating, String note) {
        this.isbn = isbn;
        this.rating = rating;
        this.note = note;
    }

    public String getIsbn() {
        return isbn;
    }

    public int getRating() {
        return rating;
    }

    public String getNote() {
        return note;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}