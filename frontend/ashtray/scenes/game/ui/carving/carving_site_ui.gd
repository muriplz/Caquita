class_name CarvingSiteUI
extends Control

@onready var grid = $Grid

func _ready():
	CarvingStore.carving_site_ready.connect(_on_carving_site_ready)
	CarvingStore.carving_site_updated.connect(_on_carving_site_updated)

func _on_carving_site_ready():
	var site_data = CarvingStore.get_carving_site()
	print(site_data)
	grid.build_grid(site_data)

func _on_carving_site_updated():
	var site_data = CarvingStore.get_carving_site()
	grid.update_grid(site_data)
