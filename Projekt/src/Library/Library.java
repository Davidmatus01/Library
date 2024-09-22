package Library;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class Library {
    private List<Book> books;
    private List<String> names;

    public Library() {
        this.books = new ArrayList<>();
        this.names = new ArrayList<>();
    }

    public void addBook(Book book) {
        books.add(book);
    }

    private void addName(String name){
        names.add(name);
    }

    public List<Book> getAllBooks() {
        return books.stream()
                .sorted(Comparator.comparing(Book::getTitle))
                .collect(toList());
    }

    public List<Book> getFilteredBooks(boolean read) {
        return getAllBooks()
                .stream()
                .filter(book -> book.isRead() == read)
                .toList();
    }

    public List<String> getNames() {
        return names.stream()
                .sorted()
                .collect(toList());
    }

    public void addAllBooks (List<Book> books){
        for (Book book: books) {
            addBook(book);
            addName(book.getTitle());
        }
    }

    public void markBookAsRead(String title) {
        for (Book book : books) {
            if (book.getTitle().equals(title)) {
                book.markAsRead();
                break;
            }
        }
    }

    public void removeBook(String title){
        books.removeIf(book -> book.getTitle().equals(title));
    }
}
