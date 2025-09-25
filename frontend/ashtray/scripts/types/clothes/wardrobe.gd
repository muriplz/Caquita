extends Resource
class_name Wardrobe

@export var id: int
@export var userId: int
@export var duster: Duster
@export var dusters: Array

static func from_dict(dict: Dictionary) -> Wardrobe:
	var wardrobe = Wardrobe.new()
	wardrobe.id = dict.get("id", 0)
	wardrobe.userId = dict.get("user_id", 0)
	wardrobe.duster = Duster.from_dict(dict.get("duster"))
	
	var dusters_array = dict.get("dusters", [])
	wardrobe.dusters.clear()
	for duster_dict in dusters_array:
		wardrobe.dusters.append(duster_dict)
	
	return wardrobe
