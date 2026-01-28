package org.example.dto;

public class CreateShiftRequest {
    public String tenantId;
    public String shiftTypeId;
    public String startDate; // YYYY-MM-DD
    public String startTime; // HH:MM:SS
    public String endDate;   // YYYY-MM-DD
    public String endTime;   // HH:MM:SS
}
