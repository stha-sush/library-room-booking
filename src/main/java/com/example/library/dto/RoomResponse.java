package com.example.library.dto;

import com.example.library.model.Room;

public record RoomResponse(Long id, String building, int floor, String roomNumber, int capacity, String type,
		boolean projector, boolean whiteboard) {
	public static RoomResponse from(Room r) {
		return new RoomResponse(r.getId(), r.getBuilding(), r.getFloor(), r.getRoomNumber(), r.getCapacity(),
				r.getType() == null ? null : r.getType().name(), r.isProjector(), r.isWhiteboard());
	}
}