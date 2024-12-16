package com.dogood.dogoodbackend.domain.volunteerings.scheduling;

import java.util.Date;

public class DatePair {
    private Date start;
    private Date end;

    public DatePair(Date start, Date end) {
        this.start = start;
        this.end = end;
    }

    public Date getStart() {
        return start;
    }

    public Date getEnd() {
        return end;
    }
}
