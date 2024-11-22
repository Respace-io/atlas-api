package io.redspace.atlasapi.api.data;


import java.util.List;

/**
 * @param layers Unordered list of {@link ModelLayer}s to be baked into a model
 */
public record BakingPreparations(List<ModelLayer> layers) {
    @Override
    public int hashCode() {
        return layers.hashCode();
    }
}
