package com.nguyenduchoc.bt5.dto;

import java.time.LocalDate;

public final class Models {
    private Models() { }
    public record Account(int id, String username, String fullName, String email, String phone,
                          String password, int roleId, boolean active, LocalDate createdDate) {
        public int getId() { return id; } public String getUsername() { return username; }
        public String getFullName() { return fullName; } public String getEmail() { return email; }
        public String getPhone() { return phone; } public int getRoleId() { return roleId; }
        public boolean isActive() { return active; } public LocalDate getCreatedDate() { return createdDate; }
    }
    public record Category(int id, String name, String icon) {
        public int getId() { return id; } public String getName() { return name; } public String getIcon() { return icon; }
    }
    public record Page<T>(java.util.List<T> items, int number, int totalPages, long total) {
        public java.util.List<T> getItems() { return items; } public int getNumber() { return number; }
        public int getTotalPages() { return totalPages; } public long getTotal() { return total; }
    }
}
