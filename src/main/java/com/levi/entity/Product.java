package com.levi.entity;

public record Product (String name, double price, String desc) {
    @Override
    public String toString() {
        return "Product{" + "name='" + name + '\'' +
                ", price=" + price +
                ", desc='" + desc + '\'' +
                '}';
    }
}
