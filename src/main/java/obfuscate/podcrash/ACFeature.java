package obfuscate.podcrash;

public enum ACFeature {

    F5("f5"),
    SOUNDBOOST("soundboost"),
    ;

    final String feature;

    private ACFeature(String name) {
        this.feature = name;
    }

    public String getFeatureName() {
        return feature;
    }
}
