package ui.fx;

import model.House;
import model.User;
import service.AutomationService;
import service.DeviceService;
import service.EnergieService;
import service.HouseService;
import service.SenzorService;
import service.UserService;

/**
 * Singleton care tine starea aplicatiei JavaFX: user curent, casa activa, servicii.
 */
public final class AppContext {

    private static AppContext instance;

    private final UserService userService = new UserService();
    private final HouseService houseService = new HouseService();
    private final DeviceService deviceService = new DeviceService();
    private final SenzorService senzorService = new SenzorService();
    private final AutomationService automationService = new AutomationService();
    private final EnergieService energieService = new EnergieService();

    private User currentUser;
    private House currentHouse;

    private AppContext() {}

    public static synchronized AppContext getInstance() {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
    }

    public UserService getUserService() { return userService; }
    public HouseService getHouseService() { return houseService; }
    public DeviceService getDeviceService() { return deviceService; }
    public SenzorService getSenzorService() { return senzorService; }
    public AutomationService getAutomationService() { return automationService; }
    public EnergieService getEnergieService() { return energieService; }

    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User user) { this.currentUser = user; }

    public House getCurrentHouse() { return currentHouse; }
    public void setCurrentHouse(House house) { this.currentHouse = house; }

    public void logout() {
        this.currentUser = null;
        this.currentHouse = null;
    }
}
