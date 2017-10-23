package com.example.jatin.myapplication;

public class ThreeStrings {
    private String subject;
    private String author;
    private String content;

    public ThreeStrings(String subject, String author, String content) {
        this.subject = subject;
        this.author = author;
        this.content = content;
    }
    public String getSubject()
    {
        return this.subject;
    }
    public String getAuthor()
    {
        return this.author;
    }
    public String getContent() { return this.content;}
}
