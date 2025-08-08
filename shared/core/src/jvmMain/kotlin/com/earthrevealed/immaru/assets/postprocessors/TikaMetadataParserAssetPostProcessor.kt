package com.earthrevealed.immaru.assets.postprocessors

import com.earthrevealed.immaru.assets.FileAsset
import mu.KotlinLogging
import org.apache.tika.Tika
import org.apache.tika.metadata.IPTC
import org.apache.tika.metadata.Metadata
import org.apache.tika.metadata.TIFF
import org.apache.tika.metadata.TikaCoreProperties
import java.nio.file.Path
import kotlin.time.toKotlinInstant

private val logger = KotlinLogging.logger { }

class TikaMetadataParserAssetPostProcessor {

    fun postProcess(asset: FileAsset, path: Path) {
        logger.info { "Processing metadata for asset [asset.id=${asset.id}]" }

        val parsingMetadata = Metadata()
        Tika().parse(path, parsingMetadata)

        parsingMetadata.created?.let { asset.originalCreatedAt = it }

        // TODO: Save meta data
        parsingMetadata.names().sorted().forEach { name ->
            if (parsingMetadata.isMultiValued(name))
                println("$name: ${parsingMetadata.getValues(name).toList()}")
            else
                println("$name: ${parsingMetadata.get(name)}")
        }
    }

    private val Metadata.created
        get() = (
                this.getDate(TikaCoreProperties.CREATED)?:
                this.getDate(IPTC.DATE_CREATED)?:
                this.getDate(TIFF.ORIGINAL_DATE))?.toInstant()?.toKotlinInstant()


    //
//        //Nadeel van Tika is dat de relatie van Directory -> Tag -> Value kwijt is. (valt mee want is gescheiden met dubbele punt :)
//        //Nadeel van de JpegParser is dat voor ieder file formaat zijn eigen parser gebruikt moet worden.
    // png -> Tika ImageParser
    // jpg -> Tika JpegParser
    // tif -> Tika TiffParser

    // En videos??
//    }
}