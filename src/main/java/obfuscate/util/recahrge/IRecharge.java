package obfuscate.util.recahrge;

public interface IRecharge<T>
{
    boolean done(T anchor, String ability, Long delay);
}
