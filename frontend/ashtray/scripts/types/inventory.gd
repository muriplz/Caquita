class_name Inventory
extends RefCounted

var id: int
var user_id: int
var items: Array[InventoryItem] = []
var height: int
var width: int

func _init(inventory_data: Dictionary = {}):
	if inventory_data.is_empty():
		return

	id = int(inventory_data.get("id", 0))
	user_id = int(inventory_data.get("user_id", 0))
	var raw_items = inventory_data.get("items", [])
	items.clear()
	for data_dict in raw_items:
		var itm = InventoryItem.new(data_dict)
		items.append(itm)

	height = int(inventory_data.get("height", 0))
	width = int(inventory_data.get("width", 0))

func to_dict() -> Dictionary:
	return {
		"id": id,
		"user_id": user_id,
		"items": items,
		"height": height,
		"width": width
	}
