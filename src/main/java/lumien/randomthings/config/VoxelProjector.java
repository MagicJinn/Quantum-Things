package lumien.randomthings.config;

import lumien.randomthings.lib.ConfigOption;

public class VoxelProjector {
        public static final String CATEGORY = "Voxel Projector";
        @ConfigOption(category = CATEGORY, name = "ClientModelSaving",
            comment = "Should the client save models received by the server to disk so that they don't have to be requested again later?") public static boolean MODEL_CLIENT_SAVING =
                    true;

    @ConfigOption(category = CATEGORY, name = "ModelTransferBandwidth",
            comment = "The amount of bytes that can be used to transfer models to clients per tick (The default 1000 Byte equal 20 kbyte/sec)") public static int MODEL_TRANSFER_BANDWIDTH =
                    1000;
}

