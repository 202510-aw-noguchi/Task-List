package com.example.todo.dto;

public class MonthlyProgressSummary {
    private long totalCount;
    private long completedCount;
    private long incompleteCount;

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public long getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(long completedCount) {
        this.completedCount = completedCount;
    }

    public long getIncompleteCount() {
        return incompleteCount;
    }

    public void setIncompleteCount(long incompleteCount) {
        this.incompleteCount = incompleteCount;
    }
}
