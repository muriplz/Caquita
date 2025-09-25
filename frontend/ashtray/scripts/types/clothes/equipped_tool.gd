extends Resource
class_name EquippedTool

@export var id: String
@export var durability: int
@export var erre: float

static func from_dict(dict: Dictionary) -> EquippedTool:
	var equippedTool = EquippedTool.new()
	
	equippedTool.id = dict.get("id", "")
	equippedTool.durability = dict.get("durability", 0)
	equippedTool.erre = dict.get("erre", 0)

	return equippedTool
