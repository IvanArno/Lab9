package edu.java.lab2;
import org.junit.Before;
import org.junit.Test;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;

import static org.junit.Assert.*;

public class AppTest {

    private App app;
    private DefaultTableModel model;
    
    @Before
    public void setUp() {
        app = new App();
        model = new DefaultTableModel(new String[]{"Фильм", "Жанр", "Сеанс", "Проданные билеты", "Режиссер", "Год", "Студия"}, 0);
        app.model = model; // Устанавливаем модель в App для тестирования
    }

    @Test
    public void testSaveFilmList() {
        // Добавление примера данных
        model.addRow(new Object[]{"Дюна", "Научная фантастика", "18:00", "120", "Дени Вильнёв", "2021", "Legendary Pictures"});

        try {
            app.saveFilmList();
            
            // Проверяем, что файл корректно сохранен
            BufferedReader reader = new BufferedReader(new FileReader("test.txt"));
            String line = reader.readLine();
            assertEquals("Дюна", line); // Проверяем первую строку
            reader.close();
        } catch (IOException e) {
            fail("IOException was thrown: " + e.getMessage());
        }
    }

    @Test
    public void testOpenFilmList() {
        try {
            // Создание файла с данными для загрузки
            BufferedWriter writer = new BufferedWriter(new FileWriter("testOpen.txt"));
            writer.write("Темные времена\nДрама\n20:30\n90\nДжо Райт\n2017\nWorking Title Films\n");
            writer.close();

            // Открываем список фильмов
            app.openFilmList();

            // Проверка, что данные были добавлены в модель
            assertEquals(1, model.getRowCount());
            assertEquals("Темные времена", model.getValueAt(0, 0));
        } catch (IOException e) {
            fail("IOException was thrown: " + e.getMessage());
        }
    }

    @Test
    public void testCheckNameValid() throws Exception {
        JTextField filmNameField = new JTextField("Valid Film");
        app.checkName(filmNameField); // Не должно выбрасывать исключение
    }

    @Test
    public void testCheckNameInvalid() {
        JTextField filmNameField = new JTextField("Название фильма");
        Exception exception = null;

        try {
            app.checkName(filmNameField);
        } catch (App.MyException | NullPointerException e) {
            exception = e;
        }

        assertNotNull(exception);
        assertEquals("Вы не ввели название фильма для поиска.", exception.getMessage());
    }
}
