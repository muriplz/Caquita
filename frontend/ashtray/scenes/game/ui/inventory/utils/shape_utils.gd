extends Node
class_name ShapeUtils

static func get_bounding_box(shape: Array) -> Rect2i:
	var size = shape.size()
	if size == 0:
		return Rect2i()
	var min_rows = size
	var max_rows = -1
	var min_cols = size
	var max_cols = -1
	for i in range(size):
		for j in range(size):
			if shape[i][j] != 0:
				min_rows = min(min_rows, i)
				max_rows = max(max_rows, i)
				min_cols = min(min_cols, j)
				max_cols = max(max_cols, j)
	if max_rows < 0:
		return Rect2i()
	return Rect2i(min_cols, min_rows, max_cols - min_cols + 1, max_rows - min_rows + 1)

static func get_dimensions(shape: Array) -> Vector2i:
	var rect = get_bounding_box(shape)
	return rect.size

static func trim_shape(shape: Array) -> Array:
	var rect = get_bounding_box(shape)
	if rect.size == Vector2i():
		return []
	var result = []
	for i in range(rect.position.y, rect.position.y + rect.size.y):
		result.append(shape[i].slice(rect.position.x, rect.size.x))
	return result
	
static func get_anchor(item: InventoryItem) -> Cell:
	var rect = get_bounding_box(ItemKindStore.get_item_kind(item.id).shape)
	if rect.size == Vector2i():
		return null

	var anchor_col = rect.position.x
	var anchor_row = rect.position.y

	for cell in item.cells:
		if cell.col == anchor_col and cell.row == anchor_row:
			return Cell.from_dict(cell)
	return null
	
