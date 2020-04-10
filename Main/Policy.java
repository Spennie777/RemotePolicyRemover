package Main;

import java.util.Date;

public class Policy {

    private int id;
    private Date date;

    public Policy(int id, Date date) {
        this.id = id;
        this.date = date;
    }

    protected int getId() {
        return id;
    }

    protected Date getDate() {
        return date;
    }
}
