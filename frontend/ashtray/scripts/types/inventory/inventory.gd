extends Resource
class_name Inventory

@export var id: int = 0
@export var user_id: int = 0
@export var items: Array = []
@export var height: int = 3
@export var width: int = 2

static func from_dict(dict: Dictionary) -> Inventory:
	var inventory = Inventory.new()
	inventory.id = dict.get("id", 0)
	inventory.user_id = dict.get("user_id", 0)
	inventory.items = dict.get("items", [])
	inventory.height = dict.get("height", 3)
	inventory.width = dict.get("width", 0)
	return inventory
