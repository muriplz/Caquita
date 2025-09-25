extends Resource
class_name Duster

@export var dusterId: String
@export var equippedTool: EquippedTool
@export var pockets: int
@export var toolPocketsShape: Array


static func from_dict(dict: Dictionary) -> Duster:
	var duster = Duster.new()
	
	duster.dusterId = dict.get("duster_id", "")
	duster.equippedTool = EquippedTool.from_dict(dict.get("equipped_tool"))
	duster.pockets = dict.get("pockets", 0)
	
	duster.toolPocketsShape = dict.get("tool_pockets_shape", []).map(func(row):
		return row.map(func(v):
			return int(v)
		)
	)
	
	return duster
