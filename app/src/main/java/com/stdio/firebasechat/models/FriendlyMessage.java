package com.stdio.firebasechat.models;

public class FriendlyMessage {

    private String id;
    private String text;
    private String name;
    private String photoUrl;
    private String imageUrl;
    private String uid;
    private String forwardedMessage;
    private String forwardedImg;
    private String forwardedMessageSender;
    private int forwardedMessagePosition;

    public FriendlyMessage() {
    }

    public FriendlyMessage(String text, String name, String photoUrl, String imageUrl) {
        this.text = text;
        this.name = name;
        this.photoUrl = photoUrl;
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getText() {
        return text;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getForwardedMessage() {
        return forwardedMessage;
    }

    public void setForwardedMessage(String forwardedMessage) {
        this.forwardedMessage = forwardedMessage;
    }

    public String getForwardedImg() {
        return forwardedImg;
    }

    public void setForwardedImg(String forwardedImg) {
        this.forwardedImg = forwardedImg;
    }

    public String getForwardedMessageSender() {
        return forwardedMessageSender;
    }

    public void setForwardedMessageSender(String forwardedMessageSender) {
        this.forwardedMessageSender = forwardedMessageSender;
    }

    public int getForwardedMessagePosition() {
        return forwardedMessagePosition;
    }

    public void setForwardedMessagePosition(int forwardedMessagePosition) {
        this.forwardedMessagePosition = forwardedMessagePosition;
    }
}
