package service;

import model.Room;
import model.device.Device;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class DeviceService {
    private final List<Device> allDevices = new ArrayList<>();

    public void addDevice(Room room, Device device) {
        Objects.requireNonNull(room, "Camera nu poate fi null.");
        Objects.requireNonNull(device, "Device-ul nu poate fi null.");
        if (allDevices.stream().anyMatch(existing -> existing.getId() == device.getId())) {
            throw new IllegalArgumentException("Exista deja un device cu id-ul " + device.getId());
        }

        device.setRoom(room);
        room.addDevice(device);
        allDevices.add(device);
        System.out.println("Device adaugat in " + room.getNume() + ": " + device);
    }

    public void removeDevice(Room room, Device device) {
        Objects.requireNonNull(room, "Camera nu poate fi null.");
        Objects.requireNonNull(device, "Device-ul nu poate fi null.");
        if (!room.removeDevice(device)) {
            throw new IllegalArgumentException("Device-ul nu exista in camera " + room.getNume());
        }
        allDevices.removeIf(existing -> existing.getId() == device.getId());
        device.setRoom(null);
        System.out.println("Device sters: " + device.getNume() + " din " + room.getNume());
    }

    public void turnOnDevice(Device device) {
        Objects.requireNonNull(device, "Device-ul nu poate fi null.");
        device.setStatus(true);
        System.out.println("Device pornit: " + device.getNume());
    }

    public void turnOffDevice(Device device) {
        Objects.requireNonNull(device, "Device-ul nu poate fi null.");
        device.setStatus(false);
        System.out.println("Device oprit: " + device.getNume());
    }

    public void moveDevice(Device device, Room fromRoom, Room toRoom) {
        Objects.requireNonNull(device, "Device-ul nu poate fi null.");
        Objects.requireNonNull(fromRoom, "Camera sursa nu poate fi null.");
        Objects.requireNonNull(toRoom, "Camera destinatie nu poate fi null.");
        if (fromRoom.equals(toRoom)) {
            throw new IllegalArgumentException("Camera sursa si destinatie nu pot fi aceleasi.");
        }
        if (!fromRoom.getDevices().contains(device)) {
            throw new IllegalArgumentException("Device-ul nu exista in camera sursa.");
        }
        if (toRoom.getDevices().contains(device)) {
            throw new IllegalArgumentException("Device-ul exista deja in camera destinatie.");
        }

        fromRoom.removeDevice(device);
        toRoom.addDevice(device);
        device.setRoom(toRoom);
        System.out.println("Device " + device.getNume() + " mutat din " +
                fromRoom.getNume() + " in " + toRoom.getNume());
    }

    public List<Device> getDevicesByRoom(Room room) {
        Objects.requireNonNull(room, "Camera nu poate fi null.");
        return room.getDevices();
    }

    public List<Device> getDevicesSortedByConsum(Room room) {
        Objects.requireNonNull(room, "Camera nu poate fi null.");
        List<Device> sorted = new ArrayList<>(room.getDevices());
        Collections.sort(sorted);
        return sorted;
    }

    public List<Device> getAllDevices() {
        return Collections.unmodifiableList(allDevices);
    }
}