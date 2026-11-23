# Unikart



**A Java-Based Multi-Vendor Marketplace for College and Everyday Essentials**



Unikart is a console-based shopping application developed using Java and MySQL. It allows customers to purchase products from Unikart and independent sellers through a single marketplace.



The application provides separate customer, seller, and administrator workflows, with database-backed product management, shopping carts, order processing, and seller approval.



## Features



### Customer

- Register and log in securely.

- Browse, search, filter, and sort products.

- Explore products by category.

- Add products to a shopping cart.

- Update quantities and remove cart items.

- Place orders through checkout.

- View order history and payment details.

- Apply to become a seller.



### Seller

- Apply for seller access through a customer account.

- Add new products.

- View and manage listed products.

- Update product details and stock.

- Delete products.

- Track product approval status.

- Resubmit rejected products for approval.



### Administrator

- Manage product listings.

- Approve or reject submitted products.

- Review seller applications.

- Approve or reject seller applications.

- Manage the marketplace's administrative workflows.



## Technology Stack



| Technology | Purpose |

|---|---|

| Java 26 | Application development |

| MySQL 8.0 | Relational database |

| JDBC | Database connectivity |

| Maven | Dependency and build management |

| JUnit | Automated testing |

| Git & GitHub | Version control |



**Key dependency:** MySQL Connector/J 9.5.0



## Project Architecture



The project follows a modular, package-based structure.



```text

unikart/
|-- src/
|   |-- main/
|   |   |-- java/com/unikart/
|   |   |   |-- admin/
|   |   |   |-- cart/
|   |   |   |-- customer/
|   |   |   |-- database/
|   |   |   |-- order/
|   |   |   |-- product/
|   |   |   |-- seller/
|   |   |   |-- service/
|   |   |   `-- user/
|   |   `-- resources/
|   |       |-- schema.sql
|   |       `-- db.properties
|   `-- test/
|       `-- java/com/unikart/
|-- pom.xml
`-- README.md

```



## Database Design

Unikart uses MySQL with the following main tables:

1. `users` - Stores customer, seller, and administrator accounts.
2. `seller_applications` - Stores seller registration applications.
3. `categories` - Stores product categories.
4. `products` - Stores marketplace product information.
5. `carts` - Stores customer shopping carts.
6. `cart_items` - Stores individual cart products.
7. `orders` - Stores customer orders.
8. `seller_order_groups` - Organizes orders by seller.
9. `order_items` - Stores products included in orders.
10. `payments` - Stores payment records.

The database schema, relationships, constraints, and initial categories are provided in `src/main/resources/schema.sql`.
## Setup and Installation



### Prerequisites



Install the following:



- JDK 26

- Apache Maven

- MySQL Server 8.0 or compatible version

- Git



### 1. Clone the Repository



```bash

git clone https://github.com/Manansh2107/unikart.git

cd unikart

```



### 2. Create the Database



Open `src/main/resources/schema.sql` in MySQL Workbench and execute the script.



This creates the `unikart_db` database and its required tables.



### 3. Configure Database Credentials



Create or update the local file:



`src/main/resources/db.properties`



Use the following structure:



```properties

db.url=jdbc:mysql://localhost:3306/unikart_db

db.username=YOUR_MYSQL_USERNAME

db.password=YOUR_MYSQL_PASSWORD

```



Replace the placeholder values with your local MySQL credentials.



**Important:** `db.properties` is intentionally excluded from Git because it contains local database credentials. Never commit your actual database password.



### 4. Build and Test



Run:



```bash

mvn test

```



### 5. Run the Application



Build the dependency classpath:



```powershell

mvn dependency:build-classpath "-Dmdep.outputFile=target\classpath.txt"

```



Launch Unikart:



```powershell

java -cp "target\classes;$(Get-Content target\classpath.txt)" com.unikart.App

```



## Testing



Unikart includes automated tests and manual end-to-end testing.



Testing covers:



- User authentication and password management.

- Product and category operations.

- Cart functionality.

- Checkout and order processing.

- Seller application and approval workflows.

- Product approval and visibility.

- Stock validation and concurrent checkout scenarios.



The automated test suite completed successfully with **55 tests passed and zero failures, errors, or skipped tests** in the recorded test run.



## Checkout and Payment



The checkout process uses database transactions and stock validation to maintain order consistency.



It includes:

- Stock availability checks.

- Transaction-based order creation.

- Stock updates.

- Seller-wise order grouping.

- Payment record creation.

- Cart clearing after successful checkout.



**Payment limitation:** Payment processing is simulated for academic demonstration purposes. No real payment gateway or actual financial transaction is integrated.



## Project Team



- Manansh Goyal

- Shreyansh

- Himanshu



## Future Scope



- Integration with a real payment gateway.

- Development of a graphical user interface.

- Product reviews and ratings.

- Order tracking and notifications.

- Deployment for wider accessibility.



## Project Status



**Development Completed**



Unikart was developed as an academic project to demonstrate Java programming, object-oriented concepts, JDBC connectivity, relational database management, and multi-user marketplace workflows.
