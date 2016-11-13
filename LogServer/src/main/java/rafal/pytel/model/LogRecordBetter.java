package rafal.pytel.model;

import java.io.Serializable;
import java.sql.Time;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Rafal on 2016-11-12.
 */
public class LogRecordBetter implements Serializable {
    private Long id;




    private Time time;
    private List<String> type = new ArrayList<>();
    private Long logId;
    private int eventId;

    public LogRecordBetter() {

    }

    public int getEventId() {
        return eventId;
    }
    public Time getTime() {
        return time;
    }

    public void setTime(Time time) {
        this.time = time;
    }
    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }



    public List<String> getType() {
        return type;
    }

    public void setType(List<String> type) {
        this.type = type;
    }

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }


}
