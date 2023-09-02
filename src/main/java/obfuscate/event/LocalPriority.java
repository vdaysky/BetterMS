package obfuscate.event;

public enum LocalPriority
{
    PRE_HIGH(4),
    PRE(3),
    NATIVE(2),
    POST(1),
    POST_HIGH(0);

    int prior;

    LocalPriority(int p)
    {
        prior = p;
    }

    public int getPriority()
    {
        return prior;
    }



}
