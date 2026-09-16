package model;

public class Currency {
    private final int id;
    private final String code;
    private final String name;
    private final String sign;

    public Currency(int id, String code, String name, String sign) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.sign = sign;
    }

    public int getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return name;
    }

    public String getSign() {
        return sign;
    }
}
