package de.hs_mannheim.informatik.ct.util;

import java.util.HashMap;

public class RoomTypeConverter {
    private static RoomTypeConverter instance;
    private HashMap<Integer, RoomType> roomTypes;

    private RoomTypeConverter(){
        roomTypes = new HashMap<>();
        roomTypes.put(210, RoomType.BUERO);  // Büroraum
        roomTypes.put(330, RoomType.LABOR);  // Techn.Labor
        roomTypes.put(338, RoomType.LABOR);  // Techn.Labor mit Berstwand
        roomTypes.put(533, RoomType.HOERSAAL);  // Medienunt.Unterrichtsr.
        roomTypes.put(514, RoomType.HOERSAAL);  // Hör-/Lehrsaal
        roomTypes.put(513, RoomType.HOERSAAL);  // Hör-/Lehrsaal ansteigend
        roomTypes.put(910, RoomType.HOERSAAL);  // Test
    }

    public static RoomTypeConverter getInstance(){
        if(instance==null){
            synchronized (RoomTypeConverter.class){
                if(instance==null){
                    instance = new RoomTypeConverter();
                }
            }
        }
        return instance;
    }

    public static enum RoomType {
        BUERO,
        LABOR,
        HOERSAAL,
    }

    public RoomType convertRNAToRoomType(Integer rna){
        return roomTypes.getOrDefault(rna, null);
    }
}
