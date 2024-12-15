package edu.java.lab2;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.BorderLayout;
import java.awt.event.*;
import java.io.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;


import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import javax.xml.transform.*;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;



/* 
 * Author: Ivan_Arno
 * Version: 1.1
 * Since: 2024
 */

public class App {
    private JFrame frame;
    private JToolBar buttonsPanel;
    private JButton save;
    private JButton open;
    private JButton print;
    private JButton add;
    private JButton edit;
    private JButton delete;
    private JButton info;
    private JButton filter;
    DefaultTableModel model;
    private JTable films;
    private JComboBox<String> name;
    private JTextField filmName;
    private JPanel filterPanel;
    private SharedResource sharedResource;
    
    public App() {
        sharedResource = new SharedResource();
    }
    
    // Основной метод для отображения окна приложения
    public void show() {
        setupFrame();
        setupToolbar();
        setupTable();
        setupSearchPanel();
        setupEventHandlers();
        
        // Отображение окна
        frame.setVisible(true);
    }

    // Настройка основного окна
    private void setupFrame() {
        frame = new JFrame("Список фильмов");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 300);
        frame.setLocationRelativeTo(null);
    }

    // Настройка панели инструментов с кнопками
    private void setupToolbar() {
        buttonsPanel = new JToolBar();
        
        // Создание кнопок с иконками и подсказками
        save = new JButton(new ImageIcon("./Icons/save2.png"));
        save.setToolTipText("Сохранить список фильмов");
        
        open = new JButton(new ImageIcon("./Icons/Open.png"));
        open.setToolTipText("Открыть список фильмов");
        
        print = new JButton(new ImageIcon("./Icons/Print.png"));
        print.setToolTipText("Открыть окно печати");
        
        add = new JButton(new ImageIcon("./Icons/add.png"));
        add.setToolTipText("Добавить новый фильм");
        
        edit = new JButton(new ImageIcon("./Icons/edit.png"));
        edit.setToolTipText("Редактировать выбранный фильм");
        
        delete = new JButton(new ImageIcon("./Icons/trash.png"));
        delete.setToolTipText("Удалить выбранный фильм");
        
        info = new JButton(new ImageIcon("./Icons/info.png"));
        info.setToolTipText("Посмотреть информацию о выбранном фильме");

        // Добавление кнопок на панель
        buttonsPanel.add(save);
        buttonsPanel.add(open);
        buttonsPanel.add(print);
        buttonsPanel.add(add);
        buttonsPanel.add(edit);
        buttonsPanel.add(delete);
        buttonsPanel.add(info);
        
        // Добавление панели инструментов в верхнюю часть окна
        frame.getContentPane().add(BorderLayout.NORTH, buttonsPanel);
    }

    // Настройка таблицы для отображения данных о фильмах
    private void setupTable() {
        String[] columns = { "Фильм", "Жанр", "Сеанс", "Проданные билеты", "Режиссер", "Год", "Студия" };
        Object[][] data = {
            {"Дюна", "Научная фантастика", "18:00", "120", "Дени Вильнёв", "2021", "Legendary Pictures"},
            {"Темные времена", "Драма", "20:30", "90", "Джо Райт", "2017", "Working Title Films"}
        };
        
        model = new DefaultTableModel(data, columns);
        films = new JTable(model);
        
        // Добавление таблицы с прокруткой в центр окна
        frame.add(BorderLayout.CENTER, new JScrollPane(films));
    }

    // Настройка панели поиска
    private void setupSearchPanel() {
        name = new JComboBox<>(new String[] { "Фильм", "Жанр", "Сеанс" });
        filmName = new JTextField("Название фильма");
        filter = new JButton("Поиск");
        
        filterPanel = new JPanel();
        filterPanel.add(name);
        filterPanel.add(filmName);
        filterPanel.add(filter);
        
        // Добавление панели поиска в нижнюю часть окна
        frame.add(BorderLayout.SOUTH, filterPanel);
    }

    // Настройка обработчиков событий
    private void setupEventHandlers() {
        ButtonListener buttonListener = new ButtonListener();
        
        save.setActionCommand("Сохранить");
        open.setActionCommand("Открыть");
        print.setActionCommand("Печать");
        info.setActionCommand("Информация");
        add.setActionCommand("Добавить");
        delete.setActionCommand("Удалить");
        edit.setActionCommand("Редактировать");
        
        save.addActionListener(buttonListener);
        open.addActionListener(buttonListener);
        print.addActionListener(buttonListener);
        info.addActionListener(buttonListener);
        add.addActionListener(buttonListener);
        delete.addActionListener(buttonListener);
        edit.addActionListener(buttonListener);
        
        // Обработчик для кнопки "Поиск"
        filter.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                try {
                    checkName(filmName);
                } catch (NullPointerException ex) {
                    JOptionPane.showMessageDialog(frame, ex.toString());
                } catch (MyException myEx) {
                    JOptionPane.showMessageDialog(null, myEx.getMessage());
                }
            }
        });
        
        // Обработчик фокуса для поля "Название фильма"
        filmName.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (filmName.getText().equals("Название фильма")) {
                    filmName.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (filmName.getText().isEmpty()) {
                    filmName.setText("Название фильма");
                }
            }
        });
    }

    // Обработчик событий кнопок
    private class ButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            switch (command) {
            	case "Сохранить":
            		new TThread(3, sharedResource, model).start();
            		break;
            	case "Открыть":
            		new TThread(1, sharedResource, model).start();
            		//readXML();
            		//openFilmList();
            		break;
            	case "Печать":
            		generatePDFReport();
            	    break;
                case "Информация":
                    showFilmInfo();
                    break;
                case "Редактировать":
                    editFilm();
                    break;
                case "Добавить":
                	new TThread(2, sharedResource, model).start();
                    break;
                case "Удалить":
                    deleteFilm();
                    break;
            }
        }
    }
        
    
        //Метод для сохранения списка фильмов
        void saveFilmList() {
        	FileDialog save = new FileDialog(frame, "Сохранение данных", FileDialog.SAVE);
        	save.setFile("*.txt");
        	save.setVisible(true); // Отобразить запрос пользователю
        	// Определить имя выбранного каталога и файла
        	String fileName = save.getDirectory() + save.getFile();
        	if (fileName == null) return; // Если пользователь нажал «отмена»
        	try {
        		BufferedWriter writer = new BufferedWriter (new FileWriter("test.txt"));
        		for (int i = 0; i < model.getRowCount(); i++) // Для всех строк
        		for (int j = 0; j < model.getColumnCount(); j++) // Для всех столбцов
        		{writer.write ((String) model.getValueAt(i, j)); // Записать значение из ячейки
        		writer.write("\n"); // Записать символ перевода каретки
        		}
        		writer.close();
        		 }
        		catch(IOException e) // Ошибка записи в файл
        		{ e.printStackTrace(); }
        }
        
      //Метод для открытия списка фильмов
        public void openFilmList() {
        	FileDialog open = new FileDialog(frame, "Открытие данных", FileDialog.LOAD);
        	open.setFile("*.txt");
        	open.setVisible(true); // Отобразить запрос пользователю
        	// Определить имя выбранного каталога и файла
        	String fileName = open.getDirectory() + open.getFile();
        	if(fileName == null) return; // Если пользователь нажал «отмена»
        	try {
        		BufferedReader reader = new BufferedReader(new FileReader("testOpen.txt"));
        		int rows = model.getRowCount();
        		for (int i = 0; i < rows; i++) model.removeRow(0); // Очистка таблицы
        		String film;
        		do {
        		film = reader.readLine();
        		if(film != null)
        		{ String genre = reader.readLine();	
        		String session = reader.readLine();
        		String ticketsSold = reader.readLine();
        		String director = reader.readLine();
        		String year = reader.readLine();
        		String studion = reader.readLine();
        		model.addRow(new String[]{film, genre, session, ticketsSold, director, year, studion}); // Запись строки в таблицу
        		}
        		} while(film != null);
        		reader.close();
        		} catch (FileNotFoundException e) {e.printStackTrace();} // файл не найден
        		 catch (IOException e) {e.printStackTrace();}

        }

        // Метод для показа информации о фильме
        private void showFilmInfo() {
            int selectedRow = films.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(frame, "Пожалуйста, выберите фильм для просмотра информации.");
                return;
            }

            String filmName = (String) model.getValueAt(selectedRow, 0);
            String genre = (String) model.getValueAt(selectedRow, 1);
            String session = (String) model.getValueAt(selectedRow, 2);
            String ticketsSold = (String) model.getValueAt(selectedRow, 3);
            String director = (String) model.getValueAt(selectedRow, 4);
            String year = (String) model.getValueAt(selectedRow, 5);
            String studio = (String) model.getValueAt(selectedRow, 6);

            Object[] message = {
                "Название фильма: " + filmName,
                "Жанр: " + genre,
                "Сеанс: " + session,
                "Проданные билеты: " + ticketsSold,
                "Режиссер: " + director,
                "Год выхода: " + year,
                "Студия: " + studio
            };

            JOptionPane.showMessageDialog(frame, message, "Информация о фильме", JOptionPane.INFORMATION_MESSAGE);
        }

        // Метод для редактирования фильма
        void editFilm() {
            int selectedRow = films.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(frame, "Пожалуйста, выберите строку для редактирования.");
                return;
            }

            JTextField filmNameInput = new JTextField((String) model.getValueAt(selectedRow, 0));
            JTextField genreInput = new JTextField((String) model.getValueAt(selectedRow, 1));
            JTextField sessionInput = new JTextField((String) model.getValueAt(selectedRow, 2));
            JTextField ticketsInput = new JTextField((String) model.getValueAt(selectedRow, 3));
            JTextField directorInput = new JTextField((String) model.getValueAt(selectedRow, 4));
            JTextField yearInput = new JTextField((String) model.getValueAt(selectedRow, 5));
            JTextField studioInput = new JTextField((String) model.getValueAt(selectedRow, 6));

            Object[] message = {
                "Название фильма:", filmNameInput,
                "Жанр:", genreInput,
                "Сеанс:", sessionInput,
                "Проданные билеты:", ticketsInput,
                "Режиссер:", directorInput,
                "Год выхода:", yearInput,
                "Студия:", studioInput
            };

            int option = JOptionPane.showConfirmDialog(frame, message, "Редактировать фильм", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                model.setValueAt(filmNameInput.getText(), selectedRow, 0);
                model.setValueAt(genreInput.getText(), selectedRow, 1);
                model.setValueAt(sessionInput.getText(), selectedRow, 2);
                model.setValueAt(ticketsInput.getText(), selectedRow, 3);
                model.setValueAt(directorInput.getText(), selectedRow, 4);
                model.setValueAt(yearInput.getText(), selectedRow, 5);
                model.setValueAt(studioInput.getText(), selectedRow, 6);
            }
        }

        // Метод для удаления фильма
        void deleteFilm() {
            int selectedRow = films.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(frame, "Не выбрана строка для удаления. Пожалуйста, выберите строку.");
            } else {
                model.removeRow(selectedRow);
            }
        }
    

    // Проверка корректности введенного названия фильма
     void checkName(JTextField bName) throws MyException, NullPointerException {
        String sName = bName.getText();
        if (sName.contains("Название фильма")) throw new MyException();
        if (sName.length() == 0) throw new NullPointerException();
    }

    // Кастомное исключение для пустого названия фильма
    class MyException extends Exception {
        public MyException() {
            super("Вы не ввели название фильма для поиска.");
        }
    }
    
    //XML документ
    private void exportToXML() {
        org.w3c.dom.Document doc = null;
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = builder.newDocument();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return;
        }
        
        // Создаем корневой элемент и добавляем его
        Element filmlist = doc.createElement("filmlist");
        doc.appendChild(filmlist);
        
        // Создаем элементы "film" для каждой строки в таблице
        for (int i = 0; i < model.getRowCount(); i++) {
            Element film = doc.createElement("film");
            film.setAttribute("name", (String) model.getValueAt(i, 0));
            film.setAttribute("genre", (String) model.getValueAt(i, 1));
            film.setAttribute("session", (String) model.getValueAt(i, 2));
            film.setAttribute("ticketsSold", (String) model.getValueAt(i, 3));
            film.setAttribute("director", (String) model.getValueAt(i, 4));
            film.setAttribute("year", (String) model.getValueAt(i, 5));
            film.setAttribute("studio", (String) model.getValueAt(i, 6));
            filmlist.appendChild(film);
        }
        
        try {
            // Настройка преобразователя и запись в файл
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
            
            FileWriter writer = new FileWriter("films.xml");
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            writer.close();
        } catch (TransformerException | IOException e) {
            e.printStackTrace();
        }
        JOptionPane.showMessageDialog(frame, "XML-документ успешно сохранен.");
    }
    
   //Чтение XML
    private void readXML(DefaultTableModel model) {
    	org.w3c.dom.Document doc = null;
    	try {
    		// Создание парсера документа
    		DocumentBuilder dBuilder =
    		DocumentBuilderFactory.newInstance().newDocumentBuilder();
    		// Чтение документа из файла
    		doc = dBuilder.parse(new File("films.xml"));
    		// Нормализация документа
    		doc.getDocumentElement().normalize();
    		}
    		catch (ParserConfigurationException e) { e.printStackTrace(); }
    		// Обработка ошибки парсера при чтении данных из XML-файла
    		catch (SAXException e) { e.printStackTrace(); }
    		catch (IOException e) { e.printStackTrace(); }
    		// Получение списка элементов с именем book
    		 NodeList nlfilms = doc.getElementsByTagName("film");
    		// Цикл просмотра списка элементов и запись данных в таблицу
    		 for (int temp = 0; temp < nlfilms.getLength(); temp++) {
    		// Выбор очередного элемента списка
    		Node elem = nlfilms.item(temp);
    		// Получение списка атрибутов элемента
    		NamedNodeMap attrs = elem.getAttributes();
    		// Чтение атрибутов элемента
    		String name = attrs.getNamedItem("name").getNodeValue();
    		String genre = attrs.getNamedItem("genre").getNodeValue();
    		String session = attrs.getNamedItem("session").getNodeValue();
    		String ticketsSold = attrs.getNamedItem("ticketsSold").getNodeValue();
    		String director = attrs.getNamedItem("director").getNodeValue();
    		String year = attrs.getNamedItem("year").getNodeValue();
    		String studio = attrs.getNamedItem("studio").getNodeValue();
    		// Запись данных в таблицу
    		int rows = model.getRowCount();
    		for (int i = 0; i < rows; i++) model.removeRow(0); // Очистка таблицы
    		model.addRow(new String[]{name, genre, session, ticketsSold, director, year, studio});
    		}	
    }
    
    public class PdfSave {
    public static void savePdf(DefaultTableModel tableModel) throws IOException {
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfPTable table = new PdfPTable(tableModel.getColumnCount());

        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(".\\report.pdf"));
            document.open();

            BaseFont bfComic = BaseFont.createFont("C:\\Windows\\Fonts\\times.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font font1 = new Font(bfComic, 12);

            // Добавление заголовков колонок
            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                table.addCell(new PdfPCell(new Phrase(tableModel.getColumnName(i), font1)));
            }

            // Добавление данных из таблицы
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    table.addCell(new Phrase((String) tableModel.getValueAt(i, j), font1));
                }
            }

            document.add(table);
        } catch (FileNotFoundException | DocumentException e) {
            e.printStackTrace();
            throw new IOException("Ошибка при создании PDF"); // Проброс исключения
        } finally {
            document.close();
        }
    }
   }
    
    public class HtmlSave {

        public static void saveHtml(DefaultTableModel tableModel) {
            PrintWriter pw = null;
            try {
                pw = new PrintWriter(new FileWriter(".\\report.html"));
                pw.println("<html><head><title>Список фильмов</title></head><body>");
                pw.println("<h1>Список фильмов</h1>");
                pw.println("<TABLE BORDER><TR><TH>Фильм</TH><TH>Жанр</TH><TH>Сеанс</TH><TH>Проданные билеты</TH><TH>Режиссер</TH><TH>Год</TH><TH>Студия</TH></TR>");

                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    pw.println("<TR><TD>" + (String) tableModel.getValueAt(i, 0) + 
                               "<TD>" + (String) tableModel.getValueAt(i, 1) + 
                               "<TD>" + (String) tableModel.getValueAt(i, 2) + 
                               "<TD>" + (String) tableModel.getValueAt(i, 3) + 
                               "<TD>" + (String) tableModel.getValueAt(i, 4) + 
                               "<TD>" + (String) tableModel.getValueAt(i, 5) + 
                               "<TD>" + (String) tableModel.getValueAt(i, 6) + "</TD></TR>");
                }
                
                pw.println("</TABLE>");
                pw.println("</body></html>");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (pw != null) {
                    pw.close();
                }
            }
        }
    }
    
    private void generatePDFReport() {
        try {
            PdfSave.savePdf(model);
            JOptionPane.showMessageDialog(frame, "PDF-документ успешно сохранен.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Ошибка при сохранении PDF: " + e.getMessage());
        }
    }
    
    private void chooseSaveFormat() {
        String[] options = {"XML", "HTML"};
        int choice = JOptionPane.showOptionDialog(frame,
                "Выберите формат для сохранения:",
                "Выбор формата",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == 0) {
            exportToXML();
        } else if (choice == 1) {
            exportToHtml(model); // Передача модели
        }
    }
    
    private void chooseOpenFormat(DefaultTableModel model) {
        String[] options = {"XML", "TXT"};
        int choice = JOptionPane.showOptionDialog(frame,
                "Выберите формат открытия:",
                "Выбор формата",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == 0) {
            readXML(model);
        } else if (choice == 1) {
            openFilmList(); // Передача модели
        }
    }

    private void exportToHtml(DefaultTableModel tableModel) {
        HtmlSave.saveHtml(tableModel);
        JOptionPane.showMessageDialog(frame, "HTML-документ успешно сохранен.");
    }

    class TThread extends Thread {
        private final int type;
        private final SharedResource shared;
        private final DefaultTableModel model;

        public TThread(int type, SharedResource shared, DefaultTableModel model) {
            this.type = type;
            this.shared = shared;
            this.model = model;
        }

        @Override
        public void run() {
            switch (type) {
                case 1:
                    shared.openXML(model);
                    break;
                case 2:
                    shared.addRow(model);
                    break;
                case 3:
                    shared.savePDFandHTML();
                    break;
                default:
                    throw new IllegalArgumentException("Неправильный тип потока: " + type);
            }
        }
    }

    // Класс для управления общими ресурсами с блокировками
    class SharedResource {
        private final ReentrantLock lock = new ReentrantLock();
        private final Condition condition = lock.newCondition();
        private boolean isProcessing = false;

        public void openXML(DefaultTableModel model) {
            lock.lock();
            try {
                while (isProcessing) {
                    condition.await();
                }
                isProcessing = true;
                // Логика открытия XML
                chooseOpenFormat(model);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                isProcessing = false;
                condition.signalAll(); // Уведомление остальных потоков
                lock.unlock();
            }
        }

        public void addRow(DefaultTableModel tableModel) {
            lock.lock();
            try {
                while (isProcessing) {
                    condition.await();
                }
                isProcessing = true;
                tableModel.addRow(new String[]{"Ночь", "Денщик и офицер", "1684", "Да"});
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                isProcessing = false;
                condition.signalAll();
                lock.unlock();
            }
        }

        public void savePDFandHTML() {
            lock.lock();
            try {
                while (isProcessing) {
                    condition.await();
                }
                isProcessing = true;
                chooseSaveFormat();
//                generatePDFReport(); // Сохранение PDF
//                exportToHtml(tableModel); // Сохранение HTML
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                isProcessing = false;
                condition.signalAll();
                lock.unlock();
            }
        }
    }
    

    	

    public static void main(String[] args) {
    	SwingUtilities.invokeLater(() -> new App().show());
    }
}

