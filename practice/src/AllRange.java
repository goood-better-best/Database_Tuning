import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class AllRange {

    public static void execute(Connection con) throws Exception {
        int arraySize = 10;
        String SQLStmt = "select object_id, object_name from all_objects";
        Statement stmt = con.createStatement();
        stmt.setFetchSize(arraySize);
        ResultSet rs = stmt.executeQuery(SQLStmt);
        while (rs.next()) {
            System.out.println(rs.getLong(1) + " : " + rs.getString(2));
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
