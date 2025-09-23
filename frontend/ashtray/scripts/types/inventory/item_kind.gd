extends Resource
class_name ItemKind

enum Rarity { JUNK, COMMON, UNCOMMON, RARE, RELIC }
enum ResourceType { PLASTIC, METAL, GLASS, CARDBOARD, OTHER }

@export var id: String
@export var shape: Array
@export var rarity: int
@export var resource_type: int
@export var classification: String
@export var max_durability: int = -1  # -1 means not a tool
@export var action_shape: Array = []

static func from_dict(dict: Dictionary) -> ItemKind:
	var ik = ItemKind.new()
	ik.id = dict.get("id", "")
	
	ik.shape = dict.get("shape", []).map(func(row):
		return row.map(func(v):
			return int(v)
		)
	)
	
	ik.rarity = Rarity[dict.get("rarity", "COMMON")]
	ik.resource_type = ResourceType[dict.get("resourceType", "OTHER")]
	ik.classification = dict.get("classification", "")
	ik.max_durability = int(dict.get("maxDurability", -1))
	
	if dict.has("actionShape"):
		ik.action_shape = dict.get("actionShape", []).map(func(row):
			return row.map(func(v):
				return int(v)
			)
		)
		
	print(dict)
	
	return ik

func is_tool() -> bool:
	return max_durability > -1
