package com.openelements.oss.license.data;

public record License(String name, String url) {

    public static final License UNKNOWN = new License("Unknown", "Unknown");

    public static final License APACHE = new License("Apache License 2.0", "https://api.github.com/licenses/apache-2.0");

    public static final License MIT = new License("MIT License", "https://api.github.com/licenses/mit");
}
