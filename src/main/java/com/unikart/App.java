
package com.unikart;

import com.unikart.admin.AdminMenu;
import com.unikart.user.AuthService;
import com.unikart.user.User;

import java.sql.SQLException;
import java.util.Optional;
import java.util.Scanner;

public class App {

    private static final Scanner scanner = new Scanner(System.in);
    private static final AuthService authService = new AuthService();

    public static void main(String[] args) {

        System.out.println("\n====================================");
        System.out.println("          WELCOME TO UNIKART");
        System.out.println("====================================");

        while (true) {

            System.out.println("\n========== MAIN MENU ==========");
            System.out.println("1. Login");
            System.out.println("2. Register as Customer");
            System.out.println("3. Exit");
            System.out.println("===============================");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1":
                    login();
                    break;

                case "2":
                    registerCustomer();
                    break;

                case "3":
                    System.out.println("\nThank you for using Unikart!");
                    System.out.println("Goodbye!");
                    return;

                default:
                    System.out.println("\nInvalid choice. Please try again.");
            }
        }
    }

    // USER LOGIN

    private static void login() {

        System.out.println("\n========== USER LOGIN ==========");

        try {

            System.out.print("Enter Chitkara Email: ");
            String email = scanner.nextLine().trim();

            System.out.print("Enter Password: ");
            String password = scanner.nextLine();

            Optional<User> userOptional = authService.login(
                    email,
                    password
            );

            if (userOptional.isEmpty()) {

                System.out.println("\nInvalid email or password.");
                return;
            }

            User user = userOptional.get();

            System.out.println("\nLogin successful!");
            System.out.println("Welcome, " + user.getName());

            // Redirect user according to their role.
            switch (user.getRole()) {

                case ADMIN:

                    System.out.println("Role: ADMIN");

                    AdminMenu adminMenu = new AdminMenu(
                            user,
                            scanner
                    );

                    adminMenu.showMenu();
                    break;

                case SELLER:

                    System.out.println("Role: SELLER");
                    System.out.println(
                            "Seller Panel will be connected next."
                    );
                    break;

                case CUSTOMER:

                    System.out.println("Role: CUSTOMER");
                    System.out.println(
                            "Customer Panel will be connected next."
                    );
                    break;

                default:

                    System.out.println("Unknown user role.");
            }

        } catch (SQLException e) {

            System.out.println(
                    "\nDatabase error during login: " + e.getMessage()
            );

        } catch (Exception e) {

            System.out.println(
                    "\nSomething went wrong: " + e.getMessage()
            );
        }
    }

    // CUSTOMER REGISTRATION

    private static void registerCustomer() {

        System.out.println("\n========== CUSTOMER REGISTRATION ==========");

        try {

            System.out.print("Enter Your Name: ");
            String name = scanner.nextLine().trim();

            System.out.print("Enter Chitkara Email: ");
            String email = scanner.nextLine().trim();

            System.out.print("Create Password: ");
            String password = scanner.nextLine();

            System.out.print("Enter Phone Number (optional): ");
            String phone = scanner.nextLine().trim();

            User registeredUser = authService.registerCustomer(
                    name,
                    email,
                    password,
                    phone
            );

            System.out.println("\nRegistration successful!");
            System.out.println("Welcome to Unikart, " + registeredUser.getName());
            System.out.println("Your account has been created.");
            System.out.println("Account Role: CUSTOMER");
            System.out.println("You can now log in.");

        } catch (IllegalArgumentException e) {

            System.out.println("\nRegistration failed: " + e.getMessage());

        } catch (SQLException e) {

            System.out.println(
                    "\nDatabase error during registration: " + e.getMessage()
            );

        } catch (Exception e) {

            System.out.println(
                    "\nSomething went wrong: " + e.getMessage()
            );
        }
    }
}
