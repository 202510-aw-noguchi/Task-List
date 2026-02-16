package com.example.todo.dto;

public class MonthlyProgressSummary {
    private long totalCount;
    private long notStartedCount;
    private long inProgressCount;
    private long completedCount;
    private long incompleteCount;

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public long getNotStartedCount() {
        return notStartedCount;
    }

    public void setNotStartedCount(long notStartedCount) {
        this.notStartedCount = notStartedCount;
    }

    public long getInProgressCount() {
        return inProgressCount;
    }

    public void setInProgressCount(long inProgressCount) {
        this.inProgressCount = inProgressCount;
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
