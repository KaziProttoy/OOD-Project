import java.io.*;
import java.util.*;

class Bus implements Serializable {
    int busId;
    String busName;
    String fromCity;
    String toCity;
    double ticketPrice;
    int totalSeats;
    int availableSeats;
    boolean[] seatStatus;

    public Bus(int busId, String busName, String fromCity, String toCity, double ticketPrice, int totalSeats) {
        this.busId = busId;
        this.busName = busName;
        this.fromCity = fromCity;
        this.toCity = toCity;
        this.ticketPrice = ticketPrice;
        this.totalSeats = totalSeats;
        this.availableSeats = totalSeats;
        this.seatStatus = new boolean[totalSeats];
    }
}

class Booking implements Serializable {
    static int nextId = 1;
    int bookingId;
    String passengerName;
    int busId;
    List<Integer> seatNumbers;
    double totalPrice;

    public Booking(String passengerName, int busId, List<Integer> seatNumbers, double totalPrice) {
        this.bookingId = nextId++;
        this.passengerName = passengerName;
        this.busId = busId;
        this.seatNumbers = seatNumbers;
        this.totalPrice = totalPrice;
    }
}

class HelpRequest implements Serializable {
    int requestId;
    String passengerName;
    String message;

    public HelpRequest(int requestId, String passengerName, String message) {
        this.requestId = requestId;
        this.passengerName = passengerName;
        this.message = message;
    }
}

public class BusReservationSystem {
    static List<Bus> buses = new ArrayList<>();
    static List<Booking> bookings = new ArrayList<>();
    static List<HelpRequest> helpRequests = new ArrayList<>();
    static List<String> users = new ArrayList<>();
    static Scanner scanner = new Scanner(System.in);
    static final String ADMIN_USERNAME = "admin";
    static final String ADMIN_PASSWORD = "admin123";
    static final String USERS_FILE = "users.txt";
    static final String BUSES_FILE = "buses.dat";
    static final String BOOKINGS_FILE = "bookings.dat";
    static final String HELP_FILE = "helpline.dat";

    public static void main(String[] args) {
        loadData();
        while (true) {
            System.out.println("\n--- Main Menu ---");
            System.out.println("1. Admin Login");
            System.out.println("2. Passenger Menu");
            System.out.println("3. Exit");
            System.out.print("Choose option: ");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1 -> {
                    if (adminLogin()) adminMenu();
                    else System.out.println("Login failed.");
                }
                case 2 -> passengerMenu();
                case 3 -> {
                    saveData();
                    System.exit(0);
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    static boolean adminLogin() {
        scanner.nextLine();
        System.out.print("Enter username: ");
        String user = scanner.nextLine();
        System.out.print("Enter password: ");
        String pass = scanner.nextLine();
        return user.equals(ADMIN_USERNAME) && pass.equals(ADMIN_PASSWORD);
    }

    static void adminMenu() {
        while (true) {
            System.out.println("\n--- Admin Menu ---");
            System.out.println("1. Add Bus");
            System.out.println("2. View Buses");
            System.out.println("3. View Bookings");
            System.out.println("4. Cancel Booking");
            System.out.println("5. View Help Requests");
            System.out.println("6. Logout");
            System.out.print("Choose option: ");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1 -> addBus();
                case 2 -> viewBuses();
                case 3 -> viewBookings();
                case 4 -> cancelBooking();
                case 5 -> viewHelpRequests();
                case 6 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    static void passengerMenu() {
        while (true) {
            System.out.println("\n--- Passenger Menu ---");
            System.out.println("1. View Buses");
            System.out.println("2. Book Ticket");
            System.out.println("3. View Bookings");
            System.out.println("4. Help Line");
            System.out.println("5. Logout");
            System.out.print("Choose option: ");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1 -> viewBuses();
                case 2 -> bookTicket();
                case 3 -> viewBookings();
                case 4 -> submitHelpRequest();
                case 5 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    static void addBus() {
        scanner.nextLine();
        System.out.print("Enter bus name: ");
        String name = scanner.nextLine();
        System.out.print("From: ");
        String from = scanner.nextLine();
        System.out.print("To: ");
        String to = scanner.nextLine();
        System.out.print("Ticket price: ");
        double price = scanner.nextDouble();
        System.out.print("Total seats: ");
        int seats = scanner.nextInt();
        buses.add(new Bus(buses.size() + 1, name, from, to, price, seats));
        saveData();
        System.out.println("Bus added.");
    }

    static void viewBuses() {
        for (Bus bus : buses) {
            System.out.printf("ID: %d | %s (%s to %s) | Price: %.2f | Available: %d\n",
                bus.busId, bus.busName, bus.fromCity, bus.toCity, bus.ticketPrice, bus.availableSeats);
        }
    }

    static void bookTicket() {
        System.out.print("Enter Bus ID: ");
        int id = scanner.nextInt();
        Bus bus = findBusById(id);
        if (bus == null) {
            System.out.println("Bus not found.");
            return;
        }

        scanner.nextLine();
        System.out.print("Your name: ");
        String name = scanner.nextLine();
        System.out.print("Seats to book: ");
        int count = scanner.nextInt();
        if (count > bus.availableSeats || count <= 0) {
            System.out.println("Invalid seat count.");
            return;
        }

        List<Integer> booked = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            System.out.print("Choose seat (1 to " + bus.totalSeats + "): ");
            int s = scanner.nextInt();
            if (s < 1 || s > bus.totalSeats || bus.seatStatus[s - 1]) {
                System.out.println("Seat unavailable.");
                i--;
            } else {
                bus.seatStatus[s - 1] = true;
                booked.add(s);
            }
        }

        double total = booked.size() * bus.ticketPrice;
        bookings.add(new Booking(name, id, booked, total));
        bus.availableSeats -= booked.size();
        saveData();
        System.out.println("Booking complete. Total: " + total);
    }

    static void cancelBooking() {
        System.out.print("Enter Booking ID: ");
        int id = scanner.nextInt();
        Iterator<Booking> it = bookings.iterator();
        while (it.hasNext()) {
            Booking b = it.next();
            if (b.bookingId == id) {
                Bus bus = findBusById(b.busId);
                for (int s : b.seatNumbers) bus.seatStatus[s - 1] = false;
                if (bus != null) bus.availableSeats += b.seatNumbers.size();
                it.remove();
                saveData();
                System.out.println("Booking canceled.");
                return;
            }
        }
        System.out.println("Booking not found.");
    }

    static void viewBookings() {
        if (bookings.isEmpty()) {
            System.out.println("No bookings.");
        } else {
            for (Booking b : bookings) {
                System.out.printf("ID: %d | Name: %s | Bus: %d | Seats: %s | Total: %.2f\n",
                        b.bookingId, b.passengerName, b.busId, b.seatNumbers.toString(), b.totalPrice);
            }
        }
    }

    static void submitHelpRequest() {
        scanner.nextLine();
        System.out.print("Your name: ");
        String name = scanner.nextLine();
        System.out.print("Message: ");
        String msg = scanner.nextLine();
        helpRequests.add(new HelpRequest(helpRequests.size() + 1, name, msg));
        saveData();
        System.out.println("Help request submitted.");
    }

    static void viewHelpRequests() {
        if (helpRequests.isEmpty()) {
            System.out.println("No help requests.");
        } else {
            for (HelpRequest hr : helpRequests) {
                System.out.printf("ID: %d | Name: %s | Message: %s\n",
                        hr.requestId, hr.passengerName, hr.message);
            }
        }
    }

    static Bus findBusById(int id) {
        for (Bus b : buses) if (b.busId == id) return b;
        return null;
    }

    static void loadData() {
        loadUsers();
        loadBuses();
        loadBookings();
        loadHelpRequests();
    }

    static void saveData() {
        saveUsers();
        saveBuses();
        saveBookings();
        saveHelpRequests();
    }

    static void loadUsers() {
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                users.add(line);
            }
        } catch (IOException e) {
            System.out.println("Error loading users.");
        }
    }

    static void saveUsers() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE))) {
            for (String user : users) {
                writer.write(user);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving users.");
        }
    }

    static void loadBuses() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(BUSES_FILE))) {
            buses = (List<Bus>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading buses.");
        }
    }

    static void saveBuses() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(BUSES_FILE))) {
            oos.writeObject(buses);
        } catch (IOException e) {
            System.out.println("Error saving buses.");
        }
    }

    static void loadBookings() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(BOOKINGS_FILE))) {
            bookings = (List<Booking>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading bookings.");
        }
    }

    static void saveBookings() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(BOOKINGS_FILE))) {
            oos.writeObject(bookings);
        } catch (IOException e) {
            System.out.println("Error saving bookings.");
        }
    }

    static void loadHelpRequests() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(HELP_FILE))) {
            helpRequests = (List<HelpRequest>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading help requests.");
        }
    }

    static void saveHelpRequests() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(HELP_FILE))) {
            oos.writeObject(helpRequests);
        } catch (IOException e) {
            System.out.println("Error saving help requests.");
        }
    }
}