package org.example.service;

import org.example.dto.OnboardTenantRequest;
import org.example.repo.HospitalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class OnboardingService {

    private final HospitalRepository repo;

    public OnboardingService(HospitalRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public String onboardTenantAllOrNothing(OnboardTenantRequest req) {
        String tenantId = repo.createTenant(req.tenantName);

        Map<String, String> shiftTypeNameToId = new HashMap<>();
        Map<String, String> usernameToId = new HashMap<>();
        Map<String, String> shiftKeyToId = new HashMap<>();

        if (req.shiftTypes != null) {
            for (OnboardTenantRequest.ShiftTypeItem st : req.shiftTypes) {
                boolean active = st.active == null || st.active;
                String shiftTypeId = repo.insertShiftType(tenantId, st.name, st.description, active);
                shiftTypeNameToId.put(st.name, shiftTypeId);
            }
        }

        if (req.users != null) {
            for (OnboardTenantRequest.UserItem u : req.users) {
                boolean loggedIn = u.loggedIn != null && u.loggedIn;
                String userId = repo.insertUser(tenantId, u.username, loggedIn, u.timezone);
                usernameToId.put(u.username, userId);
            }
        }

        if (req.shifts != null) {
            for (OnboardTenantRequest.ShiftItem s : req.shifts) {
                String stId = shiftTypeNameToId.get(s.shiftTypeName);
                String shiftId = repo.insertShift(tenantId, stId, s.startDate, s.startTime, s.endDate, s.endTime);

                String key = s.shiftTypeName + "|" + s.startDate + "|" + s.startTime;
                shiftKeyToId.put(key, shiftId);
            }
        }

        if (req.assignments != null) {
            for (OnboardTenantRequest.AssignmentItem a : req.assignments) {
                String userId = usernameToId.get(a.username);
                String key = a.shiftTypeName + "|" + a.shiftStartDate + "|" + a.shiftStartTime;
                String shiftId = shiftKeyToId.get(key);
                repo.assignUserToShift(tenantId, shiftId, userId);
            }
        }

        // Failure test: trigger an error at the very end (null username violates NOT NULL)
        if (req.triggerFailure != null && req.triggerFailure) {
            repo.insertUser(tenantId, null, false, "Asia/Kolkata");
        }

        return tenantId;
    }
}
