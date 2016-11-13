package rafal.pytel.resources;

import rafal.pytel.model.LogRecordBetter;
import rafal.pytel.model.LogRecordBetterList;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.ws.rs.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Rafal on 2016-11-12.
 */
@Path("/logagent/{logId}")
@Produces("application/json")
@Consumes("application/json")
public class LogRecordResource {

    public LogRecordResource() {
    }

    protected Connection getConnection() throws SQLException, NamingException {
        InitialContext ic = new InitialContext();
        DataSource ds = (DataSource) ic.lookup("jdbc/DSTix");
        return ds.getConnection();
    }


    public boolean ifNewType(int requiredSize, Connection db) throws SQLException {


        PreparedStatement st = db.prepareStatement("SELECT * from eventlogdb.database_max_size;");
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            int size = rs.getInt("possible_sizes");
            if (size == requiredSize) {
                return false;
            }
        }
        return true;
    }

    public void createNewTable(Connection db, int requiredSize, int logId) throws SQLException {
        String typeString = new String(" ");
        for (int i = 1; i <= requiredSize; i++) {
            typeString += ", type" + i + " VARCHAR(100)";
        }
        db.setAutoCommit(false);
        PreparedStatement statement = db.prepareStatement("INSERT INTO eventlogdb.database_max_size(possible_sizes) VALUES (?);");
        statement.setInt(1, requiredSize);
        statement.executeUpdate();
        String sqlStatement = new String("CREATE TABLE eventlogdb.logevent" + requiredSize + " (id INT(11) PRIMARY KEY AUTO_INCREMENT," +
                "date datetime NOT NULL DEFAULT NOW(),eventId INT(11) , logId INT(11)" + typeString + ");");
        PreparedStatement st = db.prepareStatement(sqlStatement);
        st.executeUpdate();
        PreparedStatement ps = db.prepareStatement("INSERT INTO eventlogdb.logs_to_event_type (logId,logevent) " +
                "VALUES (?,?);");
        ps.setInt(1, logId);
        ps.setInt(2, new Integer(requiredSize));
        ps.executeUpdate();
        db.commit();
        db.setAutoCommit(true);
    }


    public PreparedStatement getInsertQuery(Connection db, LogRecordBetter lr, int typeSize, int logId) throws SQLException {

        String typeString = new String();
        String valueNumber = new String();
        for (int i = 1; i <= typeSize; i++) {
            typeString += ",type" + i;
            valueNumber += ",?";
        }
        PreparedStatement st = db.prepareStatement("INSERT INTO eventlogdb.logevent" + typeSize +
                " (logId,eventId" + typeString + ") VALUES (?,?" + valueNumber + ");");
        st.setInt(1, logId);
        st.setInt(2, lr.getEventId());
        int i = 3;
        for (String type : lr.getType()) {
            st.setString(i, type);
            i++;
        }
        return st;

    }

    protected int whatTable(Connection db, int id) throws SQLException {
        PreparedStatement st = db.prepareStatement("SELECT  * From eventlogdb.logs_to_event_type WHERE logId=?;");
        st.setInt(1, id);
        ResultSet rs = st.executeQuery();
        rs.next();
        return rs.getInt("logevent");
    }

    public PreparedStatement getListQuery(Connection db, int id) throws SQLException {
        int whatTable = whatTable(db, id);
        PreparedStatement st = db.prepareStatement("SELECT * FROM eventlogdb.logevent" + whatTable + " where logId=?;");
        st.setInt(1, id);
        return st;
    }

    protected LogRecordBetter getFromResultSet(Connection db, int id, ResultSet rs) throws SQLException {

        int whatTable = whatTable(db, id);
        LogRecordBetter lr = new LogRecordBetter();
        lr.setId(new Long(rs.getInt("id")));
        lr.setTime(new Time((rs.getTimestamp("date").getTime())));
        lr.setEventId(rs.getInt("eventId"));
        List<String> list = new ArrayList<>();

        for (int i = 1; i <= whatTable; i++) {
            list.add(rs.getString("type" + i));
        }

        lr.setType(list);
        return lr;
    }

    protected PreparedStatement getDeleteQuery(Connection db, int id, int logId) throws SQLException {
        int whatTable = whatTable(db, logId);
        PreparedStatement st = db.prepareStatement("DELETE from eventlogdb.logevent" + whatTable + " WHERE id=? AND logId=?;");
        st.setInt(1, id);
        st.setInt(2, logId);
        return st;
    }

    protected void assignToTable(Connection db, int logId, int requiredSize) throws SQLException {
        PreparedStatement ps = db.prepareStatement("INSERT INTO eventlogdb.logs_to_event_type (logId,logevent) " +
                "VALUES (?,?);");
        ps.setInt(1, logId);
        ps.setInt(2, new Integer(requiredSize));
        ps.executeUpdate();
    }

    protected boolean notYetAssign(Connection db, int logId) throws SQLException {
        PreparedStatement st = db.prepareStatement("SELECT  * From eventlogdb.logs_to_event_type WHERE logId=?;");
        st.setInt(1, logId);
        ResultSet rs = st.executeQuery();
        if (!rs.isBeforeFirst()) {
            return true;
        }
        System.out.println("assigned");
        return false;
    }

    @GET
    public LogRecordBetterList getList(@PathParam("logId") int id) throws SQLException, NamingException {
        List<LogRecordBetter> records = new ArrayList<>();
        Connection db = getConnection();
        try {
            PreparedStatement st = getListQuery(db, id);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                LogRecordBetter t = getFromResultSet(db, id, rs);
                records.add(t);
            }
            LogRecordBetterList recordList = new LogRecordBetterList();
            recordList.setLogRecord(records);

            recordList.setLogEventType(whatTable(db, id));
            return recordList;
        } finally {
            db.close();
        }
    }

    @POST
    public void create(LogRecordBetter t, @PathParam("logId") int logId) throws SQLException, NamingException {
        Connection db = getConnection();
        if (ifNewType(t.getType().size(), db)) {
            createNewTable(db, t.getType().size(), logId);
        }
        PreparedStatement st = getInsertQuery(db, t, t.getType().size(), logId);
        if (1 != st.executeUpdate()) {
            throw new WebApplicationException("Unexpected number of rows inserted");
        }
        db.close();
    }

    @DELETE
    @Path("{id}")
    public void delete(@PathParam("logId") int logId, @PathParam("id") int id) throws SQLException, NamingException {
        Connection db = getConnection();
        PreparedStatement st = getDeleteQuery(db, id, logId);
        if (1 != st.executeUpdate()) {
            throw new WebApplicationException("Unexpected number of rows deleted");
        }
        db.close();
    }

    @POST
    @Path("newbase/{newSize}")
    public void newSize(@PathParam("logId") int logId, @PathParam("newSize") int newSize) throws SQLException, NamingException {
        Connection db = getConnection();
        try {
            if (ifNewType(newSize, db))
                createNewTable(db, newSize, logId);

            if (notYetAssign(db, logId)) {
                assignToTable(db, logId, newSize);
                System.out.println("notYetAssign");
            }
        } finally {
            db.close();
        }
    }
}


