package ru.company.reference.entity;

public enum EmployeeRole {
    ADMIN,
    EMPLOYEE;

    public static boolean isAdmin(String role) {
        return ADMIN.name().equalsIgnoreCase(role);
    }

    public static boolean canDeleteUsers(String role) {
        return isAdmin(role);
    }
}