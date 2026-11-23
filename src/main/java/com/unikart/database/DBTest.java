/*

 * Developer: MANANSH GOYAL
 * Development Date: 16-11-2026
 */
package com.unikart.database;

import java.sql.Connection;

public class DBTest {

    public static void main(String[] args) {

        try (Connection connection = DBConnection.getConnection()) {

            System.out.println("================================");
            System.out.println(" UNIKART DATABASE CONNECTED!");
            System.out.println("================================");

        } catch (Exception e) {

            System.out.println("Database connection failed!");
            e.printStackTrace();

        }
    }
}
