package com.ttn.ai.core.services.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Paginated ticket list result.
 */
public class TicketListResult {

    private long total;
    private int offset;
    private int limit;
    private List<TicketDto> tickets = new ArrayList<>();

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public List<TicketDto> getTickets() {
        return tickets;
    }

    public void setTickets(List<TicketDto> tickets) {
        this.tickets = tickets;
    }
}
