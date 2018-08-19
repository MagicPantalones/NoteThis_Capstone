package io.magics.notethis.ui.fragments;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.noties.markwon.renderer.ImageSize;
import ru.noties.markwon.renderer.ImageSizeResolverDef;

public class AdaptiveImageSizeResolver extends ImageSizeResolverDef {

    public AdaptiveImageSizeResolver() {
        super();
    }

    @NonNull
    @Override
    public Rect resolveImageSize(@Nullable ImageSize imageSize, @NonNull Rect imageBounds,
                                 int canvasWidth, float textSize) {
        return imageSize == null ? fitWidth(imageBounds, canvasWidth)
                : super.resolveImageSize(imageSize, imageBounds, canvasWidth, textSize);
    }

    private Rect fitWidth(Rect imgBounds, int canvasWidth) {
        final int outWidth = imgBounds.height() > imgBounds.width()
                ? canvasWidth / 2 : canvasWidth;
        final float ratio = (float) imgBounds.width() / imgBounds.height();
        final int height = (int) (outWidth / ratio + .5f);

        return new Rect(0, 0, outWidth, height);
    }
}
