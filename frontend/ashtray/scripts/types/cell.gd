class_name Cell
extends RefCounted

var col: int
var row: int

func _init(a = null, b = null) -> void:
	if a is Dictionary:
		# Called with a Dictionary
		col = int(a.get("col", 0))
		row = int(a.get("row", 0))
	else:
		# Called with two ints (or nothing)
		col = int(a) if typeof(a) == TYPE_INT else 0
		row = int(b) if typeof(b) == TYPE_INT else 0

func to_dict() -> Dictionary:
	return {
		"col": col,
		"row": row
	}
