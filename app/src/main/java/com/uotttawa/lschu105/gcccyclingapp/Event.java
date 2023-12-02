package com.uotttawa.lschu105.gcccyclingapp;

import java.util.Map;
import java.util.List;
public class Event {
    private String name;
    private String age;
    private String description;
    private String createdBy;
    private String difficultyLevel;
    private String eventType;
    private String eventName;
    private Map<String, String> requirements;
    private List<String> requirementsList;
    private Integer day;
    private Integer month;
    private Integer year;
    private String location;

    // Default constructor (needed for Firebase)
    public Event() {
    }

    // For testing quick sort
    public Event(Integer day, Integer month, Integer year) {
        this.day = day;
        this.month = month;
        this.year = year;
    }

    //Constructor for creating event types
    public Event(String description, String name, List<String> Requirements) {
        this.name = name;
        this.description = description;
        this.requirementsList = Requirements;
    }
    //Constructor for creating events
    public Event(String createdBy, String difficultyLevel, String eventType, String eventName, String location, Map<String, String> requirements, Integer day, Integer month, Integer year) {
        this.createdBy = createdBy;
        this.difficultyLevel = difficultyLevel;
        this.eventType = eventType;
        this.eventName = eventName;
        this.location = location;
        this.requirements = requirements;
        this.day = day;
        this.month = month;
        this.year = year;
    }
    // Getters and setters (needed by firebase do not delete)
    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(String difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getEventName() {
        return eventName;
    }
    public String getName() {
        return name;
    }

    public String getDescription(){
        return description;
    }
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getLocation(){
        return location;
    }
    public void setLocation(String location){
        this.location = location;
    }

    public Map<String, String> getRequirements() {
        return requirements;
    }

    public void setRequirements(Map<String, String> requirements) {
        this.requirements = requirements;
    }

    public List<String> getRequirementsList(){return requirementsList;}
    public void setRequirementsList(List<String> requirementsList){
        this.requirementsList = requirementsList;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }
    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }
    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public int compareTo(Event b) {
        Event a = this;

        // compares years
        if (a.getYear() != b.getYear()) {
            return Integer.compare(a.getYear(), b.getYear());
        }

        // compares months
        if (a.getMonth() != b.getMonth()) {
            return Integer.compare(a.getMonth(), b.getMonth());
        }

        // compares days
        return Integer.compare(a.getDay(), b.getDay());
    }

    public String toString() {
        String sDay = day > 10 ? Integer.toString(day) : "0" + Integer.toString(day);
        String sMonth = month > 10 ? Integer.toString(month) : "0" + Integer.toString(month);
        return String.format("%s/%s/%d", sDay, sMonth, year);
    }
}
