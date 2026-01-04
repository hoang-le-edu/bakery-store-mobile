package com.dev.thecodecup.model;

public class ToppingSelection {
    public String toppingId;
    public String toppingName;
    public String extraPrice;

    public ToppingSelection(String toppingId, String toppingName, String extraPrice) {
        this.toppingId = toppingId;
        this.toppingName = toppingName;
        this.extraPrice = extraPrice;
    }
}
