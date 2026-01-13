package org.example.controller;

import org.example.dto.*;
import org.example.repo.HospitalRepository;
import org.example.service.OnboardingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HospitalController {

    private final HospitalRepository repo;
    private final OnboardingService onboardingService;

    public HospitalController(HospitalRepository repo, OnboardingService onboardingService) {
        this.repo = repo;
        this.onboardingService = onboardingService;
    }

    // 1A: Create Tenant (needed before inserting users/shift types)
    @PostMapping("/tenants")
    public ResponseEntity<Map<String, Object>> createTenant(@RequestBody CreateTenantRequest req) {
        String tenantId = repo.createTenant(req.name);

        Map<String, Object> resp = new HashMap<>();
        resp.put("tenantId", tenantId);
        return ResponseEntity.ok(resp);
    }

    // Exercise 1 - Task 1: Insert User (native insert via JdbcTemplate)
    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody CreateUserRequest req) {
        boolean loggedIn = req.loggedIn != null && req.loggedIn;
        String id = repo.insertUser(req.tenantId, req.username, loggedIn, req.timezone);

        Map<String, Object> resp = new HashMap<>();
        resp.put("id", id);
        resp.put("tenantId", req.tenantId);
        return ResponseEntity.ok(resp);
    }

    // Exercise 1 - Task 1: Insert Shift Type (native insert via JdbcTemplate)
    @PostMapping("/shift-types")
    public ResponseEntity<Map<String, Object>> createShiftType(@RequestBody CreateShiftTypeRequest req) {
        boolean active = req.active == null || req.active;
        String id = repo.insertShiftType(req.tenantId, req.name, req.description, active);

        Map<String, Object> resp = new HashMap<>();
        resp.put("id", id);
        resp.put("tenantId", req.tenantId);
        return ResponseEntity.ok(resp);
    }

    // Exercise 1 - Task 2: Fetch all users for a tenant
    @GetMapping("/tenants/{tenantId}/users")
    public ResponseEntity<List<Map<String, Object>>> getUsers(@PathVariable String tenantId) {
        return ResponseEntity.ok(repo.getUsersByTenant(tenantId));
    }

    // Exercise 1 - Task 2: Fetch all shifts for a tenant
    @GetMapping("/tenants/{tenantId}/shifts")
    public ResponseEntity<List<Map<String, Object>>> getShifts(@PathVariable String tenantId) {
        return ResponseEntity.ok(repo.getShiftsByTenant(tenantId));
    }

    // Exercise 1 - Task 3: Update user info (native UPDATE via JdbcTemplate)
    @PutMapping("/tenants/{tenantId}/users/{userId}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable String tenantId,
            @PathVariable String userId,
            @RequestBody UpdateUserRequest req
    ) {
        int rows = repo.updateUser(tenantId, userId, req.username, req.timezone, req.loggedIn);

        Map<String, Object> resp = new HashMap<>();
        resp.put("updatedRows", rows);
        resp.put("tenantId", tenantId);
        resp.put("userId", userId);
        return ResponseEntity.ok(resp);
    }

    // Exercise 2: Transactional onboarding (all-or-nothing)
    @PostMapping("/tenants/onboard")
    public ResponseEntity<Map<String, Object>> onboard(@RequestBody OnboardTenantRequest req) {
        String tenantId = onboardingService.onboardTenantAllOrNothing(req);

        Map<String, Object> resp = new HashMap<>();
        resp.put("tenantId", tenantId);
        resp.put("message", "onboarding finished");
        return ResponseEntity.ok(resp);
    }

    // Exercise 3: Pagination + sorting for users
    // Example: /api/tenants/{tenantId}/users/paged?page=0&size=10&sort=asc
    @GetMapping("/tenants/{tenantId}/users/paged")
    public ResponseEntity<List<Map<String, Object>>> getUsersPaged(
            @PathVariable String tenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "asc") String sort
    ) {
        int offset = page * size;
        return ResponseEntity.ok(repo.getUsersByTenantPaged(tenantId, size, offset, sort));
    }
}
