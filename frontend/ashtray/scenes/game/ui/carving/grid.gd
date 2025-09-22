extends Control

@export var cell_size: int = 64
@export var spacing: int = 0

var grid_width: int = 6
var grid_height: int = 10
var cells: Array[Array] = []
var cell_scene = preload("res://scenes/game/ui/carving/carving_cell.tscn")

func _ready():
	pass

func build_grid():
	clear_grid()
	var site_data = CarvingStore.get_carving_site()
	if not site_data:
		return
		
	var carvables = site_data.carvables
	
	var total_width = grid_width * cell_size
	var total_height = grid_height * cell_size
	
	var start_x = -floor(total_width / 2.0) + cell_size / 2
	var start_y = -floor(total_height / 2.0) + cell_size / 2
	
	for y in range(grid_height):
		var row: Array[CarvingCell] = []
		for x in range(grid_width):
			var cell = cell_scene.instantiate()
			cell.custom_minimum_size = Vector2(cell_size, cell_size)
			cell.position = Vector2(start_x + x * cell_size, start_y + y * cell_size)
			cell.grid_x = x
			cell.grid_y = y
			cell.terrain_type = carvables[y][x] if y < carvables.size() and x < carvables[y].size() else "unknown"
			cell.cell_clicked.connect(_on_cell_clicked)
			add_child(cell)
			row.append(cell)
		cells.append(row)
	
	update_grid()

func update_grid():
	var site_data = CarvingStore.get_carving_site()
	if not site_data or cells.is_empty():
		return
		
	# Update terrain
	for y in range(grid_height):
		for x in range(grid_width):
			if y < cells.size() and x < cells[y].size():
				var terrain = site_data.carvables[y][x] if y < site_data.carvables.size() and x < site_data.carvables[y].size() else "unknown"
				cells[y][x].terrain_type = terrain
	
	# Clear all items first
	for y in range(grid_height):
		for x in range(grid_width):
			if y < cells.size() and x < cells[y].size():
				cells[y][x].clear_item()
	
	# Set items that cover cells
	for item in site_data.items:
		var item_kind = ItemKindStore.get_item_kind(item.item)
		if item_kind:
			var shape = item_kind.shape
			for sy in range(shape.size()):
				for sx in range(shape[sy].size()):
					if shape[sy][sx] == 1:
						var cell_x = item.anchorX + sx
						var cell_y = item.anchorY + sy
						if cell_x < grid_width and cell_y < grid_height and cell_x >= 0 and cell_y >= 0:
							cells[cell_y][cell_x].set_item(item)

func _on_cell_clicked(x: int, y: int):
	CarvingService.carve("plastic:fork", x, y)

func clear_grid():
	for child in get_children():
		child.queue_free()
	cells.clear()
