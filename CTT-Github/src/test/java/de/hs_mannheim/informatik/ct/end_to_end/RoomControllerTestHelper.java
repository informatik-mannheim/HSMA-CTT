package de.hs_mannheim.informatik.ct.end_to_end;

/*
 * Corona Tracking Tool der Hochschule Mannheim
 * Copyright (C) 2021 Hochschule Mannheim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import de.hs_mannheim.informatik.ct.model.Room;
import de.hs_mannheim.informatik.ct.persistence.InvalidEmailException;
import de.hs_mannheim.informatik.ct.persistence.services.RoomVisitService;
import de.hs_mannheim.informatik.ct.persistence.services.VisitorService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;

public class RoomControllerTestHelper {
    @Autowired
    private RoomVisitService roomVisitService;

    @Autowired
    private VisitorService visitorService;

    private ArrayList<Room> activeRooms = new ArrayList<Room>();
    private int activeUserCounter;

    public void addRoom(String name, int size){
        this.activeRooms.add(new Room(name, "A", 10));
    }

    public void fillRoom(String name, int ammount) throws InvalidEmailException {
        Room room = activeRooms.stream().filter(r -> r.getName() == name).findFirst().get();

        activeUserCounter = ammount;

        for(int i = 0; i < ammount; i++) {
            String user = "" + i + "@stud.hs-mannheim.de";
            roomVisitService.visitRoom(visitorService.findOrCreateVisitor(user), room);
        }
    }

    public void signOut(Room room){

    }
}
