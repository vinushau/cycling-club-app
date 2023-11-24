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
    private int day;
    private int month;
    private int year;

    // Default constructor (needed for Firebase)
    public Event() {
    }

    //Constructor for creating event types
    public Event(String description, String name, List<String> Requirements) {
        this.name = name;
        this.description = description;
        this.requirementsList = Requirements;
    }
    //Constructor for creating events
    public Event(String createdBy, String difficultyLevel, String eventType, String eventName, Map<String, String> requirements, int day, int month, int year) {
        this.createdBy = createdBy;
        this.difficultyLevel = difficultyLevel;
        this.eventType = eventType;
        this.eventName = eventName;
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

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }
    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }
    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
}
