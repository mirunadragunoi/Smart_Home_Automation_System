package service;

import exception.DuplicateEntityException;
import exception.NotFoundException;
import exception.ValidationException;
import model.Room;
import model.device.Camera;
import model.device.Device;
import model.device.DoorLock;
import model.device.Lumina;
import model.device.Termostat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeviceService {
    private final List<Device> allDevices = new ArrayList<>();

    public void addDevice(Room room, Device device) {
        requireNonNull(room, "Camera nu poate fi null.");
        requireNonNull(device, "Device-ul nu poate fi null.");
        if (allDevices.stream().anyMatch(existing -> existing.getId() == device.getId())) {
            throw new DuplicateEntityException("Exista deja un device cu id-ul " + device.getId());
        }
        validateDeviceBusinessRules(device);

        device.setRoom(room);
        room.addDevice(device);
        allDevices.add(device);
        System.out.println("Device adaugat in " + room.getNume() + ": " + device);
    }

    public void removeDevice(Room room, Device device) {
        requireNonNull(room, "Camera nu poate fi null.");
        requireNonNull(device, "Device-ul nu poate fi null.");
        if (!room.removeDevice(device)) {
            throw new NotFoundException("Device-ul nu exista in camera " + room.getNume());
        }
        allDevices.removeIf(existing -> existing.getId() == device.getId());
        device.setRoom(null);
        System.out.println("Device sters: " + device.getNume() + " din " + room.getNume());
    }

    public void turnOnDevice(Device device) {
        requireNonNull(device, "Device-ul nu poate fi null.");
        device.setStatus(true);
        System.out.println("Device pornit: " + device.getNume());
    }

    public void turnOffDevice(Device device) {
        requireNonNull(device, "Device-ul nu poate fi null.");
        device.setStatus(false);
        System.out.println("Device oprit: " + device.getNume());
    }

    public void moveDevice(Device device, Room fromRoom, Room toRoom) {
        requireNonNull(device, "Device-ul nu poate fi null.");
        requireNonNull(fromRoom, "Camera sursa nu poate fi null.");
        requireNonNull(toRoom, "Camera destinatie nu poate fi null.");
        if (fromRoom.equals(toRoom)) {
            throw new ValidationException("Camera sursa si destinatie nu pot fi aceleasi.");
        }
        if (!fromRoom.getDevices().contains(device)) {
            throw new NotFoundException("Device-ul nu exista in camera sursa.");
        }
        if (toRoom.getDevices().contains(device)) {
            throw new DuplicateEntityException("Device-ul exista deja in camera destinatie.");
        }

        fromRoom.removeDevice(device);
        toRoom.addDevice(device);
        device.setRoom(toRoom);
        System.out.println("Device " + device.getNume() + " mutat din " +
                fromRoom.getNume() + " in " + toRoom.getNume());
    }

    public List<Device> getDevicesByRoom(Room room) {
        requireNonNull(room, "Camera nu poate fi null.");
        return room.getDevices();
    }

    public List<Device> getDevicesSortedByConsum(Room room) {
        requireNonNull(room, "Camera nu poate fi null.");
        List<Device> sorted = new ArrayList<>(room.getDevices());
        Collections.sort(sorted);
        return sorted;
    }

    public List<Device> getAllDevices() {
        return Collections.unmodifiableList(allDevices);
    }

    private static void requireNonNull(Object object, String message) {
        if (object == null) {
            throw new ValidationException(message);
        }
    }

    private static void validateDeviceBusinessRules(Device device) {
        if (device.getNume() == null || device.getNume().trim().isEmpty()) {
            throw new ValidationException("Numele device-ului nu poate fi gol.");
        }
        if (device.getPutereConsumata() <= 0) {
            throw new ValidationException("Puterea consumata trebuie sa fie > 0.");
        }

        if (device instanceof Lumina lumina) {
            if (lumina.getLuminozitate() < 0 || lumina.getLuminozitate() > 100) {
                throw new ValidationException("Luminozitatea trebuie sa fie intre 0 si 100.");
            }
            if (lumina.getColor() == null || lumina.getColor().trim().isEmpty()) {
                throw new ValidationException("Culoarea luminii nu poate fi goala.");
            }
            return;
        }

        if (device instanceof Termostat termostat) {
            if (termostat.getTemperatura() < -20 || termostat.getTemperatura() > 50) {
                throw new ValidationException("Temperatura curenta a termostatului trebuie sa fie in intervalul [-20, 50].");
            }
            if (termostat.getTargetTemperatura() < 10 || termostat.getTargetTemperatura() > 35) {
                throw new ValidationException("Temperatura tinta trebuie sa fie in intervalul [10, 35].");
            }
            return;
        }

        if (device instanceof Camera camera) {
            if (camera.getRezolutie() < 240) {
                throw new ValidationException("Rezolutia camerei trebuie sa fie de minim 240.");
            }
            return;
        }

        if (device instanceof DoorLock doorLock) {
            if (doorLock.getCodAcces() == null || doorLock.getCodAcces().trim().length() < 4) {
                throw new ValidationException("Codul de acces trebuie sa aiba minim 4 caractere.");
            }
        }
    }
}