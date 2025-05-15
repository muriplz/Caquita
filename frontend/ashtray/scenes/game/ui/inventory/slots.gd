extends Control

const SLOT_SCENE = preload("res://scenes/game/ui/inventory/inventory_slot.tscn")
const INVENTORY_ITEM_SCENE = preload("res://scenes/game/ui/inventory/inventory_item.tscn")

@onready var inventory : Inventory = InventoryStore.get_inventory()

func _ready() -> void:
	var cols = inventory.width
	var rows = inventory.height

	var cell_size = Vector2(100, 100)

	var total_w = cols * cell_size.x + cols - 1
	var total_h = rows * cell_size.y + rows - 1

	var origin = -Vector2(total_w, total_h) * 0.5

	for y in range(rows):
		for x in range(cols):
			var slot = SLOT_SCENE.instantiate() as Control
			slot.set_anchors_preset(Control.PRESET_TOP_LEFT)
			slot.size = cell_size
			var offset = Vector2(x * cell_size.x, y * cell_size.y)
			slot.slot_coords = Vector2(x, y)
			slot.position = origin + offset

			add_child(slot)
			
	for inventory_item in inventory.items:
		var inventory_item_scene = INVENTORY_ITEM_SCENE. instantiate() as TextureRect
		
		inventory_item_scene.id = inventory_item.id
		
		# [[0,1],[0,1] for example
		var shape = ItemKindStore.get_item_kind(inventory_item.id).shape
		
		var orientation = inventory_item.orientation as InventoryItem.Orientation
		
		# an array of slots but just the occupied cells
		var cells = inventory_item.cells
		
		add_child(inventory_item_scene)
