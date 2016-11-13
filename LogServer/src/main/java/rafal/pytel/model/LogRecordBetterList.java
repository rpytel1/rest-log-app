package rafal.pytel.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rafal on 2016-11-13.
 */
public class LogRecordBetterList {

    List<LogRecordBetter> logRecord=new ArrayList<>();
    private int logEventType;



    public LogRecordBetterList() {

    }
    public List<LogRecordBetter> getLogRecord() {
        return logRecord;
    }

    public void setLogRecord(List<LogRecordBetter> logRecord) {
        this.logRecord = logRecord;
    }

    public int getLogEventType() {
        return logEventType;
    }

    public void setLogEventType(int logEventType) {
        this.logEventType = logEventType;
    }
}
