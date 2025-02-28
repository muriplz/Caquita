package com.kryeit;

public class Config {
    public static final int apiPort = 6996;
    public static final boolean production = true;

    public static final String dbUrl = production
            ? "jdbc:postgresql://kryeit.com:5432/caqui"
            : "jdbc:postgresql://localhost:5432/caqui";

    public static final String dbUser = production ?
            System.getenv("DB_USER")
            : "postgres";
    public static final String dbPassword = production ?
            System.getenv("DB_PASSWORD")
            : "lel";

    public static final String stadiaKey = production
            ? System.getenv("STADIA_KEY")
            : "AAAAAAAAAAAAAAAAAAAAAAAA";

    public static final String jwtSecret = production
            ? System.getenv("JWT_SECRET")
            : "17930e2ce819b77517793f93ee94acec528c0278a5b099cea680ba627c4927cbda6814a29efc23572f3db26e2fddb4bad90136a8696413fa1dd087e612ffe79fc8840bce9c5aad483e46592b0f90d99b060ae7109eb2ddfaa8c9629972fd92db77a9392e5f9efa43915f9a498722dfbb06654b2e0ac7a9f5b9f35049541b8df91d9102b961ac8ee2cbe4f4fde9eeaf249045f1719898ed8ccdba006aea370fea11424dacab44ca8ffa555a4bbe04c9209ca78d736322f46f564e69fce6c6e43f13e38a3e4f7481535da37967b6dfc54edb64830d811a49db1072de0f48cbddc6fe8efc48f3aa3046d3b9c19c6e6cae4148fd7d0900445e7b7f11c5495751a567";
}
