package Library;

import javax.xml.stream.*;
import java.io.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class XmlHandler {
    private static final String FILE_NAME = "library.xml";

    /**
     * uloží knihovnu do xml souboru
     * @param books obsah knihovny
     */
    public static void saveLibrary(List<Book> books) {
        try {
            XMLOutputFactory factory = XMLOutputFactory.newInstance();
            OutputStream outputStream = new FileOutputStream(FILE_NAME);
            XMLStreamWriter writer = factory.createXMLStreamWriter(outputStream, "UTF-8");

            writer.writeStartDocument("UTF-8", "1.0");
            writer.writeStartElement("library");

            for (Book book : books) {
                writer.writeStartElement("book");
                writeBook(writer, book);
                writer.writeEndElement();
            }

            writer.writeEndElement();
            writer.writeEndDocument();
            writer.flush();
            writer.close();
        } catch (FileNotFoundException | XMLStreamException e) {
            e.printStackTrace();
        }
    }

    /**
     * zapíše knihu do XML souboru
     * @param writer zapisovatel do souboru
     * @param book kniha co se má zapsat
     * @throws XMLStreamException XML error
     */
    private static void writeBook(XMLStreamWriter writer, Book book) throws XMLStreamException {
        Method[] methods = Book.class.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(SaveLib.class)) {
                SaveLib annotation = method.getAnnotation(SaveLib.class);
                String name = annotation.name();
                try {
                    Object value = method.invoke(book);
                    writer.writeStartElement(name);
                    writer.writeCharacters(value.toString());
                    writer.writeEndElement();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Načte knihovnu z XML souboru
     * @return ArrayList načtených knih
     */
    public static List<Book> loadLibrary() {
        List<Book> books = new ArrayList<>();
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            InputStream inputStream = new FileInputStream(FILE_NAME);
            XMLStreamReader reader = factory.createXMLStreamReader(inputStream);

            Book book = null;
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getLocalName().equals("book")) {
                        book = new Book();
                    } else if (book != null) {
                        String localName = reader.getLocalName();
                        setBookField(book, localName, reader.getElementText());
                    }
                } else if (event == XMLStreamConstants.END_ELEMENT && reader.getLocalName().equals("book")) {
                    books.add(book);
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("Library was not found, it will be created upon exit");
        } catch (XMLStreamException e){
            e.printStackTrace();
        }
        return books;
    }

    /**
     * Z XML načte data knihy a přislušně vyplní atribut knihy
     * @param book kniha do které se zapisuje
     * @param fieldName název atributu který se má vložit
     * @param value hodnota atributu
     */
    private static void setBookField(Book book, String fieldName, String value) {
        Method[] methods = Book.class.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(LoadLib.class)) {
                LoadLib annotation = method.getAnnotation(LoadLib.class);
                if (annotation.name().equals(fieldName)) {
                    method.setAccessible(true);
                    try {
                        switch (method.getParameterTypes()[0].getName()) {
                            case "int" -> method.invoke(book, Integer.parseInt(value));
                            case "boolean" -> method.invoke(book, Boolean.parseBoolean(value));
                            case "java.lang.String" -> method.invoke(book, value);
                            default -> throw new IllegalArgumentException("Unsupported field type: " + method.getParameterTypes()[0].getName());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
    }
}