extends Resource
class_name ToolPocket

@export var locked: bool
@export var tool: ToolData

static func from_dict(dict: Dictionary) -> ToolPocket:
	if dict == null:
		return null
	
	var pocket = ToolPocket.new()
	pocket.locked = dict.get("locked", false)
	pocket.tool = ToolData.from_dict(dict.get("tool"))
	return pocket
