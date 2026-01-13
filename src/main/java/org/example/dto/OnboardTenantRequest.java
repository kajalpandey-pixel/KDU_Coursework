package org.example.dto;

import java.util.List;

public class OnboardTenantRequest {
    public String tenantName;

    public List<ShiftTypeItem> shiftTypes;
    public List<UserItem> users;
    public List<ShiftItem> shifts;
    public List<AssignmentItem> assignments;

    // if true, we will trigger a failure at the end to test rollback
    public Boolean triggerFailure;

    public static class ShiftTypeItem {
        public String name;
        public String description;
        public Boolean active;
    }

    public static class UserItem {
        public String username;
        public Boolean loggedIn;
        public String timezone;
    }

    public static class ShiftItem {
        public String shiftTypeName; // will map to created shift_type id
        public String startDate;
        public String startTime;
        public String endDate;
        public String endTime;
    }

    public static class AssignmentItem {
        public String username;        // will map to created user id
        public String shiftStartDate;  // simple way to find shift
        public String shiftStartTime;
        public String shiftTypeName;
    }
}
