package com.example.library.dto;

import com.example.library.model.RoomStatus;
import com.example.library.model.RoomType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateRoomRequest {

	@NotBlank
	private String building;

	@Min(0)
	private int floor;

	@NotBlank
	private String roomNumber;

	@Min(1)
	private int capacity;

	@NotNull
	private RoomType type;

	private boolean projector;

	private boolean whiteboard;

	@NotNull
	private RoomStatus status;
}