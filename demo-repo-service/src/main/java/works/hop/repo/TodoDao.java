package works.hop.repo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.h2.tools.RunScript;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class TodoDao {

    private final Properties properties;
    private final String propsFile;
    private final String initSchema;
    private final String initData;

    public TodoDao() {
        this("/todos-db.properties", "/create-tables.sql", "/initial-data.sql");
    }


    public TodoDao(String propsFile, String initSchema, String initData) {
        this.propsFile = propsFile;
        this.initSchema = initSchema;
        this.initData = initData;

        this.properties = new Properties();
        try {
            properties.load(TodoDao.class.getResourceAsStream(propsFile));
            Class.forName(properties.getProperty("jdbc.driver"));
            initialize();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void initialize() {
        try (Connection conn = connection()) {
            //Initialize the script runner
            RunScript.execute(conn, new BufferedReader(new InputStreamReader(TodoDao.class.getResourceAsStream(initSchema))));
            RunScript.execute(conn, new BufferedReader(new InputStreamReader(TodoDao.class.getResourceAsStream(initData))));
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private Connection connection() {
        try {
            return DriverManager.getConnection(properties.getProperty("jdbc.url"), properties.getProperty("jdbc.username"), properties.getProperty("jdbc.password"));
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public List<Todo> fetchTodoItems(Integer limit, Integer offset) {
        List<Todo> todoItems = new ArrayList<>();
        try (Connection conn = connection()) {
            PreparedStatement st = conn.prepareStatement("SELECT name, completed from tbl_todos limit ? offset ?");
            st.setInt(1, limit);
            st.setInt(2, offset);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                String name = rs.getString("name");
                Boolean done = rs.getBoolean("completed");
                todoItems.add(new Todo(name, done));
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
            throw new RuntimeException(ex);
        }
        return todoItems;
    }

    public void addTodoItem(String name) {
        try (Connection conn = connection()) {
            PreparedStatement st = conn.prepareStatement("merge INTO tbl_todos (name) key (name) VALUES (?)");
            st.setString(1, name);
            st.executeUpdate();
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    public void updateTodoItem(Todo todo) {
        try (Connection conn = connection()) {
            PreparedStatement st = conn.prepareStatement("UPDATE tbl_todos SET completed=? WHERE name=?");
            st.setBoolean(1, !todo.completed);
            st.setString(2, todo.name);
            st.executeUpdate();
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    public void removeTodoItem(String name) {
        try (Connection conn = connection()) {
            PreparedStatement st = conn.prepareStatement("DELETE FROM tbl_todos WHERE name=?");
            st.setString(1, name);
            st.executeUpdate();
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    public static class Todo {

        public final String name;
        public final Boolean completed;

        @JsonCreator
        public Todo(@JsonProperty("name") String name, @JsonProperty("completed") Boolean completed) {
            this.name = name;
            this.completed = completed;
        }
    }
}
