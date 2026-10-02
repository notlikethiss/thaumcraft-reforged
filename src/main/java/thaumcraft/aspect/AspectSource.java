package thaumcraft.aspect;

public interface AspectSource {
    int addToSource(Aspect aspect, int amount);

    boolean takeFromSource(Aspect aspect, int amount);

    boolean takeFromSource(AspectList aspects);

    boolean doesSourceContainAmount(Aspect aspect, int amount);

    boolean doesSourceContain(AspectList aspects);

    int sourceContains(Aspect aspect);

    AspectList getSourceTags();
}
