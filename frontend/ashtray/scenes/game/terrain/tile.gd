extends Sprite3D

func _ready():
	$"../MapApi".get_tile(0, 0, self)
	rotation_degrees.x = -90
	
func set_texture_from_image(image: Image) -> void:
	if image:
		var texture = ImageTexture.create_from_image(image)
		self.texture = texture
