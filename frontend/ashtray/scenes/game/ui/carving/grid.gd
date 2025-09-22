extends Control

@export var cell_size: int = 64
@export var spacing: int = 2

var grid_width: int = 6
var grid_height: int = 10
var cell_buttons: Array[Array] = []

func get_terrain_color(terrain_type: String) -> Color:
	match terrain_type:
		"dirt:grown_grass":
			return Color.FOREST_GREEN
		"dirt:grass":
			return Color.GREEN
		"dirt:dirt":
			return Color.SADDLE_BROWN
		"dirt:coarse_dirt":
			return Color.PERU
		"dirt:gravel":
			return Color.GRAY
		"empty":
			return Color.BLACK
		_:
			return Color.WHITE

func build_grid(site_data: CarvingSite):
	clear_grid()
	
	var carvables = site_data.carvables
	
	for y in range(grid_height):
		var row: Array[ColorRect] = []
		for x in range(grid_width):
			var cell = ColorRect.new()
			cell.custom_minimum_size = Vector2(cell_size, cell_size)
			cell.position = Vector2(x * (cell_size + spacing), y * (cell_size + spacing))
			
			var terrain_type = carvables[y][x] if y < carvables.size() and x < carvables[y].size() else "unknown"
			cell.color = get_terrain_color(terrain_type)
			
			# Make it clickable
			cell.gui_input.connect(func(event): 
				if event is InputEventMouseButton and event.pressed and event.button_index == MOUSE_BUTTON_LEFT:
					_on_cell_clicked(x, y)
			)
			
			add_child(cell)
			row.append(cell)
		cell_buttons.append(row)

func update_grid(site_data: CarvingSite):
	var carvables = site_data.carvables
	for y in range(grid_height):
		for x in range(grid_width):
			if y < cell_buttons.size() and x < cell_buttons[y].size():
				var terrain_type = carvables[y][x] if y < carvables.size() and x < carvables[y].size() else "unknown"
				cell_buttons[y][x].color = get_terrain_color(terrain_type)

func _on_cell_clicked(x: int, y: int):
	CarvingService.carve("plastic:fork", x, y)

func clear_grid():
	for child in get_children():
		child.queue_free()
	cell_buttons.clear()
