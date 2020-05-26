package com.ldtteam.scriptedcolonies.luamethods;

import com.ldtteam.scriptedcolonies.helpers.MinecraftScheduler;
import com.ldtteam.scriptedcolonies.luadto.LuaTableReader;
import com.ldtteam.scriptedcolonies.luadto.StructureDto;
import com.ldtteam.scriptedcolonies.runner.ScriptRunner;
import com.ldtteam.scriptedcolonies.runner.ScriptRunnerException;
import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structures.helpers.Structure;
import com.ldtteam.structurize.management.StructureName;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IRSComponent;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.InstantStructurePlacer;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.blocks.huts.BlockHutTownHall;
import com.minecolonies.coremod.blocks.huts.BlockHutWareHouse;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildBuilding;
import com.minecolonies.coremod.entity.ai.citizen.builder.ConstructionTapeHelper;
import com.minecolonies.coremod.util.ColonyUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.registries.ForgeRegistries;
import org.squiddev.cobalt.*;
import org.squiddev.cobalt.function.VarArgFunction;

public class McPlaceStructure extends VarArgFunction {
	private final ScriptRunner runner;

	public McPlaceStructure(ScriptRunner runner) {
		this.runner = runner;
	}

	@Override
	public Varargs invoke(LuaState luaState, Varargs varargs) throws LuaError, UnwindThrowable {
		if(varargs.count() != 1) {
			return Constants.NONE;
		}

		StructureDto structureDto = LuaTableReader.readTable(varargs.arg(1).checkTable(), StructureDto.class);
		StructureName structureName = new StructureName(structureDto.name);
		Mirror mirror = structureDto.mirrored ? Mirror.FRONT_BACK : Mirror.NONE;
		Rotation rotation = BlockPosUtil.getRotationFromRotations(structureDto.rotation);
		BlockPos pos = structureDto.pos.toBlockPos();

		MinecraftScheduler.schedule(() -> {
			MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
			ServerPlayerEntity serverPlayerEntity = server.getPlayerList().getPlayerByUUID(this.runner.getUserId());

			if(!structureName.isHut()) {
				serverPlayerEntity.sendMessage(new StringTextComponent("Only huts are supported at the moment"));
				return;
			}

			IColonyManager colonyManager = IColonyManager.getInstance();


			if(serverPlayerEntity == null) {
				throw new ScriptRunnerException("Couldn't load user");
			}

			//TODO: make this configurable
			ServerWorld world = server.getWorld(DimensionType.OVERWORLD);

			IColony colony = colonyManager.getIColony(world, pos);

			if(colony != null) {
				serverPlayerEntity.sendMessage(new StringTextComponent("No such colony"));
			}

			Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("minecolonies", "blockhut" + structureName.getHutName()));

			if(!(block instanceof AbstractBlockHut)) {
				serverPlayerEntity.sendMessage(new StringTextComponent(String.format("%s is not a valid hut", structureDto.name)));
				return;
			}

			AbstractBlockHut hutBlock = (AbstractBlockHut)block;

			String hutPlacementError = canHutBePlaced(world, colony, hutBlock, pos);

			if(hutPlacementError != null) {
				serverPlayerEntity.sendMessage(new StringTextComponent(hutPlacementError));
				return;
			}

			Structure structure = new com.ldtteam.structures.helpers.Structure(world, structureDto.name, new PlacementSettings(mirror, rotation));
			Blueprint blueprint = structure.getBluePrint();

			if(blueprint == null) {
				serverPlayerEntity.sendMessage(new StringTextComponent("Could not load blueprint"));
				return;
			}

			BlockState hutBlockState = blueprint.getBlockInfoAsMap().get(blueprint.getPrimaryBlockOffset()).getState();

			world.destroyBlock(pos, false);
			world.setBlockState(pos, hutBlockState);

			//TODO: refactor stuff below (code duplication with BuildToolPasteMessage
			hutBlock.onBlockPlacedByBuildTool(world, pos, world.getBlockState(pos), serverPlayerEntity, null, structureDto.mirrored, structureName.getStyle());

			IBuilding building = IColonyManager.getInstance().getBuilding(world, pos);

			if(building == null) {
				serverPlayerEntity.sendMessage(new StringTextComponent("Could not retrieve building"));
				return;
			}

			AbstractTileEntityColonyBuilding tileEntity = building.getTileEntity();

			if(tileEntity == null) {
				serverPlayerEntity.sendMessage(new StringTextComponent("Could not retrieve tile entity"));
				return;
			}

			tileEntity.setColony(colony);

			//TODO: Structure name should have some way to get the level of the building
			String schematicName = structureName.getSchematic();
			try {
				int level = Integer.parseInt(schematicName.substring(schematicName.length() - 1));
				building.setBuildingLevel(level);
			}
			catch (final NumberFormatException e) {
				Log.getLogger().warn("Couldn't parse the level.", e);
				return;
			}

			building.setStyle(structureName.getStyle());

			if (!(building instanceof IRSComponent))
			{
				ConstructionTapeHelper.removeConstructionTape(building.getCorners(), world);
				final WorkOrderBuildBuilding workOrder = new WorkOrderBuildBuilding(building, 1);
				final com.ldtteam.structures.helpers.Structure wrapper = new com.ldtteam.structures.helpers.Structure(world, workOrder.getStructureName(), new PlacementSettings());
				final Tuple<Tuple<Integer, Integer>, Tuple<Integer, Integer>> corners
					= ColonyUtils.calculateCorners(building.getPosition(),
					world,
					wrapper,
					workOrder.getRotation(world),
					workOrder.isMirrored());

				building.setCorners(corners.getA().getA(), corners.getA().getB(), corners.getB().getA(), corners.getB().getB());
				building.setHeight(wrapper.getHeight());
			}

			if (structureDto.mirrored)
			{
				building.invertMirror();
			}

			InstantStructurePlacer.loadAndPlaceStructureWithRotation(serverPlayerEntity.world, structureDto.name, pos, BlockPosUtil.getRotationFromRotations(structureDto.rotation), mirror, false);

			building = IColonyManager.getInstance().getBuilding(world, pos);
			if (building != null)
			{
				building.onUpgradeComplete(building.getBuildingLevel());
				final WorkOrderBuildBuilding workOrder = new WorkOrderBuildBuilding(building, 1);
				ConstructionTapeHelper.removeConstructionTape(workOrder, CompatibilityUtils.getWorldFromEntity(serverPlayerEntity));
			}

			return;
		});

		return Constants.NONE;
	}

	private String canHutBePlaced(final World world, final IColony existingColony, AbstractBlockHut hutBlock, BlockPos pos) {

		if(hutBlock instanceof BlockHutTownHall) {
			if(existingColony == null) {
				//Check if the new colony would be to close to an existing colony
				if(IColonyManager.getInstance().isTooCloseToColony(world, pos)) {
					return "Too close to existing colony";
				}

				return null;
			}

			if(existingColony.hasTownHall()) {
				return "Colony has a town hall";
			}
		} else if(existingColony == null) {
			return "Not in a colony";
		} else if(hutBlock instanceof BlockHutWareHouse) {
			if(existingColony.hasWarehouse() && MineColonies.getConfig().getCommon().limitToOneWareHousePerColony.get()) {
				return "Warehouse already build";
			}
		}

		return null;
	}


}
