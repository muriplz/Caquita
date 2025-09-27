extends Resource
class_name EquippedDuster

@export var duster: String
@export var equippedTool: ToolData
@export var pockets: int
@export var toolPocketsShape: Array[Array]

static func from_dict(dict: Dictionary) -> EquippedDuster:
	if dict == null:
		return null
	
	var equippedDuster = EquippedDuster.new()
	equippedDuster.duster = dict.get("duster", "")
	equippedDuster.equippedTool = ToolData.from_dict(dict.get("equippedTool"))
	equippedDuster.pockets = dict.get("pockets", 0)
	
	var shape = dict.get("toolPocketsShape", [])
	equippedDuster.toolPocketsShape = []
	for row in shape:
		var toolRow = []
		for pocketDict in row:
			toolRow.append(ToolPocket.from_dict(pocketDict))
		equippedDuster.toolPocketsShape.append(toolRow)
	
	return equippedDuster
