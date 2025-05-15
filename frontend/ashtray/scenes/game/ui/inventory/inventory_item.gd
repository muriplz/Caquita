extends TextureRect

var id: String

func _ready() -> void:
	print(ItemKindStore.get_item_kind(id).shape)
	var tex_name = id.replace(":", "/")
	var tex = load("res://assets/textures/items/%s.png" % [tex_name]) as Texture
	
