
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author leoomoreira
 */
public class TestSQL {

    public static void main(String[] args) {
        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/smartlab", "smartlab", "ufc123");
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT \"timestamp\", laboratory_id, temperature, light, presence, proximity FROM laboratory;");
            while (resultSet.next()) {
                Timestamp t = resultSet.getTimestamp("timestamp");

               
                SimpleDateFormat d = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
                System.out.println(d.format(t.getTime()));
            }
            resultSet.close();
            statement.close();
            connection.close();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }
}
