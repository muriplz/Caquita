extends Resource
class_name ToolData

@export var toolId: String
@export var durability: int
@export var erre: float

static func from_dict(dict: Dictionary) -> ToolData:
	if dict == null:
		return null
	
	var toolData = ToolData.new()
	toolData.toolId = dict.get("toolId", "")
	toolData.durability = dict.get("durability", 0)
	toolData.erre = dict.get("erre", 0.0)
	return toolData
