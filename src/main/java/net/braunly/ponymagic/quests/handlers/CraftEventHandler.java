package net.braunly.ponymagic.quests.handlers;

import me.braunly.ponymagic.api.PonyMagicAPI;
import me.braunly.ponymagic.api.enums.EnumQuestGoalType;
import me.braunly.ponymagic.api.interfaces.IPlayerDataStorage;
import net.braunly.ponymagic.util.QuestGoalUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class CraftEventHandler {
    public CraftEventHandler() {
        // stub
    }

    @SubscribeEvent(priority=EventPriority.HIGH)
    public void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        EntityPlayer player = event.player;

        if (player == null || player.world.isRemote) return;

        IPlayerDataStorage playerData = PonyMagicAPI.playerDataController.getPlayerData(player);
        ItemStack itemStack = event.crafting;
        String goalConfigKey = QuestGoalUtils.getConfigKey(
                EnumQuestGoalType.ITEM,
                itemStack.getItem().getRegistryName(),
                itemStack.getItemDamage()
        );

        for (int i = 0; i < event.crafting.getCount(); i++) {
            String questName = "craft";
            playerData.getLevelData().decreaseGoal(questName, goalConfigKey);

            if (itemStack.getItem() instanceof ItemTool) {
                questName = "craft_tool";
                playerData.getLevelData().decreaseGoal(questName, goalConfigKey);
            } else if (itemStack.getItem() instanceof ItemArmor) {
                questName = "craft_armor";
                playerData.getLevelData().decreaseGoal(questName, goalConfigKey);
            }
        }
        PonyMagicAPI.playerDataController.savePlayerData(playerData);
    }
}
