package com.example.fidenz.config;

import com.example.fidenz.entity.*;
import com.example.fidenz.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * DataLoader is a component that seeds the database with initial data upon application startup.
 * It creates users, stores, products, inventory records, and sales transactions
 *
 */
@Component
@Profile("!test")
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final SalesTransactionRepository salesTransactionRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UserRepository userRepository, StoreRepository storeRepository, ProductRepository
            productRepository, InventoryRepository inventoryRepository, SalesTransactionRepository
            salesTransactionRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.storeRepository = storeRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.salesTransactionRepository = salesTransactionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting data seeding...");

        // Create users
        createUsers();
        
        // Create stores
        List<Store> stores = createStores();
        
        // Create products
        List<Product> products = createProducts();
        
        // Create inventory
        createInventory(stores, products);
        
        // Create sales transactions (3 months of data)
        createSalesTransactions(stores, products);

        log.info("Data seeding completed successfully!");
    }

    private void createUsers() {
        if (userRepository.count() == 0) {
            List<User> users = List.of(
                createUser("admin", "admin@seveneleven.com", "Admin", "User", Role.STORE_MANAGER),
                createUser("manager1", "manager1@seveneleven.com", "gayantha", "Manager", Role.STORE_MANAGER),
                createUser("operator1", "operator1@seveneleven.com", "sandeep", "Operator", Role.STORE_OPERATOR),
                createUser("operator2", "operator2@seveneleven.com", "shanuka", "Operator", Role.STORE_OPERATOR),
                createUser("api_client1", "api1@external.com", "pasan", "Client", Role.API_CLIENT),
                createUser("pos_system", "pos@seveneleven.com", "chamara", "System", Role.API_CLIENT)
            );
            
            userRepository.saveAll(users);
            log.info("Created {} users", users.size());
        }
    }

    private User createUser(String username, String email, String firstName, String lastName, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("password123"));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role);
        return user;
    }

    private List<Store> createStores() {
        if (storeRepository.count() == 0) {
            List<Store> stores = List.of(
                createStore("Downtown Store", "123 Main St, Downtown", "555-0101", "Gayntha Manager"),
                createStore("Mall Store", "456 Mall Ave, Shopping Center", "555-0102", "Supun Manager"),
                createStore("Airport Store", "789 Airport Blvd, Terminal 1", "555-0103", "Vihanaga Manager")
            );
            
            List<Store> savedStores = storeRepository.saveAll(stores);
            log.info("Created {} stores", savedStores.size());
            return savedStores;
        }
        return storeRepository.findAll();
    }

    private Store createStore(String name, String address, String phone, String managerName) {
        Store store = new Store();
        store.setName(name);
        store.setLocation(address);
        store.setContactNumber(phone);
        store.setEmail(managerName);
        return store;
    }

    private List<Product> createProducts() {
        if (productRepository.count() == 0) {
            List<Product> products = new ArrayList<>();
            
            // Beverages
            products.add(createProduct("Coca Cola 330ml", "Classic Coca Cola", new BigDecimal("1.50"), "Beverages", "COKE330", 100, 10));
            products.add(createProduct("Pepsi 330ml", "Classic Pepsi", new BigDecimal("1.50"), "Beverages", "PEPSI330", 100, 10));
            products.add(createProduct("Water 500ml", "Spring Water", new BigDecimal("1.00"), "Beverages", "WATER500", 200, 20));
            products.add(createProduct("Energy Drink", "Red Bull Energy Drink", new BigDecimal("2.50"), "Beverages", "ENERGY250", 50, 5));
            products.add(createProduct("Coffee", "Starbucks Coffee", new BigDecimal("3.00"), "Beverages", "COFFEE", 30, 3));
            
            // Snacks
            products.add(createProduct("Chips", "Potato Chips", new BigDecimal("2.00"), "Snacks", "CHIPS", 80, 8));
            products.add(createProduct("Chocolate Bar", "Milk Chocolate", new BigDecimal("1.80"), "Snacks", "CHOC", 60, 6));
            products.add(createProduct("Candy", "Assorted Candy", new BigDecimal("1.20"), "Snacks", "CANDY", 100, 10));
            products.add(createProduct("Nuts", "Mixed Nuts", new BigDecimal("3.50"), "Snacks", "NUTS", 40, 4));
            products.add(createProduct("Cookies", "Chocolate Cookies", new BigDecimal("2.20"), "Snacks", "COOKIES", 50, 5));
            
            // Food
            products.add(createProduct("Sandwich", "Chicken Sandwich", new BigDecimal("4.50"), "Food", "SANDWICH", 25, 3));
            products.add(createProduct("Pizza Slice", "Pepperoni Pizza", new BigDecimal("3.50"), "Food", "PIZZA", 20, 2));
            products.add(createProduct("Salad", "Caesar Salad", new BigDecimal("5.00"), "Food", "SALAD", 15, 2));
            products.add(createProduct("Burger", "Cheeseburger", new BigDecimal("4.80"), "Food", "BURGER", 18, 2));
            products.add(createProduct("Hot Dog", "Beef Hot Dog", new BigDecimal("3.20"), "Food", "HOTDOG", 22, 3));
            
            // Health & Beauty
            products.add(createProduct("Toothpaste", "Mint Toothpaste", new BigDecimal("3.80"), "Health", "TOOTHPASTE", 30, 3));
            products.add(createProduct("Shampoo", "Hair Shampoo", new BigDecimal("5.50"), "Health", "SHAMPOO", 20, 2));
            products.add(createProduct("Soap", "Body Soap", new BigDecimal("2.50"), "Health", "SOAP", 40, 4));
            products.add(createProduct("Vitamins", "Multivitamin", new BigDecimal("8.00"), "Health", "VITAMINS", 15, 2));
            products.add(createProduct("Bandages", "Bandages", new BigDecimal("2.80"), "Health", "BANDAGES", 25, 3));
            
            // Household
            products.add(createProduct("Batteries", "AA Batteries", new BigDecimal("4.20"), "Household", "BATTERIES", 35, 4));
            products.add(createProduct("Light Bulb", "LED Light Bulb", new BigDecimal("3.50"), "Household", "BULB", 20, 2));
            products.add(createProduct("Tape", "Duct Tape", new BigDecimal("2.80"), "Household", "TAPE", 30, 3));
            products.add(createProduct("Cleaning Spray", "All-Purpose Cleaner", new BigDecimal("4.50"), "Household", "CLEANER", 25, 3));
            products.add(createProduct("Trash Bags", "Garbage Bags", new BigDecimal("3.20"), "Household", "BAGS", 40, 4));
            
            // Electronics
            products.add(createProduct("Phone Charger", "USB Charger", new BigDecimal("12.00"), "Electronics", "CHARGER", 15, 2));
            products.add(createProduct("Headphones", "Bluetooth Headphones", new BigDecimal("25.00"), "Electronics", "HEADPHONES", 10, 1));
            products.add(createProduct("Power Bank", "Portable Charger", new BigDecimal("18.00"), "Electronics", "POWERBANK", 12, 2));
            products.add(createProduct("Cable", "HDMI Cable", new BigDecimal("8.50"), "Electronics", "CABLE", 20, 2));
            products.add(createProduct("Adapter", "USB Adapter", new BigDecimal("6.00"), "Electronics", "ADAPTER", 25, 3));
            
            List<Product> savedProducts = productRepository.saveAll(products);
            log.info("Created {} products", savedProducts.size());
            return savedProducts;
        }
        return productRepository.findAll();
    }

    private Product createProduct(String name, String description, BigDecimal unitPrice, String category, 
                                String sku, Integer maxStorage, Integer minStorage) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setUnitPrice(unitPrice);
        product.setCategory(category);
        product.setSku(sku);
        product.setMaxStorageQty(maxStorage);
        product.setMinStorageQty(minStorage);
        return product;
    }

    private void createInventory(List<Store> stores, List<Product> products) {
        if (inventoryRepository.count() == 0) {
            List<Inventory> inventories = new ArrayList<>();
            Random random = new Random();
            
            for (Store store : stores) {
                for (Product product : products) {
                    Inventory inventory = new Inventory();
                    inventory.setStore(store);
                    inventory.setProduct(product);
                    // Random initial stock between 20-80% of max storage
                    int maxStock = product.getMaxStorageQty();
                    int initialStock = random.nextInt((int) (maxStock * 0.6)) + (int) (maxStock * 0.2);
                    inventory.setCurrentStock(initialStock);
                    inventories.add(inventory);
                }
            }
            
            inventoryRepository.saveAll(inventories);
            log.info("Created {} inventory records", inventories.size());
        }
    }

    private void createSalesTransactions(List<Store> stores, List<Product> products) {
        if (salesTransactionRepository.count() == 0) {
            List<SalesTransaction> transactions = new ArrayList<>();
            Random random = new Random();
            LocalDateTime now = LocalDateTime.now();
            
            // Generate 3 months of sales data
            for (int day = 0; day < 90; day++) {
                LocalDateTime transactionDate = now.minusDays(day);
                
                // Generate 5-15 transactions per day
                int transactionsPerDay = random.nextInt(11) + 5;
                
                for (int t = 0; t < transactionsPerDay; t++) {
                    Store store = stores.get(random.nextInt(stores.size()));
                    Product product = products.get(random.nextInt(products.size()));
                    
                    // Check if inventory exists for this product-store combination
                    if (inventoryRepository.findByProductAndStore(product, store).isPresent()) {
                        SalesTransaction transaction = new SalesTransaction();
                        transaction.setStore(store);
                        transaction.setProduct(product);
                        transaction.setUnitPrice(product.getUnitPrice());
                        
                        // Random quantity between 1-5
                        int quantity = random.nextInt(5) + 1;
                        transaction.setQuantity(quantity);
                        transaction.setTotalAmount(product.getUnitPrice().multiply(BigDecimal.valueOf(quantity)));
                        
                        // Random time during the day
                        int hour = random.nextInt(16) + 6; // 6 AM to 10 PM
                        int minute = random.nextInt(60);
                        transaction.setTransactionDate(transactionDate.withHour(hour).withMinute(minute));
                        
                        transactions.add(transaction);
                    }
                }
            }
            
            salesTransactionRepository.saveAll(transactions);
            log.info("Created {} sales transactions", transactions.size());
        }
    }
}
