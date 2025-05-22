extends Resource
class_name Cell

@export var col: int
@export var row: int

static func from_dict(dict: Dictionary) -> Cell:
	var cell = Cell.new()
	cell.col = dict.get("col", 0)
	cell.row = dict.get("row", 0)
	
	return cell
