class_name InventoryItem
extends RefCounted

enum Orientation {
	UP,
	DOWN,
	LEFT,
	RIGHT
}

var id: String
var cells: Array[Cell]
var orientation: Orientation

func _init(inventory_item_data: Dictionary = {}):
	if inventory_item_data.is_empty():
		return

	id = inventory_item_data.get("id", "")
	
	var raw_cells = inventory_item_data.get("cells", [])
	cells.clear()
	for dict_cell in raw_cells:
		var c = int(dict_cell.get("col", 0))
		var r = int(dict_cell.get("row", 0))
		cells.append(Cell.new(c, r))
	
	var o := str(inventory_item_data.get("orientation", "OTHER"))
	if Orientation.has(o):
		orientation = Orientation[o]

func to_dict() -> Dictionary:
	return {
		"id": id,
		"cells": cells,
		"orientation": orientation
	}
