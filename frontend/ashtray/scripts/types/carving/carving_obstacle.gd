extends Resource
class_name CarvingObstacle

@export var obstacle: String
@export var anchorX: int
@export var anchorY: int

static func from_dict(dict: Dictionary) -> CarvingObstacle:
	var carving_obstacle = CarvingObstacle.new()
	
	carving_obstacle.obstacle = dict.get("obstacle", "")
	carving_obstacle.anchorX = dict.get("anchorX", 0)
	carving_obstacle.anchorY = dict.get("anchorY", 0)
	
	return carving_obstacle
