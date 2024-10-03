package com.earthrevealed.immaru.assets.library

import com.earthrevealed.immaru.assets.FileAsset
import kotlinx.io.files.Path

/**
 * Determine the destination folder to store the asset in.
 * Sub folders are determined by the first 6 digits of the UUID:
 * id = a4e6d238-39eb-4efc-b23d-be6ac0f05e75
 * destination folder = /a4/e6/d2/38/
 */
fun FileAsset.destinationFolder(): Path {
    var subFolders = Path("")
    (0..3).forEach {
        val offset = (it * 2)
        subFolders = Path(subFolders, id.value.toString().substring(offset + 0..offset + 1))
    }
    return subFolders
}

fun FileAsset.internalFilename() =
    "${id.value}.${extension()}"

fun FileAsset.extension(): String? {
    val index = originalFilename.lastIndexOf('.')
    if (index > 0) {
        return originalFilename.substring(index + 1)
    }
    return null
}

fun FileAsset.internalFilelocation(): Path = Path(destinationFolder(), internalFilename())
