fill_args = {
	box = {
		pos = {
			x = 0,
			y = 4,
			z = 0
		},
		radius = 16 * 6,
		height = 5
	},
	block_type = "minecraft:cobblestone"
}

mc_fill_from_bottom_center(fill_args)

fill_args["box"]["pos"]["y"] = 9
fill_args["block_type"] = "minecraft:dirt"

mc_fill_from_bottom_center(fill_args)

fill_args["box"]["pos"]["y"] = 14
fill_args["box"]["height"] = 1
fill_args["block_type"] = "minecraft:grass_block"

mc_fill_from_bottom_center(fill_args)

fill_args["box"]["pos"]["y"] = 15
fill_args["box"]["height"] = 25
fill_args["block_type"] = "minecraft:air"

mc_fill_from_bottom_center(fill_args)

mc_create_colony({
	x = 0,
	y = 16,
	z = 0
})

mc_place_structure({
	name = "schematics/medievaloak/lumberjack5",
	pos = {
		x = 20,
		y = 15,
		z = 20
	},
	rotation = 0,
	mirrored = true
})

mc_place_structure({
	name = "schematics/medievaloak/smeltery5",
	pos = {
		x = 50,
		y = 16,
		z = 20
	},
	rotation = 1,
	mirrored = true
})

mc_print("Finished!")