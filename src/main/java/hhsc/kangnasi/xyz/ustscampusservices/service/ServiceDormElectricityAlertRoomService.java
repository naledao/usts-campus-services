package hhsc.kangnasi.xyz.ustscampusservices.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ServiceDormElectricityAlertRoomService {
    void addRooms() throws IOException;

    List<Map<String,String>> getRoom(String campusId, Integer buildingId);

    void bindRoom(String campus, String building, String room);
}
