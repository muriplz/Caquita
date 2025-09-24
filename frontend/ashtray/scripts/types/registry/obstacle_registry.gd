extends Resource
class_name ObstacleKind

@export var id: String
@export var shape: Array

static func from_dict(dict: Dictionary) -> ObstacleKind:
	var ok = ObstacleKind.new()
	ok.id = dict.get("id", "")
	
	ok.shape = dict.get("shape", []).map(func(row):
		return row.map(func(v):
			return int(v)
		)
	)
	return ok
