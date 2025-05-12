extends RefCounted
class_name ItemKind

enum Rarity {
	JUNK,
	COMMON,
	UNCOMMON,
	RARE,
	RELIC
}
enum ResourceType {
	PLASTIC,
	METAL,
	GLASS,
	OTHER
}

var id: String
var shape: Array = [] # Holds PackedInt32
var rarity: Rarity = Rarity.COMMON
var resource_type: ResourceType = ResourceType.PLASTIC
var lifetime: int = -1
var classification: String

static func _name_of_enum(enum_dict: Dictionary, value: int) -> String:
	for name in enum_dict.keys():
		if enum_dict[name] == value:
			return name
	return ""

func _init(data: Dictionary = {}):
	id = str(data.get("id", ""))
	
	shape.clear()
	for sub in data.get("shape", []):
		var pia := PackedInt32Array()
		for v in sub:
			pia.append(int(v))
		shape.append(pia)

	var rr := str(data.get("rarity", "COMMON"))
	if Rarity.has(rr):
		rarity = Rarity[rr]

	var rt := str(data.get("resourceType", "OTHER"))
	if ResourceType.has(rt):
		resource_type = ResourceType[rt]

	lifetime = int(data.get("lifetime", -1))
	classification = str(data.get("classification", ""))

func to_dict() -> Dictionary:
	var shape_list := []
	for pia in shape:
		shape_list.append(pia)

	var rarity_name := _name_of_enum(Rarity, rarity)
	var resource_name := _name_of_enum(ResourceType, resource_type)

	return {
		"id":             id,
		"shape":          shape_list,
		"rarity":         rarity_name,
		"resourceType":   resource_name,
		"lifetime":       lifetime,
		"classification": classification
	}
