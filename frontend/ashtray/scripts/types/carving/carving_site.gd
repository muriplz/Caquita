extends Resource
class_name CarvingSite

@export var carvables: Array
@export var obstacles: Array[CarvingObstacle] = []
@export var items: Array[CarvingItem] = []

static func from_dict(dict: Dictionary) -> CarvingSite:
	var site = CarvingSite.new()
	
	site.carvables = dict.get("carvables", [])
	
	var obstacles_array = dict.get("obstacles", [])
	site.obstacles.clear()
	for obstacle_dict in obstacles_array:
		site.obstacles.append(CarvingObstacle.from_dict(obstacle_dict))
	
	var items_array = dict.get("items", [])
	site.items.clear()
	for item_dict in items_array:
		site.items.append(CarvingItem.from_dict(item_dict))
		
	
	return site
