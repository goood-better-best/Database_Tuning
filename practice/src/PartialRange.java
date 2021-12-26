import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class PartialRange {
    public static int fetch(ResultSet rs, int arraySize) throws Exception {
        int i = 0;
        while (rs.next()) {
            System.out.println(rs.getLong(1) + " : " + rs.getString(2));
            if(++i >= arraySize) return i;
        }
        return i;
    }

    public static void execute(Connection con) throws Exception {
        int arraySize = 10;
        String SQLStmt = "select object_id, object_name from all_objects";
        Statement stmt = con.createStatement();
        stmt.setFetchSize(arraySize);
        ResultSet rs = stmt.executeQuery(SQLStmt);
        while (true) {
            int r = fetch(rs, arraySize);
            if (r < arraySize) break;
            System.out.println("Enter to Continue ...(Q)uit? ");
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            String input = in.readLine();
            if(input.equals("Q")) break;
        }

        rs.close();
        stmt.close();
    }

    public static void main(String[] args) {
        /*Connection con = getConnection();
        execute(con);
        releaseConnection();*/
    }
}
