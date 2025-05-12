extends GridContainer

@onready var inventory   = InventoryStore.get_inventory()
var slot_scene := preload("res://scenes/game/ui/inventory/inventory_slot.tscn")

func _ready() -> void:
	columns = inventory.width
	populate()

func populate() -> void:
	for c in get_children():
		c.queue_free()
	var w = inventory.width
	var h = inventory.height

	for y in range(h):
		for x in range(w):
			print("Creating empty slots")

	for item in inventory.items:
		print("Creating items on top of their occupied slots %s" % [item.id])
