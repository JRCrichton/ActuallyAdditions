/*
 * This file ("WorldData.java") is part of the Actually Additions Mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://github.com/Ellpeck/ActuallyAdditions/blob/master/README.md
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * � 2015 Ellpeck
 */

package ellpeck.actuallyadditions.misc;

import ellpeck.actuallyadditions.util.ModUtil;
import ellpeck.actuallyadditions.util.playerdata.PersistentServerData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;

public class WorldData extends WorldSavedData{

    public static final String DATA_TAG = ModUtil.MOD_ID+"WorldData";
    public static WorldData instance;

    public WorldData(String tag){
        super(tag);
    }

    public static void makeDirty(){
        if(instance != null){
            instance.markDirty();
        }
    }

    public static void init(MinecraftServer server){
        if(server != null){
            World world = server.getEntityWorld();
            if(!world.isRemote){
                WorldSavedData savedData = world.loadItemData(WorldData.class, WorldData.DATA_TAG);
                //Generate new SavedData
                if(savedData == null){
                    savedData = new WorldData(WorldData.DATA_TAG);
                    world.setItemData(WorldData.DATA_TAG, savedData);
                }
                //Set the current SavedData to the retreived one
                if(savedData instanceof WorldData){
                    WorldData.instance = (WorldData)savedData;
                }
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound){
        //Laser World Data
        NBTTagList list = compound.getTagList("Networks", 10);
        for(int i = 0; i < list.tagCount(); i++){
            LaserRelayConnectionHandler.Network network = LaserRelayConnectionHandler.getInstance().readNetworkFromNBT(list.getCompoundTagAt(i));
            LaserRelayConnectionHandler.getInstance().networks.add(network);
        }

        //Player Data
        int dataSize = compound.getInteger("PersistentDataSize");
        PersistentServerData.playerSaveData.clear();
        for(int i = 0; i < dataSize; i++){
            PersistentServerData.PlayerSave aSave = PersistentServerData.PlayerSave.fromNBT(compound, "PlayerSaveData"+i);
            if(aSave != null){
                PersistentServerData.playerSaveData.add(aSave);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound){
        //Laser World Data
        NBTTagList list = new NBTTagList();
        for(LaserRelayConnectionHandler.Network network : LaserRelayConnectionHandler.getInstance().networks){
            list.appendTag(LaserRelayConnectionHandler.getInstance().writeNetworkToNBT(network));
        }
        compound.setTag("Networks", list);

        //Player Data
        compound.setInteger("PersistentDataSize", PersistentServerData.playerSaveData.size());
        for(int i = 0; i < PersistentServerData.playerSaveData.size(); i++){
            PersistentServerData.PlayerSave theSave = PersistentServerData.playerSaveData.get(i);
            theSave.toNBT(compound, "PlayerSaveData"+i);
        }
    }
}
