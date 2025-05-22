extends Resource
class_name ItemKind

enum Rarity { JUNK, COMMON, UNCOMMON, RARE, RELIC }
enum ResourceType { PLASTIC, METAL, GLASS, OTHER }

@export var id: String
@export var shape: Array
@export var rarity: int
@export var resource_type: int
@export var classification: String

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
	
	return ik
