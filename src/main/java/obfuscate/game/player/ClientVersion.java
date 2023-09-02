package obfuscate.game.player;

public enum ClientVersion {
    V1_8(47),
    V1_9(107),
    V1_10(210),
    V1_11(315),
    V1_12(335),
    V1_13(393),
    V1_14(477),
    V1_15(573),
    V1_16(735),
    V1_17(755);

    private final int _protocolVersion;

    ClientVersion(int protocolVersion)
    {
        _protocolVersion = protocolVersion;
    }

    public int getProtocolVersion()
    {
        return _protocolVersion;
    }

    public static ClientVersion fromProtocolVersion(int protocolVersion)
    {
        for (ClientVersion version : values())
        {
            if (version.getProtocolVersion() == protocolVersion)
            {
                return version;
            }
        }

        return null;
    }
}
