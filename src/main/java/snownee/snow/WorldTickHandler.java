//package snownee.snow;
//
//import net.minecraft.world.GameRules;
//import net.minecraft.world.WorldType;
//import net.minecraft.world.server.ServerWorld;
//import net.minecraftforge.event.TickEvent;
//
//public class WorldTickHandler
//{
//    public static void tick(TickEvent.WorldTickEvent event)
//    {
//        ServerWorld world = (ServerWorld) event.world;
//        if (!world.isRaining())
//        {
//            return;
//        }
//        if (world.getWorldInfo().getGenerator() == WorldType.DEBUG_ALL_BLOCK_STATES)
//        {
//            return;
//        }
//        int tickSpeed = world.getGameRules().getInt(GameRules.RANDOM_TICK_SPEED);
//        if (tickSpeed <= 0)
//        {
//            return;
//        }
//        world.getChunkProvider().chunkManager.func_223491_f().forEach(chunk -> {
//        });
//    }
//}
