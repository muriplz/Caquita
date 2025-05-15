extends Resource
class_name ItemKind

enum Rarity { JUNK, COMMON, UNCOMMON, RARE, RELIC }
enum ResourceType { PLASTIC, METAL, GLASS, OTHER }

@export var id: String = ""
@export var shape: Array = []
@export var rarity: int = Rarity.COMMON
@export var resource_type: int = ResourceType.OTHER
@export var classification: String = ""

static func from_dict(dict: Dictionary) -> ItemKind:
	var ik = ItemKind.new()
	ik.id = dict.get("id", "")
	ik.shape = dict.get("shape", [])
	var r_name = dict.get("rarity", "")
	if Rarity.has(r_name):
		ik.rarity = Rarity[r_name]
	var rt_name = dict.get("resourceType", "")
	if ResourceType.has(rt_name):
		ik.resource_type = ResourceType[rt_name]
	ik.classification = dict.get("classification", "")
	return ik
