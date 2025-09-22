class_name CarvingSiteUI
extends Control

@onready var grid = $Grid

func _ready():
	CarvingStore.carving_site_ready.connect(_on_carving_site_ready)
	CarvingStore.carving_site_updated.connect(_on_carving_site_updated)

func _on_carving_site_ready():
	grid.build_grid()

func _on_carving_site_updated():
	grid.update_grid()
