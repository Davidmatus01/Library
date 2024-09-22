package Library;

public class Book {

    private String title;

    private String author;

    private String genre;

    private boolean read;

    private int pages;

    private String isbn;

    public Book(String title, String author, String genre, int pages, String isbn) {
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.read = false;
        this.pages = pages;
        this.isbn = isbn;
    }

    public Book() {
    }

    // Gettery a settery
    @SaveLib(name = "title")
    public String getTitle() {
        return title;
    }

    @LoadLib(name = "title")
    public void setTitle(String title) {
        this.title = title;
    }

    @SaveLib(name = "author")
    public String getAuthor() {
        return author;
    }

    @LoadLib(name = "author")
    public void setAuthor(String author) {
        this.author = author;
    }

    @SaveLib(name = "genre")
    public String getGenre() {
        return genre;
    }

    @LoadLib(name = "genre")
    public void setGenre(String genre) {
        this.genre = genre;
    }

    @SaveLib(name = "read")
    public boolean isRead() {
        return read;
    }

    @LoadLib(name = "read")
    public void setRead(boolean read) {
        this.read = read;
    }

    @SaveLib(name = "pages")
    public int getPages() {
        return pages;
    }

    @LoadLib(name = "pages")
    public void setPages(int pages) {
        this.pages = pages;
    }

    @SaveLib(name = "isbn")
    public String getIsbn() {
        return isbn;
    }

    @LoadLib(name = "isbn")
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    @Override
    public String toString() {
        return "Title: " + title + ", Author: " + author + ", Genre: " + genre  +
                ", Pages: " + pages + ", ISBN: " + isbn;
    }

    public void markAsRead() {
        this.read = true;
    }
}
