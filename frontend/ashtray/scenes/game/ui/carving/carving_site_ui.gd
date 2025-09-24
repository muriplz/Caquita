class_name CarvingSiteUI
extends Control

@onready var grid = $Grid

func _ready():
	CarvingStore.carving_site_ready.connect(_on_carving_site_ready)
	CarvingStore.carving_site_updated.connect(_on_carving_site_updated)
	
	# Enable input processing
	set_process_unhandled_input(true)

func _unhandled_input(event):
	if event is InputEventKey and event.pressed:
		if event.keycode == KEY_R:
			generate_new_site()
			get_viewport().set_input_as_handled()

func generate_new_site():
	# Clear current site first
	CarvingStore.clear_carving_site()
	
	# Generate new site with random coordinates around Barcelona
	var lat = 41.3851 + randf_range(-0.1, 0.1)
	var lon = 2.1734 + randf_range(-0.1, 0.1)
	
	CarvingService.generate(lat, lon)

func _on_carving_site_ready():
	grid.build_grid()

func _on_carving_site_updated():
	grid.update_grid()
