extends Resource
class_name Wardrobe

@export var id: int
@export var userId: int
@export var duster: EquippedDuster
@export var dusters: Array[String]

static func from_dict(dict: Dictionary) -> Wardrobe:
	var wardrobe = Wardrobe.new()
	wardrobe.id = dict.get("id", 0)
	wardrobe.userId = dict.get("userId", 0)
	wardrobe.duster = EquippedDuster.from_dict(dict.get("duster"))
	wardrobe.dusters = dict.get("dusters", [])
	return wardrobe
