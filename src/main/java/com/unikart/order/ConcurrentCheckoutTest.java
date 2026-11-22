
package com.unikart.order;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ConcurrentCheckoutTest {

    public static void main(String[] args) throws Exception {

        CheckoutService checkoutService = new CheckoutService();

        // Two threads will perform checkout simultaneously.
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Both threads wait until the test releases them.
        CountDownLatch startSignal = new CountDownLatch(1);

        try {

            System.out.println("===== CONCURRENT CHECKOUT TEST =====");
            System.out.println("Customer 2: 15 units");
            System.out.println("Customer 5: 15 units");
            System.out.println("Available stock: 20");
            System.out.println("-------------------------------------");

            Future<Integer> customer2 = executor.submit(() -> {

                startSignal.await();

                System.out.println("Customer 2 started checkout.");

                return checkoutService.checkout(2, "TEST");
            });

            Future<Integer> customer5 = executor.submit(() -> {

                startSignal.await();

                System.out.println("Customer 5 started checkout.");

                return checkoutService.checkout(5, "TEST");
            });

            // Release both threads together.
            startSignal.countDown();

            int order2 = customer2.get();
            int order5 = customer5.get();

            int successfulCheckouts = 0;

            if (order2 > 0) {
                successfulCheckouts++;
                System.out.println("Customer 2 Order ID: " + order2);
            } else {
                System.out.println("Customer 2 checkout failed.");
            }

            if (order5 > 0) {
                successfulCheckouts++;
                System.out.println("Customer 5 Order ID: " + order5);
            } else {
                System.out.println("Customer 5 checkout failed.");
            }

            System.out.println("-------------------------------------");
            System.out.println("Successful checkouts: " + successfulCheckouts);

            if (successfulCheckouts == 1) {
                System.out.println("PASS: Stock protection worked!");
            } else {
                System.out.println("CHECK: Unexpected checkout result!");
            }

            System.out.println("=====================================");

        } finally {
            executor.shutdown();
        }
    }
}
