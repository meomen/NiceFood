package com.vuducminh.nicefood.model;

public class RefundRequestModel {
    private String name,phone,cardName,cardNumber,cardExp;
    private boolean isDone;
    private double amount;


    public RefundRequestModel() {
    }

    public RefundRequestModel(String name, String phone, String cardName, String cardNumber, String cardExp, boolean isDone) {
        this.name = name;
        this.phone = phone;
        this.cardName = cardName;
        this.cardNumber = cardNumber;
        this.cardExp = cardExp;
        this.isDone = isDone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardExp() {
        return cardExp;
    }

    public void setCardExp(String cardExp) {
        this.cardExp = cardExp;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
