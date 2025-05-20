package com.opentool.llmassert

import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.inputStream

class Media private constructor(
    val mimeType: String,
    val source: () -> InputStream,
) {
    companion object {
        const val IMAGE_PNG = "image/png"
        const val IMAGE_JPEG = "image/jpeg"

        fun image(path: Path, mimeType: String): Media {
            return Media(mimeType = mimeType, source = { path.inputStream() })
        }

        fun png(path: Path): Media {
            return image(path, mimeType = IMAGE_PNG)
        }

        fun jpeg(path: Path): Media {
            return image(path, mimeType = IMAGE_JPEG)
        }
    }
}