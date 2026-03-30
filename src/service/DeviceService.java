package service;

import model.Room;
import model.device.Device;

import java.util.ArrayList;
import java.util.List;

public class DeviceService {
    private List<Device> allDevices = new ArrayList<>();

    public void addDevice(Room room, Device device) {
        device.setRoom(room);
        room.getDevices().add(device);
        allDevices.add(device);
        System.out.println("Device adaugat in " + room.getNume() + ": " + device);
    }

    public void removeDevice(Room room, Device device) {
        room.getDevices().remove(device);
        allDevices.remove(device);
        System.out.println("Device sters: " + device.getNume() + " din " + room.getNume());
    }

    public void turnOnDevice(Device device) {
        device.setStatus(true);
        System.out.println("Device pornit: " + device.getNume());
    }

    public void turnOffDevice(Device device) {
        device.setStatus(false);
        System.out.println("Device oprit: " + device.getNume());
    }

    public void moveDevice(Device device, Room fromRoom, Room toRoom) {
        fromRoom.getDevices().remove(device);
        toRoom.getDevices().add(device);
        device.setRoom(toRoom);
        System.out.println("Device " + device.getNume() + " mutat din " +
                fromRoom.getNume() + " in " + toRoom.getNume());
    }

    public List<Device> getDevicesByRoom(Room room) {
        return room.getDevices();
    }

    public List<Device> getAllDevices() {
        return allDevices;
    }
}