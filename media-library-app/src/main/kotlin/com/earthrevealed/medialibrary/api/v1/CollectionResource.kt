package com.earthrevealed.medialibrary.api.v1

import com.earthrevealed.medialibrary.domain.CollectionId
import com.earthrevealed.medialibrary.domain.collection
import com.earthrevealed.medialibrary.persistence.CollectionRepository
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*


@RestController
@RequestMapping("/collections")
class CollectionResource(
        val collectionRepository: CollectionRepository
) {

    @GetMapping("/", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun collections() =
            collectionRepository.all()

    @GetMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun collectionWithId(@PathVariable("id") id: UUID) =
        collectionRepository.get(CollectionId(id))

    @PostMapping("/", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun createCollection( @RequestBody newCollection: NewCollection) {
        val collection = collection {
            name = newCollection.name
        }
        collectionRepository.save(collection)
    }
}

data class NewCollection(var name: String) {
    constructor(): this("unknown")
}